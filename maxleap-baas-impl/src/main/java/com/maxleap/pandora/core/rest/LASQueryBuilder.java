package com.maxleap.pandora.core.rest;

import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.Direction;
import com.maxleap.pandora.core.PandoraMongoData;
import com.maxleap.pandora.core.lasdata.LASKeyType;
import com.maxleap.pandora.core.lasdata.LASQuery;
import com.maxleap.pandora.core.mongo.exception.QueryException;
import com.maxleap.pandora.core.utils.LASObjectJsons;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sneaky
 * @since 3.0.0
 */
public class LASQueryBuilder {
  public static final String ORDER_SEPARATOR = ",";
  public static final String ORDER_ASC = "+";
  public static final String ORDER_DESC = "-";
  private Map<String, Object> where;
  private LASQuery query;

  LASQueryBuilder() {
    this.query = new LASQuery();
  }

  LASQueryBuilder(LASQuery query) {
    this.query = query;
  }

  LASQueryBuilder(Map where) {
    this();
    this.where = where;
  }

  public static LASQueryBuilder start(String where) {
    if (StringUtils.isNotBlank(where)) {
      return new LASQueryBuilder(LASObjectJsons.deserialize(where, Map.class));
    } else {
      return new LASQueryBuilder();
    }
  }

  public LASQueryBuilder orders(String order) {
    if (StringUtils.isNotBlank(order)) {
      String[] orders = order.split(ORDER_SEPARATOR);
      for (String s : orders) {
        s = s.trim();
        if (s.equals("")) continue;
        if (s.startsWith(ORDER_DESC)) {
          query.sort(Direction.DESC, s.substring(1));
        } else if (s.startsWith(ORDER_ASC)) {
          query.sort(Direction.ASC, s.substring(1));
        } else {
          query.sort(Direction.ASC, s);
        }
      }
    }
    return this;
  }

  public LASQueryBuilder keys(String keys) {
    String[] keyArray = null;
    if (StringUtils.isNotBlank(keys)) {
      keyArray = keys.split(",");
      query.addProjectKey(keyArray);
      query.addProjectKey(PandoraMongoData.KEY_OBJECT_CREATED_AT).addProjectKey(PandoraMongoData.KEY_OBJECT_UPDATED_AT);
    }
    return this;
  }

  public LASQueryBuilder excludeKeys(String keys) {
    String[] keyArray = null;
    if (StringUtils.isNotBlank(keys)) {
      keyArray = keys.split(",");
      query.excludeProjectKey(keyArray);
    }
    return this;
  }

  public LASQueryBuilder limit(Integer limit) {
    if (limit != null) {
      query.setLimit(limit);
    }
    return this;
  }

  public LASQueryBuilder skip(Integer skip) {
    if (skip != null) {
      query.setSkip(skip);
    }
    return this;
  }

  public LASQueryBuilder include(String includes) {
    query.setIncludes(includes);
    return this;
  }

  public LASQuery build() {
    // where is null means find all
    if (where == null) {
      return query;
    }

    where.forEach((key, value) -> {
          if (key.equals("objectId")) {
            if (value instanceof Map) {
              Map map = (Map) value;
              for (Object queryKey : map.keySet()) {
                Object o = map.get(queryKey);
                if (o instanceof List) {
                  List newList = new ArrayList<>();
                  List list = (List) o;
                  int size = list.size();
                  for (int i = 0; i < size; i++) {
                    Object id = list.get(0);
                    newList.add(new ObjectId(id.toString()));
                  }
                  map.put(queryKey, newList);
                } else {
                  map.put(queryKey, new ObjectId(o.toString()));
                }
              }
            } else {
              query.equalTo(key, new ObjectId(value.toString()));
            }
          } else if (key.equals("$and") || key.equals("$or")) {
            List<Map> queries = $andAnd$or(value);
            if (queries != null) {
              query.equalTo(key, queries);
            }
          } else if (key.equals("$relatedTo")) {
            if (value instanceof Map) {
              Object object = ((Map) value).get("object");
              Object key1 = ((Map) value).get("key");
              if (object instanceof Map) {
                if (key1 != null) {
                  Object className = ((Map) object).get("className");
                  if (className == null) {
                    badOperator(key, value);
                  }
                  query.equalTo("$$key", key1);
                  query.equalTo("$relatedTo", object);
                }
              } else {
                badOperator(key, value);
              }
            } else {
              badOperator(key, value);
            }
          } else if (value instanceof Map) {
            Map map = (Map) value;
            if (map.get("$nearSphere") != null) {
              Object nearSphere = map.get("$nearSphere");
              List points = extractPoints(key, nearSphere);
              double maxDistance = 0;

              Object $maxDistance = map.get("$maxDistance");
              if ($maxDistance instanceof Number) {
                maxDistance = ((Number) $maxDistance).doubleValue();
              }
              Map point = new HashMap();
              point.put("type", "Point");
              point.put("coordinates", points);

              Map geometry = new HashMap();
              geometry.put("$geometry", point);

              if (maxDistance > 0) {
                geometry.put("$maxDistance", maxDistance);
              }
              ((Map) nearSphere).put(key, geometry);
            } else if (map.get("$geoWithin") != null) {
              Object geoWithin = map.get("$geoWithin");
              if (geoWithin instanceof Map && ((Map) geoWithin).get("$box") instanceof List) {
                Map $geoWithin = (Map) geoWithin;
                List box = (List) $geoWithin.get("$box");
                List points = new ArrayList(4);

                if (box.size() == 2) {
                  List southwestGeoPoint = extractPoints(key, box.get(0));
                  List northeastGeoPoint = extractPoints(key, box.get(1));

                  ArrayList<Object> l1 = new ArrayList<>();
                  l1.add(0);
                  l1.add(0);
                  points.add(l1);
                  points.add(southwestGeoPoint);
//                    points.add(new double[] {southwestGeoPoint[0], northeastGeoPoint[1]});
                  points.add(northeastGeoPoint);
                  points.add(new ArrayList<>(l1));
//                    points.add(new double[] {northeastGeoPoint[0], southwestGeoPoint[1]});

                } else {
                  badOperator(key, geoWithin);
                }

                $geoWithin.put(key, points);

              } else {
                badOperator(key, geoWithin);
              }
            } else {
              query.equalTo(key, value);
            }
          } else {
            query.equalTo(key, value);
          }
    });
    return this.query;
  }

  private List<Map> $andAnd$or(Object value) {
    if (value instanceof List) {
      int size = ((List) value).size();
      if (size >= 1) {
        List<Map> queries = new ArrayList<>();
        List $andAnd$or = (List) value;
        for (int j = 0; j < size; j++) {
          Object o = $andAnd$or.get(j);
          if (o instanceof Map) {
            queries.add(new LASQueryBuilder(new LASQuery((Map) o)).build().getQuery());
          } else {
            throw new QueryException("invalid query near: " + o);
          }
        }
        return queries;
      } else {
        return null;
      }
    } else {
      throw new QueryException("$and operator value must be array.");
    }
  }

  private List extractPoints(String key, Object geoPoint) {
    if (geoPoint instanceof Map) {
      Object type = ((Map) geoPoint).get("__type");
      double longitudeL = 0;
      double latitudeL = 0;

      if (type == null || !type.equals(LASKeyType.GeoPoint.name())) {
        badOperator(key, geoPoint);
      }

      Object longitude = ((Map) geoPoint).get("longitude");
      if (!(longitude instanceof Number)) {
        badOperator(key, geoPoint);
      } else {
        longitudeL = ((Number) longitude).doubleValue();
        if (longitudeL < -180 && longitudeL > 180) {
          throw new QueryException("Geo box queries larger than 180 and smaller than -180 degrees in longitude are not supported. Please check point order.");
        }
      }

      Object latitude = ((Map) geoPoint).get("latitude");
      if (!(latitude instanceof Number)) {
        badOperator(key, geoPoint);
      } else {
        latitudeL = ((Number) latitude).doubleValue();
        if (latitudeL < -90 && latitudeL > 90) {
          throw new QueryException("Geo box queries larger than 90 and smaller than -90 degrees in latitude are not supported. Please check point order.");
        }
      }
      List list = new ArrayList();
      list.add(longitude);
      list.add(latitudeL);

      return list;
    } else {
      badOperator(key, geoPoint);
    }
    return null;
  }

  private void badOperator(String key, Object value) {
    String error = new StringBuilder().append("Can't recognize operator: ")
        .append(key).append(". BadValue [").append(value).append("]").toString();

    throw new QueryException(error);
  }

}

package com.maxleap.pandora.data.support.lasdata;

import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.lasdata.LASClassSchema;
import com.maxleap.pandora.core.lasdata.LASKeyInfo;
import com.maxleap.pandora.core.lasdata.LASKeyType;
import com.maxleap.pandora.core.lasdata.LASQuery;
import com.maxleap.pandora.core.lasdata.types.LASPointer;
import com.maxleap.pandora.core.mongo.exception.QueryException;
import com.maxleap.pandora.data.support.ClassSchemaManager;
import com.maxleap.pandora.data.support.LASDataEntityManager;
import com.maxleap.pandora.data.support.utils.LASIncludes;
import org.bson.Document;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * @author sneaky
 * @since 3.0.0
 */
@Singleton
public class LASOperatorHandler {
  private static final String CONSUMER_OPERATOR_SELECT = "$select";
  private static final String CONSUMER_OPERATOR_NOT_SELECT = "$dontSelect";
  private static final String CONSUMER_OPERATOR_IN_QUERY = "$inQuery";
  private static final String CONSUMER_OPERATOR_NOT_IN_QUERY = "$notInQuery";
  private static final String CONSUMER_OPERATOR_RELATION_TO = "$relatedTo";

  private final ClassSchemaManager classSchemaManager;
  LASDataEntityManager lasDataEntityManager;

  @Inject
  public LASOperatorHandler(ClassSchemaManager classSchemaManager, LASDataEntityManager lasDataEntityManager) {
    this.classSchemaManager = classSchemaManager;
    this.lasDataEntityManager = lasDataEntityManager;
  }

  void include(AppFindRequest request, LASPrincipal principal, LASIncludes includes, List objects) {
    if (includes != null) {
      Map<String, Map> cachePointer = new LASObject();
      for (Object obj : objects) {
        include(principal, (Map) obj, includes.includes(), request.getClassSchema(), cachePointer);
      }
    }
  }

  void include(AppFindOneRequest request, LASPrincipal principal, LASIncludes includes, Map obj) {
    if (includes != null) {
      Map<String, Map> cachePointer = new LASObject();
      include(principal, obj, includes.includes(), request.getClassSchema(), cachePointer);
    }
  }

  /**
   * @param map
   * @param includes
   * @param classSchema
   * @param exist       The pointer had loaded.
   * @return
   */
  private void include(LASPrincipal principal, Map map, Set<LASIncludes.Include> includes, LASClassSchema classSchema, Map<String, Map> exist) {
    for (LASIncludes.Include include : includes) {
//      Map existMap = map;
//      LASClassSchema schema = classSchema;
//      while (true) {
//        String key = include.node;
//        LASKeyInfo zCloudKeyInfo = schema.getKeys().get(key);
//        if (zCloudKeyInfo != null && zCloudKeyInfo.isPointer()) {
//          /**
//           * eg if you give includes like this: a.b.c, a.d.k, the val of key is Map.
//           */
//          Map obj = (Map) existMap.get(key);
//          if (obj == null) {
//            break;
//          }
//
//          if (!isPrimitivePointer(obj)) {
//            if (include.child == null) {
//              break;
//            } else {
//              existMap = obj;
//              schema = classSchemaManager.get(classSchema.getAppId(), zCloudKeyInfo.getClassName());
//              if (schema == null) {
//                break;
//              }
//              include = include.child;
//            }
//            continue;
//          }
//
//          ObjectId objectId = new ObjectId(obj.get("objectId").toString());
//          Map cache = exist.get(objectId.toString());
//
//          Map value;
//
//          if (cache != null) {
//            value = new LASObject(cache);
//            existMap.put(key, value);
//            if (include.child == null) {
//              break;
//            } else {
//              existMap = value;
//              schema = classSchemaManager.get(classSchema.getAppId(), zCloudKeyInfo.getClassName());
//              if (schema == null) {
//                break;
//              }
//              include = include.child;
//            }
//          } else {
//            schema = classSchemaManager.get(classSchema.getAppId(), zCloudKeyInfo.getClassName());
//            if (schema != null) {
//              cache = lasDataEntityManager.get(schema.getAppId(), schema.getClassName(), principal, objectId);
//              if (cache != null) {
//                cache.put("__type", "Object");
//                cache.put("className", zCloudKeyInfo.getClassName());
//                exist.put(objectId.toString(), new LASObject(cache));
//                existMap.put(key, cache);
//
//                if (include.child == null) {
//                  break;
//                } else {
//                  include = include.child;
//                  existMap = cache;
//                }
//              } else {
//                existMap.put(key, null);
//                break;
//              }
//            } else {
//              existMap.put(key, null);
//              break;
//            }
//          }
//        } else {
//          break;
//        }
//      }
      include(principal, map, include, classSchema, exist);
    }
  }

  private void include(LASPrincipal principal, Map doc, LASIncludes.Include include, LASClassSchema classSchema, Map<String, Map> caches) {
    String key = include.node;
    LASKeyInfo zCloudKeyInfo = classSchema.getKeys().get(key);
    if (zCloudKeyInfo == null) {
     return;
    }
    if (zCloudKeyInfo.isPointer()) {
      Map pointer = (Map) doc.get(key);
      if (pointer == null) {
        return;
      }
      if (!isPrimitivePointer(pointer)) {
        if (include.child == null) {
          return;
        }
        LASClassSchema childSchema = classSchemaManager.get(classSchema.getAppId(), zCloudKeyInfo.getClassName());
        if (childSchema != null) {
          include(principal, pointer, include.child, childSchema, caches);
        }
      } else {
        ObjectId objectId = new ObjectId(pointer.get("objectId").toString());
        Map cache = caches.get(objectId.toString());

        if (cache != null) {
          caches.put(key, cache);
          doc.put(key, cache);
          if (include.child == null) {
            return;
          }
          LASClassSchema childSchema = classSchemaManager.get(classSchema.getAppId(), zCloudKeyInfo.getClassName());
          if (childSchema != null) {
            include(principal, cache, include.child, childSchema, caches);
          }
        } else {
          LASClassSchema childSchema = classSchemaManager.get(classSchema.getAppId(), zCloudKeyInfo.getClassName());
          if (childSchema != null) {
            cache = lasDataEntityManager.get(childSchema.getAppId(), childSchema.getClassName(), principal, objectId);
            if (cache != null) {
              cache.put("__type", "Object");
              cache.put("className", zCloudKeyInfo.getClassName());
              LASObject child = new LASObject(cache);
              caches.put(objectId.toString(), child);
              doc.put(key, child);

              if (include.child != null) {
                include(principal, child, include.child, childSchema, caches);
              }
            } else {
              doc.put(key, null);
            }
          } else {
            doc.put(key, null);
          }
        }
      }
    } else if (zCloudKeyInfo.isArray() && doc.get(key) instanceof List) {
      List<Map> list = (List)doc.get(key);
      if (list.size() > 0 && list.get(0) instanceof Map && isPrimitivePointerFromArray(list.get(0))) {
        LASClassSchema childSchema = null;
        List<Map> newDocs = new ArrayList<>();
        for (Map document : list) {
          ObjectId objectId = new ObjectId(document.get("objectId").toString());
          Map cache = caches.get(objectId.toString());

          if (cache != null) {
            newDocs.add(cache);
            if (include.child == null) {
              return;
            }
            if (childSchema == null) {
              childSchema = classSchemaManager.get(classSchema.getAppId(), document.get("className").toString());
            }
            if (childSchema != null) {
              include(principal, cache, include.child, childSchema, caches);
            }
          } else {
            if (childSchema == null) {
              childSchema = classSchemaManager.get(classSchema.getAppId(), document.get("className").toString());
            }
            if (childSchema != null) {
              cache = lasDataEntityManager.get(childSchema.getAppId(), childSchema.getClassName(), principal, objectId);
              if (cache != null) {
                cache.put("__type", "Object");
                cache.put("className", childSchema.getClassName());
                LASObject child = new LASObject(cache);
                caches.put(objectId.toString(), child);
                newDocs.add(child);

                if (include.child != null) {
                  include(principal, child, include.child, childSchema, caches);
                }
              }
            }
          }
        }
        doc.put(key, newDocs);
      }
    }
  }

  public void handleLASOperator(LASClassSchema lasClassSchema, LASPrincipal principal, Map criteria) {
    Deque<Subquery> deque = new ArrayDeque<>();
    extractSubquery(null, null, criteria, deque);
    Subquery subquery = null;
    while ((subquery = deque.pollLast()) != null) {
      try {
        switch (subquery.operator) {
          case CONSUMER_OPERATOR_SELECT:
          case CONSUMER_OPERATOR_NOT_SELECT:
            Map $select = (Map) ((Map) subquery.query.get(subquery.key)).get(subquery.operator);
            Map query = (Map) $select.get("query");
            Object key = $select.get("key");
            Object className = query.get("className");
            Map where = (Map) query.get("where");
            if (where == null) {
              where = new HashMap();
            }
            LASClassSchema classSchema = classSchemaManager.get(lasClassSchema.getAppId(), className.toString());
            List in = new ArrayList<>();

            if (classSchema != null) {
              LASQuery lasQuery = new LASQuery(where);
              lasQuery.addProjectKey(key.toString()).excludeProjectKey("_id");
              List<LASObject> results = lasDataEntityManager.find(classSchema.getAppId(), classSchema.getClassName(), principal, lasQuery);
              for (LASObject doc : results) {
                in.add(doc.get(key));
              }
            }
            subquery.query.put(subquery.key, subquery.operator.equals(CONSUMER_OPERATOR_SELECT) ? new Document("$in", in) : new Document("$nin", in));
            break;
          case CONSUMER_OPERATOR_IN_QUERY:
          case CONSUMER_OPERATOR_NOT_IN_QUERY:
            Map $inQuery = (Map) ((Map) subquery.query.get(subquery.key)).get(subquery.operator);
            Object className1 = $inQuery.get("className");
            Map where1 = (Map) $inQuery.get("where");

            LASClassSchema classSchema1 = classSchemaManager.get(lasClassSchema.getAppId(), className1.toString());
            List in1 = new ArrayList<>();

            if (classSchema1 != null) {
              LASQuery lasQuery = new LASQuery(where1);
              lasQuery.addProjectKey("_id");
              List<LASObject> results = lasDataEntityManager.find(classSchema1.getAppId(), classSchema1.getClassName(), principal, lasQuery);
              for (LASObject lasObject : results) {
                in1.add(lasObject.getObjectId());
              }
            }

            subquery.query.put(subquery.key, subquery.operator.equals(CONSUMER_OPERATOR_IN_QUERY) ? new Document("$in", in1) : new Document("$nin", in1));
            break;
          case CONSUMER_OPERATOR_RELATION_TO:
            LASPointer $relationTo = (LASPointer) subquery.query.get(subquery.operator);
            LASClassSchema relationToClassSchema = classSchemaManager.get(lasClassSchema.getAppId(), ($relationTo).getClassName());
            if (relationToClassSchema != null) {
              Object $key = subquery.query.get("$$key");
              if ($key == null) {
                $key = subquery.query.get("$key");
              }
              LASQuery lasQuery = new LASQuery();
              lasQuery.equalTo("objectId", new ObjectId($relationTo.getObjectId()));
              lasQuery.addProjectKey($key.toString());
              lasQuery.loadRelations(true);
              LASObject uniqueOne = lasDataEntityManager.findUniqueOne(relationToClassSchema.getAppId(), relationToClassSchema.getClassName(), principal, lasQuery);
              Document $in = new Document();
              subquery.query.put("objectId", $in);
              subquery.query.remove(CONSUMER_OPERATOR_RELATION_TO);
              subquery.query.remove("$$key");
              subquery.query.remove("$key");
              if (uniqueOne == null) {
                $in.put("$in", new ArrayList<>());
              } else {
                Object o = uniqueOne.get($key.toString());
                if (o instanceof Map) {
                  Map map = (Map) o;
                  $in.put("$in", map.get("objectIds"));
                }
              }
            }
        }

      } catch (Exception e) {
        throw new QueryException(subquery.operator + "operator is invalid [" + subquery.query+ "]. because[" + e.getMessage() + "]");
      }
    }
  }

  private void extractSubquery(Map parentCriteria, String subKey, Map criteria, Deque<Subquery> subqueries) {
    Set<String> keySet = criteria.keySet();

    for (String key : keySet) {
      switch (key) {
        case CONSUMER_OPERATOR_IN_QUERY:
          subqueries.addLast(new Subquery(subKey, parentCriteria, CONSUMER_OPERATOR_IN_QUERY));
          break;
        case CONSUMER_OPERATOR_NOT_IN_QUERY:
          subqueries.addLast(new Subquery(subKey, parentCriteria, CONSUMER_OPERATOR_NOT_IN_QUERY));
          break;
        case CONSUMER_OPERATOR_SELECT:
          subqueries.addLast(new Subquery(subKey, parentCriteria, CONSUMER_OPERATOR_SELECT));
          break;
        case CONSUMER_OPERATOR_NOT_SELECT:
          subqueries.addLast(new Subquery(subKey, parentCriteria, CONSUMER_OPERATOR_NOT_SELECT));
          break;
        case CONSUMER_OPERATOR_RELATION_TO:
          subqueries.addLast(new Subquery("objectId", criteria, CONSUMER_OPERATOR_RELATION_TO));
//          throw new QueryException(CONSUMER_OPERATOR_RELATION_TO + " operator is invalid. because[you can't do it like this.]");
      }

      if (criteria.get(key) instanceof Map) {
        extractSubquery(criteria, key, (Map) criteria.get(key), subqueries);
      } else if (criteria.get(key) instanceof List) {
        extractSubquery((List) criteria.get(key), subqueries);
      }
    }
  }

  private void extractSubquery(List criteria, Deque<Subquery> subqueries) {
    int size = criteria.size();
    for (int i = 0; i < size; i++) {
      Object obj = criteria.get(i);
      if (obj instanceof Map) {
        extractSubquery(null, null, (Map) obj, subqueries);
      } else if (obj instanceof List) {
        extractSubquery((List) obj, subqueries);
      }
    }
  }

  public static class Subquery {
    private Object key;
    private Map query;
    private String operator;

    public Subquery(String key, Map query, String operator) {
      this.key = key;
      this.query = query;
      this.operator = operator;
    }
  }

  boolean isPrimitivePointer(Map val) {
    if (val.size() == 3 && LASKeyType.Pointer == val.get("__type") && val.get("className") != null && val.get("objectId") != null) {
      return true;
    }
    return false;
  }

  boolean isPrimitivePointerFromArray(Map val) {
    if (val.size() == 3 && "Pointer".equals(val.get("__type")) && val.get("className") != null && val.get("objectId") != null) {
      return true;
    }
    return false;
  }

}

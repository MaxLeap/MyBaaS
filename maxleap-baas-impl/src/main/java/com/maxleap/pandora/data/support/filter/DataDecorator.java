package com.maxleap.pandora.data.support.filter;

import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.PandoraMongoData;
import com.maxleap.pandora.core.exception.KeyInvalidException;
import com.maxleap.pandora.core.lasdata.LASClassSchema;
import com.maxleap.pandora.core.lasdata.LASKeyInfo;
import com.maxleap.pandora.core.lasdata.LASKeyType;
import com.maxleap.pandora.core.lasdata.types.*;
import com.maxleap.pandora.core.utils.DateUtils;

import javax.inject.Singleton;
import java.util.*;

/**
 * @author sneaky
 * @since 1.0
 */
@Singleton
public class DataDecorator {
  public void requestSaveDataDecorate(Map datum, LASClassSchema schema) {
    Map<String, LASKeyInfo> keys = schema.getKeys();
    for (Object key : datum.keySet()) {
      Object val = datum.get(key);
      if (val == null) {
        continue;
      }
      if (val instanceof List) {
        List listVal = (List) val;
        if (!listVal.isEmpty() && listVal.get(0) instanceof LASPointer) {
          List newList = new ArrayList<>();
          LASKeyInfo lasKeyInfo = keys.get(key);
          if (lasKeyInfo == null || lasKeyInfo.isRelation()) {
            for (Object obj : listVal) {
              LASPointer pointer = (LASPointer) obj;
              newList.add(unWrap(pointer));
            }
          } else {
            for (Object obj : listVal) {
              LASPointer pointer = (LASPointer) obj;
              newList.add(pointer.toMap());
            }
          }
          datum.put(key, newList);
        } else {
          datum.put(key, listVal);
        }
      } else {
        datum.put(key, unWrap(val));
      }
    }
  }

  public void requestUpdateDataDecorate(Map<String, ?> datum, LASClassSchema schema) {
    Map<String, LASKeyInfo> keys = schema.getKeys();

    for (Object key : datum.keySet()) {
      if (key.equals("$set") || key.equals("$setOnInsert")) {
        Object val = datum.get(key);
        if (val instanceof Map) {
          requestSaveDataDecorate((Map) val, schema);
        }
      } else if (key.equals("$addToSet") || key.equals("$push") || key.equals("$pull") || key.equals("$pullAll")) {
        Map val = (Map) datum.get(key);
        if (val != null) {
          for (Object arrayKey : val.keySet()) {
            Object arrayVal = val.get(arrayKey);
            if (arrayVal == null) {
              continue;
            } else if (keys.get(arrayKey) != null && keys.get(arrayKey).isRelation()) {
              if (arrayVal instanceof Map) {
                List $each = (List) ((Map) arrayVal).get("$each");
                if ($each != null) {
                  List newList = new ArrayList(5);
                  for (Object field : $each) {
                    newList.add(unWrap(field));
                  }
                  $each.clear();
                  $each.addAll(newList);
                }
              } else if (arrayVal instanceof List) {
                List list = (List) arrayVal;
                int size = list.size();
                int j = 0;

                for (int i = 0; i < size; i++) {
                  Object val1 = list.get(j++);
                  Object newObj = unWrap(val1);
                  if (newObj != val1) {
                    list.remove(val1);
                    list.add(newObj);
                    j--;
                  }
                }
              }
            } else {
              val.put(arrayKey, unWrap(arrayVal));
            }
          }
        }
      }
    }
  }

  public void requestDataDecorate(Map datum, LASClassSchema schema, boolean flag) {
    for (Object key : datum.keySet()) {
      Object val = datum.get(key);

      if (val == null) {
        continue;
      }
      LASKeyInfo keyInfo = schema.getKeys().get(key);
      if (keyInfo != null && !keyInfo.isArray() || key.equals(PandoraMongoData.KEY_OBJECT_UPDATED_AT) || key.equals(PandoraMongoData.KEY_OBJECT_CREATED_AT)) {
        if (val instanceof Map) {
          requestDataDecorate((Map) val, schema, true);
        } else if (val instanceof List) {
          requestDataDecorate((List) val, schema, true);
        } else {
          datum.put(key, unWrap(val));
        }
      }

      if (val instanceof Map) {
        requestDataDecorate((Map) val, schema, flag);
      } else if (val instanceof List) {
        requestDataDecorate((List) val, schema, flag);
      } else {
        if (flag) {
          datum.put(key, unWrap(val));
        }
      }
    }
  }

  public void requestDataDecorate(List data, LASClassSchema schema, boolean flag) {
    int size = data.size();
    int j = 0;
    for (int i = 0; i < size; i++) {
      Object val = data.get(j++);
      if (val == null) {
        continue;
      }
      if (val instanceof Map) {
        requestDataDecorate((Map) val, schema, false);
      } else if (val instanceof List) {
        requestDataDecorate((List) val, schema, flag);
      } else {
        if (flag) {
          Object newVal = unWrap(val);
          if (val != newVal) {
            data.remove(val);
            data.add(newVal);
            j--;
          }
        }
      }
    }
  }

  public Map<String, ?> responseDataDecorate(LASClassSchema classSchema, Map map, boolean relations) {
    if (map == null || map.isEmpty()) {
      return map;
    }
    Map wrapResponse = new LASObject();

    for (Object key : map.keySet()) {
      LASKeyInfo zCloudKeyInfo = classSchema.getKeys().get(key);
      Object val = map.get(key);
      if (val == null) {
        wrapResponse.put(key, null);
        continue;
      }

      if (zCloudKeyInfo != null) {
        wrapResponse.put(key, wrap(val, zCloudKeyInfo, relations));
      } else if (key.equals("objectId")
          || key.equals(PandoraMongoData.KEY_OBJECT_CREATED_AT)
          || key.equals(PandoraMongoData.KEY_OBJECT_UPDATED_AT)
          || key.equals(PandoraMongoData.KEY_OBJECT_ACL)
          || key.equals("className")
          || key.equals("__type")) {
        wrapResponse.put(key, val);
      }
    }
    return wrapResponse;
  }

  public <T> T responseDataDecorate(LASClassSchema classSchema, List<Map> response, boolean relations) {
    if (response == null) {
      return null;
    }

    List<Map<String, ?>> wrapList = new ArrayList<>();

    for (Map<String, Object> map : response) {
      wrapList.add(responseDataDecorate(classSchema, map, relations));
    }

    return (T) wrapList;
  }

  private <T> T unWrap(Object val) {
    if (val instanceof String || val instanceof Number || val instanceof Boolean || val instanceof ObjectId) {
      return (T) val;
    } else if (val instanceof Map) {
      return (T) val;
    } else if (val instanceof LASFile) {
      Map file = new HashMap();
      file.put("name", ((LASFile) val).getName());
      file.put("url", ((LASFile) val).getUrl());
      return (T) file;
    } else if (val instanceof LASArray) {
      //TODO
      return (T) val;
    } else if (val instanceof LASDate) {
      return (T) ((LASDate) val).getTime();
    } else if (val instanceof LASGeoPoint) {
      Map geo = new HashMap();
      geo.put("type", "Point");

      List<Double> pointers = new ArrayList<>();
      pointers.add(((LASGeoPoint) val).getLongitude());
      pointers.add(((LASGeoPoint) val).getLatitude());

      geo.put("coordinates", pointers);
      return (T) geo;
    } else if (val instanceof LASPointer) {
      String objectId = ((LASPointer) val).getObjectId();
      return (T) new ObjectId(objectId);
    } else if (val instanceof LASRelation) {

      List<LASPointer> objects = ((LASRelation) val).getObjects();
      if (objects.isEmpty()) {
        return null;
      }

      ObjectId[] ids = new ObjectId[objects.size()];
      for (int i = 0; i < objects.size(); i++) {
        ids[i] = new ObjectId(objects.get(i).getObjectId());
      }

      return (T) ids;
    } else if (val instanceof LASBytes) {
      return (T) ((LASBytes) val).getBase64();
    } else {
      throw new KeyInvalidException("[" + val + "] is invalid LASKeyType.");
    }
  }

  private <T> T wrap(Object val, LASKeyInfo keyInfo, boolean relations) {
    LASKeyType keyType = keyInfo.getType();

    if (keyType == LASKeyType.String
        || keyType == LASKeyType.Number
        || keyType == LASKeyType.Boolean
        || keyType == LASKeyType.Object
        || keyType == LASKeyType.Array) {
      return (T) val;

    } else if (keyType == LASKeyType.Date) {
      Map dataMap = new HashMap();
      dataMap.put("__type", LASKeyType.Date);
      dataMap.put("iso", DateUtils.encodeDate(new Date((long) val)));

      return (T) dataMap;

    } else if (keyType == LASKeyType.Bytes) {
      Map dataMap = new HashMap();
      dataMap.put("__type", LASKeyType.Bytes);
      dataMap.put("base64", val);

      return (T) dataMap;

    } else if (keyType == LASKeyType.GeoPoint) {
      List list = (List) ((Map) val).get("coordinates");
      Map dataMap = new HashMap();
      dataMap.put("__type", LASKeyType.GeoPoint);
      dataMap.put("longitude", list.get(0));
      dataMap.put("latitude", list.get(1));

      return (T) dataMap;

    } else if (keyType == LASKeyType.Pointer) {
      if (val instanceof Map) {
        return (T) val;
      }
      Map dataMap = new HashMap();
      dataMap.put("__type", LASKeyType.Pointer);
      dataMap.put("className", keyInfo.getClassName());
      dataMap.put("objectId", val.toString());

      return (T) dataMap;

    } else if (keyType == LASKeyType.Relation) {
      Map dataMap = new HashMap();
      dataMap.put("__type", LASKeyType.Relation);
      dataMap.put("className", keyInfo.getClassName());
      if (relations) {
        dataMap.put("objectIds", val);
      }

      return (T) dataMap;

    } else if (keyType == LASKeyType.File) {
      Map dataMap = new HashMap();
      dataMap.put("__type", LASKeyType.File);
      if (val instanceof Map) {
        dataMap.put("name", ((Map) val).get("name"));
        dataMap.put("url", ((Map) val).get("url"));
      } else {
        dataMap.put("url", val);
      }

      return (T) dataMap;

    } else {
      throw new KeyInvalidException("[" + val + "] is invalid LASKeyType.");
    }
  }
}
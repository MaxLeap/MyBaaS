package com.maxleap.pandora.data.support;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sneaky
 * @since 3.0.0
 */
public class LasSunObjectIdMapper {

  public static com.maxleap.domain.base.ObjectId toLasSunObjectId(ObjectId obj) {
    return new com.maxleap.domain.base.ObjectId(obj.toHexString());
  }

  public static ObjectId toMongoObjectId(com.maxleap.domain.base.ObjectId obj) {
    return new ObjectId(obj.toHexString());
  }

  public static void toLasSunObjectId(Map<Object, Object> map, boolean topLevel) {
    for (Map.Entry entry : map.entrySet()) {
      Object value = entry.getValue();
      if (value instanceof  ObjectId) {
        map.put(entry.getKey(), toLasSunObjectId((ObjectId) value));
      } else if (value instanceof Map) {
        toLasSunObjectId((Map) value, false);
      } else if (value instanceof List) {
        map.put(entry.getKey(), toLasSunObjectId((List) value));
      } else if (value instanceof Object[]) {
        Object[] objects = (Object[]) value;
        List list = new ArrayList<>();
        for (Object obj : objects) {
          list.add(obj);
        }
        map.put(entry.getKey(), toLasSunObjectId(list));
      }
    }
    if (topLevel) {
      toObjectId(map);
    }
  }

  public static void toLasSunObjectId(Map map) {
    toLasSunObjectId(map, true);
  }

  public static void toMongoObjectId(Map<Object, Object> map, boolean topLevel) {
    for (Map.Entry entry : map.entrySet()) {
      Object value = entry.getValue();
      if (value instanceof com.maxleap.domain.base.ObjectId) {
        map.put( entry.getKey(), toMongoObjectId((com.maxleap.domain.base.ObjectId) value));
      } else if (value instanceof Map) {
        toMongoObjectId((Map) value, false);
      } else if (value instanceof List) {
        map.put(entry.getKey(), toMongoObjectId((List) value));
      } else if (value instanceof Object[]) {
        Object[] objects = (Object[]) value;
        List list = new ArrayList<>();
        for (Object obj : objects) {
          list.add(obj);
        }
        map.put(entry.getKey(), toMongoObjectId(list));
      }
    }
    if (topLevel) {
      to_id(map);
    }
  }

  public static void toMongoObjectIdInMongoQuery(Map<Object, Object> map) {
    for (Map.Entry entry : map.entrySet()) {
      Object value = entry.getValue();
      if (value instanceof com.maxleap.domain.base.ObjectId) {
        map.put(entry.getKey(), toMongoObjectId((com.maxleap.domain.base.ObjectId) value));
      } else if (value instanceof Map) {
        toMongoObjectIdInMongoQuery((Map) value);
      } else if (value instanceof List) {
        map.put(entry.getKey(), toMongoObjectIdInMongoQuery((List) value));
      } else if (value instanceof Object[]) {
        Object[] objects = (Object[]) value;
        List list = new ArrayList<>();
        for (Object obj : objects) {
          list.add(obj);
        }
        map.put(entry.getKey(), toMongoObjectIdInMongoQuery(list));
      }
    }
    to_id(map);
  }

  public static void toMongoObjectId(Map map) {
    toMongoObjectId(map, true);
  }

  public static void to_id(Map map) {
    Object objectId = map.get("objectId");
    if (objectId != null) {
      map.put("_id", objectId);
      map.remove("objectId");
    }
  }

  public static void toObjectId(Map map) {
    Object _id = map.get("_id");
    if (_id != null) {
      map.put("objectId", _id);
      map.remove("_id");
    }
  }

  public static List toLasSunObjectId(List list) {
    List newList = new ArrayList();
    int size = list.size();
    for (int i = 0; i < size; i++) {
      Object val = list.get(i);
      if (val instanceof Map) {
        toLasSunObjectId((Map) val, false);
        newList.add(val);
      } else if (val instanceof List) {
        newList.add(toLasSunObjectId((List) val));
      } else if (val instanceof ObjectId) {
        newList.add(toLasSunObjectId((ObjectId) val));
      } else if (val instanceof Object[]) {
        Object[] objects = (Object[]) val;
        List list1 = new ArrayList<>();
        for (Object obj : objects) {
          list1.add(obj);
        }
        newList.add(toLasSunObjectId(list1));
      } else {
        newList.add(val);
      }
    }
    return newList;
  }

  public static List toMongoObjectIdInMongoQuery(List list) {
    List newList = new ArrayList();
    int size = list.size();
    for (int i = 0; i < size; i++) {
      Object val = list.get(i);
      if (val instanceof Map) {
        toMongoObjectIdInMongoQuery((Map) val);
        newList.add(val);
      } else if (val instanceof List) {
        newList.add(toMongoObjectIdInMongoQuery((List) val));
      } else if (val instanceof com.maxleap.domain.base.ObjectId) {
        newList.add(toMongoObjectId((com.maxleap.domain.base.ObjectId) val));
      } else if (val instanceof Object[]) {
        Object[] objects = (Object[]) val;
        List list1 = new ArrayList<>();
        for (Object obj : objects) {
          list1.add(obj);
        }
        newList.add(toMongoObjectIdInMongoQuery(list1));
      } else {
        newList.add(val);
      }
    }
    return newList;
  }

  public static List toMongoObjectId(List list) {
    List newList = new ArrayList();
    int size = list.size();
    for (int i = 0; i < size; i++) {
      Object val = list.get(i);
      if (val instanceof Map) {
        toMongoObjectId((Map) val, false);
        newList.add(val);
      } else if (val instanceof List) {
        newList.add(toMongoObjectId((List) val));
      } else if (val instanceof com.maxleap.domain.base.ObjectId) {
        newList.add(toMongoObjectId((com.maxleap.domain.base.ObjectId) val));
      } else if (val instanceof Object[]) {
        Object[] objects = (Object[]) val;
        List list1 = new ArrayList<>();
        for (Object obj : objects) {
          list1.add(obj);
        }
        newList.add(toMongoObjectId(list1));
      } else {
        newList.add(val);
      }
    }
    return newList;
  }

}

package com.maxleap.pandora.data.support.filter;

import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.PandoraMongoData;
import com.maxleap.pandora.core.exception.KeyInvalidException;
import com.maxleap.pandora.core.exception.LASDataException;
import com.maxleap.pandora.core.lasdata.LASClassSchema;
import com.maxleap.pandora.core.lasdata.LASKeyInfo;
import com.maxleap.pandora.core.lasdata.LASKeyType;
import com.maxleap.pandora.core.lasdata.types.*;
import com.maxleap.pandora.core.mongo.MongoUpdate;
import com.maxleap.pandora.data.support.ClassSchemaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sneaky
 * @since 2.0.0
 */
@Singleton
public class ClassSchemaAdjuster {
  private static transient final Logger log = LoggerFactory.getLogger(DataDecoratorFilter.class);
  private ClassSchemaManager classSchemaManager;

  @Inject
  public ClassSchemaAdjuster(ClassSchemaManager classSchemaManager) {
    this.classSchemaManager = classSchemaManager;
  }

  public LASClassSchema adjust(MongoUpdate update, LASClassSchema schema) throws LASDataException {
    Map<String, LASKeyInfo> notExistKeyInfo = new HashMap<>();
    Map<String, LASKeyInfo> keys = schema.getKeys();
    Map<String, ?> doc = update.getModifierOps();

    for (String key : doc.keySet()) {
      if (key.indexOf("$") < 0) {
        throw new LASDataException(LASDataException.INTERNAL_SERVER_ERROR, "Unknown modifier: " + key);
      }
      if (key.equals("$set") || key.equals("$inc") || key.equals("$mul") || key.equals("$addToSet") || key.equals("$push")) {
        Map<String, ?> data = (Map) doc.get(key);
        checkKeyTypeAndUpdateSchema(keys, data, notExistKeyInfo);
      }
    }

    if (!notExistKeyInfo.isEmpty()) {
      classSchemaManager.setKeyInfo(schema.getObjectId(), notExistKeyInfo);
      log.info("Auto modified schema because schema keys not exists. app id: {}, className: {}, notExistKeys: {}",
          schema.getAppId(), schema.getClassName(), notExistKeyInfo);
      return classSchemaManager.get(schema.getObjectId());
    }

    return schema;
  }

  public LASClassSchema adjust(LASObject object, LASClassSchema schema) throws LASDataException {
    Map<String, LASKeyInfo> notExistKeyInfo = new HashMap<>();
    checkKeyTypeAndUpdateSchema(schema.getKeys(), object.getMap(), notExistKeyInfo);

    if (!notExistKeyInfo.isEmpty()) {
      classSchemaManager.setKeyInfo(schema.getObjectId(), notExistKeyInfo);
      log.info("Auto modified schema because schema keys not exists. app id: {}, className: {}, notExistKeys: {}",
          schema.getAppId(), schema.getClassName(), notExistKeyInfo);
      return classSchemaManager.get(schema.getObjectId());
    } else {
      return schema;
    }
  }

  public LASClassSchema adjust(List<LASObject> objects, LASClassSchema schema) throws LASDataException {
    Map<String, LASKeyInfo> notExistKeyInfo = new HashMap<>();
    for (LASObject doc : objects) {
      checkKeyTypeAndUpdateSchema(schema.getKeys(), doc.getMap(), notExistKeyInfo);
    }
    if (!notExistKeyInfo.isEmpty()) {
      classSchemaManager.setKeyInfo(schema.getObjectId(), notExistKeyInfo);
      log.info("Auto modified schema because schema keys not exists. app id: {}, className: {}, notExistKeys: {}",
          schema.getAppId(), schema.getClassName(), notExistKeyInfo);
      return classSchemaManager.get(schema.getObjectId());
    } else {
      return schema;
    }
  }

  public LASClassSchema adjust(ObjectId appId, String className, LASObject obj) throws LASDataException {
    LASClassSchema schema = createClassSchema(appId, className);
    Map<String, LASKeyInfo> notExistKeyInfo = new HashMap<>();
    putKeys(obj.getMap(), notExistKeyInfo);
    schema.setKeys(notExistKeyInfo);
    schema = classSchemaManager.create(schema);
    if (log.isInfoEnabled()) {
      log.info("Auto create schema because schema not exists. app id: {}, className: {}, schema: {}", appId, className, schema);
    }
    return schema;
  }

  private void putKeys(Map<String, ?> doc, Map<String, LASKeyInfo> notExistKeyInfo) {
    Iterator<String> iterator = doc.keySet().iterator();
    for (; iterator.hasNext(); ) {
      String key = iterator.next();

      if (key.equals("ACL")
          || key.equals(PandoraMongoData.KEY_OBJECT_ID)
          || key.equals(PandoraMongoData.KEY_OBJECT_UPDATED_AT)
          || key.equals(PandoraMongoData.KEY_OBJECT_CREATED_AT)
          || key.equals("_id")) {
        continue;
      }

      LASKeyInfo valueKeyInfo = fromValue(doc.get(key));
      if (valueKeyInfo != null) {
        notExistKeyInfo.put(key, valueKeyInfo);
      } else {
        notExistKeyInfo.put(key, fromValue(""));
      }
    }
  }

  private LASClassSchema createClassSchema(ObjectId appId, String className) {
    LASClassSchema classSchema = new LASClassSchema();

    classSchema.setClassName(className);
    classSchema.setAppId(appId);
    classSchema.setCreatedAt(System.currentTimeMillis());
    classSchema.setUpdatedAt(System.currentTimeMillis());
    return classSchema;
  }

  private void checkKeyTypeAndUpdateSchema(Map<String, LASKeyInfo> keys, Map<String, ?> doc, Map<String, LASKeyInfo> notExistKeyInfo) {
    Map<String, ?> copy = new HashMap<>(doc);

    Iterator<String> iterator = copy.keySet().iterator();
    for (int j = 0; j < copy.size(); j++) {
      String key = iterator.next();

      Object val = doc.get(key);

      if (key.equals("ACL")) {
        if (val == null) {
          doc.remove(key);
        } else if (!(val instanceof Map)) {
          throw new KeyInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[ACL] expected An Object.");
        }
        continue;
      } else if (key.equals(PandoraMongoData.KEY_OBJECT_ID)
          || key.equals(PandoraMongoData.KEY_OBJECT_UPDATED_AT)
          || key.equals(PandoraMongoData.KEY_OBJECT_CREATED_AT)
          || key.equals("_id")) {
        continue;
      }

      int i = key.indexOf(".");
      if (i > 0) {
        String prefixKey = key.substring(0, i);
        if (notExistKeyInfo.get(prefixKey) == null) {
          if (keys.get(prefixKey) == null) {
            notExistKeyInfo.put(prefixKey, fromValue(new HashMap<>()));
          }
        }
        continue;
      }

      LASKeyInfo expectKeyInfo = keys.get(key);

      if (val == null) {
        if (expectKeyInfo == null) {
          notExistKeyInfo.put(key, fromValue(""));
        } else if (expectKeyInfo.isRelation() || expectKeyInfo.isNumber() || expectKeyInfo.isArray() || expectKeyInfo.isObject()) {
          doc.remove(key);
        }
        continue;
      }

      if (val instanceof String && val.toString().trim().equals("")) {
        if (expectKeyInfo != null && !expectKeyInfo.isString()) {
          doc.remove(key);
        } else if (expectKeyInfo == null) {
          notExistKeyInfo.put(key, fromValue(""));
        }
        continue;
      }

      LASKeyInfo valueKeyInfo = fromValue(val);

      if (valueKeyInfo == null) {
        continue;
      }

      if (expectKeyInfo == null) {
        if (valueKeyInfo.isArray() && val instanceof List) {
          List listVal = (List) val;
          if (listVal.size() > 0 && listVal.get(0) instanceof LASPointer) {
            LASPointer pointer = (LASPointer) listVal.get(0);
            LASKeyInfo keyInfo = new LASKeyInfo();
            keyInfo.setClassName(pointer.getClassName());
            keyInfo.setType(LASKeyType.Relation);
            notExistKeyInfo.put(key, keyInfo);
          } else {
            notExistKeyInfo.put(key, valueKeyInfo);
          }
        } else {
          notExistKeyInfo.put(key, valueKeyInfo);
        }
      } else if (!expectKeyInfo.isString() && !expectKeyInfo.getType().equals(valueKeyInfo.getType())) {
        if (!(expectKeyInfo.isArray() || expectKeyInfo.isRelation() && valueKeyInfo.isArray() && checkSize(val)))  {
          throw new KeyInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[" + key + "] expected " + expectKeyInfo.getType() + ". but is " + valueKeyInfo.getType() + " actually");
        }
      }
    }
  }

  private boolean checkSize(Object val) {
    if (val instanceof Map) {
      Object $each = ((Map) val).get("$each");
      if ($each != null) {
        List array = (List) $each;
        if (array.size() == 0) {
          return true;
        }
      }
    } else if (val instanceof List) {
      List listVal = (List) val;
      if (listVal.size() > 0 && listVal.get(0) instanceof LASPointer) {
        return true;
      }
    }
    return false;
  }

  private LASKeyInfo fromValue(Object val) {
    if (val == null) {
      return null;
    }

    LASKeyInfo key = new LASKeyInfo();
    if (val instanceof String) {
      key.setType(LASKeyType.String);
    } else if (val instanceof Map) {
      Object $each = ((Map) val).get("$each");
      if ($each != null) {
        List array = (List) $each;
        key.setType(LASKeyType.Array);

        if (array.size() > 0) {
          if (array.get(0) instanceof LASPointer) {
            key.setType(LASKeyType.Relation);
            key.setClassName(((LASPointer) array.get(0)).getClassName());
          }
        }
      } else {
        key.setType(LASKeyType.Object);
      }

    } else if (val instanceof LASFile) {
      key.setType(LASKeyType.File);
    } else if (val instanceof LASArray) {
      key.setType(LASKeyType.Array);
    } else if (val instanceof LASDate) {
      key.setType(LASKeyType.Date);
    } else if (val instanceof LASGeoPoint) {
      key.setType(LASKeyType.GeoPoint);
    } else if (val instanceof LASPointer) {
      key.setType(LASKeyType.Pointer);
      key.setClassName(((LASPointer) val).getClassName());
    } else if (val instanceof LASRelation) {
      key.setType(LASKeyType.Relation);
      key.setClassName(((LASRelation) val).getClassName());
    } else if (val instanceof Number) {
      key.setType(LASKeyType.Number);
    } else if (val instanceof Boolean) {
      key.setType(LASKeyType.Boolean);
    } else if (val instanceof LASBytes) {
      key.setType(LASKeyType.Bytes);
    } else {
      throw new KeyInvalidException("[" + val + "] is invalid LASKeyType.");
    }
    return key;
  }
}

package com.maxleap.pandora.data.support.lasdata;

import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.PandoraMongoData;
import com.maxleap.pandora.core.exception.KeyInvalidException;
import com.maxleap.pandora.core.exception.LASDataException;
import com.maxleap.pandora.core.exception.TypeInvalidException;
import com.maxleap.pandora.core.exception.UnauthorizedException;
import com.maxleap.pandora.core.lasdata.LASClassSchema;
import com.maxleap.pandora.core.lasdata.LASKeyInfo;
import com.maxleap.pandora.core.lasdata.LASKeyType;
import com.maxleap.pandora.core.lasdata.types.*;
import com.maxleap.pandora.core.mongo.MongoQuery;
import com.maxleap.pandora.core.mongo.MongoUpdate;
import com.maxleap.pandora.core.utils.Assertions;
import com.maxleap.pandora.data.support.ClassSchemaManager;
import com.maxleap.pandora.data.support.MongoEntityManager;
import com.maxleap.pandora.data.support.filter.AppDataConvertFilter;
import com.maxleap.pandora.data.support.utils.DbAndTableMapper;
import com.mongodb.client.model.IndexOptions;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ClassSchemaManager.
 *
 * @author sneaky
 * @since 2.0.0
 */
@Singleton
public class ClassSchemaManagerImpl implements ClassSchemaManager {
  private final static Logger LOG = LoggerFactory.getLogger(ClassSchemaManagerImpl.class);
  static Pattern pattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_$]*$");

  MongoEntityManager mongoEntityManager;
  AppDataConvertFilter dataConvertFilter;
//  Cache cache;
  String db = "schema";
  String table = "zcloud_class_schema";
  public enum BuiltInClass {
    _User, _Role, _Installation, _Product, _Parameter, _SYS_JobLog, _SYS_ConsoleLog, _SYS_Task, _SYS_EntityHook
  }

  @Inject
  public ClassSchemaManagerImpl(MongoEntityManager mongoEntityManager, AppDataConvertFilter dataConvertFilter) {
    this.mongoEntityManager = mongoEntityManager;
    this.dataConvertFilter = dataConvertFilter;
  }

  @Override
  public LASClassSchema get(ObjectId id) {
    return mongoEntityManager.get(db, table, id, LASClassSchema.class);
  }

  @Override
  public List<LASClassSchema> getAll(ObjectId appId) {
    MongoQuery query = new MongoQuery();
    query.equalTo("appId", appId);
    return mongoEntityManager.find(db, table, query, LASClassSchema.class);
  }

  @Override
  public LASClassSchema get(ObjectId appId, String className) {
    MongoQuery query = new MongoQuery();
    query.equalTo("appId", appId);
    query.equalTo("className", className);
    return mongoEntityManager.findUniqueOne(db, table, query, LASClassSchema.class);
  }

  @Override
  public int update(ObjectId appId, String className, MongoUpdate mongoUpdate) {
    builtInClassValidate(className, mongoUpdate.getModifierOps());
    MongoQuery mongoQuery = new MongoQuery();
    mongoQuery.equalTo("appId", appId);
    mongoQuery.equalTo("className", className);
    LASClassSchema lasClassSchema = mongoEntityManager.findUniqueOne(db, table, mongoQuery, LASClassSchema.class);
    if (lasClassSchema == null) {
      return 0;
    }
    Object $set = mongoUpdate.getModifierOps().get("$set");
    if ($set != null) {
      Map<String, Object> set = (Map) $set;
      for (String key : set.keySet()) {
        if (key.startsWith("keys.")) {
          Object o = set.get(key);
          if (o instanceof LASKeyInfo) {
            LASKeyInfo keyInfo = (LASKeyInfo) o;
            Map map = new HashMap<>();
            map.put("type", keyInfo.getType().name());
            String keyClassName = keyInfo.getClassName();
            if (keyClassName != null) {
              map.put("className", keyClassName);
            }
            set.put(key, map);
          }
        }
      }

      ensureGEOIndexInUpdate(lasClassSchema.getDbName(), lasClassSchema.getCollectionName(), set);
    }
    deleteKeys(lasClassSchema, mongoUpdate.getModifierOps());

    return mongoEntityManager.update(db, table, mongoQuery, mongoUpdate);
  }

  @Override
  public void setKeyInfo(ObjectId id, Map<String, LASKeyInfo> keyInfo) {
    LASClassSchema classSchema = mongoEntityManager.get(db, table, id, LASClassSchema.class);
    if (classSchema == null) {
      return;
    }
    MongoUpdate updateKeys = new MongoUpdate();

    for (String key : keyInfo.keySet()) {
      LASKeyInfo lasKeyInfo = keyInfo.get(key);
      if (lasKeyInfo != null) {
        Map map = new HashMap<>();
        map.put("type", lasKeyInfo.getType().name());
        String className = lasKeyInfo.getClassName();
        if (className != null) {
          map.put("className", className);
        }
        updateKeys.set("keys." + key, map);
      }
    }

    ensureGEOIndexInUpdate(classSchema.getDbName(), classSchema.getCollectionName(), (Map) updateKeys.getModifierOps().get("$set"));
    builtInClassValidate(classSchema.getClassName(), updateKeys.getModifierOps());
    mongoEntityManager.update(db, table, id, updateKeys);
  }

  @Override
  public void unsetKeyInfo(ObjectId id, List<String> classNameList) {
    LASClassSchema classSchema = mongoEntityManager.get(db, table, id, LASClassSchema.class);
    if (classSchema == null) {
      return;
    }
    MongoUpdate mongoUpdate = new MongoUpdate();
    for (String key : classNameList) {
      mongoUpdate.unset(key);
    }
    mongoEntityManager.update(db, table, id, mongoUpdate);
  }

  @Override
  public LASClassSchema create(LASClassSchema entity) {
    this.validateClassName(entity.getClassName());

    if (get(entity.getAppId(), entity.getClassName()) != null) {
      throw new LASDataException(LASDataException.INTERNAL_SERVER_ERROR, "class is exist. [class name: " + entity.getClassName() + "]");
    }

    if (StringUtils.isEmpty(entity.getDbName())) {
      entity.setDbName(DbAndTableMapper.getDB(entity.getAppId()));
    }

    if (StringUtils.isEmpty(entity.getCollectionName())) {
      entity.setCollectionName(DbAndTableMapper.getTable(entity.getAppId(), entity.getClassName()));
    }

    if (entity.getKeys() == null) {
      entity.setKeys(new HashMap<>());
    }
    validateKeys(entity.getKeys(), entity.getClassName());

    builtInClassProcess(entity);
    ensureIndexInCreate(entity.getDbName(), entity.getCollectionName(), entity.getClassName());

    LASClassSchema classSchema = mongoEntityManager.create(db, table, entity);
    ensureGEOIndexInCreate(classSchema.getDbName(), classSchema.getCollectionName(), classSchema.getKeys());
    return classSchema;
  }

  @Override
  public int delete(ObjectId id) {
    LASClassSchema classSchema = mongoEntityManager.get(db, table, id, LASClassSchema.class);
    if (classSchema == null) {
      return 0;
    }

    ObjectId appId = classSchema.getAppId();
    String className = classSchema.getClassName();
    return delete(appId, className);
  }

  @Override
  public int delete(ObjectId appId, String className) {
    deleteCheck(className);//Check className

    MongoQuery query = new MongoQuery();
    query.equalTo("appId", appId);
    query.equalTo("className", className);
    int delete = mongoEntityManager.delete(db, table, query);
    if (delete > 0) {
      mongoEntityManager.getMongoCollection(DbAndTableMapper.getDB(appId), DbAndTableMapper.getTable(appId, className)).drop();
      if (LOG.isInfoEnabled()) {
        LOG.info("Drop Collection. appId: {}, className: {}", appId, className);
      }
    }
    return delete;
  }

  private void builtInClassProcess(LASClassSchema entity) {
    if (entity.getClassName().equals(BuiltInClass._Installation.name())) {
      Map<String, LASKeyInfo> keys = entity.getKeys();

      if (keys.get("badge") != null && !keys.get("badge").isNumber()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[badge] expected number. but is" + keys.get("badge").getType() + " actually");
      }
      if (keys.get("channels") != null && !keys.get("channels").isArray()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[channels] expected array. but is" + keys.get("channels").getType() + " actually");
      }
      if (keys.get("deviceToken") != null && !keys.get("deviceToken").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[deviceToken] expected string. but is" + keys.get("deviceToken").getType() + " actually");
      }
      if (keys.get("deviceType") != null && !keys.get("deviceType").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[deviceType] expected string. but is" + keys.get("deviceType").getType() + " actually");
      }
      if (keys.get("installationId") != null && !keys.get("installationId").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[installationId] expected string. but is" + keys.get("installationId").getType() + " actually");
      }
      if (keys.get("timeZone") != null && !keys.get("timeZone").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[timeZone] expected string. but is" + keys.get("timeZone").getType() + " actually");
      }
      if (keys.get("pushType") != null && !keys.get("pushType").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[pushType] expected string. but is" + keys.get("pushType").getType() + " actually");
      }

      keys.put("badge", new LASKeyInfo(LASKeyType.Number));
      keys.put("channels", new LASKeyInfo(LASKeyType.Array));
      keys.put("deviceToken", new LASKeyInfo(LASKeyType.String));
      keys.put("deviceType", new LASKeyInfo(LASKeyType.String));
      keys.put("installationId", new LASKeyInfo(LASKeyType.String));
      keys.put("timeZone", new LASKeyInfo(LASKeyType.String));
      keys.put("pushType", new LASKeyInfo(LASKeyType.String));
      keys.put("locale", new LASKeyInfo(LASKeyType.String));
      keys.put("language", new LASKeyInfo(LASKeyType.String));

    } else if (entity.getClassName().equals(BuiltInClass._User.name())) {
      Map<String, LASKeyInfo> keys = entity.getKeys();

      if (keys.get("authData") != null && !keys.get("authData").isObject()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[authData] expected object. but is" + keys.get("authData").getType() + " actually");
      }
      if (keys.get("email") != null && !keys.get("email").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[email] expected string. but is" + keys.get("email").getType() + " actually");
      }
      if (keys.get("emailVerified") != null && !keys.get("emailVerified").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[emailVerified] expected string. but is" + keys.get("emailVerified").getType() + " actually");
      }
      if (keys.get("password") != null && !keys.get("password").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[password] expected string. but is" + keys.get("password").getType() + " actually");
      }
      if (keys.get("username") != null && !keys.get("username").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[username] expected string. but is" + keys.get("username").getType() + " actually");
      }
      if (keys.get("coin") != null && !keys.get("coin").isNumber()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[coin] expected string. but is" + keys.get("coin").getType() + " actually");
      }

      keys.put("authData", new LASKeyInfo(LASKeyType.Object));
      keys.put("email", new LASKeyInfo(LASKeyType.String));
      keys.put("emailVerified", new LASKeyInfo(LASKeyType.Boolean));
      keys.put("password", new LASKeyInfo(LASKeyType.String));
      keys.put("username", new LASKeyInfo(LASKeyType.String));
      keys.put("coin", new LASKeyInfo(LASKeyType.Number));

    } else if (entity.getClassName().equals(BuiltInClass._Role.name())) {
      Map<String, LASKeyInfo> keys = entity.getKeys();

      if (keys.get("name") != null && !keys.get("name").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[name] expected string. but is" + keys.get("name").getType() + " actually");
      }
      if (keys.get("roles") != null && !keys.get("roles").isRelation()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[roles] expected relation. but is" + keys.get("roles").getType() + " actually");
      }
      if (keys.get("users") != null && !keys.get("users").isRelation()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[users] expected relation. but is" + keys.get("users").getType() + " actually");
      }

      keys.put("name", new LASKeyInfo(LASKeyType.String));
      keys.put("roles", new LASKeyInfo(LASKeyType.Relation, BuiltInClass._Role.name()));
      keys.put("users", new LASKeyInfo(LASKeyType.Relation, BuiltInClass._User.name()));

    } else if (entity.getClassName().equals(BuiltInClass._Product.name())) {
      Map<String, LASKeyInfo> keys = entity.getKeys();

      if (keys.get("download") != null && !keys.get("download").isFile()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[download] expected file. but is" + keys.get("download").getType() + " actually");
      }
      if (keys.get("downloadName") != null && !keys.get("downloadName").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[downloadName] expected string. but is" + keys.get("downloadName").getType() + " actually");
      }
      if (keys.get("icon") != null && !keys.get("icon").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[icon] expected string. but is" + keys.get("icon").getType() + " actually");
      }
      if (keys.get("order") != null && !keys.get("order").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[order] expected number. but is" + keys.get("order").getType() + " actually");
      }
      if (keys.get("productIdentifier") != null && !keys.get("productIdentifier").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[productIdentifier] expected string. but is" + keys.get("productIdentifier").getType() + " actually");
      }
      if (keys.get("subtitle") != null && !keys.get("subtitle").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[subtitle] expected string. but is" + keys.get("subtitle").getType() + " actually");
      }
      if (keys.get("title") != null && !keys.get("title").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[title] expected string. but is" + keys.get("title").getType() + " actually");
      }

      keys.put("download", new LASKeyInfo(LASKeyType.File));
      keys.put("downloadName", new LASKeyInfo(LASKeyType.String));
      keys.put("icon", new LASKeyInfo(LASKeyType.File));
      keys.put("order", new LASKeyInfo(LASKeyType.Number));
      keys.put("productIdentifier", new LASKeyInfo(LASKeyType.String));
      keys.put("subtitle", new LASKeyInfo(LASKeyType.String));
      keys.put("title", new LASKeyInfo(LASKeyType.String));


    } else if (entity.getClassName().equals(BuiltInClass._Parameter.name())) {
      Map<String, LASKeyInfo> keys = entity.getKeys();

      if (keys.get("key") != null && !keys.get("key").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[key] expected file. but is" + keys.get("key").getType() + " actually");
      }
      if (keys.get("version") != null && !keys.get("version").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[version] expected file. but is" + keys.get("version").getType() + " actually");
      }
      if (keys.get("locale") != null && !keys.get("locale").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[locale] expected file. but is" + keys.get("locale").getType() + " actually");
      }
      if (keys.get("value") != null && !keys.get("value").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[value] expected file. but is" + keys.get("value").getType() + " actually");
      }
      if (keys.get("desc") != null && !keys.get("desc").isString()) {
        throw new TypeInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[desc] expected file. but is" + keys.get("desc").getType() + " actually");
      }

      keys.put("key", new LASKeyInfo(LASKeyType.String));
      keys.put("version", new LASKeyInfo(LASKeyType.String));
      keys.put("locale", new LASKeyInfo(LASKeyType.String));
      keys.put("value", new LASKeyInfo(LASKeyType.String));
      keys.put("desc", new LASKeyInfo(LASKeyType.String));

    }
  }

  public void validateClassName(String className) {
    if (StringUtils.isBlank(className)) {
      throw new LASDataException(LASDataException.INVALID_CLASS_NAME, "className must be null");
    }
    int one = className.charAt(0);
    boolean error = true;
    if (!(one >= 65 && one <= 90 || one >= 97 && one <= 122)) {
      if (one == 95) {
        if (className.equals(BuiltInClass._Installation.name())
            || className.equals(BuiltInClass._Product.name())
            || className.equals(BuiltInClass._Role.name())
            || className.equals(BuiltInClass._Parameter.name())
            || className.equals(BuiltInClass._User.name())
            || className.toLowerCase().startsWith("_sys_")) {
          return;
        }
      }
      error = false;
    }

    Matcher matcher = pattern.matcher(className);
    error = matcher.find();

    if (!error) {
      throw new LASDataException(LASDataException.INVALID_CLASS_NAME, "ClassName must be start with a letters, and letters, numbers, _, $ are the only valid characters");
    }
  }

  @Override
  public void validateKeyType(ObjectId appId, String className, Map doc) {
    Assertions.notNull("appId", appId);
    Assertions.notBlank("className", className);
    Assertions.notNull("doc", doc);

    LASClassSchema lasClassSchema = get(appId, className);
    dataConvertFilter.parse(doc);
    Map<String, LASKeyInfo> existKeys = null;
    Map notExistKeys = new HashMap<>();

    if (lasClassSchema == null) {
      existKeys = new HashMap<>();
    } else {
      existKeys = lasClassSchema.getKeys();
    }

    for (Object key : doc.keySet()) {
      Object val = doc.get(key);
      if (val == null) {
        continue;
      }
      if (key.equals(PandoraMongoData.KEY_OBJECT_ACL) && !(val instanceof Map)) {
        throw new KeyInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[ACL] expected An Object.");
      } else if (key.equals(PandoraMongoData.KEY_OBJECT_ID)
          || key.equals(PandoraMongoData.KEY_OBJECT_UPDATED_AT)
          || key.equals(PandoraMongoData.KEY_OBJECT_CREATED_AT)
          || key.equals("_id")
          || key.equals(PandoraMongoData.KEY_OBJECT_ACL)) {
        continue;
      }

      LASKeyInfo expectKeyInfo = existKeys.get(key);
      LASKeyInfo valueKeyInfo = fromValue(val);

      if (expectKeyInfo == null) {
        notExistKeys.put(key, valueKeyInfo);
      } else if (!expectKeyInfo.isString() && !expectKeyInfo.getType().equals(valueKeyInfo.getType())) {
        if (!(expectKeyInfo.isArray() || expectKeyInfo.isRelation() && valueKeyInfo.isArray() && checkSize((Map) val)))  {
          throw new KeyInvalidException(LASDataException.INCORRECT_TYPE, "The type of key[" + key + "] expected " + expectKeyInfo.getType() + ". but is " + valueKeyInfo.getType() + " actually");
        }
      } else if (expectKeyInfo.isPointer() && !expectKeyInfo.getClassName().equals(valueKeyInfo.getClassName())) {
        throw new KeyInvalidException(LASDataException.INCORRECT_TYPE, "The className of key[" + key + "] expected " + expectKeyInfo.getClassName() + ". but is " + valueKeyInfo.getClassName() + " actually");
      }
    }
    if (notExistKeys.size() > 0) {
      if (lasClassSchema == null) {
        lasClassSchema = new LASClassSchema();
        lasClassSchema.setAppId(appId);
        lasClassSchema.setClassName(className);
        lasClassSchema.setKeys(notExistKeys);
        long ts = System.currentTimeMillis();
        lasClassSchema.setUpdatedAt(ts);
        lasClassSchema.setCreatedAt(ts);
        lasClassSchema.setDbName(DbAndTableMapper.getDB(appId));
        lasClassSchema.setCollectionName(DbAndTableMapper.getTable(appId, className));
        create(lasClassSchema);
      } else {
        setKeyInfo(lasClassSchema.getObjectId(), notExistKeys);
      }
    }
  }

  private void builtInClassValidate(String className, Map<String, ?> newDoc) {
    Set<String> keys = new HashSet<>();

    Object $set = newDoc.get("$set");
    if ($set instanceof Map) {
      keys.addAll(((Map) $set).keySet());
    }

    Object $unset = newDoc.get("$unset");
    if ($unset instanceof Map) {
      keys.addAll(((Map) $unset).keySet());
    }

    if (className.equals(BuiltInClass._Installation.name())) {
      for (String key : keys) {
        key = key.trim();
        if (key.equals("keys.badge") || key.equals("keys.channels") || key.equals("keys.deviceToken") || key.equals("keys.deviceType")
            || key.equals("keys.installationId") || key.equals("keys.timeZone") || key.equals("keys.timeZone")) {
          throw new KeyInvalidException("the " + key + " key is a reserved word in " + className);
        }
        validateKey(key, className);
      }
    } else if (className.equals(BuiltInClass._User.name())) {
      for (String key : keys) {
        key = key.trim();
        if (key.equals("keys.authData") || key.equals("keys.email") || key.equals("keys.emailVerified")
            || key.equals("keys.password") || key.equals("keys.username") || key.equals("keys.coin")) {
          throw new KeyInvalidException("the " + key + " key is a reserved word in " + className);
        }
        validateKey(key, className);
      }

    } else if (className.equals(BuiltInClass._Role.name())) {
      for (String key : keys) {
        key = key.trim();
        if (key.equals("keys.name") || key.equals("keys.roles") || key.equals("keys.users")) {
          throw new KeyInvalidException("the " + key + " key is a reserved word in " + className);
        }
        validateKey(key, className);
      }

    } else if (className.equals(BuiltInClass._Product.name())) {
      for (String key : keys) {
        key = key.trim();
        if (key.equals("keys.download") || key.equals("keys.downloadName") || key.equals("keys.icon")
            || key.equals("keys.order") || key.equals("keys.productIdentifier") || key.equals("keys.subtitle") || key.equals("keys.title")) {
          throw new KeyInvalidException("the " + key + " key is a reserved word in " + className);
        }
        validateKey(key, className);
      }
    } else if (className.equals(BuiltInClass._Parameter.name())) {
      for (String key : keys) {
        key = key.trim();
        if (key.equals("keys.key") || key.equals("keys.version") || key.equals("keys.locale")
            || key.equals("keys.desc") || key.equals("keys.value")) {
          throw new KeyInvalidException("the " + key + " key is a reserved word in " + className);
        }
        validateKey(key, className);
      }
    } else {
      for (String key : keys) {
        validateKey(key.trim(), className);
      }
    }
  }

  private void deleteCheck(String className) {
    if (className.equals(BuiltInClass._Installation.name())
        || className.equals(BuiltInClass._Product.name())
        || className.equals(BuiltInClass._Role.name())
        || className.equals(BuiltInClass._Parameter.name())
        || className.equals(BuiltInClass._User.name())) {
      throw new UnauthorizedException("The class: " + className + " is reserved.");
    }
  }

  private void ensureGEOIndexInUpdate(String db, String table, Map<String, ?> keys) {
    if (keys == null) {
      return;
    }
    for (String key : keys.keySet()) {
      Object val = keys.get(key);
      if (key.startsWith("keys.") && val instanceof Map) {
        Map lasKeyInfo = (Map) val;
        if (lasKeyInfo != null && LASKeyType.GeoPoint.name().equals(lasKeyInfo.get("type"))) {
          int i = key.indexOf(".");
          if (i > 0) {
            Document indexKey = new Document();
            indexKey.put(key.substring(i + 1), "2dsphere");
            IndexOptions indexOptions = new IndexOptions();
            indexOptions.background(true);
            mongoEntityManager.getMongoCollection(db, table).createIndex(indexKey, indexOptions);
          }
        }
      }
    }
  }

  private void ensureGEOIndexInCreate(String db, String table, Map<String, LASKeyInfo> keys) {
    for (String key : keys.keySet()) {
      if (keys.get(key).isGeoPoint()) {
        Document indexKey = new Document();
        indexKey.put(key, "2dsphere");
        IndexOptions indexOptions = new IndexOptions();
        indexOptions.background(true);
        mongoEntityManager.getMongoCollection(db, table).createIndex(indexKey, indexOptions);
      }
    }
  }

  private void ensureIndexInCreate(String schema, String collection, String className) {
    if (className.equals(BuiltInClass._Installation.name())) {
      ensureIndex(collection, new String[]{"deviceToken"}, new int[]{1}, null, false, false, false, -1, schema);
      ensureIndex(collection, new String[]{"installationId"}, new int[]{1}, null, false, false, false, -1, schema);
    } else if (className.equals(BuiltInClass._User.name())) {
      ensureIndex(collection, new String[]{"username"}, new int[]{1}, null, true, false, false, -1, schema);
      ensureIndex(collection, new String[]{"email"}, new int[]{1}, null, false, false, false, -1, schema);
      ensureIndex(collection, new String[]{"passport"}, new int[]{1}, null, false, false, false, -1, schema);
    }
  }

  /**
   * Triggers the actual index creation.
   *
   * @param table              the collection to create the index in
   * @param name               the name of the index about to be created
   * @param properties         the index definition field
   * @param unique             whether it shall be a unique index
   * @param directions         index order
   * @param dropDups           whether to drop duplicates
   * @param sparse             sparse or not
   * @param expireAfterSeconds the time to live for documents in the collection
   * @param db                 the schema name
   */
  protected void ensureIndex(String table, String[] properties, int[] directions, String name, boolean unique,
                             boolean dropDups, boolean sparse, int expireAfterSeconds, String db) {
    Document indexKey = new Document();

    for (int i = 0; i < properties.length; i++) {
      indexKey.put(properties[i], directions.length > i ? directions[i] : 1);
    }

    IndexOptions indexOptions = new IndexOptions();
    indexOptions.background(true);
    if (unique) {
      indexOptions.unique(true);
    }
    if (sparse) {
      indexOptions.sparse(true);
    }
    mongoEntityManager.getMongoCollection(db, table).createIndex(indexKey, indexOptions);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Created compound index " + indexKey);
    }

  }

  private void deleteKeys(LASClassSchema classSchema, Map<String, ?> updateMap) {
    Object $unset = updateMap.get("$unset");
    if ($unset instanceof Map && ((Map) $unset).size() > 0) {
      MongoUpdate mongoUpdate = new MongoUpdate();

      for (Object key : ((Map) $unset).keySet()) {
        int i = key.toString().indexOf("keys.");
        if (i >= 0) {
          mongoUpdate.unset(key.toString().substring(5));
        }
      }

      String db = classSchema.getDbName();
      String table = classSchema.getCollectionName();
      if (mongoEntityManager.count(db, table) <= 100000) {
        mongoEntityManager.update(db,table, new MongoQuery(), mongoUpdate);
      }
    }
  }

  private void validateKey(String key, String className) {
    if (key.equals("keys.createdAt")
        || key.equals("keys.updatedAt")
        || key.equals("keys.ACL")
        || key.equals("keys.objectId")
        || key.equals("keys._id")) {
      throw new KeyInvalidException("the " + key + " key is a reserved word in " + className);
    }
    Matcher matcher = pattern.matcher(key.substring(5));
    if (!matcher.find()) {
      throw new KeyInvalidException(String.format("field name should be start with letters, _, and content only support letters, _, $, numbers [%s]", key));
    }
  }

  private void validateKeys(Map<String, ?> keys, String className) {
    for (String key : keys.keySet()) {
      key = key.trim();
      if (key.equals("createdAt")
          || key.equals("updatedAt")
          || key.equals("ACL")
          || key.equals("objectId")
          || key.equals("_id")) {
        throw new KeyInvalidException("the " + key + " key is a reserved word in " + className);
      }
      Matcher matcher = pattern.matcher(key);
      if (!matcher.find()) {
        throw new KeyInvalidException(String.format("field name should be start with letters, _, and content only support letters, _, $, numbers [%s]", key));
      }
    }
  }

  private boolean checkSize(Map map) {
    Object $each = map.get("$each");
    if ($each != null) {
      List array = (List) $each;
      if (array.size() == 0) {
        return true;
      }
    }
    return false;
  }

  private LASKeyInfo fromValue(Object val) {
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

  private String getCacheKey(ObjectId appId, String className) {
    return appId + className;
  }

}

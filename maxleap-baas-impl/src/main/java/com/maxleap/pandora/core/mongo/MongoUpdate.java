package com.maxleap.pandora.core.mongo;

import com.maxleap.pandora.core.mongo.exception.MongoDataAccessException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * object update for Mongo
 *
 * @author sneaky
 * @since 3.0.0
 */
public class MongoUpdate {
  public enum Position {
    LAST, FIRST
  }

  protected Map modifierOps = new LinkedHashMap<>();
  protected boolean upsert;

  public MongoUpdate() {
  }

  public MongoUpdate(Map map) {
    this.modifierOps = map;
  }

  /**
   * Static factory method to create an  empty MongoUpdate
   *
   * @return
   */
  public static MongoUpdate getMongoUpdate() {
    return new MongoUpdate();
  }

  /**
   * Static factory method to create an MongoUpdate using the provided key in $set behavior
   *
   * @param key
   * @return
   */
  public static MongoUpdate getMongoUpdate(String key, Object value) {
    return new MongoUpdate().set(key, value);
  }

  /**
   * MongoUpdate using the {@literal $set} update modifier
   *
   * @param key
   * @param value
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/set/</b>
   */
  public MongoUpdate set(String key, Object value) {
    addMultiFieldOperation("$set", key, value);
    return this;
  }

  /**
   * MongoUpdate using the {@literal $set} update modifier
   * <p/>
   * Just update a given field in sub documents
   *
   * @param key
   * @param value
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/set/</b>
   */
  public MongoUpdate setBright(String key, Object value) {
    if (value instanceof Map) {
      for (Object childKey : ((Map) value).keySet()) {
        setBright(key + "." + childKey, ((Map) value).get(childKey));
      }
    } else {
      addMultiFieldOperation("$set", key, value);
    }
    return this;
  }

  public MongoUpdate setAllBright(Map<String, Object> map) {
    if (map != null) {
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        setBright(entry.getKey(), entry.getValue());
      }
    }
    return this;
  }

  public MongoUpdate setAllBright(Map<String, Object> map, String... excludes) {
    if (map != null) {
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        boolean flag = true;
        if (excludes != null) {
          for (String key : excludes) {
            if (entry.getKey().equals(key)) {
              set(entry.getKey(), entry.getValue());
              flag = false;
            }
          }
        }
        if (flag) {
          setBright(entry.getKey(), entry.getValue());
        }
      }
    }
    return this;
  }

  public MongoUpdate setAll(Map<String, Object> map) {
    if (map != null) {
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        set(entry.getKey(), entry.getValue());
      }
    }
    return this;
  }

  /**
   * MongoUpdate using the {@literal $setOnInsert} update modifier
   *
   * @param key
   * @param value
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/setOnInsert/</b>
   */
  public MongoUpdate setOnInsert(String key, Object value) {
    addMultiFieldOperation("$setOnInsert", key, value);
    return this;
  }

  /**
   * MongoUpdate using the {@literal $unset} update modifier
   *
   * @param key
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/unset/</b>
   */
  public MongoUpdate unset(String key) {
    addMultiFieldOperation("$unset", key, 1);
    return this;
  }

  public MongoUpdate unsetAll(List<String> keys) {
    if (keys != null) {
      for (String key : keys) {
        unset(key);
      }
    }
    return this;
  }

  /**
   * MongoUpdate using the {@literal $inc} update modifier
   *
   * @param key
   * @param inc
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/inc/</b>
   */
  public MongoUpdate inc(String key, Number inc) {
    addMultiFieldOperation("$inc", key, inc);
    return this;
  }

  /**
   * MongoUpdate using the {@code $push} update modifier. <br>
   *
   * @param key
   * @param values
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/push/</b>
   */
  public MongoUpdate push(String key, List values) {
    Map $each = new LinkedHashMap<>();
    $each.put("$each", values);
    addMultiFieldOperation("$push", key, $each);
    return this;
  }

  /**
   * MongoUpdate using the {@literal $push} update modifier
   *
   * @param key
   * @param value
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/push/</b>
   */
  public MongoUpdate push(String key, Object value) {
    addMultiFieldOperation("$push", key, value);
    return this;
  }

  /**
   * MongoUpdate using the {@literal $addToSet} update modifier
   *
   * @param key
   * @param value
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/addToSet/</b>
   */
  public MongoUpdate addToSet(String key, Object value) {
    addMultiFieldOperation("$addToSet", key, value);
    return this;
  }

  /**
   * MongoUpdate using the {@literal $addToSet} update modifier
   *
   * @param key
   * @param values
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/addToSet/</b>
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/each/#up._S_each</b>
   */
  public MongoUpdate addToSet(String key, List values) {
    Map $each = new LinkedHashMap<>();
    $each.put("$each", values);
    addMultiFieldOperation("$addToSet", key, $each);
    return this;
  }

  /**
   * MongoUpdate using the {@literal $pop} update modifier
   *
   * @param key
   * @param pos
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/pop/</b>
   */
  public MongoUpdate pop(String key, Position pos) {
    addMultiFieldOperation("$pop", key, pos == Position.FIRST ? -1 : 1);
    return this;
  }

  /**
   * MongoUpdate using the {@literal $pull} update modifier
   *
   * @param key
   * @param value
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/pull/</b>
   */
  public MongoUpdate pull(String key, Object value) {
    addMultiFieldOperation("$pull", key, value);
    return this;
  }


  /**
   * Update using the {@literal $pull} update modifier
   *
   * @param key
   * @param filters
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/pull/</b>
   */
  public MongoUpdate pull(String key, MongoQuery.EmbeddedElemMatcher filters) {
    Map $elemMatch = new LinkedHashMap<>();
    $elemMatch.put("$elemMatch", filters.getElemMatherFiler());
    addMultiFieldOperation("$pull", key, $elemMatch);
    return this;
  }

  /**
   * MongoUpdate using the {@literal $pullAll} update modifier
   *
   * @param key
   * @param values
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/pullAll/</b>
   */
  public MongoUpdate pullAll(String key, List values) {
    addMultiFieldOperation("$pullAll", key, values);
    return this;
  }

  /**
   * MongoUpdate using the {@literal $rename} update modifier
   *
   * @param oldName
   * @param newName
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/rename/</b>
   */
  public MongoUpdate rename(String oldName, String newName) {
    addMultiFieldOperation("$rename", oldName, newName);
    return this;
  }

  public Map getModifierOps() {
    return modifierOps;
  }

  public void setModifierOps(Map modifierOps) {
    this.modifierOps = modifierOps;
  }

  protected void addMultiFieldOperation(String operator, String key, Object value) {
//    Assert.hasText(key, "Key for update must not be null or blank.");
    Object existingValue = this.modifierOps.get(operator);
    Map keyValueMap;

    if (existingValue == null) {
      keyValueMap = new LinkedHashMap<>();
      this.modifierOps.put(operator, keyValueMap);
    } else {
      if (existingValue instanceof LinkedHashMap) {
        keyValueMap = (LinkedHashMap) existingValue;
      } else {
        throw new MongoDataAccessException("Modifier Operations should be a LinkedHashMap but was " + existingValue.getClass());
      }
    }
    keyValueMap.put(key, value);
  }

  public boolean isUpsert() {
    return upsert;
  }

  public MongoUpdate setUpsert(boolean upsert) {
    this.upsert = upsert;
    return this;
  }

  @Override
  public String toString() {
    return "{" +
        "modifierOps=" + modifierOps +
        ", upsert=" + upsert +
        '}';
  }
}


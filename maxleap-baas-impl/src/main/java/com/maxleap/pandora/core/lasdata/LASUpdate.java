package com.maxleap.pandora.core.lasdata;

import com.maxleap.pandora.core.lasdata.types.LASPointer;
import com.maxleap.pandora.core.mongo.MongoQuery;
import com.maxleap.pandora.core.mongo.MongoUpdate;
import com.maxleap.pandora.core.mongo.exception.MongoDataAccessException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * object update for LASData
 *
 * @author sneaky
 * @since 3.0.0
 */
public class LASUpdate extends MongoUpdate {
  public enum Position {
    LAST, FIRST
  }

  public LASUpdate() {
  }

  public LASUpdate(Map map) {
    this.modifierOps = map;
  }

  /**
   * Static factory method to create an  empty LASUpdate
   *
   * @return
   */
  public static LASUpdate getLASUpdate() {
    return new LASUpdate();
  }

  /**
   * Static factory method to create an LASUpdate using the provided key in $set behavior
   *
   * @param key
   * @return
   */
  public static LASUpdate getLASUpdate(String key, Object value) {
    return new LASUpdate().set(key, value);
  }

  /**
   * LASUpdate using the {@literal $set} update modifier
   *
   * @param key
   * @param value
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/set/</b>
   */
  public LASUpdate set(String key, Object value) {
    addMultiFieldOperation("$set", key, value);
    return this;
  }

  /**
   * LASUpdate using the {@literal $set} update modifier
   * <p/>
   * Just update a given field in sub documents
   *
   * @param key
   * @param value
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/set/</b>
   */
  public LASUpdate setBright(String key, Object value) {
    if (value instanceof Map) {
      for (Object childKey : ((Map) value).keySet()) {
        setBright(key + "." + childKey, ((Map) value).get(childKey));
      }
    } else {
      addMultiFieldOperation("$set", key, value);
    }
    return this;
  }

  public LASUpdate setAllBright(Map<String, Object> map) {
    if (map != null) {
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        setBright(entry.getKey(), entry.getValue());
      }
    }
    return this;
  }

  public LASUpdate setAllBright(Map<String, Object> map, String... excludes) {
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

  public LASUpdate setAll(Map<String, Object> map) {
    if (map != null) {
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        set(entry.getKey(), entry.getValue());
      }
    }
    return this;
  }

  /**
   * LASUpdate using the {@literal $setOnInsert} update modifier
   *
   * @param key
   * @param value
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/setOnInsert/</b>
   */
  public LASUpdate setOnInsert(String key, Object value) {
    addMultiFieldOperation("$setOnInsert", key, value);
    return this;
  }

  /**
   * LASUpdate using the {@literal $unset} update modifier
   *
   * @param key
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/unset/</b>
   */
  public LASUpdate unset(String key) {
    addMultiFieldOperation("$unset", key, 1);
    return this;
  }

  public LASUpdate unsetAll(List<String> keys) {
    if (keys != null) {
      for (String key : keys) {
        unset(key);
      }
    }
    return this;
  }

  /**
   * LASUpdate using the {@literal $inc} update modifier
   *
   * @param key
   * @param inc
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/inc/</b>
   */
  public LASUpdate inc(String key, Number inc) {
    addMultiFieldOperation("$inc", key, inc);
    return this;
  }

  /**
   * LASUpdate using the {@code $push} update modifier. <br>
   *
   * @param key
   * @param values
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/push/</b>
   */
  public LASUpdate push(String key, List values) {
    Map $each = new LinkedHashMap<>();
    $each.put("$each", values);
    addMultiFieldOperation("$push", key, $each);
    return this;
  }

  /**
   * LASUpdate using the {@literal $push} update modifier
   *
   * @param key
   * @param value
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/push/</b>
   */
  public LASUpdate push(String key, Object value) {
    addMultiFieldOperation("$push", key, value);
    return this;
  }

  /**
   * LASUpdate using the {@literal $addToSet} update modifier
   *
   * @param key
   * @param value
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/addToSet/</b>
   */
  public LASUpdate addToSet(String key, Object value) {
    addMultiFieldOperation("$addToSet", key, value);
    return this;
  }

  /**
   * LASUpdate using the {@literal $addToSet} update modifier
   *
   * @param key
   * @param values
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/addToSet/</b>
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/each/#up._S_each</b>
   */
  public LASUpdate addToSet(String key, List values) {
    Map $each = new LinkedHashMap<>();
    $each.put("$each", values);
    addMultiFieldOperation("$addToSet", key, $each);
    return this;
  }

  /**
   * LASUpdate using the {@literal $pop} update modifier
   *
   * @param key
   * @param pos
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/pop/</b>
   */
  public LASUpdate pop(String key, Position pos) {
    addMultiFieldOperation("$pop", key, pos == Position.FIRST ? -1 : 1);
    return this;
  }

  /**
   * LASUpdate using the {@literal $pull} update modifier
   *
   * @param key
   * @param value
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/pull/</b>
   */
  public LASUpdate pull(String key, Object value) {
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
  public LASUpdate pull(String key, MongoQuery.EmbeddedElemMatcher filters) {
    Map $elemMatch = new LinkedHashMap<>();
    $elemMatch.put("$elemMatch", filters.getElemMatherFiler());
    addMultiFieldOperation("$pull", key, $elemMatch);
    return this;
  }

  /**
   * LASUpdate using the {@literal $pullAll} update modifier
   *
   * @param key
   * @param values
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/pullAll/</b>
   */
  public LASUpdate pullAll(String key, List values) {
    addMultiFieldOperation("$pullAll", key, values);
    return this;
  }

  /**
   * LASUpdate using the {@literal $rename} update modifier
   *
   * @param oldName
   * @param newName
   * @return
   * @see <b>http://docs.mongodb.org/manual/reference/operator/update/rename/</b>
   */
  public LASUpdate rename(String oldName, String newName) {
    addMultiFieldOperation("$rename", oldName, newName);
    return this;
  }

  public Map getModifierOps() {
    return modifierOps;
  }

  protected void addMultiFieldOperation(String operator, String key, Object value) {
//    Assert.hasText(key, "Key for update must not be null or blank.");
    Object existingValue = this.modifierOps.get(operator);
    Map keyValueMap;

    if (existingValue == null) {
      keyValueMap = new LinkedHashMap<>();
      this.modifierOps.put(operator, keyValueMap);
    } else {
      if (existingValue instanceof Map) {
        keyValueMap = (Map) existingValue;
      } else {
        throw new MongoDataAccessException("Modifier Operations should be a Map but was " + existingValue.getClass());
      }
    }
    keyValueMap.put(key, value);
  }

  public LASUpdate addRelation(String key, LASPointer pointer) {
    List<LASPointer> pointers = new ArrayList();
    pointers.add(pointer);
    addToSet(key, pointers);
    return this;
  }

  public LASUpdate addRelations(String key, List<LASPointer> pointers) {
    addToSet(key, pointers);
    return this;
  }

  public LASUpdate removeRelation(String key, LASPointer pointer) {
    pull(key, pointer);
    return this;
  }

  public LASUpdate removeRelations(String key, List<LASPointer> pointers) {
    pullAll(key, pointers);
    return this;
  }

  public boolean isUpsert() {
    return upsert;
  }

  public LASUpdate setUpsert(boolean upsert) {
    this.upsert = upsert;
    return this;
  }

  public void setModifierOps(Map modifierOps) {
    this.modifierOps = modifierOps;
  }

  @Override
  public String toString() {
    return "{" +
        "modifierOps=" + modifierOps +
        ", upsert=" + upsert +
        '}';
  }
}


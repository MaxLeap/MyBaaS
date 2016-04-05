package com.maxleap.domain.base;

import com.maxleap.domain.mongo.BaseEntity;
import com.maxleap.domain.mongo.DateUtils;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.*;

/**
 * User: qinpeng
 * Date: 14-5-6
 * Time: 14:59
 */
public class LASObject extends BaseEntity<ObjectId> implements Map {
  protected Map<String, Object> data = new HashMap<>();
  public static String FIELD_CREATED_AT = "createdAt";
  public static String FIELD_UPDATED_AT = "updatedAt";
  public static String FIELD_OBJECT_ID = "objectId";
  public static String FIELD_ACL = "ACL";

  public LASObject() {
  }

  @JsonCreator
  public LASObject(Map<String, Object> map) {
    this.putAll(map);
  }

  @Override
  public String toString() {
    return data.toString();
  }

  public Object get(String key) {
    return data.get(key);
  }

  @JsonAnySetter
  public void put(String key, Object value) {
    if (FIELD_OBJECT_ID.equals(key)) {
      if (!(value instanceof ObjectId)) {
        throw new IllegalArgumentException("value must by ObjectId type");
      }
    }
    data.put(key, value);
  }

  //@JsonAnyGetter
  public Map<String, Object> getMap() {
    return data;
  }

  @JsonValue
  private Map<String, Object> jsonMap() {
    Map jsonMap = new HashMap();
    for (Entry<String, Object> entry : data.entrySet()) {
      String key = entry.getKey();
      if (FIELD_CREATED_AT.equals(key) && entry.getValue() instanceof Long || FIELD_UPDATED_AT.equals(key) && entry.getValue() instanceof Long) {
        jsonMap.put(key, DateUtils.encodeDate(new Date((long) entry.getValue())));
      } else {
        jsonMap.put(key, entry.getValue());
      }
    }
    return jsonMap;
  }

  @Override
  public void setObjectId(ObjectId objectId) {
    this.put(FIELD_OBJECT_ID, objectId);
  }

  @Override
  public long getCreatedAt() {
    return this.get(FIELD_CREATED_AT) == null ? 0 : (Long) this.get(FIELD_CREATED_AT);
  }

  @Override
  public void setCreatedAt(long createdAt) {
    this.put(FIELD_CREATED_AT, createdAt);
  }

  @Override
  public long getUpdatedAt() {
    return this.get(FIELD_UPDATED_AT) == null ? 0 : (Long) this.get(FIELD_UPDATED_AT);
  }

  @Override
  public void setUpdatedAt(long updatedAt) {
    this.put(FIELD_UPDATED_AT, updatedAt);
  }

  public Map getACL() {
    return this.get(FIELD_ACL) == null ? null : (Map) this.get(FIELD_ACL);
  }

  public void setACL(Map ACL) {
    this.put(FIELD_ACL, ACL);
  }

  public void setCreatedAt(String createdAt) {
    this.put(FIELD_CREATED_AT, DateUtils.parseDate(createdAt).getTime());
  }

  public void setUpdatedAt(String updatedAt) {
    this.put(FIELD_UPDATED_AT, DateUtils.parseDate(updatedAt).getTime());
  }

  @JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
  @Override
  public ObjectId getObjectId() {
    return this.get(FIELD_OBJECT_ID) == null ? null : (ObjectId) this.get(FIELD_OBJECT_ID);
  }

  @Override
  public int size() {
    return data.size();
  }

  @Override
  public boolean isEmpty() {
    return data.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return data.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return data.containsValue(value);
  }

  @Override
  public Object get(Object key) {
    return data.get(key);
  }

  @Override
  public Object put(Object key, Object value) {
    return data.put(key.toString(), value);
  }

  @Override
  public Object remove(Object key) {
    return data.remove(key);
  }

  @Override
  public void putAll(Map map) {
    data.putAll(map);
    Object val = map.get(FIELD_CREATED_AT);
    if (val instanceof String) {
      data.put(FIELD_CREATED_AT, DateUtils.parseDate(map.get(FIELD_CREATED_AT).toString()).getTime());
    }
    val = map.get(FIELD_UPDATED_AT);
    if (val instanceof String) {
      data.put(FIELD_UPDATED_AT, DateUtils.parseDate(map.get(FIELD_UPDATED_AT).toString()).getTime());
    }
    val = map.get(FIELD_OBJECT_ID);
    if (val instanceof String) {
      data.put(FIELD_OBJECT_ID, new ObjectId((String) val));
    }
  }

  @Override
  public void clear() {
    data.clear();
  }

  @Override
  public Set keySet() {
    return data.keySet();
  }

  @Override
  public Collection values() {
    return data.values();
  }

  @Override
  public Set<Entry> entrySet() {
    return (Set) data.entrySet();
  }
}

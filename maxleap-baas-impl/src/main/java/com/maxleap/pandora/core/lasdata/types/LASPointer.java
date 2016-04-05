package com.maxleap.pandora.core.lasdata.types;

import com.maxleap.pandora.core.lasdata.LASKeyType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * User: qinpeng
 * Date: 14-5-19
 * Time: 9:21
 */
public class LASPointer implements Serializable {

  private final String __type = LASKeyType.Pointer.name();
  private String className;
  private String objectId;

  public LASPointer() {
  }

  public LASPointer(String objectId, String className) {
    this.objectId = objectId;
    this.className = className;
  }

  public String get__type() {
    return __type;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public Map toMap() {
    Map map = new HashMap();
    map.put("__type", "Pointer");
    map.put("className", className);
    map.put("objectId", objectId);
    return map;
  }

  @Override
  public boolean equals(Object obj) {
    LASPointer o = (LASPointer) obj;
    return ((o.getClassName() == null && className == null) || o.getClassName().equals(className))
        && ((o.getObjectId() == null && objectId == null) || o.getObjectId().equals(objectId));
  }
}

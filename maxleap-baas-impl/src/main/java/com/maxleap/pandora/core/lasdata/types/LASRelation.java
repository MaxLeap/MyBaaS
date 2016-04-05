package com.maxleap.pandora.core.lasdata.types;

import com.maxleap.pandora.core.lasdata.LASKeyType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: qinpeng
 * Date: 14-5-19
 * Time: 9:21
 */
public class LASRelation implements Serializable {

  private final String __type = LASKeyType.Relation.name();
  private String className;
  private List<LASPointer> objects = new ArrayList<>();

  public LASRelation() {
  }

  public LASRelation(String className) {
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

  public List getObjects() {
    return objects;
  }

  public void setObjects(List objects) {
    this.objects = objects;
  }

  public void addObject(LASPointer pointer) {
    this.objects.add(pointer);
  }

  public void removeObject(LASPointer pointer) {
    this.objects.remove(pointer);
  }

  public Map toMap() {
    Map map = new HashMap();
    map.put("__type", "Relation");
    map.put("className", className);
    List objs = new ArrayList();
    for (int i = 0; i < objects.size(); i++) {
      objs.add(objects.get(i).toMap());
    }
    map.put("objects", objs);
    return map;
  }
}

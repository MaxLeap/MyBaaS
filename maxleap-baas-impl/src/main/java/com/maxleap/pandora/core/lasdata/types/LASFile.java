package com.maxleap.pandora.core.lasdata.types;

import com.maxleap.pandora.core.lasdata.LASKeyType;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qinpeng
 * Date: 14-5-19
 * Time: 10:20
 */
public class LASFile {

  private static String __type = LASKeyType.File.name();
  private String name;
  private String url;

  public LASFile(String url, String name) {
    this.url = url;
    this.name = name;
  }

  public LASFile() {

  }

  public String get__type() {
    return __type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Map toMap() {
    Map map = new HashMap();
    map.put("__type", "File");
    map.put("name", name);
    map.put("url", url);
    return map;
  }
}

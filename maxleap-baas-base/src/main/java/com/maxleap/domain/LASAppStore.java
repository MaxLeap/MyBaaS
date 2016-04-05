package com.maxleap.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * User: yuyangning
 * Date: 11/17/14
 * Time: 11:13 AM
 */
public enum LASAppStore {
  IOS("itunesId"), ANDROID("packageName"), UNKNOWN("unkown");

  private String storeIdName;

  private LASAppStore(String storeIdName) {
    this.storeIdName = storeIdName;
  }

  public String idname() {
    return this.storeIdName;
  }


  @JsonValue
  public String toString() {
    return String.valueOf(this.storeIdName);
  }

  @JsonCreator
  public static LASAppStore fromString(String idname) {
    if (idname != null) {
      for (LASAppStore store : LASAppStore.values()) {
        if (idname.equalsIgnoreCase(store.idname())) {
          return store;
        }
      }
    }
    return LASAppStore.UNKNOWN;
  }

}

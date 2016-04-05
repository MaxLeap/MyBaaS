package com.maxleap.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * User: yuyangning
 * Date: 11/20/14
 * Time: 10:14 AM
 */
public enum LASAppPlatform {
  ios("ios"), android("android"), unkown("unkown");

  private String platform;

  private LASAppPlatform(String platform) {
    this.platform = platform;
  }

  @JsonValue
  public String toString() {
    return String.valueOf(this.platform);
  }

  @JsonCreator
  public static LASAppPlatform fromString(String name) {
    if (name != null) {
      for (LASAppPlatform platform : LASAppPlatform.values()) {
        if (name.equalsIgnoreCase(platform.toString())) {
          return platform;
        }
      }
    }
    return LASAppPlatform.unkown;
  }
}

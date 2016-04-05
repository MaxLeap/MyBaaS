package com.maxleap.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by shunlv on 16-2-4.
 */
public enum LASUserType {
  AppUser(0), OrgUser(1), OrgAdmin(2), SysUser(9), SysAdmin(10);

  private final int type;

  private LASUserType(int type) {
    this.type = type;
  }

  @JsonValue
  public String toString() {
    return String.valueOf(this.type);
  }

  public int toInt() {
    return type;
  }

  public static LASUserType fromInt(int t) {
    for (LASUserType userType : LASUserType.values()) {
      if (userType.type == t) {
        return userType;
      }
    }

    return null;
  }

  @JsonCreator
  public static LASUserType fromString(String str) {
    int t = Integer.parseInt(str);
    return fromInt(t);
  }
}

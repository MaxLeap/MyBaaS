package com.maxleap.domain.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * Created by shunlv on 15-9-23.
 */
public class LASClassPermissionNode implements Serializable {
  private RestACLPermission restAcl;

  public RestACLPermission getRestAcl() {
    return restAcl;
  }

  public void setRestAcl(RestACLPermission restAcl) {
    this.restAcl = restAcl;
  }

  public static enum RestACLPermission {
    Shared, Private, ReadOnly, Full;

    @JsonValue
    public String toString() {
      return String.valueOf(this.name());
    }

    @JsonCreator
    public static RestACLPermission fromString(String str) {
      if (str == null) return null;
      if (str.equals(Shared.name())) {
        return Shared;
      } else if (str.equals(Private.name())) {
        return Private;
      } else if (str.equals(ReadOnly.name())) {
        return ReadOnly;
      } else if (str.equals(Full.name())) {
        return Full;
      }

      return null;
    }
  }
}

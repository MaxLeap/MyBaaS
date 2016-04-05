package com.maxleap.domain.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by shunlv on 15-9-18.
 */
public enum IdentifierType {
  SYS_ADMIN("SysAdmin"),
  SYS_USER("SysUser"),
  ORG_ADMIN("OrgAdmin"),
  ORG_USER("OrgUser"),
  MASTER_KEY("MasterKey"),
  APP_USER("AppUser"),
  API_KEY("APIKey"),
  CLIENT_KEY("ClientKey");

  private String creatorType;

  private IdentifierType(String creatorType) {
    this.creatorType = creatorType;
  }

  public String getCreatorType() {
    return creatorType;
  }

  @JsonValue
  public String toString() {
    return name();
  }

  @JsonCreator
  public IdentifierType fromString(String name) {
    if (name == null) return null;

    for (IdentifierType identifierType : IdentifierType.values()) {
      if (identifierType.name().equals(name)) {
        return identifierType;
      }
    }

    return null;
  }
}

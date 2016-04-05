package com.maxleap.domain;

import com.maxleap.domain.base.ObjectId;
import com.maxleap.domain.mongo.BaseEntity;

public class LASSessionToken extends BaseEntity<ObjectId> {
  public static final String FIELD_USER_ID = "userId";
  public static final String FIELD_TOKEN = "token";
  public static final String FIELD_EXPIRED_AT = "expireAt";
  public static final String FIELD_TYPE = "type";
  public static final String FIELD_ORG_ID = "orgId";

  private String userId; // orgUser / appUser
  private String token;
  private Long expireAt;
  private int type;
  private String orgId;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Long getExpireAt() {
    return expireAt;
  }

  public void setExpireAt(Long expireAt) {
    this.expireAt = expireAt;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }
}
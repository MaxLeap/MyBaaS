package com.maxleap.domain;

import com.maxleap.domain.base.LASObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Created by shunlv on 16-1-5.
 */
public class LASBlacklist extends LASObject {
  public static final String ORG_ID = "orgId";
  public static final String APP_ID = "appId";
  public static final String PATH = "path";
  public static final String METHOD = "method";

  public LASBlacklist() {
  }

  @JsonCreator
  public LASBlacklist(Map<String, Object> data) {
    super(data);
  }

  public void setOrgId(String orgId) {
    this.put(ORG_ID, orgId);
  }

  public String getOrgId() {
    return (String) this.get(ORG_ID);
  }

  public void setAppId(String appId) {
    this.put(APP_ID, appId);
  }

  public String getAppId() {
    return (String) this.get(APP_ID);
  }

  public void setPath(String path) {
    this.put(PATH, path);
  }

  public String getPath() {
    return (String) this.get(PATH);
  }

  public void setMethod(String method) {
    this.put(METHOD, method);
  }

  public String getMethod() {
    return (String) this.get(METHOD);
  }

  @Override
  public int hashCode() {
    return (this.get(APP_ID) == null ? 0 : this.get(APP_ID).hashCode())
        + (this.get(ORG_ID) == null ? 0 : this.get(ORG_ID).hashCode())
        + (this.get(PATH) == null ? 0 : this.get(PATH).hashCode())
        + (this.get(METHOD) == null ? 0 : this.get(METHOD).hashCode());
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof LASBlacklist)) {
      return false;
    }

    LASBlacklist lasBlacklist = (LASBlacklist) obj;
    return StringUtils.equals(lasBlacklist.getAppId(), this.getAppId())
        && StringUtils.equals(lasBlacklist.getOrgId(), this.getOrgId())
        && StringUtils.equals(lasBlacklist.getPath(), this.getPath())
        && StringUtils.equals(lasBlacklist.getMethod(), this.getMethod());
  }
}

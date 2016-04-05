package com.maxleap.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;
import java.util.Map;

/**
 * User: qinpeng
 * Date: 14-6-11
 * Time: 13:50
 */
public class LASOrgUser extends LASUser {
  public static final String FIELD_ORG_ID = "orgId";
  public static final String FIELD_USER_TYPE = "userType";
  public static final String FIELD_CAPTCHA = "captcha";
  public static final String FIELD_ROLES = "roles";
  public static final String FIELD_APPS = "apps";
  public static final String FIELD_DELETED = "deleted";
  public static final String FIELD_ORG_DISABLED = "orgDisabled";
  public static final String FIELD_LANGUAGE = "language";
  public static final String FIELD_TIMEZONE = "timezone";
  public static final String FIELD_IS_NEW = "isNew";

  public LASOrgUser() {
    super();
  }

  @JsonCreator
  public LASOrgUser(Map<String, Object> data) {
    super(data);
  }

  public String getOrgId() {
    return (String) this.get(FIELD_ORG_ID);
  }

  public void setOrgId(String orgId) {
    this.put(FIELD_ORG_ID, orgId);
  }

  public int getUserType() {
    return this.get(FIELD_USER_TYPE) == null ? LASUserType.OrgAdmin.toInt() : (int) this.get(FIELD_USER_TYPE);
  }

  public void setUserType(int userType) {
    this.put(FIELD_USER_TYPE, userType);
  }

  public void setCaptcha(Map<String, String> captcha) {
    this.put(FIELD_CAPTCHA, captcha);

  }

  public Map<String, String> getCaptcha() {
    return (Map<String, String>) this.get(FIELD_CAPTCHA);
  }

  public void setRoles(List<String> roles) {
    this.put(FIELD_ROLES, roles);
  }

  public List<String> getRoles() {
    return (List<String>) this.get(FIELD_ROLES);
  }

  public List<String> getApps() {
    return (List<String>) this.get(FIELD_APPS);
  }

  public void setApps(List<String> apps) {
    this.put(FIELD_APPS, apps);
  }

  public boolean isDeleted() {
    return this.get(FIELD_DELETED) != null && (boolean) this.get(FIELD_DELETED);
  }

  public void setDeleted(boolean deleted) {
    this.put(FIELD_DELETED, deleted);
  }

  public boolean isOrgDisabled() {
    return this.get(FIELD_ORG_DISABLED) != null && (boolean) this.get(FIELD_ORG_DISABLED);
  }

  public void setOrgDisabled(boolean orgDisabled) {
    this.put(FIELD_ORG_DISABLED, orgDisabled);
  }

  public String getLanguage() {
    return (String) this.get(FIELD_LANGUAGE);
  }

  public void setLanguage(String language) {
    this.put(FIELD_LANGUAGE, language);
  }

  public String getTimezone() {
    return (String) this.get(FIELD_TIMEZONE);
  }

  public void setTimezone(String timezone) {
    this.put(FIELD_TIMEZONE, timezone);
  }

  public Map<String, Boolean> getIsNew() {
    return (Map<String, Boolean>) this.get(FIELD_IS_NEW);
  }

  public void setIsNew(Map<String, Boolean> isNew) {
    this.put(FIELD_IS_NEW, isNew);
  }
}

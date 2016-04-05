package com.maxleap.domain;


import com.maxleap.domain.base.LASObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Map;

/**
 * User: qinpeng
 * Date: 14-5-6
 * Time: 15:00
 */
public class LASUser extends LASObject {

  public static final String FIELD_USERNAME = "username";
  public static final String FIELD_PASSWORD = "password";
  public static final String FIELD_AUTH_DATA = "authData";
  public static final String FIELD_EMAIL_VERIFIED = "emailVerified";
  public static final String FIELD_EMAIL = "email";
  public static final String FIELD_MOBILEPHONE = "mobilePhone";
  public static final String FIELD_MOBILEPHONE__VERIFIED = "mobilePhoneVerified";
  public static final String FIELD_INSTALLATIONIDS = "installationIds";
  public static final String FIELD_SESSION_TOKEN = "sessionToken";
  public static final String FIELD_ANONYMOUS = "anonymous";
  public static final String FIELD_PASSPORT = "passport";
  public static final String FIELD_COIN = "coin";
  public static final String FIELD_IS_NEW = "isNew";
  public static final String FIELD_ENABLED = "enabled";

  public LASUser() {
    super();
  }

  @JsonCreator
  public LASUser(Map<String, Object> map) {
    super(map);
  }

  @JsonIgnore
  public String getUsername() {
    return (String) this.get(FIELD_USERNAME);
  }

  public void setUsername(String username) {
    this.put(FIELD_USERNAME, username);
  }

  @JsonIgnore
  public String getPassword() {
    return (String) this.get(FIELD_PASSWORD);
  }

  public void setPassword(String password) {
    this.put(FIELD_PASSWORD, password);
  }

  @JsonIgnore
  public Map<String, Map<String, Object>> getAuthData() {
    return this.get(FIELD_AUTH_DATA) == null ? null : (Map<String, Map<String, Object>>) this.get(FIELD_AUTH_DATA);
  }

  public void setAuthData(Map<String, Map<String, Object>> authData) {
    this.put(FIELD_AUTH_DATA, authData);
  }

  @JsonIgnore
  public boolean isEmailVerified() {
    return this.get(FIELD_EMAIL_VERIFIED) == null ? false : (boolean) this.get(FIELD_EMAIL_VERIFIED);
  }

  public void setEmailVerified(boolean emailVerified) {
    this.put(FIELD_EMAIL_VERIFIED, emailVerified);
  }

  @JsonIgnore
  public String getEmail() {
    return (String) this.get(FIELD_EMAIL);
  }

  public void setEmail(String email) {
    this.put(FIELD_EMAIL, email);
  }

  @JsonIgnore
  public String getSessionToken() {
    return (String) this.get(FIELD_SESSION_TOKEN);
  }

  public void setSessionToken(String sessionToken) {
    this.put(FIELD_SESSION_TOKEN, sessionToken);
  }

  @JsonIgnore
  public Map<String, Object> getAnonymous() {
    return this.get(FIELD_ANONYMOUS) == null ? null : (Map<String, Object>) this.get(FIELD_ANONYMOUS);
  }

  public void setAnonymous(Map<String, Object> anonymous) {
    this.put(FIELD_ANONYMOUS, anonymous);
  }

  @JsonIgnore
  public String getPassport() {
    return (String) this.get(FIELD_PASSPORT);
  }

  public void setPassport(String passport) {
    this.put(FIELD_PASSPORT, passport);
  }

  @JsonIgnore
  public double getCoin() {
    return this.get(FIELD_COIN) == null ? 0 : (double) this.get(FIELD_COIN);
  }

  public void setCoin(double coin) {
    this.put(FIELD_COIN, coin);
  }
  public List<String> getInstallationIds() {
    return this.get(FIELD_INSTALLATIONIDS) == null ? null : (List<String>) this.get(FIELD_INSTALLATIONIDS);
  }

  public void setInstallationIds(List<String> installationIds) {
    this.put(FIELD_INSTALLATIONIDS, installationIds);
  }

  public boolean isNew() {
    return this.get(FIELD_IS_NEW) != null && (boolean) this.get(FIELD_IS_NEW);
  }

  public void setNew(boolean _new) {
    this.put(FIELD_IS_NEW, _new);
  }

  public boolean isEnabled() {
    return this.get(FIELD_ENABLED) == null || (boolean) this.get(FIELD_ENABLED);
  }

  public void setEnabled(boolean enabled) {
    this.put(FIELD_ENABLED, enabled);
  }

  public String getMobilePhone() {
    return (String) this.get(FIELD_MOBILEPHONE);
  }

  public void setMobilePhone(String mobilePhone) {
    this.put(FIELD_MOBILEPHONE, mobilePhone);
  }

  public boolean isMobilePhoneVerified() {
    return this.get(FIELD_MOBILEPHONE__VERIFIED) == null || (boolean) this.get(FIELD_MOBILEPHONE__VERIFIED);
  }

  public void setMobilePhoneVerified(boolean mobilePhoneVerified) {
    this.put(FIELD_MOBILEPHONE__VERIFIED, mobilePhoneVerified);
  }
}

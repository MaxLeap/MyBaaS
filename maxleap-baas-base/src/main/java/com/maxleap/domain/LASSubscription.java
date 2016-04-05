package com.maxleap.domain;

import com.maxleap.domain.base.LASObject;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;
import java.util.Map;

/**
 * Created by shunlv on 15-11-13.
 */
public class LASSubscription extends LASObject {
  public static final String MAIL = "mail";
  public static final String APP_IDS = "appIds";
  public static final String MODULES = "modules";
  public static final String CREATOR_ID = "creatorId";
  public static final String ORG_ID = "orgId";

  public LASSubscription() {
    super();
  }

  @JsonCreator
  public LASSubscription(Map<String, Object> data) {
    super(data);
  }

  public String getMail() {
    return (String) this.get(MAIL);
  }

  public void setMail(String mail) {
    this.put(MAIL, mail);
  }

  public List<String> getAppIds() {
    return (List<String>) this.get(APP_IDS);
  }

  public void setAppIds(List<String> appIds) {
    this.put(APP_IDS, appIds);
  }

  public List<Map<String, Object>> getModules() {
    return (List<Map<String, Object>>) this.get(MODULES);
  }

  public void setModules(List<Map<String, Object>> modules) {
    this.put(MODULES, modules);
  }

  public String getCreatorId() {
    return (String) this.get(CREATOR_ID);
  }

  public void setCreatorId(String creatorId) {
    this.put(CREATOR_ID, creatorId);
  }

  public String getOrgId() {
    return (String) this.get(ORG_ID);
  }

  public void setOrgId(String orgId) {
    this.put(ORG_ID, orgId);
  }
}

package com.maxleap.domain.auth;

import java.io.Serializable;

/**
 * Created by shunlv on 15-9-18.
 */
public class LASAccessPair implements Serializable {
  private LASPrincipal lasPrincipal;
  private boolean canAccess = false;
  private boolean cCodeMapping = false;
  private String language;

  public boolean isCanAccess() {
    return canAccess;
  }

  public void setCanAccess(boolean canAccess) {
    this.canAccess = canAccess;
  }

  public LASPrincipal getLASPrincipal() {
    return lasPrincipal;
  }

  public void setLASPrincipal(LASPrincipal lasPrincipal) {
    this.lasPrincipal = lasPrincipal;
  }

  public boolean iscCodeMapping() {
    return cCodeMapping;
  }

  public void setcCodeMapping(boolean cCodeMapping) {
    this.cCodeMapping = cCodeMapping;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }
}

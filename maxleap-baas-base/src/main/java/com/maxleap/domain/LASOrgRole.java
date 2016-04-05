package com.maxleap.domain;


import com.maxleap.domain.auth.PermissionType;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.domain.mongo.BaseEntity;

import java.util.Set;

/**
 * User: yuyangning
 * Date: 12/11/14
 * Time: 3:17 PM
 */
public class LASOrgRole extends BaseEntity<ObjectId> {
  public static final String NAME = "name";
  public static final String PERMISSIONS = "permissions";
  public static final String ORG_ID = "orgId";

  private String name;
  private Set<PermissionType> permissions;
  private String orgId;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<PermissionType> getPermissions() {
    return permissions;
  }

  public void setPermissions(Set<PermissionType> permissions) {
    this.permissions = permissions;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }
}

package com.maxleap.domain.auth;


import java.util.HashSet;
import java.util.Set;

/**
 * Created by shunlv on 15-9-18.
 */
public class LASPrincipal extends Principal {
  private String orgId;
  private Set<PermissionType> permissions;
  private String appId;

  public LASPrincipal() {
  }

  public LASPrincipal(PermissionType permissionType) {
    permissions = new HashSet<>();
    permissions.add(permissionType);
  }

  public LASPrincipal(Set<PermissionType> permissions) {
    this.permissions = permissions;
  }

  public LASPrincipal(String orgId, Set<PermissionType> permissions) {
    this.orgId = orgId;
    this.permissions = permissions;
  }

  public LASPrincipal(String orgId, Set<PermissionType> permissions, String identifier, IdentifierType type) {
    super(identifier, type);
    this.orgId = orgId;
    this.permissions = permissions;
    this.appId = appId;
  }

  public LASPrincipal(String orgId, Set<PermissionType> permissions, String identifier, IdentifierType type, String appId) {
    super(identifier, type);
    this.orgId = orgId;
    this.permissions = permissions;
    this.appId = appId;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public Set<PermissionType> getPermissions() {
    return permissions;
  }

  public void setPermissions(Set<PermissionType> permissions) {
    this.permissions = permissions;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }
}

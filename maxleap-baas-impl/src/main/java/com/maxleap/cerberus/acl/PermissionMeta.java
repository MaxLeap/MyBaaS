package com.maxleap.cerberus.acl;

import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.domain.auth.PermissionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PermissionMeta implements Serializable {
  private final static Logger logger = LoggerFactory.getLogger(PermissionMeta.class);
  public final static PermissionMeta NONE = new PermissionMeta(null, null);

  private Set<PermissionType> types = new HashSet<>();
  private String permission;

  public PermissionMeta(Set<PermissionType> types, String permission) {
    this.types = types;
    this.permission = permission;
  }

  public Set<PermissionType> getTypes() {
    return Collections.unmodifiableSet(types);
  }

  public void setTypes(Set<PermissionType> types) {
    this.types = types;
  }

  public void addType(PermissionType type) {
    this.types.add(type);
  }

  public String getPermission() {
    return permission;
  }

  public void setPermission(String permission) {
    this.permission = permission;
  }

  public boolean isPermitted(LASPrincipal principal) {
    boolean hasPermission = false;
    if (principal != null && principal.getPermissions() != null) {
      for (PermissionType type : this.getTypes()) {
        if (principal.getPermissions().contains(type)) {
          hasPermission = true;
          break;
        }
      }
    }
    return hasPermission;
  }

  @Override
  public String toString() {
    return "PermissionMeta{" +
        "types=" + types +
        ", permission='" + permission + '\'' +
        '}';
  }
}
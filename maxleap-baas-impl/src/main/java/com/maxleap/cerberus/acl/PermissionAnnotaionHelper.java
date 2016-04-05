package com.maxleap.cerberus.acl;

import com.maxleap.domain.auth.PermissionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class PermissionAnnotaionHelper {
  private final static Logger logger = LoggerFactory.getLogger(PermissionAnnotaionHelper.class);

  public static PermissionMeta getMeta(Permission permission) {
    Set<PermissionType> typeSet = new HashSet<>();
    for (PermissionType type : permission.type()) {
      typeSet.add(type);
    }
    PermissionMeta permissionMeta = new PermissionMeta(typeSet, permission.permisson());
    if (logger.isDebugEnabled()) {
      logger.debug("Permission meta " + permissionMeta);
    }
    return permissionMeta;
  }
}
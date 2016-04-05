package com.maxleap.cerberus.acl.impl;

import com.maxleap.domain.LASOrgRole;
import com.maxleap.domain.LASOrgUser;
import com.maxleap.domain.auth.PermissionType;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.mongo.MongoQuery;
import com.maxleap.pandora.data.support.MongoEntityManager;
import com.maxleap.platform.LASTables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by shunlv on 16-2-3.
 */
@Singleton
public class PermissionService {
  private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);

  private MongoEntityManager mongoEntityManager;

  @Inject
  public PermissionService(MongoEntityManager mongoEntityManager) {
    this.mongoEntityManager = mongoEntityManager;
  }

  public Set<PermissionType> getUserPermissions(String orgUserId) {
    LASOrgUser orgUser = mongoEntityManager.get(LASTables.PLATFORM_DATA, LASTables.PlatformData.ORGANIZATION_USER, new ObjectId(orgUserId), LASOrgUser.class);
    if (orgUser == null || !orgUser.containsKey(LASOrgUser.FIELD_ROLES)) {
      return null;
    }

    List<String> roleIds = orgUser.getRoles();
    if (roleIds == null || roleIds.isEmpty()) {
      return Collections.emptySet();
    }

    MongoQuery inQuery = new MongoQuery();
    inQuery.in("objectId", roleIds);
    List<LASOrgRole> roles = mongoEntityManager.find(LASTables.PLATFORM_DATA, LASTables.PlatformData.ORGANIZATION_ROLE, inQuery, LASOrgRole.class);

    if (roles == null || roles.isEmpty()) {
      return Collections.emptySet();
    }

    Set<PermissionType> permissionTypes = new HashSet<>();
    for (LASOrgRole role : roles) {
      Set<PermissionType> permissions = role.getPermissions();
      if (permissions != null && !permissions.isEmpty()) {
        permissionTypes.addAll(permissions);
      }
    }

    return permissionTypes;
  }

  public Set<PermissionType> getRolePermissions(String roleId) {
    LASOrgRole role = mongoEntityManager.get(LASTables.PLATFORM_DATA, LASTables.PlatformData.ORGANIZATION_ROLE, new ObjectId(roleId), LASOrgRole.class);

    if (role == null) {
      return null;
    }

    return role.getPermissions();
  }
}

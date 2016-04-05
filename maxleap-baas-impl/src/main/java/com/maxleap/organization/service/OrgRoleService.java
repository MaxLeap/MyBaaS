package com.maxleap.organization.service;

import com.maxleap.domain.LASOrgRole;
import com.maxleap.domain.auth.PermissionType;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.exception.LASException;
import com.maxleap.pandora.core.mongo.MongoQuery;
import com.maxleap.pandora.core.mongo.MongoUpdate;
import com.maxleap.pandora.data.support.MongoEntityManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by shunlv on 16-2-16.
 */
@Singleton
public class OrgRoleService {
  private MongoEntityManager mongoEntityManager;
  private static final String DB = "platform_data";
  private static final String TABLE = "zcloud_organization_role";

  @Inject
  public OrgRoleService(MongoEntityManager mongoEntityManager) {
    this.mongoEntityManager = mongoEntityManager;
  }

  public LASOrgRole createOrgRole(LASOrgRole orgRole) {
    MongoQuery query = new MongoQuery();
    query.equalTo(LASOrgRole.NAME, orgRole.getName());
    long count = mongoEntityManager.count(DB, TABLE, query);
    if (count > 0) {
      throw new LASException(LASException.NAME_TAKEN, "name has been taken! " + orgRole.getName());
    }
    return mongoEntityManager.create(DB, TABLE, orgRole);
  }

  public LASOrgRole getOrgRole(ObjectId objectId) {
    return mongoEntityManager.get(DB, TABLE, objectId, LASOrgRole.class);
  }

  public LASOrgRole getOrgRole(String objectId, String orgId) {
    MongoQuery query = new MongoQuery();
    query.equalTo("objectId", objectId);
    query.equalTo(LASOrgRole.ORG_ID, orgId);
    return mongoEntityManager.findUniqueOne(DB, TABLE, query, LASOrgRole.class);
  }

  public int updateOrgRole(ObjectId objectId, MongoUpdate update) {
    return mongoEntityManager.update(DB, TABLE, objectId, update);
  }

  public int deleteOrgRole(ObjectId objectId) {
    return mongoEntityManager.delete(DB, TABLE, objectId);
  }

  public List<LASOrgRole> queryOrgRole(MongoQuery query) {
    return mongoEntityManager.find(DB, TABLE, query, LASOrgRole.class);
  }

  public long count(MongoQuery query) {
    return mongoEntityManager.count(DB, TABLE, query);
  }

  public Set<PermissionType> getRolePermissions(ObjectId roleId) {
    LASOrgRole role = mongoEntityManager.get(DB, TABLE, roleId, LASOrgRole.class);

    if (role == null) {
      return null;
    }

    return role.getPermissions();
  }

  public Set<PermissionType> getRolePermissions(List<String> roleIds) {
    if (roleIds == null || roleIds.isEmpty()) {
      return null;
    }

    MongoQuery inQuery = new MongoQuery();
    inQuery.in("objectId", roleIds);
    List<LASOrgRole> roles = mongoEntityManager.find(DB, TABLE, inQuery, LASOrgRole.class);

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
}

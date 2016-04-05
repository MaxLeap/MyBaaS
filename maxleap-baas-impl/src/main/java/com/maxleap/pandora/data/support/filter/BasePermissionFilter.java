package com.maxleap.pandora.data.support.filter;

import com.maxleap.domain.LASRole;
import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.lasdata.LASClassSchema;
import com.maxleap.pandora.core.lasdata.LASQuery;
import com.maxleap.pandora.data.support.ClassSchemaManager;
import com.maxleap.pandora.data.support.LASDataEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * User: qinpeng
 *
 * @since 1.0
 */
public abstract class BasePermissionFilter {
  private static transient final Logger log = LoggerFactory.getLogger(BasePermissionFilter.class);
  protected ClassSchemaManager classSchemaManager;
  protected LASDataEntityManager lasDataEntityManager;

  protected static final String PREFIX_ROLE_COLLECTION = "role_";
  protected static final String PREFIX_ROLE_ACL = "role:";
  protected static final String PREFIX_ACL = "ACL";

  /**
   * @param appId
   * @param userId
   * @return
   */
  protected List<Set<String>> findActRoleAndUsers(ObjectId appId, String userId) {
    Set<String> actRole = new HashSet<>();
    Set<String> actUser = new HashSet<>();
    actUser.add(userId);
    LASQuery query = new LASQuery();
    List<ObjectId> list = new ArrayList<>();
    list.add(new ObjectId(userId));
    query.in(LASRole.FIELD_USERS, list);
    LASClassSchema classSchema = classSchemaManager.get(appId, "_Role");
    List<LASObject> roles = lasDataEntityManager.find(classSchema.getAppId(), classSchema.getClassName(), null, query);
    if (roles != null) {
      for (LASObject role : roles)
        actRole.add(role.get(LASRole.FIELD_NAME).toString());
    }
    List<Set<String>> act = new ArrayList<>();
    act.add(actRole);
    act.add(actUser);
    return act;
  }

  /**
   * 所有的role 和 自己对应所有的祖先
   *
   * @param appId
   * @return
   */
  protected Map<String, Set<String>> buildAncestorRoles(ObjectId appId) {
    LASClassSchema schema = classSchemaManager.get(appId, "_Role");
    LASQuery lasQuery = new LASQuery();
    lasQuery.loadRelations(true);
    List<LASObject> roles = lasDataEntityManager.find(appId, schema.getClassName(), null, lasQuery);

    Map<String, Set<String>> rMap = new HashMap();
    for (LASObject r : roles) {
      Map children = (Map) r.get(LASRole.FIELD_ROLES);
      if (children == null) {
        continue;
      }

      Object childrenIds = children.get("objectIds");
      if (childrenIds instanceof List) {
        for (Object son : (List) childrenIds) {
          ObjectId roleId = new ObjectId(son.toString());
          LASObject roleObj = lasDataEntityManager.get(schema.getAppId(), schema.getClassName(), null, roleId);
          if (roleObj == null)
            continue;

          if (rMap.containsKey(roleObj.get(LASRole.FIELD_NAME))) {
            rMap.get(roleObj.get(LASRole.FIELD_NAME)).add(r.get(LASRole.FIELD_NAME).toString());
          } else {
            HashSet value = new HashSet();
            value.add(r.get(LASRole.FIELD_NAME).toString());
            rMap.put(roleObj.get(LASRole.FIELD_NAME).toString(), value);
          }
        }
      }
    }
    for (Map.Entry<String, Set<String>> rEntry : rMap.entrySet()) {
      Set<String> allAncestors = findAncestorRoles(rEntry.getKey(), rEntry.getValue(), rMap);
      rEntry.setValue(allAncestors);
    }

    return rMap;

  }

  /**
   * role 所有的祖先，不包括自己
   *
   * @param role
   * @param ancestorsResult
   * @param level1Data
   * @return
   */
  private Set<String> findAncestorRoles(String role, Set<String> ancestorsResult, final Map<String, Set<String>> level1Data) {
    // 必须初始化
    if (ancestorsResult == null)
      return null;
    for (String anc : ancestorsResult) {
      if (!level1Data.containsKey(anc))
        continue;

      Set<String> sets = level1Data.get(anc);
      Set<String> ancestorsParents = new HashSet<>(sets);
      ancestorsParents.removeAll(ancestorsResult);
      if (ancestorsParents == null || ancestorsParents.size() < 1)
        continue;

      // 新的祖先
      ancestorsResult.addAll(ancestorsParents);
      for (String newAncestor : ancestorsParents) {
        if (newAncestor.equals(role))
          continue;
        ancestorsResult = findAncestorRoles(newAncestor, ancestorsResult, level1Data);
      }
    }
    return ancestorsResult;
  }
}

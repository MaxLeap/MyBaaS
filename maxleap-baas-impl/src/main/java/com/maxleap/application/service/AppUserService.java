package com.maxleap.application.service;

import com.maxleap.cloudcode.service.ZCloudCodeService;
import com.maxleap.domain.LASApplication;
import com.maxleap.domain.LASSessionToken;
import com.maxleap.domain.LASUser;
import com.maxleap.domain.LASUserType;
import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.domain.auth.PermissionType;
import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.exception.LASException;
import com.maxleap.organization.service.SessionTokenService;
import com.maxleap.pandora.core.lasdata.LASQuery;
import com.maxleap.pandora.core.lasdata.LASUpdate;
import com.maxleap.pandora.data.support.LASDataEntityManager;
import com.maxleap.utils.EncryptUtils;
import com.maxleap.utils.SessionTokenUtils;
import com.maxleap.las.baas.CloudDataUpdateToLASUpdate;
import com.maxleap.las.sdk.DeleteMsg;
import com.maxleap.las.sdk.SaveMsg;
import com.maxleap.las.sdk.UpdateMsg;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by shunlv on 16-2-22.
 */
@Singleton
public class AppUserService {
  private LASDataEntityManager lasDataEntityManager;
  private ZCloudCodeService zCloudCodeService;
  private SessionTokenService sessionTokenService;
  private ApplicationService applicationService;

  private static final String CLASS_NAME = "_User";

  @Inject
  public AppUserService(LASDataEntityManager lasDataEntityManager,
                        ZCloudCodeService zCloudCodeService,
                        SessionTokenService sessionTokenService,
                        ApplicationService applicationService) {
    this.lasDataEntityManager = lasDataEntityManager;
    this.zCloudCodeService = zCloudCodeService;
    this.sessionTokenService = sessionTokenService;
    this.applicationService = applicationService;
  }

  public LASUser registerUser(ObjectId appId, LASUser user, LASPrincipal principal, boolean cloudcode) {
    if (!validate(appId, user, null)) {
      throw new LASException(LASException.INVALID_PARAMETER, "username or email has been taken!");
    }

    if (cloudcode) {
      SaveMsg saveMsg = zCloudCodeService.invokeCreate(appId.toHexString(), CLASS_NAME, user, principal);
      user.setObjectId(new ObjectId(saveMsg.objectIdString()));
      user.setCreatedAt(saveMsg.createdAt());
      user.setUpdatedAt(saveMsg.createdAt());

    } else {
      if (StringUtils.isNotBlank(user.getPassword())) {
        user.setPassword(EncryptUtils.encryptPassword(user.getPassword()));
      }
      LASObject lasObject = lasDataEntityManager.create(appId, CLASS_NAME, principal, user);
      user.setObjectId(lasObject.getObjectId());
      user.setCreatedAt(lasObject.getCreatedAt());
      user.setUpdatedAt(lasObject.getUpdatedAt());
    }

    LASSessionToken lasSessionToken = sessionTokenService.createSessionToken(SessionTokenUtils.genTokenForUser(user.getObjectId().toHexString(), principal.getOrgId(), LASUserType.AppUser.toInt()));
    user.setSessionToken(lasSessionToken == null ? null : lasSessionToken.getToken());

    return user;
  }

  public boolean validate(ObjectId appId, LASUser user, ObjectId userId) {
    if (StringUtils.isBlank(user.getUsername())
        && StringUtils.isBlank(user.getEmail())) {
      return true;
    }

    LASQuery orQuery = null;
    if (StringUtils.isNotBlank(user.getUsername())) {
      orQuery = new LASQuery().equalTo(LASUser.FIELD_USERNAME, user.getUsername());
    }

    if (StringUtils.isNotBlank(user.getEmail())) {
      if (orQuery == null) {
        orQuery = new LASQuery().equalTo(LASUser.FIELD_EMAIL, user.getEmail());
      } else {
        orQuery = orQuery.or(new LASQuery().equalTo(LASUser.FIELD_EMAIL, user.getEmail()));
      }
    }

    if (userId != null) {
      orQuery.notEqualTo("objectId", userId);
    }
    Set<PermissionType> permissionTypes = new HashSet<>();
    permissionTypes.add(PermissionType.MASTER_KEY);
    long count = lasDataEntityManager.count(appId, CLASS_NAME, new LASPrincipal(permissionTypes), orQuery);
    return count <= 0;
  }

  public int updateUser(ObjectId appId, ObjectId userId, Map<String, Object> params, LASPrincipal principal, boolean cloudcode) {
    if (params.containsKey(LASUser.FIELD_USERNAME) || params.containsKey(LASUser.FIELD_EMAIL)) {
      if (!validate(appId, new LASUser(params), userId)) {
        throw new LASException(LASException.INVALID_PARAMETER, "username or email has been taken!");
      }
    }

    if (cloudcode) {
      LASUpdate update = new LASUpdate(params);

      UpdateMsg updateMsg = zCloudCodeService.invokeUpdate(appId.toHexString(), CLASS_NAME, userId.toHexString(), update, principal);

      return updateMsg == null ? 0 : updateMsg.number();
    }

    if (params.containsKey(LASUser.FIELD_PASSWORD)) {
      params.put(LASUser.FIELD_PASSWORD, EncryptUtils.encryptPassword(String.valueOf(params.get(LASUser.FIELD_PASSWORD))));
    }

    LASUpdate update = CloudDataUpdateToLASUpdate.from(params);
    return lasDataEntityManager.update(appId, CLASS_NAME, principal, userId, update);
  }

  public LASUser getUser(ObjectId appId, ObjectId userId, LASPrincipal principal) {
    LASObject lasObject = lasDataEntityManager.get(appId, CLASS_NAME, principal, userId);

    if (lasObject != null) {
      return new LASUser(lasObject.getMap());
    }

    return null;
  }

  public int deleteUser(ObjectId appId, ObjectId userId, LASPrincipal principal, boolean cloudcode) {
    if (cloudcode) {
      DeleteMsg deleteMsg = zCloudCodeService.invokeDelete(appId.toHexString(), CLASS_NAME, userId.toHexString(), principal);
      return deleteMsg == null ? 0 : deleteMsg.number();
    }

    return lasDataEntityManager.delete(appId, CLASS_NAME, principal, userId);
  }

  public List<LASObject> query(ObjectId appId, LASPrincipal principal, LASQuery query) {
    return lasDataEntityManager.find(appId, CLASS_NAME, principal, query);
  }

  public long count(ObjectId appId, LASPrincipal principal, LASQuery query) {
    return lasDataEntityManager.count(appId, CLASS_NAME, principal, query);
  }

  public LASUser login(ObjectId appId, String username, String password, LASPrincipal principal) {
    if (!checkLoginService(appId)) {
      throw new LASException(LASException.UNSUPPORTED_SERVICE, "The application is not a supported username login service.");
    }

    LASQuery query = new LASQuery();
    query.equalTo(LASUser.FIELD_USERNAME, username);
    LASUser lasUser = findUnique(appId, principal, query);
    if (lasUser == null) {
      throw new LASException(LASException.NOT_FIND_USER, "No user found. " + username);
    }

    if (lasUser.containsKey(LASUser.FIELD_ENABLED) && !lasUser.isEnabled()) {
      throw new LASException(LASException.OPERATION_FORBIDDEN, "this user has been disabled!");
    }

    if (!EncryptUtils.checkPassword(password, lasUser.getPassword())) {
      throw new LASException(LASException.PASSWORD_MISMATCH, "The username and password mismatch.");
    }

    LASSessionToken lasSessionToken = sessionTokenService.getTokenForUser(lasUser.getObjectId().toHexString(), principal.getOrgId(), LASUserType.AppUser.toInt());
    lasUser.setSessionToken(lasSessionToken == null ? null : lasSessionToken.getToken());

    return lasUser;
  }

  public LASUser findUnique(ObjectId appId, LASPrincipal principal, LASQuery query) {
    query.setLimit(1);
    List<LASObject> lasObjects = query(appId, principal, query);
    if (lasObjects == null || lasObjects.isEmpty()) {
      return null;
    }

    return new LASUser(lasObjects.get(0).getMap());
  }

  public boolean checkLoginService(ObjectId appId) {
    LASApplication application = applicationService.findById(appId);
    if (application == null) {
      throw new LASException(LASException.OBJECT_NOT_FOUND, "app not found. " + appId.toHexString());
    }

    if (!application.getAuthConfig().isBaseUserName()) {
      throw new LASException(LASException.UNSUPPORTED_SERVICE, "The application is not a supported username login service.");
    }

    return application.getAuthConfig().isDefaultAuth();
  }

  public int logout(String userId, String orgId) {
    return sessionTokenService.delete(userId, orgId);
  }
}

package com.maxleap.cerberus.acl.impl;

import com.maxleap.cerberus.acl.spi.AccessControlConstants;
import com.maxleap.cerberus.acl.spi.AccessControlException;
import com.maxleap.cerberus.acl.spi.AccessControlService;
import com.maxleap.cerberus.acl.spi.AccessRequest;
import com.maxleap.domain.*;
import com.maxleap.domain.auth.IdentifierType;
import com.maxleap.domain.auth.LASAccessPair;
import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.domain.auth.PermissionType;
import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.exception.LASException;
import com.maxleap.pandora.core.mongo.MongoQuery;
import com.maxleap.pandora.data.support.LASDataEntityManager;
import com.maxleap.pandora.data.support.MongoEntityManager;
import com.maxleap.platform.LASTables;
import com.maxleap.utils.SessionTokenUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by shunlv on 16-2-3.
 */
@Singleton
public class AccessControlServiceImpl implements AccessControlService {
  private final Logger logger = LoggerFactory.getLogger(AccessControlServiceImpl.class);
  private final MongoEntityManager entityManager;
  private final LASDataEntityManager dataEntityManager;
  private final PermissionService permissionService;

  @Inject
  public AccessControlServiceImpl(MongoEntityManager entityManager,
                                  LASDataEntityManager dataEntityManager,
                                  PermissionService permissionService) {
    this.entityManager = entityManager;
    this.dataEntityManager = dataEntityManager;
    this.permissionService = permissionService;
  }

  @Override
  public LASAccessPair authenticate(AccessRequest request) {
    return handle(request);
  }

  @Override
  public LASAccessPair authenticateSessionToken(String sessionToken) {
    AccessRequest request = new AccessRequest();
    request.setSessionToken(sessionToken);
    return authenticate(request);
  }

  @Override
  public LASAccessPair authenticateSessionToken(String appId, String sessionToken) {
    AccessRequest request = new AccessRequest();
    request.setAppId(appId);
    request.setSessionToken(sessionToken);
    return authenticate(request);

  }

  @Override
  public LASAccessPair authenticateApiKey(String appId, String apiKey) {
    AccessRequest request = new AccessRequest();
    request.setAppId(appId);
    request.setApiKey(apiKey);

    return authenticate(request);

  }

  @Override
  public LASAccessPair authenticateMasterKey(String appId, String masterKey) {
    AccessRequest request = new AccessRequest();
    request.setAppId(appId);
    request.setMasterKey(masterKey);

    return authenticate(request);
  }

  @Override
  public LASAccessPair authenticateClientKey(String appId, String clientKey) {
    AccessRequest request = new AccessRequest();
    request.setAppId(appId);
    request.setClientKey(clientKey);
    return authenticate(request);

  }

  @Override
  public LASAccessPair authenticateSign(String appId, String sign) {
    AccessRequest request = new AccessRequest();
    request.setAppId(appId);
    request.setSign(sign);

    return authenticate(request);
  }

  private LASSessionToken canAccessWithSession(LASApplication app, String token) {
    if (StringUtils.isEmpty(token)) return null;
    LASSessionToken sessionToken = getSessionToken(token);
    String userId = sessionToken.getUserId();
    int type = sessionToken.getType();

    switch (LASUserType.fromInt(type)) {
      case AppUser:
        if (app == null) {
          throw new AccessControlException(AccessControlConstants.NO_PERMISSION, "This session need appId.");
        }
        LASObject user = getAppUser(app.getObjectId(), new ObjectId(userId));
        if (user != null && (user.get(LASUser.FIELD_ENABLED) == null || (boolean) user.get(LASUser.FIELD_ENABLED)))
          return sessionToken;
        throw new AccessControlException(AccessControlConstants.SESSION_TOKEN_INVALID, "SESSION_TOKEN_INVALID");
      case OrgUser:
        if (app == null) return sessionToken;
        List<String> orgUserIds = app.getOrgUserIds();
        if (orgUserIds != null && !orgUserIds.isEmpty() && orgUserIds.contains(userId)) {
          return sessionToken;
        }
        throw new AccessControlException(AccessControlConstants.APPID_AND_SESSION_NOT_MATCHED, "APPID_AND_SESSION_NOT_MATCHED");
      case OrgAdmin:
        if (app == null || app.getOrgId().equals(sessionToken.getOrgId())) {
          return sessionToken;
        }
        throw new AccessControlException(AccessControlConstants.NO_PERMISSION, "Not the same organization.");
      case SysUser:
      case SysAdmin:
        return sessionToken;
      default:
        throw new AccessControlException(AccessControlConstants.SESSION_TOKEN_INVALID, "Invalid token type " + sessionToken.getType());
    }
  }

  private boolean canAccessWithKey(LASApplication app, String key, String value) throws InterruptedException, ExecutionException, TimeoutException {
    if (app != null && app.getStatus() == LASAppStatus.enabled.toInt() && StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
      switch (key) {
        case LASApplication.FIELD_MASTER_KEY:
          return value.equals(app.getMasterKey());
        case LASApplication.FIELD_REST_API_KEY:
          return value.equals(app.getRestAPIKey());
        case LASApplication.FIELD_CLIENT_KEY:
          return value.equals(app.getClientKey());
      }
    }

    return false;
  }

  private String computeSingCode(String time, String key) {
    return DigestUtils.sha256Hex(key + time);
  }

  private boolean isAppEnabled(LASApplication app) {
    return app != null && app.getStatus() == LASAppStatus.enabled.toInt();
  }

  private LASSessionToken getSessionToken(String token) {
    MongoQuery query = new MongoQuery();
    query.equalTo("token", token);
    LASSessionToken sessionToken = entityManager.findUniqueOne(LASTables.PASSPORT, LASTables.Passport.SESSIONTOKEN, query, LASSessionToken.class);

    if (sessionToken == null) {
      throw new AccessControlException(AccessControlConstants.SESSION_TOKEN_INVALID, "SESSION_TOKEN_INVALID");
    }

    Long expireAt = sessionToken.getExpireAt();
    if (expireAt != null && !expireAt.equals(SessionTokenUtils.NEVER_EXPIRATION) && System.currentTimeMillis() > expireAt) {
      throw new AccessControlException(AccessControlConstants.SESSION_TOKEN_EXPIRED, "SESSION_TOKEN_EXPIRED");
    }

    return sessionToken;
  }

  private LASAccessPair handle(AccessRequest request) {
    try {
      if (request == null) {
        throw new LASException(LASException.INVALID_PARAMETER, "The AccessRequest can't be null.");
      }

      String appId = request.getAppId();
      String apiKey = request.getApiKey();
      String clientKey = request.getClientKey();
      String sign = request.getSign();
      String masterKey = request.getMasterKey();
      String token = request.getSessionToken();

      String path = request.getPath();
      String method = request.getMethod();

      LASApplication app = null;
      String orgId = null;

      if (StringUtils.isNotEmpty(appId)) {
        app = getApplication(appId);
        orgId = app.getOrgId();
      }

      // 如果使用签名，则获得APIKey,ClientKey或者MasterKey
      if (StringUtils.isNotEmpty(sign) && app != null) {
        AccessRequest signRequest = parseSign(sign, app);
        apiKey = signRequest.getApiKey();
        clientKey = signRequest.getClientKey();
        masterKey = signRequest.getMasterKey();
      }

      /**
       * 校验时，如果优先级高的先匹配，则直接返回；优先级由高到低依次为：
       * 1，SessionToken（包括AppUserSessionToken，OrgUserSessionToken，SysUserSessionToken）
       * 2，MasterKey
       * 3，APIKey, ClientKey
       */

      LASAccessPair accessPair = new LASAccessPair();
      LASPrincipal principal = null;
      // 如果使用SessionToken获得Token的类型
      LASSessionToken sessionToken;
      if ((sessionToken = canAccessWithSession(app, token)) != null) {

        if (StringUtils.isEmpty(orgId)) {
          orgId = sessionToken.getOrgId();
        }
        principal = sessionPrincipal(orgId, appId, sessionToken);
        accessPair.setCanAccess(true);
        accessPair.setLASPrincipal(principal);
      }

      if ((principal == null || principal.getType().equals(IdentifierType.APP_USER))
          && canAccessWithKey(app, LASApplication.FIELD_MASTER_KEY, masterKey)) {
        if (principal != null) {
          principal.getPermissions().add(PermissionType.MASTER_KEY);
        } else {
          Set<PermissionType> permissionTypes = new HashSet<>();
          permissionTypes.add(PermissionType.MASTER_KEY);
          principal = new LASPrincipal(orgId, permissionTypes, masterKey, IdentifierType.MASTER_KEY, appId);
        }
        accessPair.setCanAccess(true);
        accessPair.setLASPrincipal(principal);
      }

      if (principal == null && canAccessWithKey(app, LASApplication.FIELD_REST_API_KEY, apiKey)) {
        Set<PermissionType> permissionTypes = new HashSet<>();
        permissionTypes.add(PermissionType.API_KEY);
        principal = new LASPrincipal(orgId, permissionTypes, apiKey, IdentifierType.API_KEY, appId);

        accessPair.setCanAccess(true);
        accessPair.setLASPrincipal(principal);
      }

      if (principal == null && canAccessWithKey(app, LASApplication.FIELD_CLIENT_KEY, clientKey)) {
        Set<PermissionType> permissionTypes = new HashSet<>();
        permissionTypes.add(PermissionType.CLIENT_KEY);
        principal = new LASPrincipal(orgId, permissionTypes, apiKey, IdentifierType.CLIENT_KEY, appId);

        accessPair.setCanAccess(true);
        accessPair.setLASPrincipal(principal);
      }

      if (accessPair.isCanAccess()) {
        return accessPair;
      }

      throw new AccessControlException(AccessControlConstants.APPID_AND_KEY_NOT_MATCHED, "APPID_AND_KEY_NOT_MATCHED");
    } catch (TimeoutException e) {
      logger.error(e.getMessage(), e);
      throw new AccessControlException(AccessControlConstants.NO_PERMISSION, "Access control query data timeout.");
    } catch (AccessControlException e) {
      logger.error(e.getMessage(), e);
      throw new AccessControlException(e.getCode(), e.getMessage());
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new AccessControlException(AccessControlConstants.NO_PERMISSION, e.getMessage());
    }

  }

  private LASPrincipal sessionPrincipal(String orgId, String appId, LASSessionToken sessionToken) {
    LASPrincipal principal;
    Set<PermissionType> permissions;
    String userId = sessionToken.getUserId();
    switch (LASUserType.fromInt(sessionToken.getType())) {
      case AppUser:
        permissions = new HashSet<>();
        permissions.add(PermissionType.APP_USER);
        principal = new LASPrincipal(orgId, permissions, userId, IdentifierType.APP_USER, appId);
        break;
      case OrgUser:
        permissions = permissionService.getUserPermissions(userId);
        permissions.add(PermissionType.ORG_USER);
        principal = new LASPrincipal(orgId, permissions, userId, IdentifierType.ORG_USER, appId);
        break;
      case OrgAdmin:
        permissions = new HashSet<>();
        permissions.add(PermissionType.ORG_ADMIN);
        principal = new LASPrincipal(orgId, permissions, userId, IdentifierType.ORG_ADMIN, appId);
        break;
      case SysUser:
        permissions = permissionService.getUserPermissions(userId);
        permissions.add(PermissionType.SYS_USER);
        principal = new LASPrincipal(orgId, permissions, userId, IdentifierType.SYS_USER, appId);
        break;
      case SysAdmin:
        permissions = new HashSet<>();
        permissions.add(PermissionType.SYS_ADMIN);
        principal = new LASPrincipal(orgId, permissions, userId, IdentifierType.SYS_ADMIN, appId);
        break;
      default:
        throw new AccessControlException(AccessControlConstants.SESSION_TOKEN_INVALID, "Invalid token type " + sessionToken.getType());
    }

    return principal;
  }

  private AccessRequest parseSign(String sign, LASApplication app) {
    String apiKey = null;
    String clientKey = null;
    String masterKey = null;

    String[] signItems = sign.split(",");
    if (signItems.length < 2) {
      throw new AccessControlException(AccessControlConstants.SIGN_INVALID, "Invalid sign");
    }

    String code = signItems[0];
    String time = signItems[1];
    if (signItems.length == 2) {
      // restKey || clientKey
      apiKey = app.getRestAPIKey();
      clientKey = app.getClientKey();
      if (!computeSingCode(time, apiKey).equals(code) && !computeSingCode(time, clientKey).equals(code)) {
        // error
        throw new AccessControlException(AccessControlConstants.SIGN_INVALID, "Invalid sign, please check REST API key or Client key.");
      }
    } else {
      // master
      if (!signItems[2].equals("master")) {
        throw new AccessControlException(AccessControlConstants.SIGN_INVALID, MessageFormatter.format("Invalid sign, unknown sign type {}.", signItems[2]).getMessage());
      }

      masterKey = app.getMasterKey();
      if (!computeSingCode(time, masterKey).equals(code)) {
        // error
        throw new AccessControlException(AccessControlConstants.SIGN_INVALID, "Invalid sign, please check Master key.");
      }
    }

    AccessRequest request = new AccessRequest();
    request.setApiKey(apiKey);
    request.setClientKey(clientKey);
    request.setMasterKey(masterKey);

    return request;
  }

  private LASApplication getApplication(String appId) {
    LASApplication app = entityManager.get(LASTables.PLATFORM_DATA, LASTables.PlatformData.APPLICATION, new ObjectId(appId), LASApplication.class);
    if (app == null) {
      throw new LASException(LASException.INVALID_PARAMETER, "Does not exist app with id " + appId);
    }
    if (!isAppEnabled(app)) {
      throw new AccessControlException(AccessControlConstants.NO_PERMISSION, "This app is not active " + appId);
    }
    return app;
  }

  private LASObject getAppUser(ObjectId appId, ObjectId userId) {
    Set<PermissionType> permissionTypes = new HashSet<>();
    permissionTypes.add(PermissionType.ORG_USER);
    return dataEntityManager.get(appId, LASTables.AppData.USER, new LASPrincipal(permissionTypes), userId);
  }
}

package com.maxleap.las.bass.apiserver.resource;

import com.maxleap.application.service.AppUserService;
import com.maxleap.cerberus.acl.Permission;
import com.maxleap.domain.LASUser;
import com.maxleap.domain.auth.IdentifierType;
import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.domain.auth.PermissionType;
import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.exception.LASException;
import com.maxleap.las.baas.Constants;
import com.maxleap.las.bass.apiserver.web.JsonStringBuilder;
import com.maxleap.las.sdk.UpdateMsg;
import com.maxleap.pandora.core.lasdata.LASQuery;
import com.maxleap.pandora.core.rest.LASQueryBuilder;
import com.maxleap.pandora.data.support.MongoJsons;
import com.maxleap.utils.Assert;
import com.maxleap.utils.EncryptUtils;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * Created by shunlv on 16-2-22.
 */
@Singleton
@Path("/2.0")
public class AppUserResource {
  private AppUserService appUserService;

  @Inject
  public AppUserResource(AppUserService appUserService) {
    this.appUserService = appUserService;
  }

  @POST
  @Path("users")
  @Consumes(MediaType.APPLICATION_JSON)
  public void registerUser(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, principal) -> {
      LASUser user = MongoJsons.deserialize(ctx.getBodyAsString(), LASUser.class);

      Assert.isTrue(StringUtils.isNotBlank(user.getUsername()), "username can't be empty!");
      Assert.isTrue(StringUtils.isNotBlank(user.getPassword()), "password can't be empty!");

      user.setEnabled(true);
      user.setEmailVerified(true);
      user.setNew(true);

      user = appUserService.registerUser(appId, user, principal, cloudCode);

      String userId = user.getObjectId().toHexString();
      Map<String, Object> creatorMap = new HashMap<>();
      creatorMap.put("type", IdentifierType.APP_USER.getCreatorType());
      creatorMap.put("id", userId);
      Map<String, Object> aclMap = new HashMap<>();
      aclMap.put("creator", creatorMap);

      Map<String, Object> params = new HashMap<>();
      params.put(LASUser.FIELD_ACL, aclMap);

      int count = appUserService.updateUser(appId, user.getObjectId(), params, principal, cloudCode);
      if (count > 0)
        user.setACL(aclMap);
      user.remove(LASUser.FIELD_PASSWORD);

      context.response().end(MongoJsons.serialize(user));
    });
  }

  @GET
  @Path("users/:userId")
  @Permission(type = PermissionType.APP_USER, permisson = "", name = "ClientREST:User:Read get")
  public void getUserById(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, principal) -> {
      boolean hasPermission = false;

      PermissionType masterKey = PermissionType.MASTER_KEY;
      Set<PermissionType> permissionTypes = principal.getPermissions();
      do {
        if (permissionTypes.contains(masterKey)) {
          hasPermission = true;
          break;
        }

        masterKey = masterKey.getParent();
      } while (masterKey != null);

      String userId = ctx.request().getParam("userId");
      if (!hasPermission) {
        Assert.isTrue(userId.equals(principal.getIdentifier()), "Can't get other user's info!");
      }

      LASUser lasUser = appUserService.getUser(appId, new ObjectId(userId), principal);
      lasUser.remove(LASUser.FIELD_PASSWORD);

      context.response().end(MongoJsons.serialize(lasUser));
    });
  }

  @PUT
  @Path("users/:userId")
  @Consumes(MediaType.APPLICATION_JSON)
  @Permission(type = PermissionType.APP_USER, permisson = "", name = "ClientREST:User:Write update")
  public void updateUserById(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, principal) -> {
      Map<String, Object> params = MongoJsons.deserialize(ctx.getBodyAsString(), Map.class);
      Assert.isTrue(params != null, "Update params can't be empty!");

      boolean isMaster = false;

      PermissionType masterKey = PermissionType.MASTER_KEY;
      Set<PermissionType> permissionTypes = principal.getPermissions();
      do {
        if (permissionTypes.contains(masterKey)) {
          isMaster = true;
          break;
        }

        masterKey = masterKey.getParent();
      } while (masterKey != null);

      String userId = ctx.request().getParam("userId");
      if (!isMaster) {
        Assert.isTrue(userId.equals(principal.getIdentifier()), "Can't update other user's info.");
        Assert.isTrue(!params.containsKey(LASUser.FIELD_ENABLED), "enabled can't be updated!");
        Assert.isTrue(!params.containsKey(LASUser.FIELD_PASSWORD), "password can't be updated!");
      }

      long time = System.currentTimeMillis();
      params.put(LASUser.FIELD_UPDATED_AT, time);

      int result = appUserService.updateUser(appId, new ObjectId(userId), params, principal, cloudCode);

      context.response().end(MongoJsons.serialize(new UpdateMsg(result, time, null)));
    });
  }

  @DELETE
  @Path("users/:userId")
  @Permission(type = PermissionType.MASTER_KEY, permisson = "", name = "ConsoleREST:User:Write delete")
  public void deleteUserById(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, principal) -> {
      String userId = ctx.request().getParam("userId");

      int result = appUserService.deleteUser(appId, new ObjectId(userId), principal, cloudCode);

      ctx.response().end("{\"number\": " + result + "}");
    });
  }

  @GET
  @Path("users")
  @Permission(type = PermissionType.MASTER_KEY, permisson = "", name = "ConsoleREST:User:Read.")
  public void query(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, principal) -> {
      LASQuery query = null;
      Integer count = null;

      HttpServerRequest request = ctx.request();
      String c = request.getParam("count");
      if (c != null) {
        count = Integer.parseInt(c);
      }
      LASQueryBuilder queryBuilder = LASQueryBuilder.start(request.getParam("where"));
      queryBuilder.orders(request.getParam("order"))
          .keys(request.getParam("keys"))
          .excludeKeys(request.getParam("excludeKeys"))
          .include(request.getParam("include"));
      String limit = request.getParam("limit");
      if (limit != null) {
        queryBuilder.limit(Integer.parseInt(limit));
      }
      String skip = request.getParam("skip");
      if (skip != null) {
        queryBuilder.skip(Integer.parseInt(skip));
      }
      query = queryBuilder.build();
      JsonStringBuilder findResponse = JsonStringBuilder.create();
      //count
      if (count != null && count > 0) {
        long result = appUserService.count(appId, principal, query);
        findResponse.writeNumber("count", result);
        if (query.getQueryOptions().getLimit() == 0) {
          findResponse.writeObject("results", new ArrayList<>());
        } else {
          List<LASObject> users = appUserService.query(appId, principal, query);
          findResponse.writeObject("results", users);
        }
      } else {
        findResponse.writeNumber("count", -1);
        List<LASObject> users = appUserService.query(appId, principal, query);
        findResponse.writeObject("results", users);
      }

      ctx.response().end(findResponse.build());
    });
  }

  @POST
  @Path("login")
  public void login(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, principal) -> {
      Map params = MongoJsons.deserialize(ctx.getBodyAsString(), Map.class);
      Assert.isTrue(params != null, "Update params can't be empty!");
      Assert.isTrue(params.containsKey(LASUser.FIELD_USERNAME), "username can't be null!");
      Assert.isTrue(params.containsKey(LASUser.FIELD_PASSWORD), "password can't be null!");

      String username = params.get(LASUser.FIELD_USERNAME).toString();
      String password = params.get(LASUser.FIELD_PASSWORD).toString();

      LASUser lasUser = appUserService.login(appId, username, password, principal);
      lasUser.remove(LASUser.FIELD_PASSWORD);

      context.response().end(MongoJsons.serialize(lasUser));
    });
  }

  @POST
  @Path("updatePassword")
  @Consumes(MediaType.APPLICATION_JSON)
  @Permission(type = PermissionType.APP_USER, permisson = "", name = "ClientREST:User:Write update password")
  public void updatePassword(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, principal) -> {
      Map params = MongoJsons.deserialize(ctx.getBodyAsString(), Map.class);
      Assert.isTrue(params != null, "Update params can't be empty!");
      Assert.isTrue(params.containsKey("password"), "password can't be null!");
      Assert.isTrue(params.containsKey("newPassword"), "newPassword can't be null!");

      String password = params.get("password").toString();
      String newPassword = params.get("newPassword").toString();

      ObjectId userId = new ObjectId(principal.getIdentifier());
      LASUser lasUser = appUserService.getUser(appId, userId, principal);
      if (lasUser == null) {
        throw new LASException(LASException.NOT_FIND_USER, "User not find! " + principal.getIdentifier());
      }

      if (!EncryptUtils.checkPassword(password, lasUser.getPassword())) {
        throw new LASException(LASException.PASSWORD_MISMATCH, "password error.");
      }

      params = new HashMap();
      long time = System.currentTimeMillis();
      params.put(LASUser.FIELD_PASSWORD, newPassword);
      params.put(LASUser.FIELD_UPDATED_AT, time);

      int result = appUserService.updateUser(appId, userId, params, principal, cloudCode);

      context.response().end(MongoJsons.serialize(new UpdateMsg(result, time, null)));
    });
  }

  @GET
  @Path("logout")
  @Permission(type = PermissionType.APP_USER, name = "ClientREST:User:Read read")
  public void logout(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, principal) -> {
      int result = appUserService.logout(principal.getIdentifier(), principal.getOrgId());
      ctx.response().end("{\"number\": " + result + "}");
    });
  }

  private void func(RoutingContext ctx, Func func) {
    LASPrincipal principal = ctx.get(Constants.LAS_PRINCIPAL);
    ObjectId appId = ResourceUtils.getAppIdFormHeader(ctx);
    String s = ctx.request().headers().get(Constants.HEADER_MAXLEAP_CCODE_MAPPING);
    if (s == null) {
      s = ctx.request().headers().get(Constants.HEADER_LAS_MAXLEAP_CCODE_MAPPING);
    }
    int i = 0;
    if (s != null) {
      try {
        i = Integer.parseInt(s);
      } catch (NumberFormatException e) {
        throw new LASException(1, Constants.HEADER_MAXLEAP_CCODE_MAPPING + " must be number");
      }
    }
    func.func(ctx, i == 1,  appId, principal);
  }

  public interface Func {
    public void func(RoutingContext ctx, boolean cloudCode, ObjectId appId, LASPrincipal principal);
  }
}

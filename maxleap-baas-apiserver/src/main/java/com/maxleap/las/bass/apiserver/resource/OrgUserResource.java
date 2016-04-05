package com.maxleap.las.bass.apiserver.resource;

import com.maxleap.application.service.ApplicationService;
import com.maxleap.cerberus.acl.Permission;
import com.maxleap.domain.*;
import com.maxleap.domain.auth.PermissionType;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.exception.LASException;
import com.maxleap.las.baas.Constants;
import com.maxleap.las.bass.apiserver.web.JsonStringBuilder;
import com.maxleap.las.sdk.UpdateMsg;
import com.maxleap.organization.service.OrgRoleService;
import com.maxleap.organization.service.OrgUserService;
import com.maxleap.organization.service.OrganizationService;
import com.maxleap.organization.service.SessionTokenService;
import com.maxleap.pandora.core.lasdata.LASQuery;
import com.maxleap.pandora.core.mongo.MongoQuery;
import com.maxleap.pandora.core.mongo.MongoUpdate;
import com.maxleap.pandora.core.rest.LASQueryBuilder;
import com.maxleap.pandora.data.support.MongoJsons;
import com.maxleap.utils.Assert;
import com.maxleap.utils.EncryptUtils;
import com.maxleap.utils.SessionTokenUtils;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * Created by shunlv on 16-2-16.
 */
@Singleton
@Path("/2.0/orgUsers")
@Produces(MediaType.APPLICATION_JSON)
public class OrgUserResource {
  private final OrgUserService orgUserService;
  private final OrgRoleService orgRoleService;
  private final SessionTokenService sessionTokenService;
  private final OrganizationService organizationService;
  private final ApplicationService applicationService;

  @Inject
  public OrgUserResource(OrgUserService orgUserService,
                         OrgRoleService orgRoleService,
                         SessionTokenService sessionTokenService,
                         OrganizationService organizationService,
                         ApplicationService applicationService) {
    this.orgUserService = orgUserService;
    this.orgRoleService = orgRoleService;
    this.sessionTokenService = sessionTokenService;
    this.organizationService = organizationService;
    this.applicationService = applicationService;
  }

  @POST
  @Path("login")
  @Consumes(MediaType.APPLICATION_JSON)
  public void login(RoutingContext context) {
    func(context, (ctx, principal) -> {
      JsonObject jsonObject = ctx.getBodyAsJson();
      String loginid = jsonObject.getString("loginid");
      String password = jsonObject.getString("password");

      if (StringUtils.isBlank(loginid) || StringUtils.isBlank(password)) {
        throw new LASException(LASException.INVALID_PARAMETER, "loginid and password can't be empty!");
      }

      LASOrgUser orgUser = orgUserService.login(loginid, password);
      orgUser.remove(LASOrgUser.FIELD_PASSWORD);
      context.response().end(MongoJsons.serialize(orgUser));
    });
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Permission(type = PermissionType.ORG_ADMIN, name = "ClientREST:User:Write")
  public void createOrgUser(RoutingContext context) {
    func(context, (ctx, principal) -> {
      LASOrgUser orgUser = MongoJsons.deserialize(ctx.getBodyAsString(), LASOrgUser.class);
      if (orgUser == null || orgUser.getRoles() == null || orgUser.getRoles().isEmpty()) {
        throw new LASException(LASException.INVALID_PARAMETER, "roles can't be empty!");
      }

      Assert.isTrue(StringUtils.isNotBlank(orgUser.getUsername()), "username is required.");
      Assert.isTrue(StringUtils.isNotBlank(orgUser.getEmail()), "email is required.");
      Assert.isTrue(StringUtils.isNotBlank(orgUser.getPassword()), "password is required.");

      orgUser.setOrgId(principal.getOrgId());
      orgUser.setEnabled(true);

      LASUserType userType = LASUserType.OrgUser;
      Set<PermissionType> permissionTypes = orgRoleService.getRolePermissions(orgUser.getRoles());
      if (permissionTypes != null && !permissionTypes.isEmpty()) {
        for (PermissionType permissionType : permissionTypes) {
          if (permissionType != null && permissionType.getUserType() != null && permissionType.getUserType().toInt() > userType.toInt()) {
            userType = permissionType.getUserType();
          }
        }
      }

      orgUser.setUserType(userType.toInt());
      orgUser.setPassword(EncryptUtils.encryptPassword(orgUser.getPassword()));
      orgUser.setEmailVerified(true);

      orgUser = orgUserService.createOrgUser(orgUser);

      if (userType.equals(LASUserType.OrgUser)) {
        List<String> appIds = orgUser.getApps();
        MongoQuery query = new MongoQuery();
        query.in("objectId", appIds);
        MongoUpdate update = MongoUpdate.getMongoUpdate();
        update.addToSet(LASApplication.FIELD_ORG_USER_IDS, orgUser.getObjectId().toHexString());

        applicationService.update(query, update);
      }

      LASSessionToken sessionToken = sessionTokenService.createSessionToken(SessionTokenUtils.genTokenForUser(orgUser.getObjectId().toHexString(), orgUser.getOrgId(), userType.toInt()));
      orgUser.setSessionToken(sessionToken == null ? "" : sessionToken.getToken());
      orgUser.remove(LASOrgUser.FIELD_PASSWORD);

      context.response().end(MongoJsons.serialize(orgUser));
    });
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("register")
  public void registerOrgUser(RoutingContext context) {
    func(context, (ctx, principal) -> {
      LASOrgUser orgUser = MongoJsons.deserialize(ctx.getBodyAsString(), LASOrgUser.class);

      Assert.isTrue(StringUtils.isNotBlank(orgUser.getUsername()), "username is required.");
      Assert.isTrue(StringUtils.isNotBlank(orgUser.getEmail()), "email is required.");
      Assert.isTrue(StringUtils.isNotBlank(orgUser.getPassword()), "password is required.");

      LASOrganization organization = new LASOrganization(orgUser.getMap());

      // create organization
      LASOrganization lasOrganization = new LASOrganization();
      lasOrganization.setName(organization.getName() == null ? orgUser.getEmail() : organization.getName());
      lasOrganization.setAddress(organization.getAddress());
      lasOrganization.setBillingAddress(organization.getBillingAddress());
      lasOrganization.setContract(organization.getContract());
      lasOrganization.setDescription(organization.getDescription());
      lasOrganization.setEnabled(organization.isEnabled());
      lasOrganization.setInvoiceTitle(organization.getInvoiceTitle());
      lasOrganization.setOrgType(organization.getOrgType() == null ? "App" : organization.getOrgType());
      lasOrganization.setPhone(organization.getPhone());
      lasOrganization.setQq(organization.getQq());
      lasOrganization.setWebsite(organization.getWebsite());
      organization = organizationService.createOrg(lasOrganization);

      String orgId = organization.getObjectId().toHexString();

      // create org admin role
      LASOrgRole orgRole = new LASOrgRole();
      orgRole.setName("OrgAdmin");
      orgRole.setOrgId(orgId);
      Set<PermissionType> permissions = new HashSet<>();
      permissions.add(PermissionType.ORG_ADMIN);
      orgRole.setPermissions(permissions);
      orgRole = orgRoleService.createOrgRole(orgRole);

      int userType = LASUserType.OrgAdmin.toInt();
      LASOrgUser lasOrgUser = new LASOrgUser();
      lasOrgUser.setEmail(orgUser.getEmail());
      lasOrgUser.setUsername(orgUser.getUsername());
      lasOrgUser.setPassword(EncryptUtils.encryptPassword(orgUser.getPassword()));
      lasOrgUser.setEnabled(true);
      List<String> roles = new ArrayList<>();
      roles.add(orgRole.getObjectId().toHexString());
      lasOrgUser.setRoles(roles);
      lasOrgUser.setUserType(userType);
      lasOrgUser.setEmailVerified(true);
      lasOrgUser.setOrgId(orgId);

      lasOrgUser = orgUserService.createOrgUser(lasOrgUser);
      LASSessionToken sessionToken = sessionTokenService.createSessionToken(SessionTokenUtils.genTokenForUser(lasOrgUser.getObjectId().toHexString(), lasOrgUser.getOrgId(), userType));
      orgUser.setSessionToken(sessionToken == null ? "" : sessionToken.getToken());
      lasOrgUser.remove(LASOrgUser.FIELD_PASSWORD);

      context.response().end(MongoJsons.serialize(lasOrgUser));
    });
  }

  @GET
  @Path(":orgUserId")
  @Permission(type = PermissionType.ORG_USER, name = "ClientREST:User:findById")
  public void findOrgUserById(RoutingContext context) {
    func(context, (ctx, principal) -> {
      String orgUserId = ctx.request().getParam("orgUserId");
      if (orgUserId == null || !orgUserId.equals(principal.getIdentifier())) {
        throw new LASException(LASException.INVALID_PARAMETER, "orgUserId is not valid!");
      }

      LASOrgUser orgUser = orgUserService.getOrgUser(new ObjectId(orgUserId));
      orgUser.remove(LASOrgUser.FIELD_PASSWORD);
      context.response().end(MongoJsons.serialize(orgUser));
    });
  }

  @GET
  @Permission(type = PermissionType.ORG_ADMIN, name = "ClientREST:User:find")
  public void findOrgUsers(RoutingContext context) {
    func(context, (ctx, principal) -> {
      LASQuery query = null;
      Integer count = null;

      HttpServerRequest request = ctx.request();
      String c = request.getParam("count");
      if (c != null) {
        count = Integer.parseInt("count");
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

      boolean isSysUser = false;
      Set<PermissionType> permissionTypeSet = principal.getPermissions();
      PermissionType permissionType = PermissionType.SYS_USER;
      do {
        if (permissionTypeSet.contains(permissionType)) {
          isSysUser = true;
          break;
        }
        permissionType = permissionType.getParent();
      } while (permissionType != null);
      if (!isSysUser)
        query.equalTo(LASOrgRole.ORG_ID, principal.getOrgId());

      JsonStringBuilder findResponse = JsonStringBuilder.create();
      //count
      if (count != null && count > 0) {
        long result = orgUserService.count(query);
        findResponse.writeNumber("count", result);
        if (query.getQueryOptions().getLimit() == 0) {
          findResponse.writeObject("results", new ArrayList<>());
        } else {
          List<LASOrgUser> users = orgUserService.queryOrgUser(query);
          findResponse.writeObject("results", users);
        }
      } else {
        findResponse.writeNumber("count", -1);
        List<LASOrgUser> users = orgUserService.queryOrgUser(query);
        findResponse.writeObject("results", users);
      }

      ctx.response().end(findResponse.build());
    });
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Path(":orgUserId")
  @Permission(type = PermissionType.ORG_USER, name = "ClientREST:User:update")
  public void updateOrgUser(RoutingContext context) {
    func(context, (ctx, principal) -> {
      boolean isSysUser = false;
      boolean isOrgAdmin = false;
      Set<PermissionType> permissionTypeSet = principal.getPermissions();
      PermissionType permissionType = PermissionType.SYS_USER;
      do {
        if (permissionTypeSet.contains(permissionType)) {
          isSysUser = true;
          isOrgAdmin = true;
          break;
        }
        permissionType = permissionType.getParent();
      } while (permissionType != null);

      Map params = MongoJsons.deserialize(ctx.getBodyAsString(), Map.class);
      if (!isSysUser) {
        Assert.isTrue(!params.containsKey(LASOrgUser.FIELD_PASSWORD), "password can't be updated!");
        Assert.isTrue(!params.containsKey(LASOrgUser.FIELD_EMAIL), "email can't be updated!");
        Assert.isTrue(!params.containsKey(LASOrgUser.FIELD_USER_TYPE), "userType can't be updated!");
        Assert.isTrue(!params.containsKey(LASOrgUser.FIELD_DELETED), "deleted can't be updated!");
        Assert.isTrue(!params.containsKey(LASOrgUser.FIELD_ORG_DISABLED), "orgDisabled can't be updated!");
        isOrgAdmin = permissionTypeSet.contains(PermissionType.ORG_ADMIN);

        if (!isOrgAdmin) {
          Assert.isTrue(!params.containsKey(LASOrgUser.FIELD_ENABLED), "enabled can't be updated!");
        }
      }

      String orgUserId = ctx.request().getParam("orgUserId");
      int result = 0;
      long time = System.currentTimeMillis();
      if (isOrgAdmin) {
        if (orgUserId.equals(principal.getIdentifier())) {
          Assert.isTrue(!params.containsKey(LASOrgUser.FIELD_ROLES), "roles can't be updated!");
        }

        // modify userType
        if (params.containsKey(LASOrgUser.FIELD_ROLES)) {
          Set<PermissionType> permissionTypes = orgRoleService.getRolePermissions((List<String>) params.get(LASOrgUser.FIELD_ROLES));
          if (permissionTypes.contains(PermissionType.ORG_ADMIN)) {
            params.put(LASOrgUser.FIELD_USER_TYPE, LASUserType.OrgAdmin.toInt());
          }
        }

        MongoQuery query = new MongoQuery();
        query.equalTo(LASOrgUser.FIELD_OBJECT_ID, orgUserId);
        if (!isSysUser) {
          query.equalTo(LASOrgUser.FIELD_ORG_ID, principal.getOrgId());
        }

        MongoUpdate update = new MongoUpdate().setAll(params);
        result = orgUserService.updateOrgUser(query, update);

        if (result > 0 && params.containsKey(LASOrgUser.FIELD_APPS)) {
          // todo enable all apps
        }
      } else if (orgUserId.equals(principal.getIdentifier())) {
        Map updateMap = new HashMap();
        if (params.containsKey(LASOrgUser.FIELD_USERNAME)) {
          updateMap.put(LASOrgUser.FIELD_USERNAME, params.get(LASOrgUser.FIELD_USERNAME));
        }
        if (params.containsKey(LASOrgUser.FIELD_LANGUAGE)) {
          updateMap.put(LASOrgUser.FIELD_LANGUAGE, params.get(LASOrgUser.FIELD_LANGUAGE));
        }
        if (params.containsKey(LASOrgUser.FIELD_TIMEZONE)) {
          updateMap.put(LASOrgUser.FIELD_TIMEZONE, params.get(LASOrgUser.FIELD_TIMEZONE));
        }
        if (params.containsKey(LASOrgUser.FIELD_IS_NEW)) {
          updateMap.put(LASOrgUser.FIELD_IS_NEW, params.get(LASOrgUser.FIELD_IS_NEW));
        }

        if (!updateMap.isEmpty()) {
          updateMap.put(LASOrgUser.FIELD_UPDATED_AT, time);
          MongoUpdate update = new MongoUpdate().setAll(updateMap);
          result = orgUserService.updateOrgUser(new ObjectId(orgUserId), update);
        }
      }

      context.response().end(MongoJsons.serialize(new UpdateMsg(result, time, null)));
    });
  }

  @POST
  @Path("resetPassword")
  @Consumes(MediaType.APPLICATION_JSON)
  @Permission(type = PermissionType.ORG_USER, name = "ClientREST:User:Write")
  public void resetPassword(RoutingContext context) {
    func(context, (ctx, principal) -> {
      JsonObject jsonObject = ctx.getBodyAsJson();
      String password = jsonObject.getString(LASOrgUser.FIELD_PASSWORD);

      Assert.isTrue(StringUtils.isNotBlank(password), "password can't be empty!");

      long time = System.currentTimeMillis();
      MongoUpdate update = MongoUpdate.getMongoUpdate();
      update.set(LASOrgUser.FIELD_PASSWORD, EncryptUtils.encryptPassword(password));
      update.set(LASOrgUser.FIELD_UPDATED_AT, time);

      int result = orgUserService.updateOrgUser(new ObjectId(principal.getIdentifier()), update);

      context.response().end(MongoJsons.serialize(new UpdateMsg(result, time, null)));
    });
  }

  @DELETE
  @Path(":orgUserId")
  @Permission(type = PermissionType.ORG_ADMIN, name = "ClientREST:User:delete")
  public void deleteOrgUser(RoutingContext context) {
    func(context, (ctx, principal) -> {
      ObjectId objectId = new ObjectId(ctx.request().getParam("orgUserId"));

      int result = orgUserService.deleteOrgUser(objectId);
      context.response().end("{\"number\": " + result + "}");
    });
  }

  private void func(RoutingContext context, Func2 func) {
    func.func(context, context.get(Constants.LAS_PRINCIPAL));
  }
}

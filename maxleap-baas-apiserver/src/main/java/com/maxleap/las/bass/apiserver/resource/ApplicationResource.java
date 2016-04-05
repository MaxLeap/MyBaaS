package com.maxleap.las.bass.apiserver.resource;

import com.maxleap.application.service.ApplicationService;
import com.maxleap.cerberus.acl.Permission;
import com.maxleap.domain.LASAppStatus;
import com.maxleap.domain.LASApplication;
import com.maxleap.domain.LASOrgUser;
import com.maxleap.domain.auth.PermissionType;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.exception.LASException;
import com.maxleap.las.baas.Constants;
import com.maxleap.las.sdk.UpdateMsg;
import com.maxleap.organization.service.OrgUserService;
import com.maxleap.pandora.core.lasdata.LASClassSchema;
import com.maxleap.pandora.core.lasdata.LASQuery;
import com.maxleap.pandora.core.mongo.MongoUpdate;
import com.maxleap.pandora.core.rest.LASQueryBuilder;
import com.maxleap.pandora.data.support.ClassSchemaManager;
import com.maxleap.pandora.data.support.MongoJsons;
import com.maxleap.utils.Assert;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jasypt.util.password.BasicPasswordEncryptor;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by shunlv on 16-2-18.
 */
@Singleton
@Path("/2.0/apps")
@Produces(MediaType.APPLICATION_JSON)
public class ApplicationResource {
  private ApplicationService applicationService;
  private ClassSchemaManager classSchemaManager;
  private OrgUserService orgUserService;

  @Inject
  public ApplicationResource(ApplicationService applicationService,
                             ClassSchemaManager classSchemaManager,
                             OrgUserService orgUserService) {
    this.applicationService = applicationService;
    this.classSchemaManager = classSchemaManager;
    this.orgUserService = orgUserService;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Permission(type = {PermissionType.ORG_USER}, permisson = "", name = "ConsoleREST:Application:Write create")
  public void create(RoutingContext context) {
    func(context, (ctx, principal) -> {
      LASApplication application = MongoJsons.deserialize(ctx.getBodyAsString(), LASApplication.class);
      Assert.isTrue(StringUtils.isNotBlank(application.getName()), "The name is required.");

      String orgId = principal.getOrgId();
      application.setOrgId(orgId);

      if (!applicationService.validateAppName(orgId, application.getName())) {
        throw new LASException(LASException.NAME_TAKEN, "app name has been taken! " + application.getName());
      }

      application = applicationService.createApp(principal.getIdentifier(), application);
      ObjectId objectId = application.getObjectId();

      // default class schemas
      LASClassSchema _userSchema = new LASClassSchema();
      _userSchema.setAppId(objectId);
      _userSchema.setClassName("_User");
      classSchemaManager.create(_userSchema);

      LASClassSchema _installation = new LASClassSchema();
      _installation.setAppId(objectId);
      _installation.setClassName("_Installation");
      classSchemaManager.create(_installation);

      // todo create related resource

      context.response().end(MongoJsons.serialize(application));
    });
  }

  @PUT
  @Path(":appId")
  @Consumes(MediaType.APPLICATION_JSON)
  @Permission(type = PermissionType.ORG_USER, permisson = "", name = "ConsoleREST:Application:Write update")
  public void update(RoutingContext context) {
    func(context, (ctx, principal) -> {
      String appId = ctx.request().getParam("appId");
      Assert.isTrue(StringUtils.isNotBlank(appId), "appId can't be empty!");

      int result = 0;
      long time = System.currentTimeMillis();

      Map params = MongoJsons.deserialize(ctx.getBodyAsString(), Map.class);
      if (params == null || params.isEmpty()) {
        context.response().end(MongoJsons.serialize(new UpdateMsg(result, time, null)));
        return;
      }

      if (params.containsKey(LASApplication.FIELD_URL_SCHEMA)) {
        params.put(LASApplication.FIELD_URL_SCHEMA, StringEscapeUtils.escapeHtml(params.get(LASApplication.FIELD_URL_SCHEMA).toString()));
      }
      if (params.containsKey(LASApplication.FIELD_APP_STORE_ID)) {
        params.put(LASApplication.FIELD_APP_STORE_ID, StringEscapeUtils.escapeHtml(params.get(LASApplication.FIELD_APP_STORE_ID).toString()));
      }
      if (params.containsKey(LASApplication.FIELD_CATEGORY)) {
        params.put(LASApplication.FIELD_CATEGORY, StringEscapeUtils.escapeHtml(params.get(LASApplication.FIELD_CATEGORY).toString()));
      }
      if (params.containsKey(LASApplication.FIELD_DESCRIPTION)) {
        params.put(LASApplication.FIELD_DESCRIPTION, StringEscapeUtils.escapeHtml(params.get(LASApplication.FIELD_DESCRIPTION).toString()));
      }

      MongoUpdate update = MongoUpdate.getMongoUpdate();
      for (Object key : params.keySet()) {
        if (key.equals(LASApplication.FIELD_PLATFORMS)
            || key.equals(LASApplication.FIELD_APP_STORE_IDS)
            || key.equals(LASApplication.FIELD_URL_SCHEMAS)
            || key.equals(LASApplication.FIELD_IAP)
            || key.equals(LASApplication.FIELD_IS_NEW)) {
          update.set(key.toString(), params.get(key));
        } else {
          update.setBright(key.toString(), params.get(key));
        }
      }
      update.set(LASApplication.FIELD_UPDATED_AT, time);

      result = applicationService.updateById(new ObjectId(appId), update);

      context.response().end(MongoJsons.serialize(new UpdateMsg(result, time, null)));
    });
  }

  @GET
  @Path(":appId")
  @Permission(type = PermissionType.ORG_USER, permisson = "", name = "ConsoleREST:Application:Read findById")
  public void findById(RoutingContext context) {
    func(context, (ctx, principal) -> {
      String appId = ctx.request().getParam("appId");
      Assert.isTrue(StringUtils.isNotBlank(appId), "appId can't be empty!");

      LASApplication application = applicationService.findById(new ObjectId(appId));
      context.response().end(MongoJsons.serialize(application));
    });
  }

  @GET
  @Permission(type = PermissionType.ORG_USER, permisson = "", name = "ConsoleREST:Application:Read find")
  public void find(RoutingContext context) {
    func(context, (ctx, principal) -> {
      LASQuery query = null;
      HttpServerRequest request = ctx.request();
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
      if (!isSysUser) {
        query.equalTo(LASApplication.FIELD_ORG_ID, principal.getOrgId());
      }
      query.equalTo(LASApplication.FIELD_STATUS, LASAppStatus.enabled.toInt());

      List<LASApplication> docs = applicationService.queryApp(query);
      context.response().end(MongoJsons.serialize(docs));
    });
  }

  @POST
  @Path(":objectId/delete")
  @Consumes(MediaType.APPLICATION_JSON)
  @Permission(type = PermissionType.ORG_USER, permisson = "", name = "ConsoleREST:Application:Write delete")
  public void delete(RoutingContext context) {
    func(context, (ctx, principal) -> {
      HttpServerRequest request = ctx.request();
      String objectId = request.getParam("objectId");
      final String password =context.getBodyAsJson().getString("password");
      Assert.hasLength(password, "The password can't be empty.");
      String orgUserId = principal.getIdentifier();
      LASOrgUser orgUser = orgUserService.getOrgUser(new ObjectId(orgUserId));
      if (orgUser == null) {
        throw new IllegalArgumentException("orgUser not exists: " + orgUserId);
      }
      if (new BasicPasswordEncryptor().checkPassword(password, orgUser.getPassword())) {
        MongoUpdate update = new MongoUpdate().set(LASApplication.FIELD_STATUS, LASAppStatus.disabled.toInt());
        int result = applicationService.updateById(new ObjectId(objectId), update);
        context.response().end(MongoJsons.serialize(new UpdateMsg(result, System.currentTimeMillis(), null)));
      } else {
        throw new LASException(LASException.PASSWORD_MISMATCH, "The password mismatch.");
      }
    });
  }

  private void func(RoutingContext context, Func2 func) {
    func.func(context, context.get(Constants.LAS_PRINCIPAL));
  }
}

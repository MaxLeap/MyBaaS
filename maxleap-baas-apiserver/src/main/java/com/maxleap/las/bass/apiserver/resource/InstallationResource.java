package com.maxleap.las.bass.apiserver.resource;

import com.maxleap.application.service.InstallationService;
import com.maxleap.cerberus.acl.Permission;
import com.maxleap.domain.LASInstallation;
import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.domain.auth.PermissionType;
import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.exception.LASException;
import com.maxleap.lang.LocaleConverter;
import com.maxleap.pandora.core.lasdata.LASQuery;
import com.maxleap.pandora.core.lasdata.LASUpdate;
import com.maxleap.pandora.core.rest.LASQueryBuilder;
import com.maxleap.pandora.data.support.MongoJsons;
import com.maxleap.utils.Assert;
import com.maxleap.las.baas.Constants;
import com.maxleap.las.baas.CloudDataUpdateToLASUpdate;
import com.maxleap.las.bass.apiserver.web.JsonStringBuilder;
import com.maxleap.las.sdk.UpdateMsg;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shunlv on 16-2-23.
 */
@Singleton
@Path("/2.0/installations")
@Produces(MediaType.APPLICATION_JSON)
public class InstallationResource {
  private InstallationService installationService;

  @Inject
  public InstallationResource(InstallationService installationService) {
    this.installationService = installationService;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public void create(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, principal) -> {
      LASInstallation lasInstallation = MongoJsons.deserialize(ctx.getBodyAsString(), LASInstallation.class);
      Assert.isTrue(StringUtils.isNotBlank(lasInstallation.getDeviceType()), "The device type can't be empty!");
      boolean hasDeviceToken = StringUtils.isNotBlank(lasInstallation.getDeviceToken());
      boolean hasInstallationId = StringUtils.isNotBlank(lasInstallation.getInstallationId());
      Assert.isTrue(hasInstallationId || hasDeviceToken, "At least one ID field (installationId,deviceToken) must be specified in this operation. AppId [" + appId + "]");

      Map<String, Object> creatorMap = new HashMap<>();
      creatorMap.put("type", principal.getType().getCreatorType());
      creatorMap.put("id", principal.getIdentifier());
      Map<String, Object> aclMap = new HashMap<>();
      aclMap.put("creator", creatorMap);
      lasInstallation.put(LASInstallation.FIELD_ACL, aclMap);

      String langCode = lasInstallation.containsKey("language") ? lasInstallation.get("language").toString() : null;
      if (StringUtils.isNotBlank(langCode)) {
        lasInstallation.put(LASInstallation.FIELD_ISO_LANG, LocaleConverter.langToLocalString(langCode));
      }

      LASObject result = null;

      LASQuery deviceQuery = new LASQuery().equalTo(LASInstallation.FIELD_DEVICE_TOKEN, lasInstallation.getDeviceToken());
      LASQuery installQuery = new LASQuery().equalTo(LASInstallation.FIELD_INSTALLATION_ID, lasInstallation.getInstallationId());
      LASQuery andQuery = new LASQuery().equalTo(LASInstallation.FIELD_DEVICE_TOKEN, lasInstallation.getDeviceToken())
          .equalTo(LASInstallation.FIELD_INSTALLATION_ID, lasInstallation.getInstallationId());
      if (hasDeviceToken && hasInstallationId) {
        List<LASObject> installations = installationService.find(appId, principal, andQuery);
        if (installations == null || installations.isEmpty()) {
          LASUpdate update = LASUpdate.getLASUpdate();
          update.unset(LASInstallation.FIELD_DEVICE_TOKEN);
          installationService.update(appId, principal, deviceQuery, update);

          List<LASObject> installs = installationService.find(appId, principal, installQuery);
          if (installs == null || installs.isEmpty()) {
            result = installationService.create(appId, principal, lasInstallation, cloudCode);
          } else {
            LASObject install = installs.get(0);
            LASUpdate update1 = LASUpdate.getLASUpdate();
            update1.set(LASInstallation.FIELD_DEVICE_TOKEN, lasInstallation.getDeviceToken());

            installationService.update(appId, principal, install.getObjectId().toHexString(), update1, cloudCode);

            result = install;
          }
        } else {
          result = installations.get(0);
        }
      } else {
        LASQuery query;
        if (hasDeviceToken)
          query = deviceQuery;
        else query = installQuery;

        List<LASObject> installations = installationService.find(appId, principal, query);
        if (installations == null || installations.isEmpty()) {
          result = installationService.create(appId, principal, lasInstallation, cloudCode);
        } else {
          result = installations.get(0);
        }
      }

      ctx.response().end(MongoJsons.serialize(result));
    });
  }

  @PUT
  @Path(":installId")
  @Consumes(MediaType.APPLICATION_JSON)
  public void update(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, principal) -> {
      String installId = ctx.request().getParam("installId");
      Map params = MongoJsons.deserialize(ctx.getBodyAsString(), Map.class);
      Assert.isTrue(params != null, "Update params can't be empty!");
      params.remove("objectId");

      String langCode = params.containsKey("language") ? params.get("language").toString() : null;
      if (StringUtils.isNotBlank(langCode)) {
        params.put(LASInstallation.FIELD_ISO_LANG, LocaleConverter.langToLocalString(langCode));
      }

      int result = 0;
      long time = System.currentTimeMillis();

      LASUpdate update = CloudDataUpdateToLASUpdate.from(params);
      update.set(LASInstallation.FIELD_UPDATED_AT, time);
      LASQuery installQuery = new LASQuery().equalTo(LASInstallation.FIELD_INSTALLATION_ID, params.get(LASInstallation.FIELD_INSTALLATION_ID));
      LASQuery deviceQuery = new LASQuery().equalTo(LASInstallation.FIELD_DEVICE_TOKEN, params.get(LASInstallation.FIELD_DEVICE_TOKEN));

      if (params.containsKey(LASInstallation.FIELD_INSTALLATION_ID)) {
        List<LASObject> installs = installationService.find(appId, principal, installQuery);
        if (installs != null && !installs.isEmpty() && !installs.get(0).getObjectId().toHexString().equals(installId)) {
          throw new LASException(LASException.INVALID_UPDATE, "InstallationId & deviceToken must be unique.");
        }

        if (params.containsKey(LASInstallation.FIELD_DEVICE_TOKEN)) {
          LASUpdate update1 = LASUpdate.getLASUpdate();
          update1.unset(LASInstallation.FIELD_DEVICE_TOKEN);
          installationService.update(appId, principal, deviceQuery, update1);
          result = installationService.update(appId, principal, installId, update, cloudCode);
        } else {
          result = installationService.update(appId, principal, installId, update, cloudCode);
        }
      } else {
        if (params.containsKey(LASInstallation.FIELD_DEVICE_TOKEN)) {
          LASUpdate update1 = LASUpdate.getLASUpdate();
          update1.unset(LASInstallation.FIELD_DEVICE_TOKEN);
          installationService.update(appId, principal, deviceQuery, update1);
          result = installationService.update(appId, principal, installId, update, cloudCode);
        } else {
          result = installationService.update(appId, principal, installId, update, cloudCode);
        }
      }

      context.response().end(MongoJsons.serialize(new UpdateMsg(result, time, null)));
    });
  }

  @GET
  @Path(":installId")
  public void get(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, principal) -> {
      String installId = ctx.request().getParam("installId");

      LASObject result = installationService.get(appId, principal, new ObjectId(installId));
      ctx.response().end(MongoJsons.serialize(result));
    });
  }

  @GET
  @Permission(type = PermissionType.MASTER_KEY, name = "ClientREST:Install:Find")
  public void query(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, principal) -> {
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

      JsonStringBuilder findResponse = JsonStringBuilder.create();
      //count
      if (count != null && count > 0) {
        long result = installationService.count(appId, principal, query);
        findResponse.writeNumber("count", result);
        if (query.getQueryOptions().getLimit() == 0) {
          findResponse.writeObject("results", new ArrayList<>());
        } else {
          List<LASObject> docs = installationService.find(appId, principal, query);
          findResponse.writeObject("results", docs);
        }
      } else {
        findResponse.writeNumber("count", -1);
        List<LASObject> docs = installationService.find(appId, principal, query);
        findResponse.writeObject("results", docs);
      }
      ctx.response().end(findResponse.build());
    });
  }

  @DELETE
  @Path(":installId")
  @Permission(type = PermissionType.MASTER_KEY, name = "ClientREST:Install:Delete")
  public void delete(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, principal) -> {
      String installId = ctx.request().getParam("installId");

      int result = installationService.delete(appId, principal, new ObjectId(installId));
      ctx.response().end("{\"number\": " + result + "}");
    });
  }

  private void func(RoutingContext ctx, Func func) {
    LASPrincipal principal = ctx.get(Constants.LAS_PRINCIPAL);
    ObjectId appId = ResourceUtils.getAppIdFormHeader(ctx);
    try {
      String s = ctx.request().headers().get(Constants.HEADER_MAXLEAP_CCODE_MAPPING);
      if (s != null) {
        func.func(ctx, Integer.parseInt(s) == 1, appId, principal);
      } else {
        func.func(ctx, false, appId, principal);
      }
    } catch (NumberFormatException e) {
      throw new LASException(1, Constants.HEADER_MAXLEAP_CCODE_MAPPING + " must be number");
    }
  }

  public interface Func {
    public void func(RoutingContext ctx, boolean cloudCode, ObjectId appId, LASPrincipal principal);
  }
}

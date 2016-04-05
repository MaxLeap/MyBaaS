package com.maxleap.las.bass.apiserver.resource;

import com.maxleap.cerberus.acl.Permission;
import com.maxleap.domain.LASOrgRole;
import com.maxleap.domain.auth.PermissionType;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.exception.LASException;
import com.maxleap.organization.service.OrgRoleService;
import com.maxleap.pandora.core.lasdata.LASQuery;
import com.maxleap.pandora.core.mongo.MongoUpdate;
import com.maxleap.pandora.core.rest.LASQueryBuilder;
import com.maxleap.pandora.data.support.MongoJsons;
import com.maxleap.las.baas.Constants;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by shunlv on 16-2-16.
 */
@Singleton
@Path("/2.0/orgRoles")
@Produces(MediaType.APPLICATION_JSON)
public class OrgRoleResource {
  private final OrgRoleService orgRoleService;

  @Inject
  public OrgRoleResource(OrgRoleService orgRoleService) {
    this.orgRoleService = orgRoleService;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Permission(type = PermissionType.ORG_ADMIN, name = "ClientREST:Role:Write")
  public void createOrgRole(RoutingContext context) {
    func(context, (ctx, principal) -> {
      LASOrgRole orgRole = MongoJsons.deserialize(ctx.getBodyAsString(), LASOrgRole.class);
      if (StringUtils.isBlank(orgRole.getName())) {
        throw new LASException(LASException.INVALID_PARAMETER, "name can't be null!");
      }

      orgRole.setOrgId(principal.getOrgId());

      orgRole = orgRoleService.createOrgRole(orgRole);
      ctx.response().end(MongoJsons.serialize(orgRole));
    });
  }

  @GET
  @Path(":orgRoleId")
  @Permission(type = PermissionType.ORG_USER, name = "ClientREST:Role:findById")
  public void findOrgRoleById(RoutingContext context) {
    func(context, (ctx, principal) -> {
      String roleId = context.request().getParam("orgRoleId");
      String orgId = principal.getOrgId();

      LASOrgRole orgRole = orgRoleService.getOrgRole(roleId, orgId);
      ctx.response().end(MongoJsons.serialize(orgRole));
    });
  }

  @GET
  @Permission(type = PermissionType.ORG_USER, name = "ClientREST:Role:find")
  public void findOrgRoles(RoutingContext context) {
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
        long result = orgRoleService.count(query);
        findResponse.writeNumber("count", result);
        if (query.getQueryOptions().getLimit() == 0) {
          findResponse.writeObject("results", new ArrayList<>());
        } else {
          List<LASOrgRole> docs = orgRoleService.queryOrgRole(query);
          findResponse.writeObject("results", docs);
        }
      } else {
        findResponse.writeNumber("count", -1);
        List<LASOrgRole> docs = orgRoleService.queryOrgRole(query);
        findResponse.writeObject("results", docs);
      }
      ctx.response().end(findResponse.build());
    });
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Path(":orgRoleId")
  @Permission(type = PermissionType.ORG_ADMIN, name = "ClientREST:Role:update")
  public void updateOrgRole(RoutingContext context) {
    func(context, (ctx, principal) -> {
      Map params = MongoJsons.deserialize(ctx.getBodyAsString(), Map.class);
      ObjectId objectId = new ObjectId(context.request().getParam("orgRoleId"));

      MongoUpdate update = new MongoUpdate().setAll(params);
      long time = System.currentTimeMillis();
      update.set("updatedAt", time);
      int result = orgRoleService.updateOrgRole(objectId, update);

      context.response().end(MongoJsons.serialize(new UpdateMsg(result, time, null)));
    });
  }

  @DELETE
  @Path(":orgRoleId")
  @Permission(type = PermissionType.ORG_ADMIN, name = "ClientREST:Role:delete")
  public void deleteOrgRole(RoutingContext context) {
    func(context, (ctx, principal) -> {
      ObjectId objectId = new ObjectId(context.request().getParam("orgRoleId"));
      int result = orgRoleService.deleteOrgRole(objectId);

      context.response().end("{\"number\": " + result + "}");
    });
  }

  private void func(RoutingContext context, Func2 func) {
    func.func(context, context.get(Constants.LAS_PRINCIPAL));
  }
}

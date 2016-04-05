package com.maxleap.las.bass.apiserver.resource;

import com.maxleap.cloudcode.CCodeCategory;
import com.maxleap.cloudcode.service.ZCloudCodeService;
import com.maxleap.pandora.data.support.MongoJsons;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * User: poplar
 * Date: 7/10/14
 * Time: 1:45 PM
 */
@Singleton
@Path("/2.0")
public class CloudCodeResource {
  private static final Logger logger = LoggerFactory.getLogger(CloudCodeResource.class);

  private ZCloudCodeService zCloudCodeService;
  @Inject
  public CloudCodeResource(ZCloudCodeService zCloudCodeService) {
    this.zCloudCodeService = zCloudCodeService;
  }

  @POST
  @Path("functions/:name")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.WILDCARD)
  public void functionAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String name = context.request().getParam("name");
      Map param = MongoJsons.deserialize(context.getBodyAsString(), Map.class);
      String result = zCloudCodeService.invokeFunc(appId.toHexString(), name, param, CCodeCategory.Function, context.response(), null);
      context.response().end(result);
    });
  }

  @POST
  @Path("jobs/:name")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void jobAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String name = context.request().getParam("name");
      Map param = MongoJsons.deserialize(context.getBodyAsString(), Map.class);
      String result = zCloudCodeService.invokeFunc(appId.toHexString(), name, param, CCodeCategory.Job, context.response(), null);
      context.response().end(result);
    });
  }

  @GET
  @Path("console/jobNames")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void jobNamesAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String result = zCloudCodeService.invokeConsole(appId.toHexString(), "jobNames", null, null);
      context.response().end(result);
    });
  }

  @GET
  @Path("console/functionNames")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void functionNamesAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String result = zCloudCodeService.invokeConsole(appId.toHexString(), "functionNames", null, null);
      context.response().end(result);
    });
  }

}

package com.maxleap.las.bass.apiserver.resource;

import com.maxleap.cloudcode.job.CloudCodeJob;
import com.maxleap.cloudcode.job.domain._SYS_Task;
import com.maxleap.domain.auth.SessionContext;
import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.Direction;
import com.maxleap.pandora.core.exception.ParameterInvalidException;
import com.maxleap.pandora.core.lasdata.LASQuery;
import com.maxleap.pandora.core.rest.LASQueryBuilder;
import com.maxleap.pandora.core.utils.LASObjectJsons;
import com.maxleap.pandora.data.support.LASDataEntityManager;
import com.maxleap.pandora.data.support.MongoJsons;
import com.maxleap.code.CloudCodeContants;
import com.maxleap.las.sdk.FindMsg;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

/**
 * User：poplar
 * Date：14-9-16
 */
@Singleton
@Path("/2.0/scheduler")
public class CloudCodeJobResource {

  private static final Logger logger = LoggerFactory.getLogger(CloudCodeJobResource.class);

  private LASDataEntityManager lasDataEntityManager;
  private CloudCodeJob cloudCodeJob;

  @Inject
  public CloudCodeJobResource(LASDataEntityManager lasDataEntityManager,CloudCodeJob cloudCodeJob) {
    this.lasDataEntityManager = lasDataEntityManager;
    this.cloudCodeJob = cloudCodeJob;
  }

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void queryTasksAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String limit = context.request().getParam("limit");
      String skip = context.request().getParam("skip");
      String name = context.request().getParam("name");

      LASQuery query = new LASQuery();
      query.sort(Direction.DESC,"createdAt");
      if (limit != null) {
        query.setLimit(Integer.valueOf(limit));
      } else {
        query.setLimit(10);
      }
      if (skip != null) {
        query.setSkip(Integer.valueOf(skip));
      } else {
        query.setSkip(-1);
      }
      if (!StringUtils.isEmpty(name)) {
        query.equalTo("name", name);
      }
      List<LASObject> findMsg = lasDataEntityManager.find(appId, "_SYS_Task", null, query);
      if (findMsg == null) context.response().end(LASObjectJsons.serialize(new FindMsg<>()));
      else context.response().end(LASObjectJsons.serialize(new FindMsg<>(findMsg.size(), findMsg)));
    });
  }

  @GET
  @Path(":taskId")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void queryTaskAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String taskId = context.request().getParam("taskId");
      LASQuery query = new LASQuery();
      query.equalTo("objectId", taskId);
      List<LASObject> findMsg = lasDataEntityManager.find(appId, "_SYS_Task", null, query);
      if (findMsg != null && findMsg.size() > 0) {
        context.response().end(LASObjectJsons.serialize(findMsg.get(0)));
      } else {
        context.response().end("{}");
      }
    });
  }

  private boolean isEmpty(Object str) {
    return (str == null || "".equals(str));
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void addTaskAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      if(isEmpty(context.getBodyAsString())) throw new ParameterInvalidException("The description and jobName must be specified.");
      Map param = MongoJsons.deserialize(context.getBodyAsString(), Map.class);

      Object desc = param.get("desc");
      Object jobName = param.get("jobName");
      Object jobParams = param.get("params");
      Object startTimestamp = param.get("startTimestamp");
      Object interval = param.get("interval");
      if (isEmpty(desc) || isEmpty(jobName)) {
        throw new ParameterInvalidException("The description and jobName must be specified.");
      }
      if (isEmpty(startTimestamp) || !NumberUtils.isNumber(startTimestamp.toString())) {
        throw new ParameterInvalidException("The startTimestamp must be specified and numeric.");
      }
      if (!isEmpty(interval)) {
        if (!NumberUtils.isNumber(interval.toString()))
          throw new ParameterInvalidException("The interval must be numeric when it's not null.");
      }
      _SYS_Task task = new _SYS_Task();
      task.setApplicationId(appId.toString());
      task.setName(jobName.toString());
      task.setVersion(CloudCodeContants.GLOBAL_CONFIG.getVersion());
      long executeTime = Long.parseLong(startTimestamp.toString());
      if (executeTime != -1 && executeTime < System.currentTimeMillis()) {
        throw new ParameterInvalidException("The executeTime is invalid.It must be more than current time.");
      }
      task.setExecuteTime(executeTime);
      if (jobParams != null) {
        task.setParams((Map) jobParams);
      }
      task.setDesc(desc.toString());
      if (!isEmpty(interval)) {
        task.setInterval(Long.parseLong(interval.toString()));
      }
      LASObject saveMsg = cloudCodeJob.addTask(task);
      context.response().end((LASObjectJsons.serialize(saveMsg)));
    });
  }

  @PUT
  @Path(":taskId")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void modifyTaskAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String taskId = context.request().getParam("taskId");

      Map param = MongoJsons.deserialize(context.getBodyAsString(), Map.class);
      Object desc = param.get("desc");
      Object jobName = param.get("jobName");
      Object jobParams = param.get("params");
      Object startTimestamp = param.get("startTimestamp");
      Object interval = param.get("interval");
      Object version = param.get("version");
      if (isEmpty(version)) {
        throw new ParameterInvalidException("The version must be specified.");
      }
      if (isEmpty(desc) || isEmpty(jobName)) {
        throw new ParameterInvalidException("The description and jobName must be specified.");
      }
      if (isEmpty(startTimestamp) || !NumberUtils.isNumber(startTimestamp.toString())) {
        throw new ParameterInvalidException("The startTimestamp must be specified and numeric.");
      }
      if (!isEmpty(interval)) {
        if (!NumberUtils.isNumber(interval.toString()))
          throw new ParameterInvalidException("The interval must be numeric when it's not null.");
      }
      _SYS_Task task = new _SYS_Task();
      task.setObjectId(new ObjectId(taskId));
      task.setApplicationId(appId.toHexString());
      task.setName(jobName.toString());
      task.setVersion(version.toString());
      long executeTime = Long.parseLong(startTimestamp.toString());
      if (executeTime != -1 && executeTime < System.currentTimeMillis()) {
        throw new ParameterInvalidException("The executeTime is invalid.It must be more than current time.");
      }
      task.setExecuteTime(executeTime);
      if (jobParams != null) {
        task.setParams((Map) jobParams);
      }
      task.setDesc(desc.toString());
      if (!isEmpty(interval)) {
        task.setInterval(Long.parseLong(interval.toString()));
      }
      Object updateMsg = cloudCodeJob.updateTask(task);
      context.response().end(LASObjectJsons.serialize(updateMsg));
    });

  }

  @DELETE
  @Path(":taskId")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void deleteTasksAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String taskId = context.request().getParam("taskId");
      int count = cloudCodeJob.deleteTask(appId.toString(), taskId);
      context.response().end("{\"number\":"+count+"}");
    });
  }

  @POST
  @Path("run/:taskId")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void runTaskAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String taskId = context.request().getParam("taskId");
      boolean isSucc = cloudCodeJob.runTask(appId.toString(), taskId);
      JsonObject result = new JsonObject().put("success", isSucc);
      context.response().end(result.encode());
    });
  }

  @POST
  @Path("enable/:taskId")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void enableTaskAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String taskId = context.request().getParam("taskId");
      boolean isSucc = cloudCodeJob.enableTask(appId.toString(), taskId);
      JsonObject result = new JsonObject().put("success", isSucc);
      context.response().end(result.encode());
    });
  }

  @POST
  @Path("disable/:taskId")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void disableTaskAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String taskId = context.request().getParam("taskId");
      boolean isSucc = cloudCodeJob.disableTask(appId.toString(), taskId);
      JsonObject result = new JsonObject().put("success", isSucc);
      context.response().end(result.encode());
    });
  }

  @GET
  @Path("status")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void queryJogLogAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String limit = context.request().getParam("limit");
      String skip = context.request().getParam("skip");
      String version = context.request().getParam("version");
      String where = context.request().getParam("where");
      String order = context.request().getParam("order");
      String keys = context.request().getParam("keys");
      String excludeKeys = context.request().getParam("excludeKeys");
      String include = context.request().getParam("include");

      LASQuery query = LASQueryBuilder.start(where).orders((order == null || order.length() <= 0) ? "-updatedAt" : order).keys(keys).excludeKeys(excludeKeys).build();
      if (version != null) query.equalTo("version", version);
      if (limit != null) {
        query.setLimit(Integer.parseInt(limit));
      }
      if (skip != null) {
        query.setSkip(Integer.parseInt(skip));
      }
      query.setIncludes(include);

      List<LASObject> findMsg = lasDataEntityManager.find(appId, "_SYS_JobLog", SessionContext.getCurrentLASPrincipal(), query);
      context.response().end(LASObjectJsons.serialize(new FindMsg<>(findMsg.size(), findMsg)));
    });
  }


}

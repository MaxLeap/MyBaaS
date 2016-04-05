package com.maxleap.las.bass.apiserver.resource;

import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.mongo.DateUtils;
import com.maxleap.exception.LASException;
import com.maxleap.las.bass.apiserver.web.JsonStringBuilder;
import com.maxleap.pandora.core.exception.LASDataException;
import com.maxleap.pandora.core.lasdata.LASClassSchema;
import com.maxleap.pandora.core.lasdata.LASKeyInfo;
import com.maxleap.pandora.core.mongo.MongoUpdate;
import com.maxleap.pandora.data.support.ClassSchemaManager;
import com.maxleap.pandora.data.support.MongoEntityManager;
import com.maxleap.pandora.data.support.MongoJsons;
import com.maxleap.pandora.data.support.lasdata.UpdateResult;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sneaky
 * @since 1.0.0
 */
@Singleton
@Path("/2.0/schemas")
public class ClassSchemaResource {
  private MongoEntityManager mongoEntityManager;
  private ClassSchemaManager classSchemaManager;

  @Inject
  public ClassSchemaResource(MongoEntityManager mongoEntityManager,
                             ClassSchemaManager classSchemaManager) {
    this.mongoEntityManager = mongoEntityManager;
    this.classSchemaManager = classSchemaManager;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public void create(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      LASClassSchema schema = MongoJsons.deserialize(ctx.getBodyAsString(), LASClassSchema.class);
      String className = schema.getClassName();
      if ("User".equals(className) || "Installation".equals(className) || "Role".equals(className)) {
        throw new LASException(LASException.INTERNAL_SERVER_ERROR, "className name is reserved");
      }
      if (className != null && className.startsWith("_sys")) {
        throw new LASException(LASException.INTERNAL_SERVER_ERROR, "Invalid className. className can't be start with _sys");
      }

      Map<String, LASKeyInfo> keys = schema.getKeys();
      if (keys != null) {
        keys.forEach((k, v) -> validation(v));
      }

      schema.setAppId(appId);
      LASClassSchema res = classSchemaManager.create(schema);
      JsonStringBuilder createdResponse = JsonStringBuilder.create().writeString("objectId", res.getObjectId().toString()).writeString("createdAt", DateUtils.encodeDate(new Date(res.getCreatedAt())));
      ctx.response().end(createdResponse.build());
    });
  }

  @PUT
  @Path(":className/addKey")
  @Consumes(MediaType.APPLICATION_JSON)
  public void addKey(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      LASClassSchema schema = MongoJsons.deserialize(ctx.getBodyAsString(), LASClassSchema.class);
      Map<String, LASKeyInfo> keys = schema.getKeys();
      if (keys == null) {
        throw new LASDataException(LASDataException.INTERNAL_SERVER_ERROR, "keys must be null");
      }
      Map<String, Object> transformKeys = new HashMap<>();
      keys.forEach((k, v) -> transformKeys.put("keys." + k, validation(v)));
      long updatedAt = System.currentTimeMillis();
      MongoUpdate update = new MongoUpdate().setAll(transformKeys).set("updatedAt", updatedAt);
      int res = classSchemaManager.update(appId, ctx.request().getParam("className"), update);
      context.response().end(MongoJsons.serialize(new UpdateResult(res, updatedAt)));
    });
  }

  @PUT
  @Path(":className/delKey")
  @Consumes(MediaType.APPLICATION_JSON)
  public void delKey(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      JsonObject bodyAsJson = ctx.getBodyAsJson();
      JsonObject keys = bodyAsJson.getJsonObject("keys");
      if (keys == null) {
        throw new LASDataException(LASDataException.INTERNAL_SERVER_ERROR, "keys must be null");
      }
      List<String> transformKeys = new ArrayList<>();
      keys.forEach(k -> transformKeys.add("keys." + k.getKey()));
      long updatedAt = System.currentTimeMillis();
      MongoUpdate update = new MongoUpdate().unsetAll(transformKeys).set("updatedAt", updatedAt);
      int res = classSchemaManager.update(appId, ctx.request().getParam("className"), update);
      context.response().end(MongoJsons.serialize(new UpdateResult(res, updatedAt)));
    });
  }

  @GET
  @Path(":className")
  public void get(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      LASClassSchema schema = classSchemaManager.get(appId, ctx.request().getParam("className"));
      if (schema == null) {
        ctx.response().end("{}");
      } else {
        ctx.response().end(MongoJsons.serialize(schema));
      }
    });
  }

  @GET
  public void find(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      List<LASClassSchema> schemas = classSchemaManager.getAll(appId);
      List<LASObject> objects = schemas.parallelStream().filter(schema -> !schema.getClassName().toLowerCase().startsWith("_sys_")).map(schema -> {
        LASObject map = MongoJsons.deserialize(MongoJsons.serialize(schema), LASObject.class);
        map.put("count", mongoEntityManager.count(schema.getDbName(), schema.getCollectionName()));
        return map;
      }).sorted(Comparator.comparingLong(o -> o.getCreatedAt())).collect(Collectors.toList());
      ctx.response().end(MongoJsons.serialize(objects));
    });
  }

  @DELETE
  @Path(":className")
  public void deleteByClassName(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> ctx.response().end(JsonStringBuilder.create().writeNumber("number", classSchemaManager.delete(appId, ctx.request().getParam("className"))).build()));
  }

  @PUT
  @Path(":className/permissions")
  @Consumes(MediaType.APPLICATION_JSON)
  public void permissions(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      JsonObject body = ctx.getBodyAsJson();
      if (body == null) {
        throw new IllegalArgumentException("body must be not null");
      }
      long updatedAt = System.currentTimeMillis();
      MongoUpdate update = new MongoUpdate().set("clientPermission", body.getMap()).set("updatedAt", updatedAt);
      int res = classSchemaManager.update(appId, ctx.request().getParam("className"), update);
      context.response().end(MongoJsons.serialize(new UpdateResult(res, updatedAt)));
    });
  }

  private LASKeyInfo validation(LASKeyInfo keyInfo) {
    if (keyInfo.getType() == null || !keyInfo.isValidType()) {
      throw new LASException(LASException.INVALID_TYPE, "the " + keyInfo.getType() + " is invalid type.");
    }

    if ((keyInfo.isPointer() || keyInfo.isRelation()) && StringUtils.isEmpty(keyInfo.getClassName())) {
      throw new LASException(LASException.INVALID_TYPE, "the " + keyInfo.getType() + " is invalid type. className must be specified.");
    }
    return keyInfo;
  }

}

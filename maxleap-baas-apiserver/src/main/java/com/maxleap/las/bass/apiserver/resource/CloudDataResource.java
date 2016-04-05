package com.maxleap.las.bass.apiserver.resource;

import com.maxleap.cloudcode.service.ZCloudCodeService;
import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.domain.mongo.DateUtils;
import com.maxleap.exception.LASException;
import com.maxleap.las.baas.CloudDataUpdateToLASUpdate;
import com.maxleap.las.baas.Constants;
import com.maxleap.las.bass.apiserver.web.JsonStringBuilder;
import com.maxleap.las.sdk.DeleteMsg;
import com.maxleap.las.sdk.SaveMsg;
import com.maxleap.las.sdk.UpdateMsg;
import com.maxleap.pandora.core.lasdata.LASQuery;
import com.maxleap.pandora.core.lasdata.LASUpdate;
import com.maxleap.pandora.core.rest.LASQueryBuilder;
import com.maxleap.pandora.data.support.LASDataEntityManager;
import com.maxleap.pandora.data.support.MongoJsons;
import com.maxleap.pandora.data.support.lasdata.UpdateResult;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

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
@Path("/2.0/classes")
public class CloudDataResource {
  private LASDataEntityManager lasDataEntityManager;
  private ZCloudCodeService cloudCodeService;

  @Inject
  public CloudDataResource(LASDataEntityManager lasDataEntityManager, ZCloudCodeService cloudCodeService) {
    this.lasDataEntityManager = lasDataEntityManager;
    this.cloudCodeService = cloudCodeService;
  }

  @POST
  @Path(":className")
  @Consumes(MediaType.APPLICATION_JSON)
  public void create(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, className, principal) -> {
      LASObject doc = MongoJsons.deserialize(ctx.getBodyAsString(), LASObject.class);
      //TODO code move to filter
//      Map<String, Object> creatorMap = Maps.newHashMap();
//      creatorMap.put("type", principal.getIdentityType().getCreatorType());
//      creatorMap.put("id", principal.getId());
//      Map<String, Object> aclMap = Maps.newHashMap();
//      aclMap.put("creator", creatorMap);
//      object.put("ACL", aclMap);
      if (cloudCode) {
        SaveMsg saveMsg = cloudCodeService.invokeCreate(appId.toString(), className, doc, null);
        ctx.response().end(MongoJsons.serialize(saveMsg));
      } else {
        LASObject created = lasDataEntityManager.create(appId, className, principal, doc);
        JsonStringBuilder createdResponse = JsonStringBuilder.create().writeString("objectId", created.getObjectId().toString()).writeString("createdAt", DateUtils.encodeDate(new Date(created.getCreatedAt())));
        ctx.response().end(createdResponse.build());
      }
    });
  }

  @GET
  @Path(":className/:objectId")
  public void get(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, className, principal) -> {
      LASObject doc = lasDataEntityManager.get(appId, className, principal, new ObjectId(context.request().getParam("objectId")));
      if (doc == null) {
        ctx.response().end("{}");
      } else {
        ctx.response().end(MongoJsons.serialize(doc));
      }
    });
  }

  @PUT
  @Path(":className/:objectId")
  @Consumes(MediaType.APPLICATION_JSON)
  public void update(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, className, principal) -> {
      Map update = MongoJsons.deserialize(ctx.getBodyAsString(), Map.class);
      ObjectId objectId = new ObjectId(context.request().getParam("objectId"));
      if (cloudCode) {
        LASUpdate lasUpdate = new LASUpdate(update);
        UpdateMsg updateMsg = cloudCodeService.invokeUpdate(appId.toString(),className,objectId.toString(),lasUpdate,null);
        context.response().end(MongoJsons.serialize(updateMsg));
      } else {
        UpdateResult result = lasDataEntityManager.updateWithIncResult(appId, className, principal, objectId, CloudDataUpdateToLASUpdate.from(update));
        context.response().end(MongoJsons.serialize(result));
      }
    });
  }

  @DELETE
  @Path(":className/:objectId")
  public void deleteById(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, className, principal) -> {
      ObjectId objectId = new ObjectId(context.request().getParam("objectId"));
      if (cloudCode) {
        DeleteMsg deleteMsg = cloudCodeService.invokeDelete(appId.toString(),className,objectId.toString(),null);
        context.response().end(MongoJsons.serialize(deleteMsg));
      } else {
        int number = lasDataEntityManager.delete(appId, className, principal, objectId);
        ctx.response().end("{\"number\": " + number +"}");
      }
    });
  }

  @POST
  @Path(":className/delete")
  @Consumes(MediaType.APPLICATION_JSON)
  public void deleteByIds(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, className, principal) -> {
      JsonObject idsJson = context.getBodyAsJson();
      Object objectIds = idsJson.getValue("objectIds");
      if (objectIds == null || !(objectIds instanceof JsonArray)) {
        throw new LASException(LASException.CUSTOM_INVALID_PARAMETER, "objectIds must be array.");
      }
      List<String> objectIdList = ((JsonArray) objectIds).getList();
      int size = objectIdList.size();
      if (size > 1000) {
        throw new LASException(LASException.CUSTOM_INVALID_PARAMETER, "delete batch size max limit 1000");
      }
      List ids = new ArrayList(size);
      objectIdList.forEach(v ->  ids.add(new ObjectId(v)));
      if (cloudCode) {
        DeleteMsg deleteMsg = cloudCodeService.invokeDelete(appId.toString(), className, ids, null);
        context.response().end(MongoJsons.serialize(deleteMsg));
      } else {
        LASQuery query = new LASQuery();
        query.in("objectId", ids);
        int number = lasDataEntityManager.delete(appId, className, principal, query);
        ctx.response().end("{\"number\": " + number +"}");
      }
    });
  }

  @GET
  @Path(":className")
  public void query(RoutingContext context) {
    queryCall(context, false);
  }

  @POST
  @Path(":className/query")
  @Consumes(MediaType.APPLICATION_JSON)
  public void postQuery(RoutingContext context) {
    queryCall(context, true);
  }

  private void queryCall(RoutingContext context, boolean post) {
    func(context, (ctx, cloudCode, appId, className, principal) -> {
      LASQuery query = null;
      Integer count = null;
      if (post) {
        JsonObject params = context.getBodyAsJson();
        count = params.getInteger("count");
        LASQueryBuilder queryBuilder = LASQueryBuilder.start(params.getString("where"));
        queryBuilder.orders(params.getString("order"))
            .keys(params.getString("keys"))
            .excludeKeys(params.getString("excludeKeys"))
            .include(params.getString("include"))
            .limit(params.getInteger("limit"))
            .skip(params.getInteger("skip"));
        query = queryBuilder.build();
      } else {
        HttpServerRequest request = context.request();
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
      }

      JsonStringBuilder findResponse = JsonStringBuilder.create();
      //count
      if (count != null && count > 0) {
        long result = lasDataEntityManager.count(appId, className, principal, query);
        findResponse.writeNumber("count", result);
        if (query.getQueryOptions().getLimit() == 0) {
          findResponse.writeObject("results", new ArrayList<>());
        } else {
          List<LASObject> docs = lasDataEntityManager.find(appId, className, principal, query);
          findResponse.writeObject("results", docs);
        }
      } else {
        findResponse.writeNumber("count", -1);
        List<LASObject> docs = lasDataEntityManager.find(appId, className, principal, query);
        findResponse.writeObject("results", docs);
      }

      ctx.response().end(findResponse.build());
    });
  }

  @POST
  @Path("/batch")
  @Consumes(MediaType.APPLICATION_JSON)
  public void batch(RoutingContext context) {
    func(context, (ctx, cloudCode, appId, className, principal) -> {
      Map batch = MongoJsons.deserialize(ctx.getBodyAsString(), Map.class);
      if (!(batch.get("requests") instanceof ArrayList)) {
        throw new LASException(LASException.INTERNAL_SERVER_ERROR, batch + ": invalid. because[requests must be Array type]");
      }

      ArrayList requests = (ArrayList) batch.get("requests");
      if (requests == null) {
        throw new LASException(1, "requests must be specified");
      }

      if (requests.size() > 50) {
        throw new LASException(LASException.INTERNAL_SERVER_ERROR, "batch size must be less more than 50.");
      }

      List<ZCloudBatchRequest> batchRequests = batchParse(requests);
      List<Map> responses = batchRequests.parallelStream().map(request -> {
        String method = request.getMethod();
        Map response = new HashMap();
        try {
          if (method.equals("POST")) {
            LASObject obj = lasDataEntityManager.create(appId, request.getClassName(), principal, new LASObject(request.getBody()));
            response.put("objectId", obj.getObjectId().toHexString());
            response.put("createdAt", DateUtils.encodeDate(new Date(obj.getCreatedAt())));
          } else if (method.equals("PUT")) {
            UpdateResult updateResult = lasDataEntityManager.updateWithIncResult(appId, request.getClassName(), principal, new ObjectId(request.getId()), CloudDataUpdateToLASUpdate.from(request.getBody()));
            response.put("number", updateResult.getNumber());
            if (updateResult.getUpdatedAt() > 0) {
              response.put("updatedAt", DateUtils.encodeDate(new Date(updateResult.getUpdatedAt())));
            }
          } else if (method.equals("DELETE")) {
            Integer deleteResult = lasDataEntityManager.delete(appId, request.getClassName(), principal, new ObjectId(request.getId()));
            response.put("number", deleteResult);
          } else {
            throw new LASException(1, "unsupported method: " + method);
          }
        } catch (Exception e) {
          response = parseException(e);
        }
        return response;
      }).collect(Collectors.toList());

      ctx.response().end(MongoJsons.serialize(responses));
    });
  }

  private List<ZCloudBatchRequest> batchParse(ArrayList requests) {
    List<ZCloudBatchRequest> batch = new ArrayList<>();
    for (Object obj : requests) {
      if (obj instanceof Map) {
        Map map = (Map) obj;
        String className = null;
        String id = null;

        if (map.get("method") == null) {
          throw new LASException(LASException.INTERNAL_SERVER_ERROR, obj + ": method must be specified");
        }
        String method = map.get("method").toString().toUpperCase();

        Object path = map.get("path");
        if (path == null) {
          throw new LASException(LASException.INTERNAL_SERVER_ERROR, obj + ": path must be specified");
        }

        String pathString = path.toString();
        int index = pathString.indexOf("classes/");
        if (index < 0) {
          error(obj + " path invalid");
        }

        if (method.equals("POST")) {
          className = pathString.substring(index + 8);
        } else if (method.equals("DELETE") || method.equals("PUT")) {
          int end = pathString.lastIndexOf("/");
          if (end < 0) {
            error(obj + " method invalid");
          }

          className = pathString.substring(index + 8, end);
          id = pathString.substring(end + 1);
          ObjectId.validate("Syntax error.near$" + obj, id);
        } else {
          error(obj + " method invalid");
        }

        Object body = map.get("body");
        if (method == null && !(body instanceof Map)) {
          error(body);
        }

        batch.add(new ZCloudBatchRequest(method, className, id, (Map) body));

      } else {
        error(obj);
      }
    }
    return batch;
  }

  private void error(Object obj) {
    throw new LASException(LASException.INTERNAL_SERVER_ERROR, "requests invalid. because[Syntax error.near$ " + obj + "]");
  }

  private Map parseException(Exception e) {
    Map map = new HashMap();
    if (e instanceof LASException) {
      LASException exception = (LASException) e;
      map.put("errorCode", exception.getCode());
      map.put("errorMessage", exception.getMessage());
    } else {
      map.put("errorCode", 1);
      map.put("errorMessage", e.getMessage());
    }
    return map;
  }

  public static class ZCloudBatchRequest {
    private String method;
    private String className;
    private String id;
    private Map body;

    public ZCloudBatchRequest(String method, String className, String id, Map body) {
      this.method = method;
      this.className = className;
      this.id = id;
      this.body = body;
    }

    public String getMethod() {
      return this.method;
    }

    public void setMethod(String method) {
      this.method = method;
    }

    public String getClassName() {
      return this.className;
    }

    public void setClassName(String className) {
      this.className = className;
    }

    public String getId() {
      return this.id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public Map getBody() {
      return this.body;
    }

    public void setBody(Map body) {
      this.body = body;
    }
  }

  private void assertCanAccessInnerClass(String className, LASPrincipal principal) {
    if (className != null && className.startsWith("_")) {
      //TODO must be MASTER KEY can be access
    }
  }

  private void func(RoutingContext ctx, Func func) {
    LASPrincipal principal = ctx.get(Constants.LAS_PRINCIPAL);
    String className = ctx.request().getParam("className");
    assertCanAccessInnerClass(className, principal);
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
    func.func(ctx, i == 1,  appId, className, principal);
  }

  public interface Func {
    public void func(RoutingContext ctx, boolean cloudCode, ObjectId appId, String className, LASPrincipal principal);
  }

}

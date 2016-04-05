package com.maxleap.las.bass.apiserver.resource;

import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.domain.mongo.DateUtils;
import com.maxleap.exception.LASException;
import com.maxleap.las.baas.CloudDataUpdateToLASUpdate;
import com.maxleap.las.baas.Constants;
import com.maxleap.pandora.data.support.LASDataEntityManager;
import com.maxleap.pandora.data.support.MongoJsons;
import com.maxleap.pandora.data.support.lasdata.UpdateResult;
import io.vertx.ext.web.RoutingContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sneaky
 * @since 1.0.0
 */
@Singleton
@Path("/2.0")
public class CloudDataBatchResource {
  private LASDataEntityManager lasDataEntityManager;

  @Inject
  public CloudDataBatchResource(LASDataEntityManager lasDataEntityManager) {
    this.lasDataEntityManager = lasDataEntityManager;
  }

  @POST
  @Path("batch")
  @Consumes(MediaType.APPLICATION_JSON)
  public void batch(RoutingContext context) {
    func(context, (ctx, appId, principal) -> {
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
            response.putAll(updateResult.getResult());
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

  private void func(RoutingContext ctx, Func func) {
    LASPrincipal principal = ctx.get(Constants.LAS_PRINCIPAL);
    ObjectId appId = ResourceUtils.getAppIdFormHeader(ctx);
    func.func(ctx,  appId, principal);
  }

  public interface Func {
    public void func(RoutingContext ctx, ObjectId appId, LASPrincipal principal);
  }

}

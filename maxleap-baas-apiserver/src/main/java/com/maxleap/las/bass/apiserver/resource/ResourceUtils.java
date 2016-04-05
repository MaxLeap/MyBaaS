package com.maxleap.las.bass.apiserver.resource;

import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.exception.LASException;
import com.maxleap.las.baas.Constants;
import io.vertx.ext.web.RoutingContext;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class ResourceUtils {
  public final static ObjectId getAppIdFormHeader(RoutingContext ctx) {
    String appId = ctx.request().headers().get(Constants.HEADER_MAXLEAP_APPID);
    if (appId == null) {
      appId = ctx.request().headers().get(Constants.HEADER_LAS_MAXLEAP_APPID);
    }
    if (appId == null) {
      throw new LASException(1, "header param: " + Constants.HEADER_MAXLEAP_APPID + " must be specified.");
    }
    if (ObjectId.isValid(appId)) {
      return new ObjectId(appId);
    } else {
      throw new LASException(1, "appId is invalid");
    }
  }

  public static void handle(RoutingContext ctx, Func func) {
    LASPrincipal principal = ctx.get(Constants.LAS_PRINCIPAL);
    ObjectId appId = getAppIdFormHeader(ctx);
    func.handle(ctx, appId, principal);
  }

}

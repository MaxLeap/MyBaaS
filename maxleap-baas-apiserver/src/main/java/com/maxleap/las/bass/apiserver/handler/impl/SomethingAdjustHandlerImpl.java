package com.maxleap.las.bass.apiserver.handler.impl;

import com.maxleap.las.baas.Constants;
import com.maxleap.las.bass.apiserver.handler.SomethingAdjustHandler;
import io.vertx.ext.web.RoutingContext;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class SomethingAdjustHandlerImpl implements SomethingAdjustHandler {
  @Override
  public void handle(RoutingContext context) {
    //when callback request.
    String lasAppId = context.request().getParam("LASAppId");
    if (lasAppId != null) {
      context.request().headers().add(Constants.HEADER_MAXLEAP_APPID, lasAppId);
    }

    context.addHeadersEndHandler(v -> {
      //cross-domain for ajax request
      context.response().headers().add("Access-Control-Allow-Origin", "*");
      context.response().headers().add("Access-Control-Allow-Headers", "X-LAS-AppId, X-LAS-APIKey, X-LAS-MasterKey, X-LAS-Hash, X-LAS-Version, X-LAS-Session-Token, Content-Type");
      context.response().headers().add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
    });

    context.next();
  }
}

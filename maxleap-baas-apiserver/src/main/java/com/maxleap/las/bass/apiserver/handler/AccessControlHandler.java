package com.maxleap.las.bass.apiserver.handler;

import com.maxleap.cerberus.acl.spi.AccessControlService;
import com.maxleap.las.bass.apiserver.handler.impl.AccessControlHandlerImpl;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * @author sneaky
 * @since 1.0.0
 */
public interface AccessControlHandler extends Handler<RoutingContext> {
  static AccessControlHandler create(AccessControlService accessControlService, JsonObject config) {
    return new AccessControlHandlerImpl(accessControlService, config);
  }
}

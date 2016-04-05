package com.maxleap.las.bass.apiserver.handler;

import com.maxleap.las.bass.apiserver.handler.impl.SomethingAdjustHandlerImpl;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * @author sneaky
 * @since 1.0.0
 */
public interface SomethingAdjustHandler extends Handler<RoutingContext> {
  static SomethingAdjustHandlerImpl create() {
    return new SomethingAdjustHandlerImpl();
  }
}

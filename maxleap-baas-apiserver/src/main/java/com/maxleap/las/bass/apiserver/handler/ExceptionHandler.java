package com.maxleap.las.bass.apiserver.handler;

import com.maxleap.las.bass.apiserver.handler.impl.ExceptionHandlerImpl;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * @author sneaky
 * @since 1.0.0
 */
public interface ExceptionHandler extends Handler<RoutingContext> {
  static ExceptionHandler create() {
    return new ExceptionHandlerImpl();
  }
}

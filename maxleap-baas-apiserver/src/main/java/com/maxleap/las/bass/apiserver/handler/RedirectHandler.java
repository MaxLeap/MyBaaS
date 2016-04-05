package com.maxleap.las.bass.apiserver.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import com.maxleap.las.bass.apiserver.handler.impl.RedirectHandlerImpl;

/**
 * @author sneaky
 * @since 1.0.0
 */
public interface RedirectHandler extends Handler<RoutingContext> {
  static RedirectHandler create() {
    return new RedirectHandlerImpl();
  }
}

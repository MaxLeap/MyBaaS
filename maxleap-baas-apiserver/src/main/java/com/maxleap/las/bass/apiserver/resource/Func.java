package com.maxleap.las.bass.apiserver.resource;

import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.domain.base.ObjectId;
import io.vertx.ext.web.RoutingContext;

/**
 * @author sneaky
 * @since 1.0.0
 */
public interface Func {
  public void handle(RoutingContext ctx, ObjectId appId, LASPrincipal principal);
}

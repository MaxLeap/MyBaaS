package com.maxleap.las.bass.apiserver.resource;

import com.maxleap.domain.auth.LASPrincipal;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by shunlv on 16-2-16.
 */
public interface Func2 {
  void func(RoutingContext context, LASPrincipal lasPrincipal);
}

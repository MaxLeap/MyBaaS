package com.maxleap.las.bass.apiserver.handler.impl;

import com.maxleap.las.bass.apiserver.handler.RedirectHandler;
import io.vertx.ext.web.RoutingContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author sneaky
 * @since 1.0.0
 */
@Singleton
public class RedirectHandlerImpl implements RedirectHandler {
  private Map<String, String> resourceMappings = new HashMap<>();
  private final Pattern pattern;

  @Inject
  public RedirectHandlerImpl() {
    resourceMappings.put("classes/_User", "users");
    resourceMappings.put("classes/_Installation", "installations");
    resourceMappings.put("classes/_Product", "products");
    resourceMappings.put("classes/_Parameter", "cparams");
    pattern = Pattern.compile(resourceMappings.keySet().stream().collect(Collectors.joining("|")));
  }

  @Override
  public void handle(RoutingContext context) {
    String path = context.request().path();
    if ("/".equals(path)) {
      context.reroute("/dashboard");
      return;
    }
    Matcher matcher = pattern.matcher(path);
    if (matcher.find()) {
      String childPath = matcher.group();
      context.reroute(path.replace(childPath, resourceMappings.get(childPath)));
    } else {
      context.next();
    }
  }

}

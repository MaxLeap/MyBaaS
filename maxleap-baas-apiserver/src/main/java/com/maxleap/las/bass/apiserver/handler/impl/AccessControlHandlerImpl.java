package com.maxleap.las.bass.apiserver.handler.impl;

import com.maxleap.cerberus.acl.spi.AccessControlService;
import com.maxleap.cerberus.acl.spi.AccessRequest;
import com.maxleap.domain.auth.LASAccessPair;
import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.pandora.core.exception.UnauthorizedException;
import com.maxleap.las.baas.Constants;
import com.maxleap.las.bass.apiserver.handler.AccessControlHandler;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class AccessControlHandlerImpl implements AccessControlHandler {
  private final AccessControlService accessControlService;
  private final Pattern whiteListPattern;

  public AccessControlHandlerImpl(AccessControlService accessControlService, JsonObject config) {
    this.accessControlService = accessControlService;
    this.whiteListPattern = Pattern.compile(Stream.of(config.getString("whiteList").split(",\\s*")).collect(Collectors.joining("|")));
  }

  @Override
  public void handle(RoutingContext context) {
    AccessRequest request = createLASAccessRequestFromRoutingContext(context);
    LASAccessPair accessPair;

    // first check white list
    if (inWhiteList(request.getPath())) {
      context.put(Constants.LAS_PRINCIPAL, new LASPrincipal(new HashSet<>()));
    } else if ((accessPair = accessControlService.authenticate(request)).isCanAccess()) {
      context.put(Constants.LAS_PRINCIPAL, accessPair.getLASPrincipal());
    } else {
      throw new UnauthorizedException("No permission");
    }
    context.next();
  }

  private AccessRequest createLASAccessRequestFromRoutingContext(RoutingContext context) {
    AccessRequest request = new AccessRequest();
    MultiMap headers = context.request().headers();
    String sign = headers.get(Constants.HEADER_MAXLEAP_REQUEST_SIGN);
    if (sign == null) {
      sign = headers.get(Constants.HEADER_LAS_MAXLEAP_REQUEST_SIGN);
    }
    request.setSign(sign);
    String appId = headers.get(Constants.HEADER_MAXLEAP_APPID);
    if (appId == null) {
      appId = headers.get(Constants.HEADER_LAS_MAXLEAP_APPID);
    }
    request.setAppId(appId);
    String apiKey = headers.get(Constants.HEADER_MAXLEAP_APIKEY);
    if (apiKey == null) {
      apiKey = headers.get(Constants.HEADER_LAS_MAXLEAP_APIKEY);
    }
    request.setApiKey(apiKey);
    String clientKey = headers.get(Constants.HEADER_MAXLEAP_CLIENTKEY);
    if (clientKey == null) {
      clientKey = headers.get(Constants.HEADER_LAS_MAXLEAP_CLIENTKEY);
    }
    request.setClientKey(clientKey);
    String masterKey = headers.get(Constants.HEADER_MAXLEAP_MASTERKEY);
    if (masterKey == null) {
      masterKey = headers.get(Constants.HEADER_LAS_MAXLEAP_MASTERKEY);
    }
    request.setMasterKey(masterKey);
    String sessionToken = headers.get(Constants.HEADER_MAXLEAP_SESSIONTOKEN);
    if (sessionToken == null) {
      sessionToken = headers.get(Constants.HEADER_LAS_SESSIONTOKEN);
    }
    request.setSessionToken(sessionToken);
    request.setPath(context.request().path());
    request.setMethod(request.getMethod());
    return request;
  }

  private boolean inWhiteList(String path) {
    if (path == null) {
      return true;
    }

    if (path.startsWith("/2.0/")) {
      path = path.substring(5);
    }

    return whiteListPattern.matcher(path).matches();
  }

}

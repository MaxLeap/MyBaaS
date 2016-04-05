package com.maxleap.code.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.maxleap.code.Logger;
import com.maxleap.code.LoggerFactory;
import io.vertx.core.http.HttpServerResponse;


/**
 *
 */
class ResponseHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ResponseHandler.class);

  void handle(MLResponse response, HttpServerResponse httpResponse) {
    try {
      if (response.getHeaders().size() > 0) {
        response.getHeaders().forEach((key,value) -> {
          httpResponse.putHeader(key.toString(),value.toString());
        });
      }
      if (response.succeeded()) {
        JavaType type = response.getResultType();
        String result;
        if (type.isPrimitive() ||
            type.getRawClass() == Integer.class ||
            type.getRawClass() == String.class ||
            type.getRawClass() == Long.class ||
            type.getRawClass() == Boolean.class ||
            type.getRawClass() == Float.class ||
            type.getRawClass() == Double.class) {
          result = response.getResult().toString();
        } else {
          result = MLJsonParser.asJson(response.getResult());
        }
        httpResponse.end(result,"UTF-8");
      } else {
        httpResponse.putHeader("content-type", "application/json; charset=utf-8");
        httpResponse.setStatusCode(545);
        httpResponse.setStatusMessage("cloud code exception");
        httpResponse.end(response.getError(),"UTF-8");
      }
    } catch (Exception e) {
      httpResponse.putHeader("content-type", "application/json; charset=utf-8");
      httpResponse.setStatusCode(545);
      httpResponse.setStatusMessage("cloud code exception");
      httpResponse.end(response.getError(),"UTF-8");
    }
  }

}

package com.maxleap.las.bass.apiserver.handler.impl;

import com.maxleap.exception.LASException;
import com.maxleap.las.bass.apiserver.handler.ExceptionHandler;
import com.maxleap.las.bass.apiserver.web.JsonStringBuilder;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * @author sneaky
 * @since 1.0.0
 */
@Singleton
public class ExceptionHandlerImpl implements ExceptionHandler {
  private final static Logger logger = LoggerFactory.getLogger(ExceptionHandlerImpl.class);

  @Override
  public void handle(RoutingContext context) {
    int code = 1;
    String msg = "";

    if (context.failed()) {
      Throwable failure = context.failure();
      if (failure != null) {
        if (failure instanceof LASException) {
          logger.error(failure.getMessage(), failure);
          code = ((LASException) failure).getCode();
        }
        msg = failure.getMessage();
      }
      int statusCode = context.statusCode();
      if (statusCode < 400) {
        context.response().setStatusCode(400);
      } else {
        context.response().setStatusCode(statusCode);
      }
      context.response().end(JsonStringBuilder.create().writeNumber("errorCode", code).writeString("errorMessage", msg).build());
    }
  }

}

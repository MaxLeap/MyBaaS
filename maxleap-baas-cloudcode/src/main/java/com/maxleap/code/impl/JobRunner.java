package com.maxleap.code.impl;

import com.maxleap.code.MLHandler;
import com.maxleap.code.Request;
import com.maxleap.code.Response;
import io.vertx.core.http.HttpServerResponse;


/**
 * Created by stream.
 */
public class JobRunner<T> implements Runnable {

  private MLHandler<Request, Response<T>> handler;
  private ResponseHandler responseHandler;
  private HttpServerResponse httpResponse;
  private Request request;
  private boolean running;

  public JobRunner(MLHandler<Request, Response<T>> handler, Request request, HttpServerResponse httpResponse, ResponseHandler responseHandler) {
    this.handler = handler;
    this.request = request;
    this.responseHandler = responseHandler;
    this.httpResponse = httpResponse;
    this.running = true;
  }

  //TODO 超时机制

  public boolean isRunning() {
    return running;
  }

  @Override
  public void run() {
    responseHandler.handle((MLResponse<T>) handler.handle(request), httpResponse);
    running = false;
  }
}

package com.maxleap.pandora.data.support.mongo;

/**
 * Data service messages consist of requests from client to server and responses
 * from server to client.
 * <pre>
 *     message   = Request | Response     ; messages
 * </pre>
 * User: qinpeng
 * Date: 14-4-28
 * Time: 11:28
 */
public abstract class Response {
  protected MgoRequest request;

  public MgoRequest getRequest() {
    return request;
  }

  public void setRequest(MgoRequest request) {
    this.request = request;
  }
}

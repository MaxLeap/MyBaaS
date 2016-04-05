package com.maxleap.cloudcode.exception;

/**
 * Created by stream.
 */
public class CloudCodeException extends RuntimeException {
  private int code;

  public CloudCodeException(int code, String message) {
    super(message);
    this.code = code;
  }

  public CloudCodeException(String message) {
    super(message);
    this.code = 400;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }
}

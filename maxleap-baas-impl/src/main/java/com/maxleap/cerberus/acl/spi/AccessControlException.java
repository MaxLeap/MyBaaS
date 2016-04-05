package com.maxleap.cerberus.acl.spi;

/**
 * Created by shunlv on 16-2-3.
 */
public class AccessControlException extends RuntimeException {

  private int code = 401;

  public AccessControlException(int code, String msg, Throwable cause) {
    super(msg, cause);
    this.code = code;
  }

  public AccessControlException(int code, String message) {
    super(message);
    this.code = code;
  }

  public AccessControlException(String message) {
    super(message);
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }
}

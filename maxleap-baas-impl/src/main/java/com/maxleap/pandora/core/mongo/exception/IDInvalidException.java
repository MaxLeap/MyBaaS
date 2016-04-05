package com.maxleap.pandora.core.mongo.exception;

/**
 * If Id is mongo id:
 * <b>
 *     Consists of 12 bytes, divided as follows:
 *     1-4 is time,
 *     5-7 is machine,
 *     8-9 is pid,
 *     10-12 is inc
 * </b>
 *
 * @author sneaky
 * @see org.bson.types.ObjectId
 * @since 1.0.0
 */
public class IDInvalidException extends DataAccessException {
  /**
   * @param msg the detail message
   */
  public IDInvalidException(String msg) {
    super(118, msg);
  }

  /**
   * @param msg the detail message
   * @param cause the root cause
   */
  public IDInvalidException(String msg, Throwable cause) {
    super(msg, cause);
  }
}

package com.maxleap.pandora.core.mongo.exception;

/**
 * Exception occurred at data access from mongo.
 *
 * @author sneaky
 * @since 1.0.0
 */
public class MongoDataAccessException extends DataAccessException {

    /**
     * @param msg the detail message
     * @param cause the root cause
     */
    public MongoDataAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * @param msg the detail message
     *
     */
    public MongoDataAccessException(String msg) {
        super(msg);
    }

    /**
     *
     * @param code
     * @param msg the detail message
     * @param cause the root cause
     */
    public MongoDataAccessException(int code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    public MongoDataAccessException(int code, String msg) {
        super(code, msg);
    }
}

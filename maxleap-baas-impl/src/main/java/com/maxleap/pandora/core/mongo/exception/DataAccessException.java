package com.maxleap.pandora.core.mongo.exception;

import com.maxleap.pandora.core.exception.LASDataException;

/**
 * Root of the hierarchy of data access exceptions
 *
 * <p>This exception hierarchy aims to let user code find and handle the
 * kind of error encountered without knowing the details of the particular
 * data access API in use (e.g. Jedis).
 *
 * @author sneaky
 * @since 1.0.0
 */
public abstract class DataAccessException extends LASDataException {
    /**
     * @param msg the detail message
     */
    public DataAccessException(String msg) {
        super(LASDataException.DATA_ACCESS, msg);
    }

    /**
     * @param msg the detail message
     * @param cause the root cause (usually from using a underlying
     * data access API such as JDBC)
     */
    public DataAccessException(String msg, Throwable cause) {
        super(DATA_ACCESS, msg, cause);
    }

    /**
     *
     * @param code
     * @param msg the detail message
     * @param cause the root cause
     */
    public DataAccessException(int code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    public DataAccessException(int code, String msg) {
        super(code, msg);
    }
}

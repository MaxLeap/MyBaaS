package com.maxleap.pandora.core.mongo.exception;


import com.maxleap.pandora.core.exception.LASDataException;

/**
 * query exception.
 *
 * @author jing zhao
 * @date 4/17/14
 * @since 1.0
 */
public class QueryException extends LASDataException {
    /**
     * @param msg the detail message
     */
    public QueryException(String msg) {
        super(INVALID_QUERY, msg);
    }

    /**
     * @param msg the detail message
     * @param cause the root cause
     */
    public QueryException(int code, String msg, Throwable cause) {
        super(code, msg, cause);
    }
}

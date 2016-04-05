package com.maxleap.pandora.core.exception;


import com.maxleap.pandora.core.mongo.exception.DataAccessException;

/**
 *
 * @author sneaky
 * @since 3.0.0
 * @see com.maxleap.pandora.core.mongo.exception.DataAccessException
 */
public class MongoException extends DataAccessException {

    /**
     * @param msg the detail message
     */
    public MongoException(String msg) {
        super(DATA_ACCESS_MONGO, msg);
    }

    /**
     * @param msg the detail message
     * @param cause the root cause (usually from using a underlying
     * data access API such as JDBC)
     */
    public MongoException(String msg, Throwable cause) {
        super(DATA_ACCESS_MONGO, msg, cause);
    }

}

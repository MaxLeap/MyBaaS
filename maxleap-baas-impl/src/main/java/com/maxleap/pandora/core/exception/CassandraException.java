package com.maxleap.pandora.core.exception;

import com.maxleap.pandora.core.mongo.exception.DataAccessException;

/**
 *
 * @author sneaky
 * @since 3.0.0
 * @see com.maxleap.pandora.core.mongo.exception.DataAccessException
 */
public class CassandraException extends DataAccessException {

    /**
     * @param msg the detail message
     */
    public CassandraException(String msg) {
        super(DATA_ACCESS_CASSANDRA, msg);
    }

    /**
     * @param msg the detail message
     * @param cause the root cause (usually from using a underlying
     * data access API such as JDBC)
     */
    public CassandraException(String msg, Throwable cause) {
        super(DATA_ACCESS_CASSANDRA, msg, cause);
    }

}

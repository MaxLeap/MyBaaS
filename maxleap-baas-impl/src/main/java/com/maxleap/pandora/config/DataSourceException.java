package com.maxleap.pandora.config;


/**
 *
 * @author sneaky
 * @since 1.0.0
 */
public class DataSourceException extends RuntimeException {

    /**
     * @param msg the detail message
     */
    public DataSourceException(String msg) {
        super(msg);
    }

    /**
     * @param msg the detail message
     * @param cause the root cause (usually from using a underlying
     * data access API such as JDBC)
     */
    public DataSourceException(String msg, Throwable cause) {
        super(msg, cause);
    }

}

package com.maxleap.pandora.core.exception;

/**
 * Timeout exception.
 *
 * @author sneaky
 * @since 1.0.0
 */
public class OperationTimeOutException extends LASDataException {
    /**
     * @param msg the detail message
     */
    public OperationTimeOutException(String msg) {
        super(TIMEOUT, msg);
    }

    /**
     * @param msg the detail message
     * @param cause the root cause
     */
    public OperationTimeOutException(String msg, Throwable cause) {
        super(TIMEOUT, msg, cause);
    }
}

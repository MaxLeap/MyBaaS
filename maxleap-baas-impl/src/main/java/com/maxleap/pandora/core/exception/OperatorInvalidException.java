package com.maxleap.pandora.core.exception;

/**
 * Operator invalid.
 *
 * @author sneaky
 * @since 1.0
 */
public class OperatorInvalidException extends LASDataException {
    /**
     * @param msg the detail message
     */
    public OperatorInvalidException(String msg) {
        super(LASDataException.INVALID_QUERY, msg);
    }

    /**
     * @param msg the detail message
     * @param cause the root cause
     */
    public OperatorInvalidException(String msg, Throwable cause) {
        super(LASDataException.INVALID_QUERY, msg, cause);
    }
}

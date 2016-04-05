package com.maxleap.pandora.core.exception;

/**
 * Unauthorized Operations.
 *
 * @author sneaky
 * @since 1.0.0
 */
public class UnauthorizedException extends LASDataException {
    /**
     * @param msg the detail message
     */
    public UnauthorizedException(String msg) {
        super(LASDataException.UNAUTHORIZED, msg);
    }

    /**
     * @param msg the detail message
     * @param cause the root cause
     */
    public UnauthorizedException(int code, String msg, Throwable cause) {
        super(code, msg, cause);
    }
}

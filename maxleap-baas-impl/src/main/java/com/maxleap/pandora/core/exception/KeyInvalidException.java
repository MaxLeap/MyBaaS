package com.maxleap.pandora.core.exception;

/**
 * Entity keys invalid.
 *
 * @author sneaky
 * @since 1.0.0
 */
public class KeyInvalidException extends LASDataException {
    /**
     * @param msg the detail message
     */
    public KeyInvalidException(String msg) {
        super(LASDataException.INVALID_KEY_NAME, msg);
    }

    /**
     * @param code the detail code
     * @param msg the detail message
     */
    public KeyInvalidException(int code, String msg) {
        super(code, msg);
    }

    /**
     * @param msg the detail message
     * @param cause the root cause
     */
    public KeyInvalidException(String msg, Throwable cause) {
        super(LASDataException.INVALID_KEY_NAME, msg, cause);
    }

    /**
     * @param msg the detail message
     * @param cause the root cause
     */
    public KeyInvalidException(int code, String msg, Throwable cause) {
        super(code, msg, cause);
    }
}

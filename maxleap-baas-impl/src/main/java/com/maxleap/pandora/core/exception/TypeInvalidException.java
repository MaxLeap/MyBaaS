package com.maxleap.pandora.core.exception;

/**
 * If invalid type of data.
 *
 * @author sneaky
 * @since 1.0.0
 */
public class TypeInvalidException extends LASDataException {
    /**
     * @param msg the detail message
     */
    public TypeInvalidException(String msg) {
        super(INVALID_TYPE, msg);
    }

    /**
     * @param code the detail code
     * @param msg the detail message
     */
    public TypeInvalidException(int code, String msg) {
        super(code, msg);
    }

    /**
     * @param msg the detail message
     * @param cause the root cause
     */
    public TypeInvalidException(int code, String msg, Throwable cause) {
        super(code, msg, cause);
    }
}

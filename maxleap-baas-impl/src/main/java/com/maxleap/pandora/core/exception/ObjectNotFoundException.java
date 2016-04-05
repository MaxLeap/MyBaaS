package com.maxleap.pandora.core.exception;


/**
 * Object doesn't exist, or has an incorrect password
 *
 * @author sneaky
 * @since 1.0.0
 */
public class ObjectNotFoundException extends LASDataException {
    /**
     * @param msg the detail message
     */
    public ObjectNotFoundException(String msg) {
        super(LASDataException.OBJECT_NOT_FOUND, msg);
    }

    /**
     * @param msg the detail message
     * @param cause the root cause
     */
    public ObjectNotFoundException(int code, String msg, Throwable cause) {
        super(code, msg, cause);
    }
}

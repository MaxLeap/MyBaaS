package com.maxleap.pandora.core.exception;

/**
 * Parameter invalid
 *
 * @author sneaky
 * @since 1.0.0
 */
public class ParameterInvalidException extends LASDataException {
    /**
     * @param msg the detail message
     */
    public ParameterInvalidException(String msg) {
        super(LASDataException.INVALID_PARAMETER, msg);
    }

    /**
     * @param msg the detail message
     * @param cause the root cause
     */
    public ParameterInvalidException(int code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    public static void notNull(String name, Object o) {
        if (o == null) {
            throw new ParameterInvalidException( name + " must be not null");
        }
    }

}

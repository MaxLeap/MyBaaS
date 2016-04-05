package com.maxleap.pandora.core.exception;

/**
 * User: qinpeng
 * Date: 14-5-13
 * Time: 13:49
 */
public class BatchException extends Exception{
    public BatchException(String message){
        super(message);
    }

    public BatchException(String message, Throwable cause){
        super(message, cause);
    }
}

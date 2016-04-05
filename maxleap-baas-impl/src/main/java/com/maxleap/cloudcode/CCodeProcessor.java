package com.maxleap.cloudcode;

/**
 * User: yuyangning
 * Date: 8/11/14
 * Time: 3:24 PM
 */
public interface CCodeProcessor<T> {

  public T process(String message);

}

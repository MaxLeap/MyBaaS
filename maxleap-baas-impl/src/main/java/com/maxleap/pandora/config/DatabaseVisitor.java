package com.maxleap.pandora.config;

/**
 * @author sneaky
 * @since 3.2.0
 */
public interface DatabaseVisitor<T> {

  public T get(String ns);

}

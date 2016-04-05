package com.maxleap.pandora.config;

import java.util.List;

/**
 * @author sneaky
 * @since 3.2.0
 */
public interface ClusterVisitor<T> {

  public T get(String ns);

  public List<T> listAll();

}

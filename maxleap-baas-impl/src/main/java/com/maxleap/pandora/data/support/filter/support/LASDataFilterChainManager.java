package com.maxleap.pandora.data.support.filter.support;


import com.maxleap.domain.base.ObjectId;

import java.util.List;
import java.util.Map;

/**
 * A {@code FilterChainManager} manages the creation and modification of {@link com.maxleap.pandora.data.support.filter.support.Filter} chains from an available pool
 * of {@link com.maxleap.pandora.data.support.filter.support.Filter} instances.
 *
 * @author sneaky
 * @since 2.0.0
 */
public interface LASDataFilterChainManager {

  /**
   *
   * @return the pool of available {@code Filter}s managed by this manager, keyed by {@code name}.
   */
  Map<String, Filter> getFilters();

  void register(Filter filter);
  
  FilterChain getFilterChain(ObjectId appId, String className);

  void addFilterChain(ObjectId appId, List<Filter> filters);

  void addFilterChain(ObjectId appId, Filter filter);

  void addFilterChain(ObjectId appId, String className, List<Filter> filters);

  void addFilterChain(ObjectId appId, String className, Filter filter);

  List<Filter> getDefaultFilterChain();

  }

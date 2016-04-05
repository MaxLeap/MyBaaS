package com.maxleap.pandora.data.support.filter.support;

import java.util.List;
import java.util.Map;

/**
 * A {@code FilterChainManager} manages the creation and modification of {@link Filter} chains from an available pool
 * of {@link Filter} instances.
 *
 * @author sneaky
 * @since 2.0.0
 */
public interface MgoFilterChainManager {

  /**
   *
   * @return the pool of available {@code Filter}s managed by this manager, keyed by {@code name}.
   */
  Map<String, Filter> getFilters();

  void register(Filter filter);
  
  FilterChain getFilterChain(String db, String table);

  void addFilterChain(String db, List<Filter> filters);

  void addFilterChain(String db, Filter filter);

  void addFilterChain(String db, String table, List<Filter> filters);

  void addFilterChain(String db, String table, Filter filter);

  List<Filter> getDefaultFilterChain();

  }

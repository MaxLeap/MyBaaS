package com.maxleap.pandora.data.support.filter.support;

import com.maxleap.pandora.data.support.filter.TimestampFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Default {@link MgoFilterChainManager} implementation maintaining a map of {@link Filter Filter} instances
 * (key: filter name, value: Filter)
 *
 * @author sneaky
 * @since 3.0.0
 */
@Singleton
public class DefaultMgoFilterChainManager implements MgoFilterChainManager {
  private static transient final Logger log = LoggerFactory.getLogger(DefaultMgoFilterChainManager.class);

  private static ConcurrentMap<String, Filter> filters = new ConcurrentHashMap<>(); //pool of filters available for creating chains
  private static Map<String, DBFilterChain> dbFilterChainMap = new ConcurrentHashMap<>();

  static MgoFilterChainManager filterChainManager;
  List<Filter> defaultFilterChain = null;
  TimestampFilter timestampFilter;

  @Inject
  public DefaultMgoFilterChainManager(TimestampFilter timestampFilter) {
    this.timestampFilter = timestampFilter;
    initDefaultFilter();
    initDefaultFilterChain();
    filterChainManager = this;
    log.debug("default filters: {}", filters);
  }

  /**
   * If filter name is exists, do nothing.
   *
   * @param filter
   */
  @Override
  public void register(Filter filter) {
    filters.putIfAbsent(filter.name(), filter);
  }

  @Override
  public FilterChain getFilterChain(String db, String table) {
    DBFilterChain dbFilterChain = dbFilterChainMap.get(db);
    if (dbFilterChain == null) {
      return new FilterChainInvocation(defaultFilterChain);
    }
    if (table != null) {
      List<Filter> tableFilterChain = dbFilterChain.getTableFilterChain(table);
      if (tableFilterChain == null) {
        return new FilterChainInvocation(dbFilterChain.getFilters());
      } else {
        return new FilterChainInvocation(tableFilterChain);
      }
    } else {
      return new FilterChainInvocation(dbFilterChain.getFilters());
    }
  }

  @Override
  public void addFilterChain(String db, List<Filter> filters) {
    DBFilterChain dbFilterChain = dbFilterChainMap.get(db);
    if (dbFilterChain == null) {
      List<Filter> newFilters = new ArrayList<>();
      newFilters.addAll(defaultFilterChain);
      newFilters.addAll(filters);
      dbFilterChainMap.put(db, new DBFilterChain(db, newFilters));
    } else {
      dbFilterChain.addDBFilterChain(filters);
    }
  }

  @Override
  public void addFilterChain(String db, Filter filter) {
    DBFilterChain dbFilterChain = dbFilterChainMap.get(db);
    if (dbFilterChain == null) {
      List<Filter> newFilters = new ArrayList<>();
      newFilters.addAll(defaultFilterChain);
      newFilters.add(filter);
      dbFilterChainMap.put(db, new DBFilterChain(db, newFilters));
    } else {
      dbFilterChain.addDBFilterChain(filter);
    }
  }

  @Override
  public void addFilterChain(String db, String table, List<Filter> filters) {
    DBFilterChain dbFilterChain = dbFilterChainMap.get(db);
    if (dbFilterChain == null) {
      dbFilterChain = new DBFilterChain(db, defaultFilterChain);
      dbFilterChainMap.put(db, dbFilterChain);
      dbFilterChain.addTableFilterChain(table, defaultFilterChain);
      dbFilterChain.addTableFilterChain(table, filters);
    } else {
      List<Filter> tableFilterChain = dbFilterChain.getTableFilterChain(table);
      if (tableFilterChain == null) {
        dbFilterChain.addTableFilterChain(table, defaultFilterChain);
      }
      dbFilterChain.addTableFilterChain(table, filters);
    }
  }

  @Override
  public void addFilterChain(String db, String table, Filter filter) {
    DBFilterChain dbFilterChain = dbFilterChainMap.get(db);
    if (dbFilterChain == null) {
      dbFilterChain = new DBFilterChain(db, defaultFilterChain);
      dbFilterChainMap.put(db, dbFilterChain);
      dbFilterChain.addTableFilterChain(table, defaultFilterChain);
      dbFilterChain.addTableFilterChainLast(table, filter);
    } else {
      List<Filter> tableFilterChain = dbFilterChain.getTableFilterChain(table);
      if (tableFilterChain == null) {
        dbFilterChain.addTableFilterChain(table, defaultFilterChain);
      }
      dbFilterChain.addTableFilterChainLast(table, filter);
    }
  }

  @Override
  public Map<String, Filter> getFilters() {
    return filters;
  }

  @Override
  public List<Filter> getDefaultFilterChain() {
    return defaultFilterChain;
  }

  private void initDefaultFilterChain() {
    defaultFilterChain = new ArrayList<>();
    defaultFilterChain.add(filters.get(TimestampFilter.name));
  }

  private void initDefaultFilter() {
    register(timestampFilter);
  }

  public static MgoFilterChainManager getDefaultMgoFilterChainManager() {
    return filterChainManager;
  }

  public static class DBFilterChain {
    List<Filter> filters = null;
    Map<String, List<Filter>> tableFilterChain = new HashMap<>();
    String db;

    public DBFilterChain(String db, List<Filter> filters) {
      this.db = db;
      this.filters = filters;
    }

    public List<Filter> getFilters() {
      return filters;
    }

    List<Filter> getTableFilterChain(String table) {
      return tableFilterChain.get(table);
    }

    public void addDBFilterChain(List<Filter> filters) {
      this.filters.addAll(filters);
    }

    public void addDBFilterChain(Filter filter) {
      filters.add(filter);
    }

    public void addTableFilterChain(String table, List<Filter> filters) {
      List<Filter> filterList = tableFilterChain.get(table);
      if (filterList != null) {
        filterList.addAll(filters);
      } else {
        tableFilterChain.put(table, new ArrayList<>(filters));
      }
    }

    public void addTableFilterChainLast(String table, Filter filter) {
      List<Filter> tableFilterChain = this.tableFilterChain.get(table);
      if (tableFilterChain == null) {
        tableFilterChain = new ArrayList<>();
      }
      tableFilterChain.add(filter);
    }

    public void addTableFilterChainFirst(String table, Filter filter) {
      List<Filter> tableFilterChain = this.tableFilterChain.get(table);
      if (tableFilterChain == null) {
        tableFilterChain = new ArrayList<>();
        tableFilterChain.add(filter);
      } else {
        tableFilterChain.add(0, filter);
      }
    }
  }
}

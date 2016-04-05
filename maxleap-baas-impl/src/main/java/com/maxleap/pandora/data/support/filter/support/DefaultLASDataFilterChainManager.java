package com.maxleap.pandora.data.support.filter.support;

import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.data.support.filter.*;
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
 * Default {@link com.maxleap.pandora.data.support.filter.support.LASDataFilterChainManager} implementation maintaining a map of {@link com.maxleap.pandora.data.support.filter.support.Filter Filter} instances
 * (key: filter name, value: Filter)
 *
 * @author sneaky
 * @since 2.0.0
 */
@Singleton
public class DefaultLASDataFilterChainManager implements LASDataFilterChainManager {
  private static transient final Logger log = LoggerFactory.getLogger(DefaultLASDataFilterChainManager.class);

  private static ConcurrentMap<String, Filter> filters = new ConcurrentHashMap<>(); //pool of filters available for creating chains
  private static Map<ObjectId, DBFilterChain> dbFilterChainMap = new ConcurrentHashMap<>();
  List<Filter> defaultFilterChain = null;

  static LASDataFilterChainManager filterChainManager;

  ACLFilter aclFilter;
  AppDataConvertFilter appDataConvertFilter;
  ClassPermissionFilter classPermissionFilter;
  ClassSchemaAdjustFilter classSchemaAdjustFilter;
  DataDecoratorFilter dataDecoratorFilter;
  DataValidateFilter dataValidateFilter;
  NativeDataDecoratorFilter nativeDataDecoratorFilter;
  SchemaBindToFilter schemaBindToFilter;
  TimestampFilter timestampFilter;

  @Inject
  public DefaultLASDataFilterChainManager(ACLFilter aclFilter,
                                          AppDataConvertFilter appDataConvertFilter,
                                          ClassPermissionFilter classPermissionFilter,
                                          ClassSchemaAdjustFilter classSchemaAdjustFilter,
                                          DataDecoratorFilter dataDecoratorFilter,
                                          DataValidateFilter dataValidateFilter,
                                          NativeDataDecoratorFilter nativeDataDecoratorFilter,
                                          SchemaBindToFilter schemaBindToFilter,
                                          TimestampFilter timestampFilter) {
      this.aclFilter = aclFilter;
      this.appDataConvertFilter = appDataConvertFilter;
      this.classPermissionFilter = classPermissionFilter;
      this.classSchemaAdjustFilter = classSchemaAdjustFilter;
      this.dataDecoratorFilter = dataDecoratorFilter;
      this.dataValidateFilter = dataValidateFilter;
      this.nativeDataDecoratorFilter = nativeDataDecoratorFilter;
      this.schemaBindToFilter = schemaBindToFilter;
      this.timestampFilter = timestampFilter;

      initDefaultFilter();
      initDefaultFilterChain();
      log.debug("default filters: {}", filters);
  }

  @Override
  public Map<String, Filter> getFilters() {
      return filters;
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
  public FilterChain getFilterChain(ObjectId appId, String className) {
    DBFilterChain dbFilterChain = dbFilterChainMap.get(appId);
    if (dbFilterChain == null) {
      return new FilterChainInvocation(defaultFilterChain);
    }
    if (className != null) {
      List<Filter> tableFilterChain = dbFilterChain.getTableFilterChain(className);
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
  public void addFilterChain(ObjectId appId, List<Filter> filters) {
    DBFilterChain dbFilterChain = dbFilterChainMap.get(appId);
    if (dbFilterChain == null) {
      List<Filter> newFilters = new ArrayList<>();
      newFilters.addAll(defaultFilterChain);
      newFilters.addAll(filters);
      dbFilterChainMap.put(appId, new DBFilterChain(appId, newFilters));
    } else {
      dbFilterChain.addDBFilterChain(filters);
    }
  }

  @Override
  public void addFilterChain(ObjectId appId, Filter filter) {
    DBFilterChain dbFilterChain = dbFilterChainMap.get(appId);
    if (dbFilterChain == null) {
      List<Filter> newFilters = new ArrayList<>();
      newFilters.addAll(defaultFilterChain);
      newFilters.add(filter);
      dbFilterChainMap.put(appId, new DBFilterChain(appId, newFilters));
    } else {
      dbFilterChain.addDBFilterChain(filter);
    }
  }

  @Override
  public void addFilterChain(ObjectId appId, String className, List<Filter> filters) {
    DBFilterChain dbFilterChain = dbFilterChainMap.get(appId);
    if (dbFilterChain == null) {
      dbFilterChain = new DBFilterChain(appId, defaultFilterChain);
      dbFilterChainMap.put(appId, dbFilterChain);
      dbFilterChain.addTableFilterChain(className, defaultFilterChain);
      dbFilterChain.addTableFilterChain(className, filters);
    } else {
      List<Filter> tableFilterChain = dbFilterChain.getTableFilterChain(className);
      if (tableFilterChain == null) {
        dbFilterChain.addTableFilterChain(className, defaultFilterChain);
      }
      dbFilterChain.addTableFilterChain(className, filters);
    }
  }

  @Override
  public void addFilterChain(ObjectId appId, String className, Filter filter) {
    DBFilterChain dbFilterChain = dbFilterChainMap.get(appId);
    if (dbFilterChain == null) {
      dbFilterChain = new DBFilterChain(appId, defaultFilterChain);
      dbFilterChainMap.put(appId, dbFilterChain);
      dbFilterChain.addTableFilterChain(className, defaultFilterChain);
      dbFilterChain.addTableFilterChainLast(className, filter);
    } else {
      List<Filter> tableFilterChain = dbFilterChain.getTableFilterChain(className);
      if (tableFilterChain == null) {
        dbFilterChain.addTableFilterChain(className, defaultFilterChain);
      }
      dbFilterChain.addTableFilterChainLast(className, filter);
    }
  }

  @Override
  public List<Filter> getDefaultFilterChain() {
    return defaultFilterChain;
  }

  private void initDefaultFilterChain() {
    defaultFilterChain = new ArrayList<>();
    defaultFilterChain.add(filters.get(ClassPermissionFilter.name));
    defaultFilterChain.add(filters.get(SchemaBindToFilter.name));
//    defaultFilterChain.add(filters.get(ACLFilter.name));
    defaultFilterChain.add(filters.get(AppDataConvertFilter.name));
    defaultFilterChain.add(filters.get(DataValidateFilter.name));
    defaultFilterChain.add(filters.get(ClassSchemaAdjustFilter.name));
    defaultFilterChain.add(filters.get(DataDecoratorFilter.name));
    defaultFilterChain.add(filters.get(TimestampFilter.name));
  }

  private void initDefaultFilter() {
    register(aclFilter);
    register(appDataConvertFilter);
    register(classPermissionFilter);
    register(classSchemaAdjustFilter);
    register(dataDecoratorFilter);
    register(dataValidateFilter);
    register(nativeDataDecoratorFilter);
    register(schemaBindToFilter);
    register(timestampFilter);
//        register(cCodeFilter.name(), cCodeFilter);
  }

  public static LASDataFilterChainManager getDefaultFilterChainManager() {
    return filterChainManager;
  }

  public static class DBFilterChain {
    List<Filter> filters = null;
    Map<String, List<Filter>> tableFilterChain = new HashMap<>();
    ObjectId appId;

    public DBFilterChain(ObjectId appId, List<Filter> filters) {
      this.appId = appId;
      this.filters = filters;
    }

    public List<Filter> getFilters() {
      return filters;
    }

    List<Filter> getTableFilterChain(String className) {
      return tableFilterChain.get(className);
    }

    public void addDBFilterChain(List<Filter> filters) {
      this.filters.addAll(filters);
    }

    public void addDBFilterChain(Filter filter) {
      filters.add(filter);
    }

    public void addTableFilterChain(String className, List<Filter> filters) {
      List<Filter> filterList = tableFilterChain.get(className);
      if (filterList != null) {
        filterList.addAll(filters);
      } else {
        tableFilterChain.put(className, new ArrayList<>(filters));
      }
    }

    public void addTableFilterChainLast(String className, Filter filter) {
      List<Filter> tableFilterChain = this.tableFilterChain.get(className);
      if (tableFilterChain == null) {
        tableFilterChain = new ArrayList<>();
      }
      tableFilterChain.add(filter);
    }

    public void addTableFilterChainFirst(String className, Filter filter) {
      List<Filter> tableFilterChain = this.tableFilterChain.get(className);
      if (tableFilterChain == null) {
        tableFilterChain = new ArrayList<>();
        tableFilterChain.add(filter);
      } else {
        tableFilterChain.add(0, filter);
      }
    }
  }
}

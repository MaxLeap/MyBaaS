package com.maxleap.pandora.core.lasdata;

import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.Direction;
import com.maxleap.pandora.core.lasdata.types.LASGeoPoint;
import com.maxleap.pandora.core.lasdata.types.LASPointer;
import com.maxleap.pandora.core.mongo.MongoQuery;
import com.maxleap.pandora.core.mongo.MongoQueryOptions;

import java.util.*;

/**
 * @author sneaky
 * @since 3.0.0
 */
public class LASQuery extends MongoQuery {
  String includes;
  boolean count;
  boolean relations;

  public LASQuery() {
  }

  public LASQuery(Map query) {
    this.query = query;
  }

  @Override
  protected void addOperand(String key, String operator, Object value) {
    if ("objectId".equals(key)) {
      if (value instanceof ObjectId) {
        //do nothing
      } else if (value instanceof String) {
        value = new ObjectId(value.toString());
      } else if (value instanceof List) {
        List list = (List) value;
        List newList = new ArrayList<>();
        for (Object id : list) {
          if (id instanceof String) {
            newList.add(new ObjectId(id.toString()));
          } else {
            newList.add(id);
          }
        }
        value = newList;
      } else if (value instanceof Set) {
        Set set = (Set) value;
        List newList = new ArrayList<>();
        for (Object id : set) {
          if (id instanceof String) {
            newList.add(new ObjectId(id.toString()));
          } else {
            newList.add(id);
          }
        }
        value = newList;
      } else if (value instanceof Object[]) {
        Object[] set = (Object[]) value;
        List newList = new ArrayList<>();
        for (Object id : set) {
          if (id instanceof String) {
            newList.add(new ObjectId(id.toString()));
          } else {
            newList.add(id);
          }
        }
        value = newList;
      }
    }
    super.addOperand(key, operator, value);
  }

  public <T> LASQuery arrayAll(String key, List<T> values) {
    addOperand(key, "$all", values);
    return this;
  }

  public LASQuery arrayAllWithElemMatch(String key, EmbeddedElemMatcher... filters) {
    List<Map> $elemMatchs = new ArrayList<>();
    for (EmbeddedElemMatcher filter : filters) {
      Map $elemMatch = new LinkedHashMap<>();
      $elemMatch.put("$elemMatch", filter.getElemMatherFiler());
      $elemMatchs.add($elemMatch);
    }
    addOperand(key, "$all", $elemMatchs);
    return this;
  }

  public LASQuery arrayElemMatch(String key, SingleElemMatcher filters) {
    addOperand(key, "$elemMatch", filters.getElemMatherFiler());
    return this;
  }

  public LASQuery arrayElemMatch(String key, EmbeddedElemMatcher filters) {
    addOperand(key, "$elemMatch", filters.getElemMatherFiler());
    return this;
  }

  public LASQuery arraySize(String key, int size) {
    addOperand(key, "$size", size);
    return this;
  }

  public LASQuery addProjectKey(String key) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.addProjectKey(key);
    return this;
  }

  public LASQuery addProjectKey(String... keys) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.addProjectKey(keys);
    return this;
  }

  public LASQuery excludeProjectKey(String key) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.excludeProjectKey(key);
    return this;
  }

  public LASQuery setSorts(LinkedHashMap sorts) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.setSort(sorts);
    return this;
  }

  public LASQuery setProjectKeys(Map projectKeys) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.setProjectKeys(projectKeys);
    return this;
  }

  public LASQuery sort(Direction direction, String... keys) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.sort(direction, keys);
    return this;
  }

  public LASQuery setSkip(int skip) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.setSkip(skip);
    return this;
  }

  public LASQuery setLimit(int limit) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.setLimit(limit);
    return this;
  }

  public LASQuery excludeProjectKey(String... keys) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.excludeProjectKey(keys);
    return this;
  }

  public LASQuery addProjectKey(String key, ArrayProjectionFilterFirstMatch filter) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.addProjectKey(key, filter);
    return this;
  }

  public LASQuery arraySlice(String arrayKey, int skip, int limit) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.arraySlice(arrayKey, skip, limit);
    return this;
  }

  public <T> LASQuery equalTo(String key, T value) {
    addOperand(key, null, value);
    return this;
  }

  public <T> LASQuery notEqualTo(String key, T value) {
    addOperand(key, "$ne", value);
    return this;
  }

  public LASQuery exists(String key) {
    addOperand(key, "$exists", true);
    return this;
  }

  public LASQuery notExist(String key) {
    addOperand(key, "$exists", false);
    return this;
  }

  public <T> LASQuery greaterThan(String key, T value) {
    addOperand(key, "$gt", value);
    return this;
  }

  public <T> LASQuery greaterThanOrEqualTo(String key, T value) {
    addOperand(key, "$gte", value);
    return this;
  }

  public <T> LASQuery lessThan(String key, T value) {
    addOperand(key, "$lt", value);
    return this;
  }

  public <T> LASQuery lessThanOrEqualTo(String key, T value) {
    addOperand(key, "$lte", value);
    return this;
  }

  public LASQuery regex(String key, String regex) {
    addOperand(key, "$regex", regex);
    return this;
  }

  public <T> LASQuery in(String key, List<T> values) {
    addOperand(key, "$in", values);
    return this;
  }

  public <T> LASQuery notIn(String key, List<T> values) {
    addOperand(key, "$nin", values);
    return this;
  }

  public LASQuery geoWithinInBox(String key, LASGeoPoint southwestGeoPoint, LASGeoPoint northeastGeoPoint) {
    super.geoWithinInBox(key, southwestGeoPoint, northeastGeoPoint);
    return this;
  }

  public LASQuery setQueryOptions(MongoQueryOptions queryOptions) {
    this.queryOptions = queryOptions;
    return this;
  }

  public LASQuery and(LASQuery query) {
    MongoQuery mongoQuery = query;
    Map $and = new LinkedHashMap<>();
    List list = new ArrayList();
    list.add(this.query);
    list.add(mongoQuery.getQuery());
    $and.put("$and", list);
    this.query = $and;
    return this;
  }

  public LASQuery and(LASQuery... queries) {
    Map $and = new LinkedHashMap<>();
    List list = new ArrayList();
    list.add(this.query);
    for (LASQuery lasQuery : queries) {
      list.add(lasQuery.getQuery());
    }
    $and.put("$and", list);
    this.query = $and;
    return this;
  }

  public LASQuery or(LASQuery query) {
    MongoQuery mongoQuery = query;
    Map $or = new LinkedHashMap<>();
    List list = new ArrayList();
    list.add(this.query);
    list.add(mongoQuery.getQuery());
    $or.put("$or", list);
    this.query = $or;
    return this;
  }

  public LASQuery or(LASQuery... queries) {
    Map $or = new LinkedHashMap<>();
    List list = new ArrayList();
    list.add(this.query);
    for (LASQuery lasQuery : queries) {
      list.add(lasQuery.getQuery());
    }
    $or.put("$or", list);
    this.query = $or;
    return this;
  }

  public LASQuery not(String key, SingleElemMatcher elemMatcher) {
    addOperand(key, "$not", elemMatcher.getElemMatherFiler());
    return this;
  }

  public <T> LASQuery inQuery(String key, InQueryOperator queryOperator) {
    addOperand(key, "$inQuery", queryOperator.toMap());
    return this;
  }

  public <T> LASQuery notInQuery(String key, InQueryOperator queryOperator) {
    addOperand(key, "$ninQuery", queryOperator);
    return this;
  }

  public <T> LASQuery select(String key, SelectOperator selectOperator) {
    addOperand(key, "$select", selectOperator.toMap());
    return this;
  }

  public <T> LASQuery notSelect(String key, SelectOperator selectOperator) {
    addOperand(key, "$dontSelect", selectOperator.toMap());
    return this;
  }

  public LASQuery relatedTo(String key, LASPointer pointer) {
    Map $relatedTo = new HashMap();
    $relatedTo.put("object", pointer.toMap());
    $relatedTo.put("key", key);
    equalTo("$relatedTo", $relatedTo);
    return this;
  }

  public void loadRelations(boolean relations) {
    this.relations = relations;
  }

  public boolean isRelations() {
    return relations;
  }

  public void setCount(boolean count) {
    this.count = count;
  }

  public boolean isCount() {
    return count;
  }

  public String getIncludes() {
    return includes;
  }

  public void setIncludes(String includes) {
    this.includes = includes;
  }

  public static class InQueryOperator {
    String className;
    ElemMatcher elemMatcher = new ElemMatcher();

    public InQueryOperator(String className) {
      this.className = className;
    }

    public InQueryOperator $eq(String key, Object filter) {
      elemMatcher.$eq(key, filter);
      return this;
    }

    public InQueryOperator $gt(String key, Object filter) {
      elemMatcher.$gt(key, filter);
      return this;
    }

    public InQueryOperator $gte(String key, Object filter) {
      elemMatcher.$gte(key, filter);
      return this;
    }

    public InQueryOperator $lt(String key, Object filter) {
      elemMatcher.$lt(key, filter);
      return this;
    }

    public InQueryOperator $exists(String key, Object filter) {
      elemMatcher.$exists(key, filter);
      return this;
    }

    public InQueryOperator $lte(String key, Object filter) {
      elemMatcher.$lte(key, filter);
      return this;
    }

    public InQueryOperator $ne(String key, Object filter) {
      elemMatcher.$ne(key, filter);
      return this;
    }

    public InQueryOperator $in(String key, Object... filters) {
      elemMatcher.$in(key, filters);
      return this;
    }

    public InQueryOperator $nin(String key, Object... filters) {
      elemMatcher.$nin(key, filters);
      return this;
    }

    Map toMap() {
      Map query = new HashMap();
      query.put("className", className);
      query.put("where", elemMatcher.getElemMatherFiler());
      return query;
    }
  }

  public static class SelectOperator {
    String className;
    String key;
    ElemMatcher elemMatcher = new ElemMatcher();

    public SelectOperator(String className, String key) {
      this.className = className;
      this.key = key;
    }

    public SelectOperator $eq(String key, Object filter) {
      elemMatcher.$eq(key, filter);
      return this;
    }

    public SelectOperator $gt(String key, Object filter) {
      elemMatcher.$gt(key, filter);
      return this;
    }

    public SelectOperator $gte(String key, Object filter) {
      elemMatcher.$gte(key, filter);
      return this;
    }

    public SelectOperator $lt(String key, Object filter) {
      elemMatcher.$lt(key, filter);
      return this;
    }

    public SelectOperator $exists(String key, Object filter) {
      elemMatcher.$exists(key, filter);
      return this;
    }

    public SelectOperator $lte(String key, Object filter) {
      elemMatcher.$lte(key, filter);
      return this;
    }

    public SelectOperator $ne(String key, Object filter) {
      elemMatcher.$ne(key, filter);
      return this;
    }

    public SelectOperator $in(String key, Object... filters) {
      elemMatcher.$in(key, filters);
      return this;
    }

    public SelectOperator $nin(String key, Object... filters) {
      elemMatcher.$nin(key, filters);
      return this;
    }

    Map toMap() {
      Map map = new HashMap<>();
      Map query = new HashMap();
      query.put("className", className);
      query.put("where", elemMatcher.getElemMatherFiler());
      map.put("query", query);
      map.put("key", key);
      return map;
    }
  }
}

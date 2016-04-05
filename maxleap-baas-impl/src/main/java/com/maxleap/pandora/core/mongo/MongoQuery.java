package com.maxleap.pandora.core.mongo;

import com.maxleap.pandora.core.Direction;
import com.maxleap.pandora.core.mongo.exception.QueryException;

import java.util.*;

/**
 * @author sneaky
 * @since 3.0.0
 */
public class MongoQuery {
  protected Map query = new LinkedHashMap<>();
  protected MongoQueryOptions queryOptions = new MongoQueryOptions();

  public MongoQuery() {
  }

  public MongoQuery(MongoQueryOptions queryOptions) {
    this.queryOptions = queryOptions;
  }

  public MongoQuery(Map query, Map projectKeys) {
    this.query = query;
    queryOptions = new MongoQueryOptions(projectKeys, null, -1, -1);
  }

  public <T> MongoQuery arrayAll(String key, List<T> values) {
    addOperand(key, "$all", values);
    return this;
  }

  public MongoQuery arrayAllWithElemMatch(String key, EmbeddedElemMatcher... filters) {
    List<Map> $elemMatchs = new ArrayList<>();
    for (EmbeddedElemMatcher filter : filters) {
      Map $elemMatch = new LinkedHashMap<>();
      $elemMatch.put("$elemMatch", filter.getElemMatherFiler());
      $elemMatchs.add($elemMatch);
    }
    addOperand(key, "$all", $elemMatchs);
    return this;
  }

  public MongoQuery arrayElemMatch(String key, SingleElemMatcher filters) {
    addOperand(key, "$elemMatch", filters.getElemMatherFiler());
    return this;
  }

  public MongoQuery arrayElemMatch(String key, EmbeddedElemMatcher filters) {
    addOperand(key, "$elemMatch", filters.getElemMatherFiler());
    return this;
  }

  public MongoQuery arraySize(String key, int size) {
    addOperand(key, "$size", size);
    return this;
  }

  public MongoQuery addProjectKey(String key) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.addProjectKey(key);
    return this;
  }

  public MongoQuery addProjectKey(String... keys) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.addProjectKey(keys);
    return this;
  }

  public MongoQuery excludeProjectKey(String key) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.excludeProjectKey(key);
    return this;
  }

  public MongoQuery setSorts(LinkedHashMap sorts) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.setSort(sorts);
    return this;
  }

  public MongoQuery setProjectKeys(Map projectKeys) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.setProjectKeys(projectKeys);
    return this;
  }

  public MongoQuery sort(Direction direction, String... keys) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.sort(direction, keys);
    return this;
  }

  public MongoQuery setSkip(int skip) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.setSkip(skip);
    return this;
  }

  public MongoQuery setLimit(int limit) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.setLimit(limit);
    return this;
  }

  public MongoQuery excludeProjectKey(String... keys) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.excludeProjectKey(keys);
    return this;
  }

  public MongoQuery addProjectKey(String key, ArrayProjectionFilterFirstMatch filter) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.addProjectKey(key, filter);
    return this;
  }

  public MongoQuery arraySlice(String arrayKey, int skip, int limit) {
    if (queryOptions == null) {
      queryOptions = new MongoQueryOptions();
    }
    queryOptions.arraySlice(arrayKey, skip, limit);
    return this;
  }

  public MongoQuery not(String key, SingleElemMatcher elemMatcher) {
    addOperand(key, "$not", elemMatcher.getElemMatherFiler());
    return this;
  }

  public <T> MongoQuery equalTo(String key, T value) {
    addOperand(key, null, value);
    return this;
  }

  public <T> MongoQuery notEqualTo(String key, T value) {
    addOperand(key, "$ne", value);
    return this;
  }

  public MongoQuery exists(String key) {
    addOperand(key, "$exists", true);
    return this;
  }

  public MongoQuery notExist(String key) {
    addOperand(key, "$exists", false);
    return this;
  }

  public <T> MongoQuery greaterThan(String key, T value) {
    addOperand(key, "$gt", value);
    return this;
  }

  public <T> MongoQuery greaterThanOrEqualTo(String key, T value) {
    addOperand(key, "$gte", value);
    return this;
  }

  public <T> MongoQuery lessThan(String key, T value) {
    addOperand(key, "$lt", value);
    return this;
  }

  public <T> MongoQuery lessThanOrEqualTo(String key, T value) {
    addOperand(key, "$lte", value);
    return this;
  }

  public MongoQuery regex(String key, String regex) {
    addOperand(key, "$regex", regex);
    return this;
  }

  public <T> MongoQuery in(String key, List<T> values) {
    addOperand(key, "$in", values);
    return this;
  }

  public <T> MongoQuery notIn(String key, List<T> values) {
    addOperand(key, "$nin", values);
    return this;
  }

  public MongoQuery geoWithinInBox(String key, GeoPoint southwestGeoPoint, GeoPoint northeastGeoPoint) {
    if (southwestGeoPoint == null) {
      throw new IllegalArgumentException("southwestGeoPoint must be not null. ");
    }
    if (northeastGeoPoint == null) {
      throw new IllegalArgumentException("northeastGeoPoint must be not null. ");
    }
    if (southwestGeoPoint.getLongitude() > 180 || southwestGeoPoint.getLongitude() <-180) {
      throw new QueryException("Longitude must be within the range (-180.0, 180.0).");
    }
    if (northeastGeoPoint.getLongitude() > 180 || northeastGeoPoint.getLongitude() <-180) {
      throw new QueryException("Longitude must be within the range (-180.0, 180.0).");
    }
    if (southwestGeoPoint.getLatitude() < -90 || southwestGeoPoint.getLatitude() > 90) {
      throw new QueryException("Latitude must be within the range (-90.0, 90.0).");
    }
    if (northeastGeoPoint.getLatitude() < -90 || northeastGeoPoint.getLatitude() > 90) {
      throw new QueryException("Latitude must be within the range (-90.0, 90.0).");
    }

    Map box = new HashMap();
    box.put("$box", new Double[][] {new Double[]{southwestGeoPoint.getLongitude(), southwestGeoPoint.getLatitude()},
        new Double[]{northeastGeoPoint.getLongitude(), northeastGeoPoint.getLatitude()}});
    addOperand(key, "$geoWithin", box);
    return this;
  }


  public MongoQuery and(MongoQuery query) {
    Object $andExists = this.query.get("$and");
    if ($andExists instanceof List) {
      List list = (List) $andExists;
      list.add(query.getQuery());
    } else {
      Map $and = new LinkedHashMap<>();
      List list = new ArrayList();
      list.add(this.query);
      list.add(query.getQuery());
      $and.put("$and", list);
      this.query = $and;
    }
    return this;
  }

  public MongoQuery or(MongoQuery query) {
    Object $orExists = this.query.get("$or");
    if ($orExists instanceof List) {
      List list = (List) $orExists;
      list.add(query.getQuery());
    } else {
      List list = new ArrayList<>();
      Map $or = new LinkedHashMap<>();
      list.add(this.query);
      list.add(query.getQuery());
      $or.put("$or", list);
      this.query = $or;
    }
    return this;
  }

  public MongoQuery setQueryOptions(MongoQueryOptions queryOptions) {
    this.queryOptions = queryOptions;
    return this;
  }

  public MongoQueryOptions getQueryOptions() {
    return queryOptions;
  }

  public Map getQuery() {
    return query;
  }

  protected void addOperand(String key, String operator, Object value) {
    if (operator == null) {
      query.put(key, value);
    } else {
      Object storedValue = query.get(key);
      Map operand;
      if (storedValue == null || !(storedValue instanceof Map)) {
        operand = new LinkedHashMap<>();
        query.put(key, operand);
      } else {
        operand = (Map) query.get(key);
      }
      operand.put(operator, value);
    }
  }

  @Override
  public String toString() {
    return "{" +
        "query=" + (query) +
        ", queryOptions=" + queryOptions +
        '}';
  }

  public static class SingleElemMatcher {
    Map elemMatherFiler = new LinkedHashMap<>();

    public static SingleElemMatcher instance() {
      return new SingleElemMatcher();
    }

    public SingleElemMatcher $eq(Object filter) {
      elemMatherFiler.put("$eq", filter);
      return this;
    }

    public SingleElemMatcher $gt(Object filter) {
      elemMatherFiler.put("$gt", filter);
      return this;
    }

    public SingleElemMatcher $gte(Object filter) {
      elemMatherFiler.put("$gte", filter);
      return this;
    }

    public SingleElemMatcher $lt(Object filter) {
      elemMatherFiler.put("$lt", filter);
      return this;
    }

    public SingleElemMatcher $lte(Object filter) {
      elemMatherFiler.put("$lte", filter);
      return this;
    }

    public SingleElemMatcher $size(int size) {
      elemMatherFiler.put("$size", size);
      return this;
    }

    public SingleElemMatcher $ne(Object filter) {
      elemMatherFiler.put("$ne", filter);
      return this;
    }

    public SingleElemMatcher $in(Object... filters) {
      elemMatherFiler.put("$in", filters);
      return this;
    }

    public SingleElemMatcher $nin(Object... filters) {
      elemMatherFiler.put("$nin", filters);
      return this;
    }

    public Map getElemMatherFiler() {
      return elemMatherFiler;
    }
  }

  public static class EmbeddedElemMatcher {
    Map elemMatherFiler = new LinkedHashMap<>();

    public static EmbeddedElemMatcher instance() {
      return new EmbeddedElemMatcher();
    }

    public EmbeddedElemMatcher $eq(String key, Object filter) {
      elemMatherFiler.put(key, toMap("$eq", filter));
      return this;
    }

    public EmbeddedElemMatcher $gt(String key, Object filter) {
      elemMatherFiler.put(key, toMap("$gt", filter));
      return this;
    }

    public EmbeddedElemMatcher $gte(String key, Object filter) {
      elemMatherFiler.put(key, toMap("$gte", filter));
      return this;
    }

    public EmbeddedElemMatcher $lt(String key, Object filter) {
      elemMatherFiler.put(key, toMap("$lt", filter));
      return this;
    }

    public EmbeddedElemMatcher $lte(String key, Object filter) {
      elemMatherFiler.put(key, toMap("$lte", filter));
      return this;
    }

    public EmbeddedElemMatcher $ne(String key, Object filter) {
      elemMatherFiler.put(key, toMap("$ne", filter));
      return this;
    }

    public EmbeddedElemMatcher $in(String key, Object... filters) {
      elemMatherFiler.put(key, toMap("$in", filters));
      return this;
    }

    public EmbeddedElemMatcher $nin(String key, Object... filters) {
      elemMatherFiler.put(key, toMap("$nin", filters));
      return this;
    }

    Map toMap(String op, Object filter) {
      Map opMap = new LinkedHashMap<>();
      opMap.put(op, filter);
      return opMap;
    }

    public Map getElemMatherFiler() {
      return elemMatherFiler;
    }
  }

  public static class ArrayProjectionFilterFirstMatch extends EmbeddedElemMatcher {
    public static ArrayProjectionFilterFirstMatch instance() {
      return new ArrayProjectionFilterFirstMatch();
    }

    public ArrayProjectionFilterFirstMatch $eq(String key, Object filter) {
      super.$eq(key, filter);
      return this;
    }

    public ArrayProjectionFilterFirstMatch $gt(String key, Object filter) {
      super.$gt(key, filter);
      return this;
    }

    public ArrayProjectionFilterFirstMatch $gte(String key, Object filter) {
      super.$gte(key, filter);
      return this;
    }

    public ArrayProjectionFilterFirstMatch $lt(String key, Object filter) {
      super.$lt(key, filter);
      return this;
    }

    public ArrayProjectionFilterFirstMatch $lte(String key, Object filter) {
      super.$lte(key, filter);
      return this;
    }

    public ArrayProjectionFilterFirstMatch $ne(String key, Object filter) {
      super.$ne(key, filter);
      return this;
    }

    public ArrayProjectionFilterFirstMatch $in(String key, Object... filters) {
      super.$in(key, filters);
      return this;
    }

    public ArrayProjectionFilterFirstMatch $nin(String key, Object... filters) {
      super.$nin(key, filters);
      return this;
    }
  }

  public static class ElemMatcher {
    Map elemMatherFiler = new HashMap();

    public static ElemMatcher instance() {
      return new ElemMatcher();
    }

    public ElemMatcher $eq(String key, Object filter) {
      elemMatherFiler.put(key, toMap("$eq", filter));
      return this;
    }

    public ElemMatcher $gt(String key, Object filter) {
      Map $eq = new HashMap();
      $eq.put(key, filter);
      elemMatherFiler.put(key, toMap("$gt", filter));
      return this;
    }

    public ElemMatcher $gte(String key, Object filter) {
      elemMatherFiler.put(key, toMap("$gte", filter));
      return this;
    }

    public ElemMatcher $lt(String key, Object filter) {
      elemMatherFiler.put(key, toMap("$lt", filter));
      return this;
    }

    public ElemMatcher $lte(String key, Object filter) {
      elemMatherFiler.put(key, toMap("$lte", filter));
      return this;
    }

    public ElemMatcher $exists(String key, Object filter) {
      elemMatherFiler.put(key, toMap("$exists", filter));
      return this;
    }

    public ElemMatcher $ne(String key, Object filter) {
      elemMatherFiler.put(key, toMap("$ne", filter));
      return this;
    }

    public ElemMatcher $in(String key, Object... filters) {
      elemMatherFiler.put(key, toMap("$in", filters));
      return this;
    }

    public ElemMatcher $nin(String key, Object... filters) {
      elemMatherFiler.put(key, toMap("$nin", filters));
      return this;
    }

    public ElemMatcher $size(String key, int size) {
      elemMatherFiler.put(key, toMap("$size", size));
      return this;
    }

    Map toMap(String op, Object filter) {
      Map opMap = new HashMap();
      opMap.put(op, filter);
      return opMap;
    }

    public Map getElemMatherFiler() {
      return elemMatherFiler;
    }
  }

}

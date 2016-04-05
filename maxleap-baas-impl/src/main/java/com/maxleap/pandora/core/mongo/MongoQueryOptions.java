package com.maxleap.pandora.core.mongo;

import com.maxleap.pandora.core.Direction;
import com.maxleap.pandora.core.mongo.exception.QueryException;

import java.util.*;

/**
 * User：poplar
 * Date：15/7/29
 */
public class MongoQueryOptions {
  LinkedHashMap sorts;
  int skip = -1;
  int limit = -1;
  Map projectKeys;

  boolean projectIncluding;
  boolean projectExcluding;

  public MongoQueryOptions() {
  }

  public MongoQueryOptions(int skip, int limit) {
    this.skip = skip;
    this.limit = limit;
  }

  public MongoQueryOptions(Map projectKeys, LinkedHashMap sorts, int skip, int limit) {
    this.projectKeys = projectKeys;
    this.sorts = sorts;
    this.skip = skip;
    this.limit = limit;
  }

  public MongoQueryOptions addProjectKey(String key) {
    if (this.projectKeys == null) {
      this.projectKeys = new LinkedHashMap<>();
      projectIncluding = true;
    } else {
      if (projectExcluding) {
        throw new QueryException("Projection cannot have a mix of inclusion and exclusion.");
      }
    }
    this.projectKeys.put(key.trim(), 1);
    return this;
  }

  public MongoQueryOptions addProjectKey(String... keys) {
    if (this.projectKeys == null) {
      this.projectKeys = new LinkedHashMap<>();
      projectIncluding = true;
    } else {
      if (projectExcluding) {
        throw new QueryException("Projection cannot have a mix of inclusion and exclusion.");
      }
    }
    for (String key : keys) {
      if (key != null) {
        this.projectKeys.put(key.trim(), 1);
      }
    }
    return this;
  }

  public MongoQueryOptions excludeProjectKey(String key) {
    if (this.projectKeys == null) {
      this.projectKeys = new LinkedHashMap<>();
      projectExcluding = true;
    } else if (projectIncluding && !(key.equals("_id") || key.equals("objectId"))) {
      throw new QueryException("Projection cannot have a mix of inclusion and exclusion.");
    }
    this.projectKeys.put(key, 0);
    return this;
  }

  public Map getProjectKeys() {
    return projectKeys;
  }

  public MongoQueryOptions setSort(LinkedHashMap sorts) {
    this.sorts = sorts;
    return this;
  }

  public MongoQueryOptions setProjectKeys(Map projectKeys) {
    this.projectKeys = projectKeys;
    return this;
  }

  public Map getSorts() {
    return sorts;
  }

  public MongoQueryOptions sort(Direction direction, String... keys) {
    if (sorts == null) {
      sorts = new LinkedHashMap<>();
    }
    for (String key : keys) {
      if (direction == null || direction == Direction.ASC) {
        sorts.put(key, 1);
      } else {
        sorts.put(key, -1);
      }
    }
    return this;
  }

  public MongoQueryOptions setSkip(int skip) {
    this.skip = skip;
    return this;
  }

  public MongoQueryOptions setLimit(int limit) {
    this.limit = limit;
    return this;
  }

  public int getLimit() {
    return limit;
  }

  public int getSkip() {
    return skip;
  }

  public MongoQueryOptions excludeProjectKey(String... keys) {
    if (this.projectKeys == null) {
      this.projectKeys = new LinkedHashMap<>();
      projectExcluding = true;
    } else if (projectIncluding) {
      throw new QueryException("Projection cannot have a mix of inclusion and exclusion.");
    }
    for (String key : keys) {
      if (key != null) {
        this.projectKeys.put(key, 0);
      }
    }
    return this;
  }

  public MongoQueryOptions addProjectKey(String key, MongoQuery.ArrayProjectionFilterFirstMatch filter) {
    if (projectKeys == null) {
      projectKeys = new LinkedHashMap<>();
      projectIncluding = true;
    } else if (projectExcluding) {
      throw new QueryException("Projection cannot have a mix of inclusion and exclusion.");
    }

    Map $elemMatch = new LinkedHashMap<>();
    $elemMatch.put("$elemMatch", filter.getElemMatherFiler());
    this.projectKeys.put(key, $elemMatch);
    return this;
  }

  public MongoQueryOptions arraySlice(String arrayKey, int skip, int limit) {
    if (projectKeys == null) {
      projectKeys = new LinkedHashMap<>();
    }
    List limited = new ArrayList();
    limited.add(skip);
    limited.add(limit);
    Map $slice = new HashMap();
    $slice.put("$slice", limited);
    this.projectKeys.put(arrayKey, $slice);
    return this;
  }

  public void setSorts(LinkedHashMap sorts) {
    this.sorts = sorts;
  }

  @Override
  public String toString() {
    return "{" +
        "projectKeys=" + projectKeys +
        ", sorts=" + sorts +
        ", limit=" + limit +
        ", skip=" + skip +
        '}';
  }
}

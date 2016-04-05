package com.maxleap.pandora.data.support.mongo;

import com.maxleap.pandora.config.mgo.MgoDatabase;
import com.maxleap.pandora.core.mongo.MongoQuery;

/**
 * @author Jing Zhao
 * @since 1.0
 */
public class CountRequest extends SimpleRequest {
  private MongoQuery query;

  public CountRequest(MgoDatabase mgoDatabase, String dbName, String table, MongoQuery query) {
    this.mgoDatabase = mgoDatabase;
    this.dbName = dbName;
    this.table = table;
    this.query = query;
  }

  public MongoQuery getQuery() {
    return query;
  }

  public void setQuery(MongoQuery query) {
    this.query = query;
  }

  @Override
  public String toString() {
    return "CountRequest{" +
        "query=" + query +
        ", db=" + dbName +
        ", table=" + table +
        '}';
  }
}

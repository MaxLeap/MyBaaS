package com.maxleap.pandora.data.support.mongo;

import com.maxleap.pandora.config.mgo.MgoDatabase;
import com.maxleap.pandora.core.mongo.MongoQuery;

/**
 * @author sneaky
 * @since 1.0
 */
public class DeleteRequest<ID> extends SimpleRequest {
  ID id;
  private MongoQuery query;

  public DeleteRequest(MgoDatabase mgoDatabase, String dbName, String table, ID id) {
    this.mgoDatabase = mgoDatabase;
    this.dbName = dbName;
    this.table = table;
    this.id = id;
  }

  public DeleteRequest(MgoDatabase mgoDatabase, String dbName, String table, MongoQuery query) {
    this.mgoDatabase = mgoDatabase;
    this.dbName = dbName;
    this.table = table;
    this.query = query;
  }

  public DeleteRequest() {
  }

  public MongoQuery getQuery() {
    return query;
  }

  public void setQuery(MongoQuery query) {
    this.query = query;
  }

  @Override
  public String toString() {
    return "{" +
        "query=" + query +
        ", id=" + id +
        ", db=" + dbName +
        ", table=" + table +
        '}';
  }
}

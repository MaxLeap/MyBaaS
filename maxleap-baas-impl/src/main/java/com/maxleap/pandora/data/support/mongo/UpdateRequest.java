package com.maxleap.pandora.data.support.mongo;

import com.maxleap.pandora.config.mgo.MgoDatabase;
import com.maxleap.pandora.core.mongo.MongoQuery;
import com.maxleap.pandora.core.mongo.MongoUpdate;

/**
 * User: qinpeng
 * Date: 14-4-28
 * Time: 15:16
 */
public class UpdateRequest<ID> extends SimpleRequest {
  private ID id;
  private MongoQuery query;
  private MongoUpdate update;

  public UpdateRequest(MgoDatabase mgoDatabase, String dbName, String table, MongoQuery query, MongoUpdate update) {
    this.mgoDatabase = mgoDatabase;
    this.dbName = dbName;
    this.table = table;
    this.update = update;
    this.query = query;
  }

  public UpdateRequest(MgoDatabase mgoDatabase, String dbName, String table, ID id, MongoUpdate update) {
    this.mgoDatabase = mgoDatabase;
    this.dbName = dbName;
    this.table = table;
    this.update = update;
    this.id = id;
  }

  public void setQuery(MongoQuery query) {
    this.query = query;
  }

  public MongoQuery getQuery() {
    return query;
  }

  public void setUpdate(MongoUpdate update) {
    this.update = update;
  }

  public MongoUpdate getUpdate() {
    return update;
  }

  @Override
  public String toString() {
    return "{" +
        "query=" + query +
        ", id=" + id +
        ", update=" + update +
        ", db=" + dbName +
        ", table=" + table +
        '}';
  }
}

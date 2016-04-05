package com.maxleap.pandora.data.support.mongo;

import com.maxleap.pandora.config.mgo.MgoDatabase;
import com.maxleap.pandora.core.mongo.MongoQuery;

/**
 * User: qinpeng
 * Date: 14-4-28
 * Time: 15:16
 */
public class FindRequest<ID> extends SimpleRequest {
  /**
   * Maybe null
   */
  MongoQuery query;

  /**
   * Maybe null
   */
  ID id;

  public FindRequest(MgoDatabase mgoDatabase, String dbName, String table, ID id) {
    this.mgoDatabase = mgoDatabase;
    this.dbName = dbName;
    this.table = table;
    this.id = id;
  }

  public FindRequest(MgoDatabase mgoDatabase, String dbName, String table, MongoQuery query) {
    this.mgoDatabase = mgoDatabase;
    this.dbName = dbName;
    this.table = table;
    this.query = query;
  }

  public MongoQuery getQuery() {
    return query;
  }

  public ID getId() {
    return id;
  }

  @Override
  public String toString() {
    return "{" +
        "id=" + id +
        ", query=" + query +
        ", db=" + dbName +
        ", table=" + table +
        '}';
  }
}

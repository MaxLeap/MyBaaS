package com.maxleap.pandora.data.support.mongo;

import com.maxleap.domain.mongo.BaseEntity;
import com.maxleap.pandora.config.mgo.MgoDatabase;

import java.io.Serializable;

/**
 * User: qinpeng
 * Date: 14-4-28
 * Time: 14:05
 */
public class CreateRequest<Entity extends BaseEntity<ID>, ID extends Serializable> extends SimpleRequest {
  private Entity document;

  /**
   * @param dbName
   * @param document
   */
  public CreateRequest(MgoDatabase mgoDatabase, String dbName, String table, Entity document) {
    this.mgoDatabase = mgoDatabase;
    this.dbName = dbName;
    this.table = table;
    this.document = document;
  }

  public CreateRequest() {
  }

  public Entity getDocument() {
    return document;
  }

  public void setDocument(Entity document) {
    this.document = document;
  }

  @Override
  public String toString() {
    return "{" +
        "document=" + document +
        ", db=" + dbName +
        ", table=" + table +
        '}';
  }
}

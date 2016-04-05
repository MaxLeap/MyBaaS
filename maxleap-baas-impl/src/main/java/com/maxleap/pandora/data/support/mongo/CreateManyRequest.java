package com.maxleap.pandora.data.support.mongo;

import com.maxleap.domain.mongo.BaseEntity;
import com.maxleap.pandora.config.mgo.MgoDatabase;

import java.io.Serializable;
import java.util.List;

/**
 * User: qinpeng
 * Date: 14-4-28
 * Time: 14:05
 */
public class CreateManyRequest<Entity extends BaseEntity<ID>, ID extends Serializable> extends SimpleRequest {
  private List<Entity> documents;

  /**
   * @param dbName
   * @param documents
   */
  public CreateManyRequest(MgoDatabase mgoDatabase, String dbName, String table, List<Entity> documents) {
    this.mgoDatabase = mgoDatabase;
    this.dbName = dbName;
    this.table = table;
    this.documents = documents;
  }

  public List<Entity> getDocuments() {
    return documents;
  }

  public void setDocuments(List<Entity> documents) {
    this.documents = documents;
  }

  @Override
  public String toString() {
    return "{" +
        "documents=" + documents +
        ", db=" + dbName +
        ", table=" + table +
        '}';
  }
}

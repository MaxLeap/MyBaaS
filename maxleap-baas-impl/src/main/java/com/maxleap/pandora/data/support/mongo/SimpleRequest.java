package com.maxleap.pandora.data.support.mongo;

/**
 * @author sneaky
 * @since 2.0
 */
public abstract class SimpleRequest extends MgoRequest {
  protected String dbName;
  protected String table;

  public String getDbName() {
    return dbName;
  }

  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }
}

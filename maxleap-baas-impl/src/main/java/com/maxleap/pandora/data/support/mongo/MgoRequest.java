package com.maxleap.pandora.data.support.mongo;

import com.maxleap.pandora.config.mgo.MgoDatabase;

/**
 * @author sneaky
 * @since 1.0.0
 */
public abstract class MgoRequest {
  protected MgoDatabase mgoDatabase;

  public MgoDatabase getMgoDatabase() {
    return mgoDatabase;
  }

  public void setMgoDatabase(MgoDatabase mgoDatabase) {
    this.mgoDatabase = mgoDatabase;
  }
}

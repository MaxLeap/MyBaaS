package com.maxleap.pandora.data.support.lasdata;

import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.pandora.config.mgo.MgoDatabase;
import com.maxleap.pandora.core.lasdata.LASClassSchema;
import com.maxleap.pandora.core.mongo.MongoQuery;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class AppCountRequest extends AppRequest {
  /**
   * Maybe null.
   */
  MongoQuery query;

  public AppCountRequest(MgoDatabase mgoDatabase, LASClassSchema classSchema, MongoQuery query, LASPrincipal principal) {
    this.mgoDatabase = mgoDatabase;
    this.classSchema = classSchema;
    this.query = query;
    this.principal = principal;
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
        ", classSchema=" + classSchema +
        '}';
  }
}

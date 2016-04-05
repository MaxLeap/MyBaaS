package com.maxleap.pandora.data.support.lasdata;

import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.pandora.config.mgo.MgoDatabase;
import com.maxleap.pandora.core.lasdata.LASClassSchema;
import com.maxleap.pandora.core.lasdata.LASQuery;
import com.maxleap.pandora.core.lasdata.LASUpdate;

/**
 * @author sneaky
 * @since 2.0
 */
public class AppUpdateRequest extends AppRequest {
  private LASQuery query;
  private LASUpdate update;

  public AppUpdateRequest() {
  }

  public AppUpdateRequest(MgoDatabase mgoDatabase, LASClassSchema classSchema, LASQuery query, LASUpdate update, LASPrincipal principal) {
    this.mgoDatabase = mgoDatabase;
    this.classSchema = classSchema;
    this.query = query;
    this.update = update;
    this.principal = principal;
  }

  public LASQuery getQuery() {
    return query;
  }

  public void setQuery(LASQuery query) {
    this.query = query;
  }

  public LASUpdate getUpdate() {
    return update;
  }

  public void setUpdate(LASUpdate update) {
    this.update = update;
  }

  @Override
  public String toString() {
    return "{" +
        "query=" + query +
        ", schema=" + classSchema +
        ", update=" + update +
        '}';
  }
}

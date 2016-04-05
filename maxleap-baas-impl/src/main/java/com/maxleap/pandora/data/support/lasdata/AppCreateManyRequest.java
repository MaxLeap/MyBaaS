package com.maxleap.pandora.data.support.lasdata;

import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.domain.mongo.BaseEntity;
import com.maxleap.pandora.config.mgo.MgoDatabase;
import com.maxleap.pandora.core.lasdata.LASClassSchema;

import java.io.Serializable;
import java.util.List;

/**
 * User: qinpeng
 * Date: 14-4-28
 * Time: 14:05
 */
public class AppCreateManyRequest<Entity extends BaseEntity<ID>, ID extends Serializable> extends AppRequest {
  private List<Entity> documents;

  public AppCreateManyRequest(MgoDatabase mgoDatabase, LASClassSchema classSchema, List<Entity> documents, LASPrincipal principal) {
    this.mgoDatabase = mgoDatabase;
    this.classSchema = classSchema;
    this.documents = documents;
    this.principal = principal;
  }

  public AppCreateManyRequest() {
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
        "document=" + documents +
        ", classSchema=" + classSchema +
        '}';
  }
}

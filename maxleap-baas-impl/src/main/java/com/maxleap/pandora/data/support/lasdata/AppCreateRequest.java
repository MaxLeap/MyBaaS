package com.maxleap.pandora.data.support.lasdata;

import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.domain.mongo.BaseEntity;
import com.maxleap.pandora.config.mgo.MgoDatabase;
import com.maxleap.pandora.core.lasdata.LASClassSchema;

import java.io.Serializable;

/**
 * User: qinpeng
 * Date: 14-4-28
 * Time: 14:05
 */
public class AppCreateRequest<Entity extends BaseEntity<ID>, ID extends Serializable> extends AppRequest {
  private Entity document;

  public AppCreateRequest(MgoDatabase mgoDatabase, LASClassSchema classSchema, Entity document, LASPrincipal principal) {
    this.mgoDatabase = mgoDatabase;
    this.classSchema = classSchema;
    this.document = document;
    this.principal = principal;
  }

  public AppCreateRequest() {
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
        ", classSchema=" + classSchema +
        '}';
  }
}

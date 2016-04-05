package com.maxleap.pandora.data.support.filter;

import com.maxleap.domain.mongo.BaseEntity;
import com.maxleap.pandora.core.PandoraMongoData;
import com.maxleap.pandora.core.exception.LASDataException;
import com.maxleap.pandora.core.mongo.MongoUpdate;
import com.maxleap.pandora.data.support.filter.support.Filter;
import com.maxleap.pandora.data.support.filter.support.FilterChain;
import com.maxleap.pandora.data.support.lasdata.AppCreateManyRequest;
import com.maxleap.pandora.data.support.lasdata.AppCreateRequest;
import com.maxleap.pandora.data.support.lasdata.AppUpdateRequest;
import com.maxleap.pandora.data.support.mongo.*;

import javax.inject.Singleton;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * CreatedAt and UpdatedAt verify.
 * </br>
 *
 * @author sneaky
 * @since 3.0.0
 */
@Singleton
public class TimestampFilter implements Filter {
  public final static String name = "TimestampFilter";

  @Override
  public void doFilter(MgoRequest request, FilterChain chain) throws LASDataException {
    modifyTimestamp(request);
    chain.doFilter(request);
  }

  @Override
  public void doFilter(Response response, FilterChain chain) throws LASDataException {
    chain.doFilter(response);
  }

  private void modifyTimestamp(MgoRequest request) {
    if (request instanceof UpdateRequest) {
      UpdateRequest updateRequest = (UpdateRequest) request;
      Map<String, Object> objNew = updateRequest.getUpdate().getModifierOps();
      Map $set = (Map) objNew.get("$set");
      adjustTimestamp(objNew, $set);
      upsertCreatedAt(updateRequest.getUpdate());
    } else if (request instanceof CreateRequest) {
      CreateRequest saveRequest = (CreateRequest) request;
      BaseEntity document = saveRequest.getDocument();
      long createdAt = document.getCreatedAt();
      if (createdAt <= 0) {
        document.setCreatedAt(System.currentTimeMillis());
      }
      long updatedAt = document.getUpdatedAt();
      if (updatedAt <= 0) {
        document.setUpdatedAt(System.currentTimeMillis());
      }
    } else if (request instanceof AppUpdateRequest) {
      AppUpdateRequest updateRequest = (AppUpdateRequest) request;
      Map<String, Object> objNew = updateRequest.getUpdate().getModifierOps();
      Map $set = (Map) objNew.get("$set");
      adjustTimestamp(objNew, $set);
      upsertCreatedAt(updateRequest.getUpdate());
    } else if (request instanceof AppCreateRequest) {
      AppCreateRequest saveRequest = (AppCreateRequest) request;
      BaseEntity document = saveRequest.getDocument();
      if (document.getCreatedAt() <= 0) {
        document.setCreatedAt(System.currentTimeMillis());
      }
      if (document.getUpdatedAt() <= 0) {
        document.setUpdatedAt(System.currentTimeMillis());
      }
    } else if (request instanceof CreateManyRequest) {
      CreateManyRequest<BaseEntity<Serializable>, Serializable> createManyRequest = (CreateManyRequest) request;
      for (BaseEntity document : createManyRequest.getDocuments()) {
        if (document.getCreatedAt() <= 0) {
          document.setCreatedAt(System.currentTimeMillis());
        }
        if (document.getUpdatedAt() <= 0) {
          document.setUpdatedAt(System.currentTimeMillis());
        }
      }
    } else if (request instanceof AppCreateManyRequest) {
      AppCreateManyRequest<BaseEntity<Serializable>, Serializable> createManyRequest = (AppCreateManyRequest) request;
      for (BaseEntity document : createManyRequest.getDocuments()) {
        if (document.getCreatedAt() <= 0) {
          document.setCreatedAt(System.currentTimeMillis());
        }
        if (document.getUpdatedAt() <= 0) {
          document.setUpdatedAt(System.currentTimeMillis());
        }
      }
    }
  }

  private void adjustTimestamp(Map<String, Object> objNew, Map $set) {
    if ($set == null) {
      $set = new HashMap();
      objNew.put("$set", $set);
      $set.put(PandoraMongoData.KEY_OBJECT_UPDATED_AT, System.currentTimeMillis());
    } else {
      Object updatedAt = $set.get(PandoraMongoData.KEY_OBJECT_UPDATED_AT);
      if (updatedAt == null || updatedAt != null && !(updatedAt instanceof Number)) {
        $set.put(PandoraMongoData.KEY_OBJECT_UPDATED_AT, System.currentTimeMillis());
      }

      Object createdAt = $set.get(PandoraMongoData.KEY_OBJECT_CREATED_AT);
      if (createdAt != null && !(createdAt instanceof Number)) {
        $set.remove(PandoraMongoData.KEY_OBJECT_CREATED_AT);
      }
    }
  }

  void upsertCreatedAt(MongoUpdate update) {
    if (update.isUpsert()) {
      Map $setOnInsert = (Map) update.getModifierOps().get("$setOnInsert");
      if ($setOnInsert == null) {
        $setOnInsert = new HashMap();
        update.getModifierOps().put("$setOnInsert", $setOnInsert);
      }
      $setOnInsert.put(PandoraMongoData.KEY_OBJECT_CREATED_AT, System.currentTimeMillis());
    }
  }

  @Override
  public String name() {
    return name;
  }

}

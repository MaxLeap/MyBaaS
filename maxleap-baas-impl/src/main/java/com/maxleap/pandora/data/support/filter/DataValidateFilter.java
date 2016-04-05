package com.maxleap.pandora.data.support.filter;

import com.maxleap.domain.base.LASObject;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.PandoraMongoData;
import com.maxleap.pandora.core.exception.LASDataException;
import com.maxleap.pandora.data.support.filter.support.Filter;
import com.maxleap.pandora.data.support.filter.support.FilterChain;
import com.maxleap.pandora.data.support.lasdata.AppCreateManyRequest;
import com.maxleap.pandora.data.support.lasdata.AppCreateRequest;
import com.maxleap.pandora.data.support.lasdata.AppUpdateRequest;
import com.maxleap.pandora.data.support.mongo.MgoRequest;
import com.maxleap.pandora.data.support.mongo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

/**
 * @author sneaky
 * @since 1.0
 */
@Singleton
public class DataValidateFilter implements Filter {
  public final static String name = "DataValidateFilter";
  private static transient final Logger log = LoggerFactory.getLogger(DataValidateFilter.class);

  @Override
  public void doFilter(MgoRequest request, FilterChain chain) throws LASDataException {
    if (request instanceof AppCreateRequest) {
      if (((AppCreateRequest) request).getDocument() instanceof LASObject) {
        validate(((LASObject) ((AppCreateRequest) request).getDocument()).getMap());
      }
    } else if (request instanceof AppUpdateRequest) {
      Map modifierOps = ((AppUpdateRequest) request).getUpdate().getModifierOps();
      validate((Map) modifierOps.get("$set"));
      validate((Map) modifierOps.get("$setOnInsert"));
    } else if (request instanceof AppCreateManyRequest) {
      List documents = ((AppCreateManyRequest) request).getDocuments();
      for (Object map : documents) {
        validate((Map) map);
      }
    }
    chain.doFilter(request);
  }

  @Override
  public void doFilter(Response response, FilterChain chain) throws LASDataException {
    chain.doFilter(response);
  }

  private void validate(Map doc) {
    if (doc == null) {
      return;
    }
    Object objectId = doc.get(PandoraMongoData.KEY_OBJECT_ID);

    if (objectId != null && !(objectId instanceof ObjectId)) {
      doc.put(PandoraMongoData.KEY_OBJECT_ID, new ObjectId(objectId.toString()));
    }
    if (doc.get("_id") != null) {
      throw new LASDataException(LASDataException.INVALID_KEY_NAME, "The _id key is a reserved word");
    }
  }

  @Override
  public String name() {
    return name;
  }

}

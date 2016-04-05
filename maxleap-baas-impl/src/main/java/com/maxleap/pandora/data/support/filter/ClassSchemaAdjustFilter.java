package com.maxleap.pandora.data.support.filter;

import com.maxleap.domain.base.LASObject;
import com.maxleap.pandora.core.exception.LASDataException;
import com.maxleap.pandora.core.lasdata.LASClassSchema;
import com.maxleap.pandora.data.support.filter.support.Filter;
import com.maxleap.pandora.data.support.filter.support.FilterChain;
import com.maxleap.pandora.data.support.lasdata.AppCreateManyRequest;
import com.maxleap.pandora.data.support.lasdata.AppCreateRequest;
import com.maxleap.pandora.data.support.lasdata.AppUpdateRequest;
import com.maxleap.pandora.data.support.mongo.MgoRequest;
import com.maxleap.pandora.data.support.mongo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author sneaky
 * @since 2.0
 */
@Singleton
public class ClassSchemaAdjustFilter implements Filter {
  public final static String name = "ClassSchemaAdjustFilter";
  private static transient final Logger log = LoggerFactory.getLogger(ClassSchemaAdjustFilter.class);
  private ClassSchemaAdjuster schemaAdjuster;

  @Inject
  public ClassSchemaAdjustFilter(ClassSchemaAdjuster classSchemaAdjuster) {
    this.schemaAdjuster = classSchemaAdjuster;
  }

  @Override
  public void doFilter(MgoRequest request, FilterChain chain) throws LASDataException {
    if (request instanceof AppCreateRequest) {
      AppCreateRequest appCreateRequest = (AppCreateRequest) request;
      LASClassSchema schema = appCreateRequest.getClassSchema();
      schema = schemaAdjuster.adjust((LASObject) ((AppCreateRequest) request).getDocument(), schema);
      appCreateRequest.setClassSchema(schema);
    } else if (request instanceof AppUpdateRequest) {
      AppUpdateRequest appUpdateRequest = (AppUpdateRequest) request;
      LASClassSchema schema = appUpdateRequest.getClassSchema();
      schema = schemaAdjuster.adjust(((AppUpdateRequest) request).getUpdate(), schema);
      appUpdateRequest.setClassSchema(schema);
    } else if (request instanceof AppCreateManyRequest) {
      AppCreateManyRequest appCreateManyRequest = ((AppCreateManyRequest) request);
      LASClassSchema classSchema = appCreateManyRequest.getClassSchema();
      schemaAdjuster.adjust(appCreateManyRequest.getDocuments(), classSchema);
    }
    chain.doFilter(request);
  }

  @Override
  public void doFilter(Response response, FilterChain chain) throws LASDataException {
    chain.doFilter(response);
  }

  @Override
  public String name() {
    return name;
  }
}

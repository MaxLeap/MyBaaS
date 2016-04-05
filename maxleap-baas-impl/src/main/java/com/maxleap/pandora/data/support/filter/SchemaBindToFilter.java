package com.maxleap.pandora.data.support.filter;

import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.exception.LASDataException;
import com.maxleap.pandora.core.lasdata.LASClassBind;
import com.maxleap.pandora.core.lasdata.LASClassSchema;
import com.maxleap.pandora.data.support.ClassSchemaManager;
import com.maxleap.pandora.data.support.filter.support.Filter;
import com.maxleap.pandora.data.support.filter.support.FilterChain;
import com.maxleap.pandora.data.support.lasdata.*;
import com.maxleap.pandora.data.support.mongo.MgoRequest;
import com.maxleap.pandora.data.support.mongo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Check database name and resolver database name and collection name.
 *
 * @author sneaky
 * @since 2.0.0
 */
public class SchemaBindToFilter implements Filter {
  private static transient final Logger log = LoggerFactory.getLogger(SchemaBindToFilter.class);
  public final static String name = "SchemaBindToFilter";
  private ClassSchemaManager classSchemaManager;

  @Inject
  public SchemaBindToFilter(ClassSchemaManager classSchemaManager) {
    this.classSchemaManager = classSchemaManager;
  }

  @Override
  public void doFilter(MgoRequest request, FilterChain chain) throws LASDataException {
    if (request instanceof AppRequest) {
      AppRequest appRequest = (AppRequest) request;
      LASClassSchema classSchema = appRequest.getClassSchema();
      if (classSchema != null) {
        LASClassBind bindTo = classSchema.getBindTo();
        if (bindTo != null) {
          LASClassSchema bindToSchema = classSchemaManager.get(new ObjectId(bindTo.getBindApp()), bindTo.getClassName());
          if (bindToSchema != null) {
            appRequest.setClassSchema(bindToSchema);
            checkPermission(request, bindTo);
            if (log.isDebugEnabled()) {
              log.debug("Mapped to BindTo ClassSchema, appId: {}, className: {}",
                  ((AppRequest) request).getClassSchema().getAppId(), ((AppRequest) request).getClassSchema().getClassName());
            }
          } else {
//                    classSchema.setBindTo(null);
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("_id", classSchema.getId());
//
//                    Map update = new HashMap();
//                    HashMap<String, Object> value = new HashMap<>();
//                    value.put(LASClassSchema.DB_FIELD_NAME.bindTo.name(), 1);
//
//                    update.put("$unset", value);
//                    classSchemaManager.update(map, update);

            throw new LASDataException(LASDataException.BIND_TO_CLASS_NOT_FOUND, "bind to class not found.");
          }
        }
      }
    }
    chain.doFilter(request);
  }

  private void checkPermission(MgoRequest request, LASClassBind bindTo) {
    if ((request instanceof AppFindOneRequest || request instanceof AppFindRequest || request instanceof AppCountRequest) && !bindTo.isRead()) {
      throw new LASDataException(LASDataException.OPERATION_FORBIDDEN, "Does not has permission to do Read, bindClass " + bindTo.getClassName());
    } else if ((request instanceof AppUpdateRequest || request instanceof AppCreateRequest) && !bindTo.isWrite()) {
      throw new LASDataException(LASDataException.OPERATION_FORBIDDEN, "Does not has permission to do Write, bindClass " + bindTo.getClassName());
    } else if (request instanceof AppDeleteRequest && !bindTo.isDelete()) {
      throw new LASDataException(LASDataException.OPERATION_FORBIDDEN, "Does not has permission to do Delete, bindClass " + bindTo.getClassName());
    }
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

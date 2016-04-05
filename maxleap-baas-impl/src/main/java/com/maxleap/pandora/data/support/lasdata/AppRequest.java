package com.maxleap.pandora.data.support.lasdata;

import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.pandora.core.lasdata.LASClassSchema;
import com.maxleap.pandora.data.support.mongo.MgoRequest;

/**
 * @author sneaky
 * @since 2.0
 */
public abstract class AppRequest extends MgoRequest {
  protected LASClassSchema classSchema;
  protected LASPrincipal principal;

  public LASPrincipal getPrincipal() {
    return principal;
  }

  public void setPrincipal(LASPrincipal principal) {
    this.principal = principal;
  }

  public LASClassSchema getClassSchema() {
    return classSchema;
  }

  public void setClassSchema(LASClassSchema classSchema) {
    this.classSchema = classSchema;
  }
}

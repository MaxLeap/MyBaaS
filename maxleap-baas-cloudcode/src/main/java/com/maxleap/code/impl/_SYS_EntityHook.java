package com.maxleap.code.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.maxleap.las.sdk.MLObject;

/**
 * User：poplar
 * Date：15-1-14
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class _SYS_EntityHook extends MLObject {
  private String values;

  public String getValues() {
    return values;
  }

  public void setValues(String values) {
    this.values = values;
  }

  @Override
  public String toString() {
    return "_SYS_EntityHook{" +
        "values='" + values + '\'' +
        "} " + super.toString();
  }
}

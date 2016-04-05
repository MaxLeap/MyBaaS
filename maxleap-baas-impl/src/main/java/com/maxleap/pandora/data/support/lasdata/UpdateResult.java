package com.maxleap.pandora.data.support.lasdata;

import com.maxleap.domain.base.ObjectId;
import com.maxleap.domain.mongo.DateUtils;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class UpdateResult {
  private int number;
  private long updatedAt;
  /**
   * $inc result.
   */
  @JsonIgnore
  private Map<String, Number> result = new HashMap<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ObjectId objectId;

  public UpdateResult() {

  }

  public UpdateResult(int number, long updatedAt) {
    this.number = number;
    this.updatedAt = updatedAt;
  }

  public UpdateResult(int number, Map result, ObjectId objectId) {
    this.number = number;
    this.result = result;
    this.objectId = objectId;
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  public long getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(long updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Map<String, Number> getResult() {
    return result;
  }

  public void setResult(Map<String, Number> result) {
    this.result = result;
  }

  public ObjectId getObjectId() {
    return objectId;
  }

  public void setObjectId(ObjectId objectId) {
    this.objectId = objectId;
  }

  @JsonProperty("updatedAt")
  public String updatedAtJsonString() {
    return DateUtils.encodeDate(new Date(this.updatedAt));
  }

  /**
   * @return result of Incs
   * @see com.maxleap.pandora.core.lasdata.LASUpdate#inc(String, Number)
   */
  @JsonAnyGetter
  public Map result() {
    return result;
  }

  @Override
  public String toString() {
    return "{" +
        "number:" + number +
        ", updatedAt:" + updatedAt +
        ", result:" + result +
        ", objectId:" + objectId +
        '}';
  }
}
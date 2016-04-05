package com.maxleap.domain.mongo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author sneaky
 * @see
 * @since 2.0.0
 */
public class BaseEntity<ID> implements Serializable {

//  @JsonSerialize(using = CustomJsonObjectIdSerializer.class)
  private ID objectId;

  private long createdAt = -1;
  private long updatedAt = -1;

  @JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
  public ID getObjectId() {
    return objectId;
  }

  public void setObjectId(ID objectId) {
    this.objectId = objectId;
  }

  public long getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("createdAt")
  @JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
  private String c() {
    return DateUtils.encodeDate(new Date(createdAt));
  }

  @JsonProperty("updatedAt")
  @JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
  private String u() {
    return DateUtils.encodeDate(new Date(updatedAt));
  }

  public void setCreatedAt(long createdAt) {
    this.createdAt = createdAt;
  }

  @JsonSetter
  public void setCreatedAt(String createdAt) {
    this.createdAt = DateUtils.parseDate(createdAt).getTime();
  }

  public long getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(long updatedAt) {
    this.updatedAt = updatedAt;
  }

  @JsonSetter
  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = DateUtils.parseDate(updatedAt).getTime();
  }
}

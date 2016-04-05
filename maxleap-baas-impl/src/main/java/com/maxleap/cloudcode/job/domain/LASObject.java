package com.maxleap.cloudcode.job.domain;

import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * User：poplar
 * Date：15/10/12
 */
public class LASObject implements Serializable {
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ObjectId objectId;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Long createdAt;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Long updatedAt;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Map ACL;

  public ObjectId objectId() {
    return objectId;
  }

  public void setObjectId(ObjectId objectId) {
    this.objectId = objectId;
  }

  @JsonProperty("objectId")
  public void setObjectId(String objectId) {
    if (objectId != null) this.objectId = new ObjectId(objectId);
  }

  @JsonProperty("objectId")
  public String objectIdString(){
    return objectId == null ? null : objectId.toString();
  }

  public long createdAt() {
    return createdAt;
  }

  public void setCreatedAt(Long createdAt) {
    this.createdAt = createdAt;
  }

  @JsonProperty("createdAt")
  public String createdAtString() {
    return createdAt == null ? null : DateUtils.encodeDate(new Date(createdAt));
  }

  @JsonProperty("createdAt")
  public void setCreatedAt(String createdAt) {
    if (createdAt != null) this.createdAt = DateUtils.parseDate(createdAt).getTime();
  }

  public long updateAt(){
    return this.updatedAt;
  }

  public void setUpdatedAt(Long updatedAt) {
    this.updatedAt = updatedAt;
  }


  @JsonProperty("updatedAt")
  public String updatedAtString() {
    return updatedAt == null ? null : DateUtils.encodeDate(new Date(updatedAt));
  }

  @JsonProperty("updatedAt")
  public void setUpdatedAt(String updatedAt) {
    if (updatedAt != null) this.updatedAt = DateUtils.parseDate(updatedAt).getTime();
  }


  public Map getACL() {
    return ACL;
  }

  @JsonProperty("ACL")
  public void setACL(Map ACL) {
    this.ACL = ACL;
  }

  @Override
  public String toString() {
    return "LASObject{" +
        "objectId=" + objectId +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        ", ACL=" + ACL +
        '}';
  }
}

package com.maxleap.domain.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * User: qinpeng
 * Date: 14-6-10
 * Time: 11:26
 */
public class LASPermission implements Serializable {

  /**
   * {"get":{"*":true},"update":{"*":true},"find":{"*":true},"delete":{"role:role002":true},"create":{"role:role002":true},"addField":{"role:role001":true}}
   */
  public static final String ROLE_PREFIX = "role:";

  private LASPermissionNode get = new LASPermissionNode();
  private LASPermissionNode update = new LASPermissionNode();
  private LASPermissionNode find = new LASPermissionNode();
  private LASPermissionNode delete = new LASPermissionNode();
  private LASPermissionNode create = new LASPermissionNode();
  private LASPermissionNode addField = new LASPermissionNode();

  @JsonProperty("class")
  private LASClassPermissionNode clazz = new LASClassPermissionNode();

  public static LASPermission instance() {
    return new LASPermission();
  }

  public LASPermission() {
  }

  public LASPermissionNode getGet() {
    return get;
  }

  public void setGet(LASPermissionNode get) {
    this.get = get;
  }

  public LASPermissionNode getUpdate() {
    return update;
  }

  public void setUpdate(LASPermissionNode update) {
    this.update = update;
  }

  public LASPermissionNode getFind() {
    return find;
  }

  public void setFind(LASPermissionNode find) {
    this.find = find;
  }

  public LASPermissionNode getDelete() {
    return delete;
  }

  public void setDelete(LASPermissionNode delete) {
    this.delete = delete;
  }

  public LASPermissionNode getCreate() {
    return create;
  }

  public void setCreate(LASPermissionNode create) {
    this.create = create;
  }

  public LASPermissionNode getAddField() {
    return addField;
  }

  public void setAddField(LASPermissionNode addField) {
    this.addField = addField;
  }

  public LASClassPermissionNode getClazz() {
    return clazz;
  }

  public void setClazz(LASClassPermissionNode clazz) {
    this.clazz = clazz;
  }
}

package com.maxleap.pandora.core.lasdata;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author sneaky
 * @since 3.1.5
 */
public class LASKeyInfo {
  private LASKeyType type;
  private String className;


  @JsonIgnore
  public static final String FIELD_TYPE = "type";
  @JsonIgnore
  public static final String FIELD_VALIDATOR = "validator";

  /**
   * if type is pointer or relation
   */
  @JsonIgnore
  public static final String FIELD_CLASS_NAME = "className";

  @JsonIgnore
  public static final String FIELD_REQUIRED = "required";

  public LASKeyInfo() {
  }

  public LASKeyInfo(LASKeyType type) {
    this.type = type;
  }

  public LASKeyInfo(LASKeyType type, String className) {
    this.type = type;
    this.className = className;
  }

  @JsonIgnore
  public boolean isPointer() {
    return this.type == LASKeyType.Pointer;
  }

  @JsonIgnore
  public boolean isNumber() {
    return this.type == LASKeyType.Number;
  }

  @JsonIgnore
  public boolean isArray() {
    return this.type == LASKeyType.Array;
  }

  @JsonIgnore
  public boolean isObject() {
    return this.type == LASKeyType.Object;
  }

  @JsonIgnore
  public boolean isFile() {
    return this.type == LASKeyType.File;
  }

  @JsonIgnore
  public boolean isRelation() {
    return this.type == LASKeyType.Relation;
  }

  @JsonIgnore
  public boolean isDate() {
    return this.type == LASKeyType.Date;
  }

  @JsonIgnore
  public boolean isString() {
    return this.type == LASKeyType.String;
  }

  @JsonIgnore
  public boolean isBinary() {
    return this.type == LASKeyType.Bytes;
  }

  @JsonIgnore
  public boolean isBoolean() {
    return this.type == LASKeyType.Boolean;
  }

  @JsonIgnore
  public boolean isGeoPoint() {
    return this.type == LASKeyType.GeoPoint;
  }

  @JsonIgnore
  public boolean isValidType() {
    return (isObject()
        || isArray()
        || isString()
        || isNumber())
        || isBoolean()
        || isFile()
        || isRelation()
        || isPointer()
        || isBinary()
        || isDate()
        || isGeoPoint();
  }

  public LASKeyType getType() {
    return type;
  }

  public void setType(LASKeyType type) {
    this.type = type;
  }

  public String getClassName() {
    return this.className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

}

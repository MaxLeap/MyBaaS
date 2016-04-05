package com.maxleap.pandora.core.lasdata;

/**
 * @author sneaky
 * @since 3.1.5
 */
public enum LASKeyType {

  String,

  Number,

  Boolean,

  Date,

  File,

  Array,

  Object,

  GeoPoint,

  Pointer,

  Relation,

  ACL,

  Bytes;

  public static final String KEY_OBJECT_TYPE = "__type";
}

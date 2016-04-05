package com.maxleap.cloudcode;

/**
 * User: yuyangning
 * Date: 8/13/14
 * Time: 5:59 PM
 */
public enum CCodeMethod {
  Create("create"), Update("update"), Delete("delete"), DeleteBatch("deleteBatch");

  private String alias;

  private CCodeMethod(String alias) {
    this.alias = alias;
  }

  public String alias() {
    return alias;
  }
}

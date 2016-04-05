package com.maxleap.cloudcode;

/**
 * User: yuyangning
 * Date: 8/11/14
 * Time: 2:28 PM
 */
public enum CCodeCategory {
  Function("function"), Job("job"), Mapping("entityManager"),Console("console");

  private String alias;

  CCodeCategory(String alias) {
    this.alias = alias;
  }

  public String alias() {
    return alias;
  }

}



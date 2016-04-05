package com.maxleap.cloudcode;


/**
 * User: yuyangning
 * Date: 8/11/14
 * Time: 2:31 PM
 */
public class CCodeEntity {

  private String appId;
  private String name;
  private CCodeCategory category;
  private Iterable parameters;

  private String address; //cloud code server address[ip:port]. eg:127.0.0.1:8080.

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CCodeCategory getCategory() {
    return category;
  }

  public void setCategory(CCodeCategory category) {
    this.category = category;
  }

  public Iterable getParameters() {
    return parameters;
  }

  public void setParameters(Iterable parameters) {
    this.parameters = parameters;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }
}

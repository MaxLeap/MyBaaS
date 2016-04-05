package com.maxleap.domain.auth;

import java.io.Serializable;

/**
 * Created by shunlv on 15-9-18.
 */
public class Principal implements Serializable {
  private String identifier;
  private IdentifierType type;

  public Principal(String identifier, IdentifierType type) {
    this.identifier = identifier;
    this.type = type;
  }

  public Principal() {
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public IdentifierType getType() {
    return type;
  }

  public void setType(IdentifierType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "Principal{" +
        "identifier='" + identifier + '\'' +
        ", type='" + type + '\'' +
        '}';
  }
}

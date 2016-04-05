package com.maxleap.lang;

/**
 * Created by shunlv on 16-2-18.
 */
public class Lang {
  private String code;
  private String name;
  private String nativeName;

  public String getNativeName() {
    return nativeName;
  }

  public void setNativeName(String nativeName) {
    this.nativeName = nativeName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
}

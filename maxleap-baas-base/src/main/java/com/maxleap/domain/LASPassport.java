package com.maxleap.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

/**
 * User: qinpeng
 * Date: 14-7-20
 * Time: 22:32
 */
public class LASPassport extends LASUser {

  public static final String FIELD_AVATAR = "avatar";
  public static final String FIELD_COIN = "coin";
  public static final String FIELD_POINT = "point";
  public static final String FIELD_STATUS = "status";
  public static final String FIELD_CAPTCHA = "captcha";
  public static final String FIELD_INSTALL_ID = "installId";

  public LASPassport() {
  }

  @JsonCreator
  public LASPassport(Map<String, Object> map) {
    super(map);
  }

  public String getAvatar() {
    return (String) getMap().get(FIELD_AVATAR);
  }

  public double getCoin() {
    return getMap().get(FIELD_COIN) != null ? (double) getMap().get(FIELD_COIN) : 0;
  }

  public long getPoint() {
    return getMap().get(FIELD_POINT) != null ? (long) getMap().get(FIELD_POINT) : 0;
  }

  public int getStatus() {
    return getMap().get(FIELD_STATUS) != null ? (int) getMap().get(FIELD_STATUS) : 0;
  }

  public Map<String, String> getCaptcha() {
    return (Map<String, String>) getMap().get(FIELD_CAPTCHA);
  }

  public void setAvatar(String avatar) {
    getMap().put(FIELD_AVATAR, avatar);
  }

  public void setCoin(double coin) {
    getMap().put(FIELD_COIN, coin);
  }

  public void setPoint(long point) {
    getMap().put(FIELD_POINT, point);
  }

  public void setStatus(int status) {
    getMap().put(FIELD_STATUS, status);
  }

  public void setCaptcha(Map<String, String> captcha) {
    getMap().put(FIELD_CAPTCHA, captcha);
  }

  public String getInstallId() {
    return (String) getMap().get(FIELD_INSTALL_ID);
  }

  public void setInstallId(String installId) {
    getMap().put(FIELD_INSTALL_ID, installId);
  }
}

package com.maxleap.domain;

import com.maxleap.domain.base.LASObject;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by ben on 9/17/15.
 * 2:25 PM
 * <p>
 * benkris1@126.com
 */
public class LASInstallation extends LASObject {
  public static final String FIELD_BADGE = "badge";
  public static final String FIELD_CHANNELS = "channels";
  public static final String FIELD_DEVICE_TOKEN = "deviceToken";
  public static final String FIELD_DEVICE_TYPE = "deviceType";
  public static final String FIELD_INSTALLATION_ID = "installationId";
  public static final String FIELD_TIME_ZONE = "timeZone";
  public static final String FIELD_PUSH_TYPE = "pushType";
  public static final String FIELD_DEVICE_ID = "deviceId";
  public static final String FIELD_DEVICE_NAME = "deviceName";
  public static final String FIELD_DEVICE_MODEL = "deviceModel";
  public static final String FIELD_OS_VERSION = "osVersion";
  public static final String FIELD_CARRIER_NAME = "carrierName";
  public static final String FIELD_LOCALE = "locale";
  public static final String FIELD_ISO_LANG = "stLang";

  public LASInstallation() {
  }

  @JsonCreator
  public LASInstallation(Map<String, Object> map) {
    super(map);
  }

  public String getPushType() {
    return get(FIELD_PUSH_TYPE) == null ? null : (String) get(FIELD_PUSH_TYPE);
  }

  public void setPushType(String pushType) {
    put(FIELD_PUSH_TYPE, pushType);
  }

  public Number getBadge() {
    return get(FIELD_BADGE) == null ? null : (Number) get(FIELD_BADGE);
  }

  public ArrayList getChannels() {
    return get(FIELD_CHANNELS) == null ? null : (ArrayList) get(FIELD_CHANNELS);
  }

  public String getDeviceToken() {
    return get(FIELD_DEVICE_TOKEN) == null ? null : (String) get(FIELD_DEVICE_TOKEN);
  }

  public String getDeviceType() {
    return get(FIELD_DEVICE_TYPE) == null ? null : (String) get(FIELD_DEVICE_TYPE);
  }

  public String getInstallationId() {
    return get(FIELD_INSTALLATION_ID) == null ? null : (String) get(FIELD_INSTALLATION_ID);
  }

  public String getTimeZone() {
    return get(FIELD_TIME_ZONE) == null ? null : (String) get(FIELD_TIME_ZONE);
  }

  public void setBadge(Number badge) {
    put(FIELD_BADGE, badge);
  }

  public void setChannels(ArrayList channels) {
    put(FIELD_CHANNELS, channels);
  }

  public void setDeviceToken(String deviceToken) {
    put(FIELD_DEVICE_TOKEN, deviceToken);
  }

  public void setDeviceType(String deviceType) {
    put(FIELD_DEVICE_TYPE, deviceType);
  }

  public void setInstallationId(String installationId) {
    put(FIELD_INSTALLATION_ID, installationId);
  }

  public void setTimeZone(String timeZone) {
    put(FIELD_TIME_ZONE, timeZone);
  }

  public String getDeviceId() {
    return get(FIELD_DEVICE_ID) == null ? null : (String) get(FIELD_DEVICE_ID);
  }

  public void setDeviceId(String deviceId) {
    put(FIELD_DEVICE_ID, deviceId);
  }

  public String getDeviceName() {
    return get(FIELD_DEVICE_NAME) == null ? null : (String) get(FIELD_DEVICE_NAME);
  }

  public void setDeviceName(String deviceName) {
    put(FIELD_DEVICE_NAME, deviceName);
  }

  public String getDeviceModel() {
    return get(FIELD_DEVICE_MODEL) == null ? null : (String) get(FIELD_DEVICE_MODEL);
  }

  public void setDeviceModel(String deviceModel) {
    put(FIELD_DEVICE_MODEL, deviceModel);
  }

  public String getOsVersion() {
    return get(FIELD_OS_VERSION) == null ? null : (String) get(FIELD_OS_VERSION);
  }

  public void setOsVersion(String osVersion) {
    put(FIELD_OS_VERSION, osVersion);
  }

  public String getCarrierName() {
    return get(FIELD_CARRIER_NAME) == null ? null : (String) get(FIELD_CARRIER_NAME);
  }

  public void setCarrierName(String carrierName) {
    put(FIELD_CARRIER_NAME, carrierName);
  }

  public String getLocale() {
    return get(FIELD_LOCALE) == null ? null : (String) get(FIELD_LOCALE);
  }

  public void setLocale(String locale) {
    put(FIELD_LOCALE, locale);
  }
}

package com.maxleap.domain;

import com.maxleap.domain.base.ObjectId;
import com.maxleap.domain.mongo.BaseEntity;

/**
 * Created by ben on 9/17/15.
 * 2:36 PM
 * <p/>
 * benkris1@126.com
 */
public class LASDevice extends BaseEntity<ObjectId> {
  private String deviceId;
  private String deviceName;
  private String deviceModel;
  private String deviceType;
  private String osVersion;
  private String carrierName;
  private String locale;

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public String getDeviceModel() {
    return deviceModel;
  }

  public void setDeviceModel(String deviceModel) {
    this.deviceModel = deviceModel;
  }

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

  public String getOsVersion() {
    return osVersion;
  }

  public void setOsVersion(String osVersion) {
    this.osVersion = osVersion;
  }

  public String getCarrierName() {
    return carrierName;
  }

  public void setCarrierName(String carrierName) {
    this.carrierName = carrierName;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }
}

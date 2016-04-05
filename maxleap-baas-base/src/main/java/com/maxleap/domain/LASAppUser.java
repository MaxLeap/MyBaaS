package com.maxleap.domain;

import com.maxleap.domain.base.ObjectId;
import com.maxleap.domain.mongo.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Created by.
 * User: ben
 * Date: 12/9/14
 * Time: 9:43 AM
 * Email:benkris1@gmail.com
 */
public class LASAppUser extends BaseEntity<ObjectId> {

  public static Logger logger = LoggerFactory.getLogger(LASAppUser.class);
  public static final String FIELD_BADGE = "badge";
  public static final String FIELD_CHANNEL = "channel";
  public static final String FIELD_DEVICE_TOKEN = "deviceToken";
  public static final String FIELD_DEVICE_TYPE = "deviceType";
  public static final String FIELD_AU_ID = "auId";//installationId
  public static final String FIELD_TIME_ZONE = "timeZone";
  public static final String FIELD_PUSH_TYPE = "pushType";
  public static final String FIELD_DEVICE_ID = "deviceId";
  public static final String FIELD_DEVICE_NAME = "deviceName";
  public static final String FIELD_DEVICE_MODEL = "deviceModel";
  public static final String FIELD_OS_VERSION = "osVersion";
  public static final String FIELD_CARRIER_NAME = "carrierName";
  public static final String FIELD_LOCALE = "locale";
  public static final String FIELD_LANGUAGE = "language";
  public static final String FIELD_TOTAL_PAYMENT = "totalPayment";
  public static final String FIELD_SEGMENTS = "segments";
  public static final String FIELD_TAGS = "tags";
  public static final String FIELD_APP_VERSION = "appVersion";
  public static final String FIELD_APP_ID = "appId";
  public static final String FIELD_GENDER = "gender";
  public static final String FIELD_AGE = "age";
  public static final String FIELD_L_SEGMENTS = "lSegments";
  public static final String FIELD_R_SEGMENTS = "rSegments";
  public static final String FIELD_LINKS = "links";
  public static final String FIELD_CURT_LINK = "curtLink";
  public static final String ADVERTISING_ID = "advId";
  public static final String LINK_TAGS = "linkTags";
  public static final String FIELD_DELETED = "deleted";
  public static final String FIELD_EMAILS = "emails";


  private String auId;
  private String appId;
  private String deviceType;
  private String channel;
  private String timeZone;
  private String appVersion;
  private String locale;
  private String osVersion;
  private String deviceId;
  private String deviceModel;
  private String language;
  private String deviceToken;
  private Double totalPayment = 0.0;
  private String advId;
  private Boolean deleted = false;
  @JsonIgnore
  private Set<String> lSegments;

  @JsonIgnore
  private Set<String> rSegments;

  private Set<String> tags;
  private String gender;
  private Number age;
  private String nation;
  private String state;
  private String pushType;
  private String deviceName;
  private Number badge;
  private List<String> links;
  private List<String> linkTags;
  private String curtLink;
  private List<String> emails;

  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  public List<String> getLinks() {
    return links;
  }

  public void setLinks(List<String> links) {
    this.links = links;
  }

  public String getAuId() {
    return auId;
  }

  public void setAuId(String auId) {
    this.auId = auId;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }


  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public String getAppVersion() {
    return appVersion;
  }

  public void setAppVersion(String appVersion) {
    this.appVersion = appVersion;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }
  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  public String getOsVersion() {
    return osVersion;
  }

  public void setOsVersion(String osVersion) {
    this.osVersion = osVersion;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  public String getDeviceModel() {
    return deviceModel;
  }

  public void setDeviceModel(String deviceModel) {
    this.deviceModel = deviceModel;
  }

  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  public Number getAge() {
    return age;
  }

  public void setAge(Number age) {
    this.age = age;
  }
  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  public String getNation() {
    return nation;
  }

  public void setNation(String nation) {
    this.nation = nation;
  }
  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  public Set<String> getTags() {
    return tags;
  }

  public void setTags(Set<String> tags) {
    this.tags = tags;
  }


  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  public String getPushType() {
    return pushType;
  }

  public void setPushType(String pushType) {
    this.pushType = pushType;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }
  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  public Number getBadge() {
    return badge;
  }

  public void setBadge(Number badge) {
    this.badge = badge;
  }

  @JsonProperty("lSegments")
  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  public Set<String> getlSegments() {
    return lSegments;
  }

  public void setlSegments(Set<String> lSegments) {
    this.lSegments = lSegments;
  }

  @JsonProperty("rSegments")
  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  public Set<String> getrSegments() {
    return rSegments;
  }

  public void setrSegments(Set<String> rSegments) {
    this.rSegments = rSegments;
  }

  public String getDeviceToken() {
    return deviceToken;
  }

  public void setDeviceToken(String deviceToken) {
    this.deviceToken = deviceToken;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public Double getTotalPayment() {
    return totalPayment;
  }

  public void setTotalPayment(Double totalPayment) {
    this.totalPayment = totalPayment;
  }
  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  public String getCurtLink() {
    return curtLink;
  }

  public void setCurtLink(String curtLink) {
    this.curtLink = curtLink;
  }


  public String getAdvId() {
    return advId;
  }

  public void setAdvId(String advId) {
    this.advId = advId;
  }

  public List<String> getLinkTags() {
    return linkTags;
  }

  public void setLinkTags(List<String> linkTags) {
    this.linkTags = linkTags;
  }

  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  public List<String> getEmails() {
    return emails;
  }

  public void setEmails(List<String> emails) {
    this.emails = emails;
  }

  public Boolean getDeleted() {
    return deleted;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  public static LASAppUser fromInstallation(LASInstallation zCloudInstallation){
    try{
      LASAppUser appUser = new LASAppUser();
      appUser.setAuId(zCloudInstallation.getInstallationId() == null ? null : zCloudInstallation.getInstallationId());
      appUser.setChannel(zCloudInstallation.get("downloadChannel") == null ? null :(String)zCloudInstallation.get("downloadChannel"));
      appUser.setDeviceId(zCloudInstallation.getDeviceId());
      appUser.setDeviceType(zCloudInstallation.getDeviceType());
      appUser.setDeviceToken(zCloudInstallation.getDeviceToken());
      appUser.setDeviceModel(zCloudInstallation.getDeviceModel());
      appUser.setLocale(zCloudInstallation.getLocale());
      appUser.setOsVersion(zCloudInstallation.getOsVersion());
      appUser.setTimeZone(zCloudInstallation.getTimeZone());
      appUser.setPushType(zCloudInstallation.getPushType());
      appUser.setDeviceName(zCloudInstallation.getDeviceName());
      appUser.setBadge(zCloudInstallation.getBadge());
      appUser.setLanguage(LASInstallation.FIELD_ISO_LANG == null ? null:(String)zCloudInstallation.get(LASInstallation.FIELD_ISO_LANG));
      appUser.setGender(zCloudInstallation.get(LASAppUser.FIELD_GENDER) == null ? null : (String) zCloudInstallation.get(LASAppUser.FIELD_GENDER));
      appUser.setAge(zCloudInstallation.get(LASAppUser.FIELD_AGE) == null ? null : (Number) zCloudInstallation.get(LASAppUser.FIELD_AGE));
      appUser.setAppVersion(zCloudInstallation.get(LASAppUser.FIELD_APP_VERSION) == null ? null : (String)zCloudInstallation.get(LASAppUser.FIELD_APP_VERSION));
      appUser.setNation(zCloudInstallation.getLocale());
      return appUser;
    }catch (Exception e){
      logger.error("installation transfer to appUser has occur error installId {}",zCloudInstallation.getInstallationId());
    }

    return null;

  }
}
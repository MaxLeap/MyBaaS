package com.maxleap.domain;

import com.maxleap.domain.base.ObjectId;
import com.maxleap.domain.mongo.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: qinpeng
 * Date: 14-5-6
 * Time: 15:00
 */
public class LASApplication extends BaseEntity<ObjectId> {
  public static final String FAQ_DEFAULT_LANG = "en";
  private String name;
  private String category;
  private String orgId;
  private List<String> orgUserIds;
  private List<String> blackList;
  private String secret;
  private String description;
  private String clientKey;
  private String javascriptKey;
  private String netKey;
  private String restAPIKey;
  private String masterKey;
  private double rateLimit = 500.0;

  public boolean enableCreateObject = true;

  private String defaultLang = "en";
  private LASAppMetadata metadata;
  private LASAppEmailConfig emailConfig;
  private LASAppAuthConfig authConfig;
  private LASAppPushConfig pushConfig;
  private LASAppFaqConfig faqConfig;
  private Map<LASAppStore, String> appStoreIds = new HashMap<>();
  private Map<LASAppPlatform, String> urlSchemas = new HashMap<>();
  private LASAppIAP iap;
  private int status = LASAppStatus.enabled.toInt();
  private LASAppType type;

  private String os;
  private Map<String, String> platforms = new HashMap<>();
  private Map<String, Boolean> isNew;

  private double userRating;
  private String appStoreId;
  private String urlSchema;

  private boolean enablePassport = true;
  private boolean cloudCodeMapping = false;
  private boolean orgDisabled;

  // App or Game
  private String appType;
  //当create一条记录的Class不存在的时候是否允许自动创建Class
  private boolean autoClassCreate;

  @JsonIgnore
  public static final String FIELD_NAME = "name";
  @JsonIgnore
  public static final String FIELD_STATUS = "status";
  @JsonIgnore
  public static final String FIELD_ORG_USER_IDS = "orgUserIds";
  @JsonIgnore
  public static final String FIELD_BLACK_LIST = "blackList";
  @JsonIgnore
  public static final String FIELD_ENABLE_PASSPORT = "enablePassport";
  @JsonIgnore
  public static final String FIELD_CLIENT_KEY = "clientKey";
  @JsonIgnore
  public static final String FIELD_MASTER_KEY = "masterKey";
  @JsonIgnore
  public static final String FIELD_REST_API_KEY = "restAPIKey";
  @JsonIgnore
  public static final String FIELD_RATE_LIMIT = "rateLimit";
  @JsonIgnore
  public static final String FIELD_ORG_ID = "orgId";
  @JsonIgnore
  public static final String FIELD_IAP = "iap";
  @JsonIgnore
  public static final String FIELD_ORG_DISABLED = "orgDisabled";
  @JsonIgnore
  public static final String FIELD_DESCRIPTION = "description";
  @JsonIgnore
  public static final String FIELD_METADATA = "metadata";
  @JsonIgnore
  public static final String FIELD_IS_NEW = "isNew";
  @JsonIgnore
  public static final String FIELD_APP_TYPE = "appType";
  @JsonIgnore
  public static final String FIELD_URL_SCHEMA = "urlSchema";
  @JsonIgnore
  public static final String FIELD_APP_STORE_ID = "appStoreId";
  @JsonIgnore
  public static final String FIELD_CATEGORY = "category";
  @JsonIgnore
  public static final String FIELD_PLATFORMS = "platforms";
  @JsonIgnore
  public static final String FIELD_APP_STORE_IDS = "appStoreIds";
  @JsonIgnore
  public static final String FIELD_URL_SCHEMAS = "urlSchemas";
  @JsonIgnore
  public static final String FIELD_UPDATED_AT = "updatedAt";

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getClientKey() {
    return clientKey;
  }

  public void setClientKey(String clientKey) {
    this.clientKey = clientKey;
  }

  public String getJavascriptKey() {
    return javascriptKey;
  }

  public void setJavascriptKey(String javascriptKey) {
    this.javascriptKey = javascriptKey;
  }

  public String getNetKey() {
    return netKey;
  }

  public void setNetKey(String netKey) {
    this.netKey = netKey;
  }

  public String getRestAPIKey() {
    return restAPIKey;
  }

  public void setRestAPIKey(String restAPIKey) {
    this.restAPIKey = restAPIKey;
  }

  public String getMasterKey() {
    return masterKey;
  }

  public void setMasterKey(String masterKey) {
    this.masterKey = masterKey;
  }

  public String getSecret() {
    return secret;
  }

  public double getRateLimit() {
    return rateLimit;
  }

  public void setRateLimit(double rateLimit) {
    this.rateLimit = rateLimit;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }


  public LASAppEmailConfig getEmailConfig() {
    return emailConfig;
  }

  public void setEmailConfig(LASAppEmailConfig emailConfig) {
    this.emailConfig = emailConfig;
  }

  public boolean isEnableCreateObject() {
    return enableCreateObject;
  }

  public void setEnableCreateObject(boolean enableCreateObject) {
    this.enableCreateObject = enableCreateObject;
  }

  public LASAppMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(LASAppMetadata metadata) {
    this.metadata = metadata;
  }

  public LASAppAuthConfig getAuthConfig() {
    return authConfig;
  }

  public void setAuthConfig(LASAppAuthConfig authConfig) {
    this.authConfig = authConfig;
  }

  public LASAppPushConfig getPushConfig() {
    return pushConfig;
  }

  public void setPushConfig(LASAppPushConfig pushConfig) {
    this.pushConfig = pushConfig;
  }

  public List<String> getOrgUserIds() {
    return orgUserIds;
  }

  public void setOrgUserIds(List<String> orgUserIds) {
    this.orgUserIds = orgUserIds;
  }

  public List<String> getBlackList() {
    return blackList;
  }

  public void setBlackList(List<String> blackList) {
    this.blackList = blackList;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public Map<String, Boolean> getIsNew() {
    return isNew;
  }

  public void setIsNew(Map<String, Boolean> isNew) {
    this.isNew = isNew;
  }

  public LASAppType getType() {
    return type;
  }

  public void setType(LASAppType type) {
    this.type = type;
  }

  public double getUserRating() {
    return userRating;
  }

  public void setUserRating(double userRating) {
    this.userRating = userRating;
  }

  public String getAppStoreId() {
    return appStoreId;
  }

  public void setAppStoreId(String appStoreId) {
    this.appStoreId = appStoreId;
  }

  public String getUrlSchema() {
    return urlSchema;
  }

  public void setUrlSchema(String urlSchema) {
    this.urlSchema = urlSchema;
  }

  public boolean isEnablePassport() {
    return enablePassport;
  }

  public void setEnablePassport(boolean enablePassport) {
    this.enablePassport = enablePassport;
  }


  public boolean isCloudCodeMapping() {
    return cloudCodeMapping;
  }

  public void setCloudCodeMapping(boolean cloudCodeMapping) {
    this.cloudCodeMapping = cloudCodeMapping;
  }

  public Map<LASAppStore, String> getAppStoreIds() {
    return appStoreIds;
  }

  public void setAppStoreIds(Map<LASAppStore, String> appStoreIds) {
    this.appStoreIds = appStoreIds;
  }

  public Map<LASAppPlatform, String> getUrlSchemas() {
    return urlSchemas;
  }

  public void setUrlSchemas(Map<LASAppPlatform, String> urlSchemas) {
    this.urlSchemas = urlSchemas;
  }

//    @Override
//    public String toString() {
//        return MongoJsons.serialize(this);
//    }

  public Map<String, String> getPlatforms() {
    return platforms;
  }

  public void setPlatforms(Map<String, String> platforms) {
    this.platforms = platforms;
  }

  public LASAppFaqConfig getFaqConfig() {
    return faqConfig;
  }

  public void setFaqConfig(LASAppFaqConfig faqConfig) {
    this.faqConfig = faqConfig;
  }

  public LASAppIAP getIap() {
    return iap;
  }

  public void setIap(LASAppIAP iap) {
    this.iap = iap;
  }

  public boolean isOrgDisabled() {
    return orgDisabled;
  }

  public void setOrgDisabled(boolean orgDisabled) {
    this.orgDisabled = orgDisabled;
  }

  public String getDefaultLang() {
    return defaultLang;
  }

  public void setDefaultLang(String defaultLang) {
    this.defaultLang = defaultLang;
  }

  public String getOs() {
    return os;
  }

  public void setOs(String os) {
    this.os = os;
  }

  public String getAppType() {
    return appType;
  }

  public void setAppType(String appType) {
    this.appType = appType;
  }

  public boolean isAutoClassCreate() {
    return autoClassCreate;
  }

  public void setAutoClassCreate(boolean autoClassCreate) {
    this.autoClassCreate = autoClassCreate;
  }
}

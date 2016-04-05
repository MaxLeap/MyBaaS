package com.maxleap.application.service;

import com.maxleap.application.utils.APPKey;
import com.maxleap.domain.*;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.pandora.core.mongo.MongoQuery;
import com.maxleap.pandora.core.mongo.MongoUpdate;
import com.maxleap.pandora.data.support.MongoEntityManager;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shunlv on 16-2-18.
 */
@Singleton
public class ApplicationService {
  private MongoEntityManager mongoEntityManager;

  private static final String DB = "platform_data";
  private static final String TABLE = "zcloud_application";

  @Inject
  public ApplicationService(MongoEntityManager mongoEntityManager) {
    this.mongoEntityManager = mongoEntityManager;
  }

  public boolean validateAppName(String orgId, String appName) {
    MongoQuery query = new MongoQuery();
    query.equalTo(LASApplication.FIELD_ORG_ID, orgId)
        .equalTo(LASApplication.FIELD_STATUS, LASAppStatus.enabled.toInt())
        .equalTo(LASApplication.FIELD_NAME, appName);

    long count = mongoEntityManager.count(DB, TABLE, query);

    return count <= 0;
  }

  public LASApplication createApp(String orgUserId, LASApplication application) {
    LASApplication lasApplication = initAppForCreate();
    lasApplication.setName(application.getName());
    List<String> orgUsersIds = new ArrayList<>();
    orgUsersIds.add(orgUserId);
    lasApplication.setOrgUserIds(orgUsersIds);
    lasApplication.setOrgId(application.getOrgId());
    lasApplication.setOs(application.getOs());
    lasApplication.setAppType(StringUtils.isBlank(application.getAppType()) ? "App" : application.getAppType());
    lasApplication.setDefaultLang(application.getDefaultLang());

    return mongoEntityManager.create(DB, TABLE, lasApplication);
  }

  private LASApplication initAppForCreate() {
    LASApplication application = new LASApplication();
    application.setClientKey(APPKey.generate());
    application.setMasterKey(APPKey.generate());
    application.setRestAPIKey(APPKey.generate());
    application.setJavascriptKey(APPKey.generate());
    application.setNetKey(APPKey.generate());
    application.setSecret(APPKey.generate());
    application.setStatus(LASAppStatus.enabled.toInt());

    application.setCloudCodeMapping(false);

    LASAppAuthConfig authConfig = new LASAppAuthConfig();
    authConfig.setAnonymousUser(true);
    authConfig.setBaseUserName(true);
    authConfig.setDefaultAuth(true);
    authConfig.setTwitterAuth(authConfig.new TwitterAuth());
    authConfig.setFaceBookAuth(authConfig.new FaceBookAuth());
    authConfig.setWeiboAuth(authConfig.new WeiboAuth());
    authConfig.setWeixinAuth(authConfig.new WeixinAuth());
    authConfig.setQqAuth(authConfig.new TecentqqAuth());
    application.setAuthConfig(authConfig);

    LASAppMetadata metadata = new LASAppMetadata();
    metadata.setDesc("");
    metadata.setIcon("");
    metadata.setName("");
    metadata.setProduction(false);
    metadata.setUrl("");
    application.setMetadata(metadata);

    LASAppEmailConfig emailConfig = new LASAppEmailConfig();
    emailConfig.setVerifyEmail(true);
    application.setEmailConfig(emailConfig);

    LASAppPushConfig pushConfig = new LASAppPushConfig();
    pushConfig.setAppleConfig(pushConfig.new AppleCertificateConfig());
    pushConfig.setEnable(false);
    pushConfig.setMessage("");
    pushConfig.setWindowsConfig(pushConfig.new WindowsCertificateConfig());
    application.setPushConfig(pushConfig);

    return application;
  }

  public LASApplication findById(ObjectId objectId) {
    return mongoEntityManager.get(DB, TABLE, objectId, LASApplication.class);
  }

  public long count(MongoQuery query) {
    return mongoEntityManager.count(DB, TABLE, query);
  }

  public List<LASApplication> queryApp(MongoQuery query) {
    return mongoEntityManager.find(DB, TABLE, query, LASApplication.class);
  }

  public int updateById(ObjectId objectId, MongoUpdate update) {
    return mongoEntityManager.update(DB, TABLE, objectId, update);
  }

  public int update(MongoQuery query, MongoUpdate update) {
    return mongoEntityManager.update(DB, TABLE, query, update);
  }
}

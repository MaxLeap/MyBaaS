package com.maxleap.organization.service;

import com.maxleap.domain.LASOrgUser;
import com.maxleap.domain.LASSessionToken;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.exception.LASException;
import com.maxleap.pandora.core.mongo.MongoQuery;
import com.maxleap.pandora.core.mongo.MongoUpdate;
import com.maxleap.pandora.data.support.MongoEntityManager;
import com.maxleap.utils.EncryptUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by shunlv on 16-2-16.
 */
@Singleton
public class OrgUserService {
  private MongoEntityManager mongoEntityManager;
  private SessionTokenService sessionTokenService;
  private static final String DB = "platform_data";
  private static final String TABLE = "zcloud_organization_user";

  @Inject
  public OrgUserService(MongoEntityManager mongoEntityManager,
                        SessionTokenService sessionTokenService) {
    this.mongoEntityManager = mongoEntityManager;
    this.sessionTokenService = sessionTokenService;
  }

  public LASOrgUser createOrgUser(LASOrgUser orgUser) {
    return mongoEntityManager.create(DB, TABLE, orgUser);
  }

  public LASOrgUser getOrgUser(ObjectId objectId) {
    return mongoEntityManager.get(DB, TABLE, objectId, LASOrgUser.class);
  }

  public int updateOrgUser(ObjectId objectId, MongoUpdate update) {
    return mongoEntityManager.update(DB, TABLE, objectId, update);
  }

  public int updateOrgUser(MongoQuery query, MongoUpdate update) {
    return mongoEntityManager.update(DB, TABLE, query, update);
  }

  public int deleteOrgUser(ObjectId objectId) {
    return mongoEntityManager.delete(DB, TABLE, objectId);
  }

  public List<LASOrgUser> queryOrgUser(MongoQuery query) {
    return mongoEntityManager.find(DB, TABLE, query, LASOrgUser.class);
  }

  public LASOrgUser login(String loginid, String password) {
    MongoQuery query = new MongoQuery();
    query.equalTo(LASOrgUser.FIELD_EMAIL, loginid)
        .notEqualTo(LASOrgUser.FIELD_DELETED, true)
        .notEqualTo(LASOrgUser.FIELD_ORG_DISABLED, true)
        .notEqualTo(LASOrgUser.FIELD_ENABLED, false);

    LASOrgUser orgUser = mongoEntityManager.findUniqueOne(DB, TABLE, query, LASOrgUser.class);
    if (orgUser == null) {
      throw new LASException(LASException.OBJECT_NOT_FOUND, "can't find orgUser with email: " + loginid);
    }

    if (!orgUser.isEmailVerified()) {
      throw new LASException(LASException.EMAIL_NOT_VERIFIED, loginid + " not verified.");
    }

    if (!EncryptUtils.checkPassword(password, orgUser.getPassword())) {
      throw new LASException(LASException.PASSWORD_MISMATCH, "The username and password mismatch.");
    }

    LASSessionToken sessionToken = sessionTokenService.getTokenForUser(orgUser.getObjectId().toHexString(), orgUser.getOrgId(), orgUser.getUserType());
    orgUser.setSessionToken(sessionToken.getToken());

    return orgUser;
  }

  public long count(MongoQuery query) {
    return mongoEntityManager.count(DB, TABLE, query);
  }
}

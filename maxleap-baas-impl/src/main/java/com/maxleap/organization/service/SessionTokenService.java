package com.maxleap.organization.service;

import com.maxleap.domain.LASSessionToken;
import com.maxleap.pandora.core.mongo.MongoQuery;
import com.maxleap.pandora.data.support.MongoEntityManager;
import com.maxleap.utils.SessionTokenUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by shunlv on 16-2-16.
 */
@Singleton
public class SessionTokenService {
  private final MongoEntityManager mongoEntityManager;

  private static final String DB = "passport";
  private static final String TABLE = "zcloud_session_token";

  @Inject
  public SessionTokenService(MongoEntityManager mongoEntityManager) {
    this.mongoEntityManager = mongoEntityManager;
  }

  public LASSessionToken getTokenForUser(String userId, String orgId, int type) {
    MongoQuery query = new MongoQuery();
    query.equalTo(LASSessionToken.FIELD_USER_ID, userId)
        .equalTo(LASSessionToken.FIELD_ORG_ID, orgId)
        .equalTo(LASSessionToken.FIELD_TYPE, type)
        .greaterThan(LASSessionToken.FIELD_EXPIRED_AT, System.currentTimeMillis());

    List<LASSessionToken> sessionTokens = mongoEntityManager.find(DB, TABLE, query, LASSessionToken.class);
    if (sessionTokens == null || sessionTokens.isEmpty()) {
      return createSessionToken(SessionTokenUtils.genTokenForUser(userId, orgId, type));
    }

    return sessionTokens.get(0);
  }

  public LASSessionToken createSessionToken(LASSessionToken lasSessionToken) {
    return mongoEntityManager.create(DB, TABLE, lasSessionToken);
  }

  public int delete(String userId, String orgId) {
    MongoQuery query = new MongoQuery();
    query.equalTo(LASSessionToken.FIELD_USER_ID, userId)
        .equalTo(LASSessionToken.FIELD_ORG_ID, orgId);
    return mongoEntityManager.delete(DB, TABLE, query);
  }
}

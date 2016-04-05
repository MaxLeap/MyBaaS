package com.maxleap.cerberus.acl.spi;

import com.maxleap.domain.auth.LASAccessPair;

/**
 * Created by shunlv on 16-2-3.
 */
public interface AccessControlService {

  public LASAccessPair authenticate(AccessRequest request);

  public LASAccessPair authenticateSessionToken(String sessionToken);

  public LASAccessPair authenticateSessionToken(String appId, String sessionToken);

  public LASAccessPair authenticateApiKey(String appId, String apiKey);

  public LASAccessPair authenticateMasterKey(String appId, String masterKey);

  public LASAccessPair authenticateClientKey(String appId, String clientKey);

  public LASAccessPair authenticateSign(String appId, String sign);

}

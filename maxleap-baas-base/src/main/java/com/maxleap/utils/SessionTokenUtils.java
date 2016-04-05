package com.maxleap.utils;

import com.maxleap.domain.LASSessionToken;
import com.maxleap.domain.LASUserType;
import org.apache.commons.codec.binary.Base64;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by shunlv on 16-2-17.
 */
public class SessionTokenUtils {
  public final static long SESSIONTOKEN_EXPIRATION = 24 * 180;
  public final static long SESSIONTOKEN_EXPIRATION_LONG_TIME = 24 * 365 * 100L;
  public final static long NEVER_EXPIRATION = -1;

  public static LASSessionToken genTokenForUser(String userId, String orgId, int userType) {
    LASSessionToken token = new LASSessionToken();

    token.setToken(generateAccessToken(userId));
    token.setExpireAt(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(SESSIONTOKEN_EXPIRATION));
    token.setType(userType);
    token.setUserId(userId);
    token.setOrgId(orgId);
    return token;
  }

  public static LASSessionToken genLongTimeTokenForUser(String userId, String orgId, LASUserType userType) {
    LASSessionToken token = new LASSessionToken();

    token.setToken(generateAccessToken(userId));
    token.setExpireAt(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(SESSIONTOKEN_EXPIRATION_LONG_TIME));
    token.setType(userType.toInt());
    token.setUserId(userId);
    token.setOrgId(orgId);
    return token;
  }

  private static String generateAccessToken(String identity) {
    ByteBuffer buffer = ByteBuffer.allocate(32);
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.reset();
      md.update(identity.getBytes(Charset.forName("UTF8")));
      buffer.put(md.digest());
    } catch (NoSuchAlgorithmException e) {

    }
    UUID uuid = UUID.randomUUID();
    buffer.putLong(uuid.getMostSignificantBits());
    buffer.putLong(uuid.getLeastSignificantBits());
    return Base64.encodeBase64URLSafeString(buffer.array());
  }

  public static boolean canAccess(LASSessionToken sessionToken) {
    if (sessionToken == null) {
      return false;
    }
    long expirationTime = sessionToken.getExpireAt();
    if (expirationTime == NEVER_EXPIRATION) {
      return true;
    }
    if (expirationTime < System.currentTimeMillis()) {
      //delete sessionToken from db.
      return false;
    }
    return true;
  }
}

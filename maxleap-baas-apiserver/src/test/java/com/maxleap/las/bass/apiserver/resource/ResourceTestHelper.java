package com.maxleap.las.bass.apiserver.resource;

import com.maxleap.platform.LASConstants;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class ResourceTestHelper {
  public static String orgId = "53aa3fd33004119b351c0d46";
  public static String orgUserId = "53a8fb1530042872f0bf39d2";
  public static String apiKey = "U6j7JTAEKHLwvznXAg";
  public static String appId = "53a8fb2530042872f0bf39db";
  public static String masterKey = "U6j7JTAEKHLwvznWEA";
  public static String sessionToken = "JDzdA_htWM93VEGgg1sAE6SwNuD8TRHjp5U8FcK-2cI";

  public static final String HOST = "http://localhost:10086/2.0";

  public static String path(String uri) {
    return HOST + uri;
  }

  public static Map headers() {
    Map<String, Object> header = new HashMap<>();
    header.put("Content-Type", MediaType.APPLICATION_JSON);
    header.put(LASConstants.HEADER_MAXLEAP_APPID, appId);
    header.put(LASConstants.HEADER_MAXLEAP_MASTERKEY, masterKey);
    header.put(LASConstants.HEADER_MAXLEAP_SESSIONTOKEN, sessionToken);
    return header;
  }

  public static String objectId(Map result) {
    return (String) result.get(LASConstants.KEY_OBJECT_ID);
  }

  public static int errorCode(Map error) {
    return (int) error.get(LASConstants.KEY_ERROR_CODE);
  }

}

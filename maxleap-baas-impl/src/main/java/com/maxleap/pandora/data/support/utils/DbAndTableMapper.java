package com.maxleap.pandora.data.support.utils;


import com.maxleap.domain.base.ObjectId;

/**
 * @author sneaky
 * @since 1.0
 */
public class DbAndTableMapper {

  public static String getDB(ObjectId appId) {
    return appId.toString();
  }

  public static String getTable(ObjectId appId, String className) {
    StringBuilder sb = new StringBuilder(translateCase2Underscore(className));
    sb.append("_");
    sb.append(appId);

    return sb.toString();
  }

  public static ObjectId getAppId(String collectionName) {
    int i = collectionName.lastIndexOf("_");
    if (i < 0) {
      throw new IllegalArgumentException("Invalid collectionName: " + collectionName);
    }
    String appId = collectionName.substring(i + 1);
    if (ObjectId.isValid(appId)) {
      return new ObjectId(appId);
    } else {
      throw new IllegalArgumentException("Invalid collectionName: " + collectionName);
    }
  }

  private static String translateCase2Underscore(String name) {
    while (name.charAt(0) == '_') {
      name = name.substring(1);
    }
    char[] chars = name.toCharArray();
    StringBuilder sb = new StringBuilder();
    boolean flag = false;
    if (isUpperCase(chars[0])) {
      sb.append(Character.toLowerCase(chars[0]));
    } else {
      sb.append(chars[0]);
      flag = true;
    }
    int serial = 0;
    for (int i = 1; i < chars.length; i++) {
      if (isUpperCase(chars[i])) {
        if (++serial == 1) {
          sb.append("_");
        }
        sb.append(Character.toLowerCase(chars[i]));
      } else {
        serial = 0;
        sb.append(chars[i]);
      }
    }
    if (flag) {
      sb.append("_l");
    }
    return sb.toString();
  }

  private static boolean isUpperCase(char c) {
    if (c <= 'Z' && c >= 'A') {
      return true;
    }

    return false;
  }
}

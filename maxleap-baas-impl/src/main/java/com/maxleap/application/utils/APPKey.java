package com.maxleap.application.utils;

import org.apache.commons.codec.binary.Base64;
import org.bson.types.ObjectId;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.UUID;

/**
 * Created by shunlv on 16-2-18.
 */
public class APPKey {
  public static final int CLIENT_KEY = 1;

  public static final int REST_API_KEY = 2;

  public static final int JAVASCRIPT_KEY = 4;

  public static final int NET_KEY = 8;

  public static final int MASTER_KEY = 16;

  public static final int SECRET = 32;

  private int keyType;
  private int time;

  private String key;

  public static void main(String[] args) {
    String key = generate();
    System.out.println(key);
  }

  public APPKey(String key) {
    try {

      byte[] bytes = Base64.decodeBase64(key);
      byte[] keys = new byte[bytes.length - 1];

      for (int i = 0; i < keys.length; i++) {
        keys[i] = bytes[i];
      }

      ObjectId objectId = new ObjectId(keys);
      this.time = objectId.getTimestamp();
      this.keyType = bytes[12];
      this.key = key;

    } catch (Exception e) {
      throw new RuntimeException("app key is invalid!");
    }
  }

  private APPKey(int keyType, int time, String key) {
    this.keyType = keyType;
    this.time = time;
    this.key = key;
  }

  /**
   * @param keyType
   * @return
   */
  public static APPKey generate(int keyType) {
    checkKeyType(keyType);

    ByteBuffer buffer = ByteBuffer.allocate(13);
    int i = 0;
    while (true) {
      ObjectId objectId = ObjectId.get();

      buffer.put(objectId.toByteArray());
      buffer.put((byte) keyType);

      String key = Base64.encodeBase64URLSafeString(buffer.array());
      if (key.indexOf("-") == -1 || i++ == 100) {
        return new APPKey(keyType, objectId.getTimestamp(), key);
      }
      buffer.clear();
    }
  }

  public static String generate() {
    String uuid = UUID.randomUUID().toString();
    String key = null;
    for (int i = 0; i < 10; i++) {
      key = Base64.encodeBase64URLSafeString(MD5.md5(uuid).getBytes());
      if (key.indexOf('-') == -1) {
        return key;
      }
    }
    return key;
  }

  /**
   * Gets the timestamp (number of seconds since the Unix epoch).
   *
   * @return the timestamp
   */
  public int getTimestamp() {
    return time;
  }

  /**
   * Gets the timestamp as a {@code Date} instance.
   *
   * @return the Date
   */
  public Date getDate() {
    return new Date(time * 1000L);
  }

  public static boolean checkKeyType(int keyType) {
    switch (keyType) {
      case CLIENT_KEY:
      case REST_API_KEY:
      case JAVASCRIPT_KEY:
      case NET_KEY:
      case MASTER_KEY:
      case SECRET:
        return true;
    }

    throw new IllegalArgumentException("keyType is invalid!!");

  }

  public String key() {
    return key;
  }

  public int keyType() {
    return keyType;
  }

  @Override
  public String toString() {
    return key;
  }
}

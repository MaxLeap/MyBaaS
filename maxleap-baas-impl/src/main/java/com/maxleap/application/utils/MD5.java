package com.maxleap.application.utils;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by shunlv on 16-2-18.
 */
public class MD5 {
  private final static Logger LOG = LoggerFactory.getLogger(MD5.class);

  public static String md5(String str) {
    MessageDigest messageDigest = null;
    try {
      messageDigest = MessageDigest.getInstance("MD5");
      messageDigest.reset();
      messageDigest.update(str.getBytes("UTF-8"));
    } catch (NoSuchAlgorithmException e) {
      LOG.error("NoSuchAlgorithmException caught!");
    } catch (UnsupportedEncodingException e) {
      LOG.error("UnsupportedEncodingException", e);
    }

    byte[] byteArray = messageDigest.digest();
    return Base64.encodeBase64URLSafeString(byteArray);
  }
}

package com.maxleap.pandora.data.support.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @author sneaky
 * @since 3.0.0
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
      LOG.error("UnsupportedEncodingException",e);
    }

    byte[] byteArray = messageDigest.digest();
    return Base64.getEncoder().encodeToString(byteArray);
  }
}

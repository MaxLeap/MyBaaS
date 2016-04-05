package com.maxleap.utils;

import org.jasypt.util.password.BasicPasswordEncryptor;

/**
 * Created by shunlv on 16-2-16.
 */
public class EncryptUtils {
  private static BasicPasswordEncryptor encryptor = new BasicPasswordEncryptor();

  public static String encryptPassword(final String password) {
    return encryptor.encryptPassword(password);
  }

  public static boolean checkPassword(final String plainPassword,
                                      final String encryptedPassword) {
    return encryptor.checkPassword(plainPassword, encryptedPassword);
  }
}

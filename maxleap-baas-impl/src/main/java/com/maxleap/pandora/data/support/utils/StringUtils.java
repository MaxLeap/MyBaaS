package com.maxleap.pandora.data.support.utils;

/**
 * @author sneaky
 * @since 3.0.0
 */
public class StringUtils {
  public static String[] delimitedListToStringArray(String str, String delimiter) {
    if (str == null) {
      return new String[0];
    }
    if (delimiter == null) {
      return new String[]{str};
    }
    String[] split = null;
    if (".".equals(delimiter)) {
      split = str.split("\\.");
    } else {
      split = str.split(delimiter);
    }
    if (split.length == 0) {
      return new String[]{str};
    } else {
      return split;
    }
  }
}

package com.maxleap.pandora.core.utils;

import java.util.List;
import java.util.Map;

/**
 * @author sneaky
 * @since 3.1.0
 */
public class Assertions {
  /**
   * Throw IllegalArgumentException if the value is null.
   *
   * @param name  the parameter name
   * @param value the value that should not be null
   * @return the value
   * @throws IllegalArgumentException if value is null
   */
  public static String notBlank(final String name, final String value) {
    if (value == null || value.length() == 0) {
      throw new IllegalArgumentException(name + " can not be empty");
    }
    return value;
  }

  /**
   * Throw IllegalArgumentException if the value is null.
   *
   * @param name  the parameter name
   * @param value the value that should not be null
   * @param <T>   the value type
   * @return the value
   * @throws IllegalArgumentException if value is null
   */
  public static <T> T notNull(final String name, final T value) {
    if (value == null) {
      throw new IllegalArgumentException(name + " can not be null");
    }
    return value;
  }

  /**
   * Throw IllegalArgumentException if the value is empty.
   *
   * @param name  the parameter name
   * @param value the value that should not be null
   * @return the value
   * @throws IllegalArgumentException if value is null
   */
  public static List notEmpty(final String name, final List value) {
    if (value == null || value.isEmpty()) {
      throw new IllegalArgumentException(name + " can not be empty");
    }
    return value;
  }

  /**
   * Throw IllegalArgumentException if the value is empty.
   *
   * @param name  the parameter name
   * @param value the value that should not be null
   * @return the value
   * @throws IllegalArgumentException if value is null
   */
  public static Map notEmpty(final String name, final Map value) {
    if (value == null || value.isEmpty()) {
      throw new IllegalArgumentException(name + " can not be empty");
    }
    return value;
  }
}

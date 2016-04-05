package com.maxleap.pandora.config;

/**
 * @author sneaky
 * @since 1.0.0
 */
public enum DataSourceStatus {
  /**
   * Not exists.
   */
  NONE,

  /**
   * Status normally
   */
  ENABLE,

  /**
   * Disable, many reasons.
   */
  DISABLE,

  /**
   * DataSource had deleted
   */
  DELETE,

  /**
   * Just readonly, can't write, delete, update.
   */
  READONLY,

  /**
   * Many reasons, data server is down, upgrade etc.
   */
  MAINTAIN
}

package com.maxleap.pandora.config;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class HostAndPort {
  private final String host;
  private final int port;

  public HostAndPort(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  @Override
  public String toString() {
    return "HostAndPort{" +
        "host='" + host + '\'' +
        ", port=" + port +
        '}';
  }
}

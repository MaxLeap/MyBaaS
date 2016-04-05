package com.maxleap.pandora.config.mgo;

import com.maxleap.pandora.config.DataSourceStatus;
import com.maxleap.pandora.config.HostAndPort;
import com.maxleap.pandora.config.Utils;
import com.mongodb.ServerAddress;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class MgoCluster {
  private String name;
  private String urls;
  private DataSourceStatus status;

  public MgoCluster(String name, String urls, DataSourceStatus status) {
    this.name = name;
    this.urls = urls;
    this.status = status;
  }

  public String getName() {
    return name;
  }


  public void setName(String name) {
    this.name = name;
  }

  public String getUrls() {
    return urls;
  }


  public void setUrls(String urls) {
    this.urls = urls;
  }

  public DataSourceStatus getStatus() {
    return status;
  }

  public DataSourceStatus getStatusAsEnum() {
    return status;
  }

  public void setStatus(DataSourceStatus status) {
    this.status = status;
  }

  public List<HostAndPort> listUrl() {
    return Utils.listUrl(urls, 27017);
  }

  public List<ServerAddress> listServerAddress() {
    return  listUrl().stream().map(h -> new ServerAddress(h.getHost(), h.getPort())).collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "MgoCluster{" +
        "name='" + name + '\'' +
        ", urls='" + urls + '\'' +
        ", status=" + status +
        '}';
  }
}

package com.maxleap.pandora.config.mgo;

import com.maxleap.pandora.config.DataSourceStatus;

/**
 * @author sneaky
 * @since 3.3.0
 */
public class MgoDatabase {
  private MgoCluster mgoCluster;

  private String name;
  private DataSourceStatus status;

  public MgoCluster getMgoCluster() {
    return mgoCluster;
  }

  public void setMgoCluster(MgoCluster mgoCluster) {
    this.mgoCluster = mgoCluster;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DataSourceStatus getStatus() {
    return status;
  }

  public void setStatus(DataSourceStatus status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "MgoDatabase{" +
        "mgoCluster=" + mgoCluster +
        ", name=" + name +
        ", status=" + status +
        '}';
  }
}

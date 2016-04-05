package com.maxleap.pandora.config.mgo;

import com.maxleap.pandora.config.ClusterVisitor;
import com.maxleap.pandora.config.DataSourceStatus;
import com.maxleap.pandora.config.Funcs;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author sneaky
 * @since 1.0.0
 */
@Singleton
public class MgoClusterVisitor implements ClusterVisitor<MgoCluster> {
  private ConcurrentMap<String, MgoCluster> mgoClusterConcurrentMap = new ConcurrentHashMap<>();
  private Lock lock = new ReentrantLock();

  @Inject
  public MgoClusterVisitor() {
  }

  @Override
  public MgoCluster get(String clusterName) {
    return Funcs.get(clusterName, lock, mgoClusterConcurrentMap, () -> new MgoCluster(clusterName, System.getProperty("mongo.urls", "1.default.mgo.las,3.default.mgo.las"), DataSourceStatus.ENABLE));
  }

  @Override
  public List<MgoCluster> listAll() {
    throw new UnsupportedOperationException("unsupported");
  }

}

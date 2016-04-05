package com.maxleap.pandora.config.mgo;

import com.maxleap.pandora.config.ClusterVisitor;
import com.maxleap.pandora.config.DataSourceStatus;
import com.maxleap.pandora.config.DatabaseVisitor;
import com.maxleap.pandora.config.Funcs;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author sneaky
 * @since 3.1.\
 */
@Singleton
public class MgoDatabaseVisitor implements DatabaseVisitor<MgoDatabase> {
  private ConcurrentMap<String, MgoDatabase> mgoDatabaseConcurrentHashMap = new ConcurrentHashMap<>();
  private Lock lock = new ReentrantLock();

  private ClusterVisitor<MgoCluster> mgoClusterVisitor;

  @Inject
  public MgoDatabaseVisitor(MgoClusterVisitor mgoClusterVisitor) {
    this.mgoClusterVisitor = mgoClusterVisitor;
  }

  @Override
  public MgoDatabase get(String db) {
    return Funcs.get(db, lock, mgoDatabaseConcurrentHashMap, () -> {
      MgoDatabase database = new MgoDatabase();
      database.setName(db);
      database.setStatus(DataSourceStatus.ENABLE);
      database.setMgoCluster(mgoClusterVisitor.get("default"));
      return database;
    });
  }
}

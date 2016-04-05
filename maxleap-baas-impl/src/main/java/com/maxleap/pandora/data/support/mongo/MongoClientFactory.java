package com.maxleap.pandora.data.support.mongo;

import com.maxleap.pandora.config.Funcs;
import com.maxleap.pandora.config.mgo.MgoDatabase;
import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author sneaky
 * @since 3.0.0
 */
@Singleton
public class MongoClientFactory {
  private final static Logger logger = LoggerFactory.getLogger(MongoClientFactory.class);

  private final static ConcurrentHashMap<String, MongoClientTuple> concurrentHashMap = new ConcurrentHashMap();
  private final static AtomicReference<Integer> cleanerIntervalMS = new AtomicReference<>();
  private final static AtomicReference<Integer> maxIdleMS = new AtomicReference<>();

  private final Lock lock = new ReentrantLock();

  static {
    cleanerIntervalMS.set(Integer.parseInt(System.getProperty("com.las.sun.mongoClient.cleanerIntervalMS", "600000")));
    maxIdleMS.set(Integer.parseInt(System.getProperty("com.las.sun.mongoClient.maxIdleMS", "18000000")));
  }

  @Inject
  public MongoClientFactory() {
    new MongoClientTupleMonitorThread().start();
  }

  public MongoClient get(MgoDatabase mgoDatabase) {
    return Funcs.get(mgoDatabase.getMgoCluster().getUrls(), lock, concurrentHashMap, () -> {
      MongoClient newOne = new MongoClient(mgoDatabase.getMgoCluster().listServerAddress());
      if (logger.isDebugEnabled()) {
        logger.debug("build new MongoClient: {}", mgoDatabase.getMgoCluster().getUrls());
      }
      return new MongoClientTuple(newOne);
    }).getMongoClient();
  }

  static class MongoClientTuple {
    MongoClient mongoClient;
    long lastVisit;

    MongoClientTuple(MongoClient mongoClient) {
      this.mongoClient = mongoClient;
      lastVisit = System.currentTimeMillis();
    }

    public MongoClient getMongoClient() {
      lastVisit = System.currentTimeMillis();
      return mongoClient;
    }

    public long getLastVisit() {
      return lastVisit;
    }
  }

  static class MongoClientTupleMonitorThread extends Thread {
    @Override
    public void run() {
      while (true) {
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<String, MongoClientTuple> mongoClientTupleEntry : concurrentHashMap.entrySet()) {
          MongoClientTuple mongoClientTuple = mongoClientTupleEntry.getValue();
          if (currentTime - mongoClientTuple.getLastVisit() > maxIdleMS.get()) {
            mongoClientTuple.getMongoClient().close();
            concurrentHashMap.remove(mongoClientTupleEntry.getKey());
            logger.info("Release mongo client: " + mongoClientTupleEntry.getKey());
          }
        }

        try {
          sleep(cleanerIntervalMS.get());
        } catch (InterruptedException e) {
          //ignore
        }
      }
    }
  }
}

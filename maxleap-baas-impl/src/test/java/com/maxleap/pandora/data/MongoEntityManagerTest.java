package com.maxleap.pandora.data;

import com.maxleap.domain.base.LASObject;
import com.maxleap.pandora.config.Funcs;
import com.maxleap.pandora.data.support.guice.PandoraModule;
import com.maxleap.pandora.data.support.MongoEntityManager;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.BeforeClass;

import java.util.List;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class MongoEntityManagerTest {
  public static MongoEntityManager mongoEntityManager;
  public static String db = "test_db";
  public static String table ="test_table";

  @BeforeClass
  public static void before() {
    Injector injector = Guice.createInjector(new PandoraModule());
    mongoEntityManager = injector.getInstance(MongoEntityManager.class);
  }

  @org.junit.Test
  public void testPerformance() {
    Funcs.timeMillis(10000000, () -> mongoEntityManager.getMongoCollection("test", "test"));
  }

  @org.junit.Test
  public void testInsertOne() {
    LASObject lasObject = new LASObject();
    lasObject.put("test001", "test001");
    LASObject re = mongoEntityManager.create(db, table, lasObject);
    System.out.println(re);
  }

  @org.junit.Test
  public void testFindAll() {
    List<LASObject> all = mongoEntityManager.getAll(db, table, LASObject.class);
    System.out.println(all);
  }
}

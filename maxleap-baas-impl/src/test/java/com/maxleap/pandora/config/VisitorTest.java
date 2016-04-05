package com.maxleap.pandora.config;

import com.maxleap.pandora.config.mgo.MgoDatabaseVisitor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class VisitorTest {
  public static ClusterVisitor mgoClusterVisitor;
  public static MgoDatabaseVisitor databaseVisitor;
  ObjectMapper objectMapper = new ObjectMapper();

  @BeforeClass
  public static void before() {
    Injector injector = Guice.createInjector(new ConfigModule());
    mgoClusterVisitor = injector.getInstance(ClusterVisitor.class);
    databaseVisitor = injector.getInstance(MgoDatabaseVisitor.class);
  }

  @org.junit.Test
  public void testPerformance() {
    Funcs.timeMillis(100000000, () -> mgoClusterVisitor.get("test001"));
  }

  @Test
  public void test() {
    Funcs.timeMillis(11111, s -> test001());
  }

  private void test001() {
    try {
      StringWriter writer = new StringWriter();
      JsonGenerator generator = objectMapper.getFactory().createGenerator(writer);
      generator.writeStartObject();
      generator.writeNumberField("errorcode", 1);
      generator.writeStringField("errorMsg", "test000111");
      generator.close();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }
}

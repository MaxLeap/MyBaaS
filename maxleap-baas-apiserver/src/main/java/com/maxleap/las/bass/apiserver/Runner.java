package com.maxleap.las.bass.apiserver;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.function.Consumer;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class Runner {
  private static final String WEB_EXAMPLES_DIR = "maxleap-baas-apiserver";
  private static final String WEB_EXAMPLES_JAVA_DIR = WEB_EXAMPLES_DIR + "/src/main/java/";

  public static void runMod(Class clazz) {
    runMod(WEB_EXAMPLES_JAVA_DIR, clazz, new VertxOptions().setClustered(false), null);
  }

  public static void runMod(Class clazz, DeploymentOptions options) {
    runMod(WEB_EXAMPLES_JAVA_DIR, clazz, new VertxOptions().setClustered(false), options);
  }

  public static void runMod(String dir, Class clazz, VertxOptions options, DeploymentOptions deploymentOptions) {
    runMod(dir + clazz.getPackage().getName().replace(".", "/"), clazz.getName(), options, deploymentOptions);
  }

  public static void runMod(String dir, String verticleID, VertxOptions options, DeploymentOptions deploymentOptions) {
    if (options == null) {
      // Default parameter
      options = new VertxOptions();
    }
    // Smart cwd detection

    System.setProperty("vertx.cwd", System.getProperty("user.dir"));
    Consumer<Vertx> runner = vertx -> {
      try {
        if (deploymentOptions != null) {
          vertx.deployVerticle(verticleID, deploymentOptions);
        } else {
          vertx.deployVerticle(verticleID);
        }
      } catch (Throwable t) {
        t.printStackTrace();
      }
    };
    if (options.isClustered()) {
      Vertx.clusteredVertx(options, res -> {
        if (res.succeeded()) {
          Vertx vertx = res.result();
          runner.accept(vertx);
        } else {
          res.cause().printStackTrace();
        }
      });
    } else {
      Vertx vertx = Vertx.vertx(options);
      runner.accept(vertx);
    }
  }
}

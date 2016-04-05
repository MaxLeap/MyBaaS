package com.maxleap.las.bass.apiserver;

import com.maxleap.application.AppModule;
import com.maxleap.cerberus.acl.ACLModule;
import com.maxleap.cloudcode.CloudCodeModule;
import com.maxleap.organization.OrgModule;
import com.maxleap.pandora.data.support.guice.PandoraModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.maxleap.las.bass.apiserver.resource.CloudCodeJobResource;
import com.maxleap.las.bass.apiserver.resource.CloudCodeResource;
import com.maxleap.las.bass.apiserver.resource.CloudDataResource;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class GuiceModel implements Module {
  private Vertx vertx;
  private JsonObject config;

  public GuiceModel(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.config = config;
  }

  @Override
  public void configure(Binder binder) {
    binder.install(new PandoraModule());
    binder.install(new ACLModule());
    binder.install(new OrgModule());
    binder.install(new AppModule());
    binder.bind(Vertx.class).toInstance(vertx);
    binder.bind(CloudDataResource.class);

    JsonObject cloudcodeConfig = config.getJsonObject("cloudcode", new JsonObject());
    String cloudcodeHost = cloudcodeConfig.getString("host", "127.0.0.1");
    int cloudcodePort = cloudcodeConfig.getInteger("port", 8080);
    binder.install(new CloudCodeModule("http://" + cloudcodeHost + ":" + cloudcodePort));

    binder.bind(CloudCodeResource.class);
    binder.bind(CloudCodeJobResource.class);
  }
}

package com.maxleap.las.bass.apiserver.resource;

import com.maxleap.cloudcode.CounterEntity;
import com.maxleap.cloudcode.LockEntity;
import com.maxleap.pandora.data.support.MongoJsons;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User：poplar
 * Date：15/8/26
 */
@Singleton
@Path("/2.0/themis")
public class CloudCodeThemisResource {
  private static final Logger logger = LoggerFactory.getLogger(CloudCodeThemisResource.class);

  private Map<CounterEntity,AtomicLong> counters = new HashMap<>();

  @POST
  @Path("count/:version/:name")
  @Consumes(MediaType.APPLICATION_JSON)
  public void generateCounterAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String version = context.request().getParam("version");
      String name = context.request().getParam("name");
      CounterEntity counterEntity = new CounterEntity(appId.toString(),version,name);
      if (!counters.containsKey(counterEntity)) counters.put(counterEntity,new AtomicLong());
      context.response().end(MongoJsons.serialize(counterEntity));
    });
  }

  @GET
  @Path("count/:version/:name")
  @Consumes(MediaType.APPLICATION_JSON)
  public void getCounterAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String version = context.request().getParam("version");
      String name = context.request().getParam("name");
      CounterEntity counterEntity = new CounterEntity(appId.toString(), version, name);
      if (!counters.containsKey(counterEntity)) counters.put(counterEntity, new AtomicLong());
      context.response().end(String.valueOf(counters.get(counterEntity)));
    });
  }

  @PUT
  @Path("count/incrementAndGet/:version/:name")
  @Consumes(MediaType.APPLICATION_JSON)
  public void incrementAndGetAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String version = context.request().getParam("version");
      String name = context.request().getParam("name");
      CounterEntity counterEntity = new CounterEntity(appId.toString(), version, name);
      if (!counters.containsKey(counterEntity)) counters.put(counterEntity, new AtomicLong(0));
      context.response().end(String.valueOf(counters.get(counterEntity).incrementAndGet()));
    });
  }

  @PUT
  @Path("count/getAndIncrement/:version/:name")
  @Consumes(MediaType.APPLICATION_JSON)
  public void getAndIncrementAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String version = context.request().getParam("version");
      String name = context.request().getParam("name");
      CounterEntity counterEntity = new CounterEntity(appId.toString(), version, name);
      if (!counters.containsKey(counterEntity)) counters.put(counterEntity, new AtomicLong(0));
      context.response().end(String.valueOf(counters.get(counterEntity).getAndIncrement()));
    });
  }

  @PUT
  @Path("count/decrementAndGet/:version/:name")
  @Consumes(MediaType.APPLICATION_JSON)
  public void decrementAndGetAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String version = context.request().getParam("version");
      String name = context.request().getParam("name");
      CounterEntity counterEntity = new CounterEntity(appId.toString(), version, name);
      if (!counters.containsKey(counterEntity)) counters.put(counterEntity, new AtomicLong(0));
      context.response().end(String.valueOf(counters.get(counterEntity).decrementAndGet()));
    });
  }

  @PUT
  @Path("count/addAndGet/:version/:name/:value")
  @Consumes(MediaType.APPLICATION_JSON)
  public void addAndGetAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String version = context.request().getParam("version");
      String name = context.request().getParam("name");
      String value = context.request().getParam("value");
      CounterEntity counterEntity = new CounterEntity(appId.toString(), version, name);
      if (!counters.containsKey(counterEntity)) counters.put(counterEntity, new AtomicLong(0));
      context.response().end(String.valueOf(counters.get(counterEntity).addAndGet(Long.valueOf(value))));
    });
  }

  @PUT
  @Path("count/getAndAdd/:version/:name/:value")
  @Consumes(MediaType.APPLICATION_JSON)
  public void getAndAddAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String version = context.request().getParam("version");
      String name = context.request().getParam("name");
      String value = context.request().getParam("value");
      CounterEntity counterEntity = new CounterEntity(appId.toString(), version, name);
      if (!counters.containsKey(counterEntity)) counters.put(counterEntity, new AtomicLong(0));
      context.response().end(String.valueOf(counters.get(counterEntity).getAndAdd(Long.valueOf(value))));
    });
  }

  @POST
  @Path("count/compareAndSet/:version/:name/:expected/:value")
  @Consumes(MediaType.APPLICATION_JSON)
  public void compareAndSetAction(RoutingContext context) {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String version = context.request().getParam("version");
      String name = context.request().getParam("name");
      String value = context.request().getParam("value");
      String expected = context.request().getParam("expected");
      CounterEntity counterEntity = new CounterEntity(appId.toString(), version, name);
      if (!counters.containsKey(counterEntity)) counters.put(counterEntity, new AtomicLong(0));
      context.response().end(String.valueOf(counters.get(counterEntity).compareAndSet(Long.valueOf(expected), Long.valueOf(value))));
    });
  }

  @GET
  @Path("lock/:version/:name")
  @Consumes(MediaType.APPLICATION_JSON)
  public void getLockAction(RoutingContext context) throws IOException {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      String version = context.request().getParam("version");
      String name = context.request().getParam("name");
      LockEntity lockEntity = new LockEntity(appId.toString(), version, name);
      context.response().end(MongoJsons.serialize(lockEntity));
    });
  }

  @DELETE
  @Path("lock/:version/:name")
  @Consumes(MediaType.APPLICATION_JSON)
  public void lockReleaseAction(RoutingContext context) throws IOException {
    ResourceUtils.handle(context, (ctx, appId, principal) -> {
      context.response().end();
    });
  }
}

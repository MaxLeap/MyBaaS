package com.maxleap.las.bass.apiserver.resource;

import com.maxleap.lang.ISOLangManager;
import com.maxleap.pandora.data.support.MongoJsons;
import com.maxleap.las.baas.Constants;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang.StringUtils;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by shunlv on 16-2-18.
 */
@Singleton
@Path("/2.0/location")
@Produces(MediaType.APPLICATION_JSON)
public class LocationResource {
  @GET
  @Path("lang")
  public void findLang(RoutingContext context) {
    func(context, (ctx, principal) -> {
      String langCode = ctx.request().getParam("langCode");

      if (StringUtils.isBlank(langCode)) {
        // get all
        context.response().end(MongoJsons.serialize(ISOLangManager.langMap.values()));
      } else {
        // get one
        context.response().end(MongoJsons.serialize(ISOLangManager.getLang(langCode)));
      }
    });
  }

  private void func(RoutingContext context, Func2 func) {
    func.func(context, context.get(Constants.LAS_PRINCIPAL));
  }
}

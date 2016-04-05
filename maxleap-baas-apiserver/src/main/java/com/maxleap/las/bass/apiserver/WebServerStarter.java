package com.maxleap.las.bass.apiserver;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.maxleap.cerberus.acl.Permission;
import com.maxleap.cerberus.acl.spi.AccessControlService;
import com.maxleap.cloudcode.job.impl.TaskAcquirer;
import com.maxleap.code.CloudCodeContants;
import com.maxleap.code.impl.CloudCodeBootstrap;
import com.maxleap.domain.LASOrgRole;
import com.maxleap.domain.LASOrgUser;
import com.maxleap.domain.LASOrganization;
import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.domain.auth.PermissionType;
import com.maxleap.domain.base.ObjectId;
import com.maxleap.exception.LASException;
import com.maxleap.las.baas.Constants;
import com.maxleap.las.bass.apiserver.handler.*;
import com.maxleap.las.bass.apiserver.web.JsonStringBuilder;
import com.maxleap.las.bass.apiserver.web.Resource;
import com.maxleap.las.bass.apiserver.web.ResourceResolver;
import com.maxleap.organization.service.OrgRoleService;
import com.maxleap.organization.service.OrgUserService;
import com.maxleap.organization.service.OrganizationService;
import com.maxleap.pandora.config.Funcs;
import com.maxleap.utils.EncryptUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import rx.util.async.Async;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class WebServerStarter extends AbstractVerticle {
  private final static Logger logger = LoggerFactory.getLogger(WebServerStarter.class);

  private Injector injector;
  private final ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 1000, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(10000));
  private final Scheduler scheduler = Schedulers.from(executor);
  private JsonObject config;
  private Map<Method, Set<PermissionType>> methodPermissionsMap;

  public static void main(String[] args) {
    Runner.runMod(WebServerStarter.class);
  }

  @Override
  public void start() throws Exception {
    init();
    methodPermissionsMap = new ConcurrentHashMap<>();
    Funcs.timeMillis(1, c -> startWebServer());
    startCloudCode();
  }

  private void init() {
    //启动配置，包括cloudcode配置
    config = new JsonObject()
        .put("web", new JsonObject()
            .put("host", "127.0.0.1").put("port", 10086))//baas服务对外提供的服务地址
        .put("cloudcode", new JsonObject()
            .put("host", "127.0.0.1").put("port", 10087))//cloudcode服务对外提供的服务地址
        .put("whiteList", System.getProperty("whiteList", "orgUsers/login, orgUsers/register"));

    //init guice container
    injector = Guice.createInjector(new GuiceModel(vertx, config));

    //init system data
    asyncInitSystemData();
  }

  private void asyncInitSystemData() {
    executor.execute(() -> {
      try {
        //create default org if not exists
        OrganizationService organizationService = injector.getInstance(OrganizationService.class);
        if (organizationService.listAll().size() == 0) {
          //create org
          LASOrganization lasOrganization = new LASOrganization();
          lasOrganization.setObjectId(new ObjectId());
          lasOrganization.setEnabled(true);
          lasOrganization.setName("MaxLeap");
          lasOrganization.setQq("270158613");
          organizationService.createOrg(lasOrganization);

          //create org roles
          OrgRoleService orgRoleService = injector.getInstance(OrgRoleService.class);
          LASOrgRole orgRole = new LASOrgRole();
          orgRole.setName("OrgAdmin");
          Set<PermissionType> permissions = new HashSet<>();
          permissions.add(PermissionType.ORG_ADMIN);
          orgRole.setPermissions(permissions);
          orgRole.setOrgId(lasOrganization.getObjectId().toHexString());
          orgRole.setObjectId(new ObjectId());
          orgRoleService.createOrgRole(orgRole);

          //create org user
          OrgUserService orgUserService = injector.getInstance(OrgUserService.class);
          LASOrgUser orgUser = new LASOrgUser();
          orgUser.setOrgId(lasOrganization.getObjectId().toHexString());
          orgUser.setUsername("admin123");
          orgUser.setEmail("admin123@gmail.com");
          orgUser.setEmailVerified(true);
          orgUser.setEnabled(true);
          orgUser.setPassword(EncryptUtils.encryptPassword("admin123"));
          orgUser.setUserType(2);
          List<String> roles = new ArrayList<>();
          roles.add(orgRole.getObjectId().toHexString());
          orgUser.setRoles(roles);
          orgUserService.createOrgUser(orgUser);
          System.out.print("Init system data success...");
        }
      } catch (Exception e) {
        System.err.println("Maybe you can't log system. some error occur.");
        e.printStackTrace();
      }
    });
  }

  private void startWebServer() {
    final Router mainRouter = Router.router(vertx);
    registerGlobalHandler(mainRouter);
    registerResourceHandler(mainRouter);
    // start a HTTP web server on port 10086
    vertx.createHttpServer().requestHandler(mainRouter::accept).listen(10086);
    System.out.println("start web success... listen on 10086");
  }

  private void registerGlobalHandler(Router mainRouter) {
    // Exception handler
    mainRouter.route().failureHandler(ExceptionHandler.create());
    mainRouter.route().handler(ResponseTimeHandler.create());
    // We need cookies and request bodies.
    // We need a cookie handler first
    mainRouter.route().handler(CookieHandler.create());
    mainRouter.route().handler(BodyHandler.create());
    mainRouter.route().handler(CustomStaticHandler.create());
    mainRouter.route().handler(RedirectHandler.create());
    mainRouter.route().handler(SomethingAdjustHandler.create());
    mainRouter.route().handler(AccessControlHandler.create(injector.getInstance(AccessControlService.class), config));
  }

  private void registerResourceHandler(Router mainRouter) {
    List<Resource> resolve = ResourceResolver.resolve();
    resolve.forEach(r ->
            r.getMethods().forEach(m -> {
              Object obj = injector.getInstance(r.getResourceClazz());
              Route route = mainRouter.route();
              if (m.path() == null) {
                route.path(r.path());
              } else {
                route.path(r.path().equals("/") ? r.path() + m.path() : r.path() + "/" + m.path());
              }
              if (m.httpMethod() != null) {
                route.method(HttpMethod.valueOf(m.httpMethod()));
              }
              if (!m.consumedTypes().isEmpty()) {
                m.consumedTypes().forEach(t -> route.consumes(t));
              } else if (!r.consumedTypes().isEmpty()) {
                r.consumedTypes().forEach(t -> route.consumes(t));
              }
              if (!m.producedTypes().isEmpty()) {
                m.producedTypes().forEach(t -> route.produces(t));
              } else if (!r.producedTypes().isEmpty()) {
                r.producedTypes().forEach(t -> route.produces(t));
              }
              route.handler(ctx ->
                      Async.start(() -> {
                        try {
                          Method method = m.getMethod();

                          handlePermission(method, ctx.get(Constants.LAS_PRINCIPAL));

                          method.invoke(obj, ctx);
                        } catch (Exception e) {
                          handleFailure(ctx, e);
                        }
                        return null;
                      }, scheduler).subscribe()
              );
            })
    );
  }

  /**
   * 启动CloudCode服务，必须在整个web服务启动完成以后才能启动
   */
  private void startCloudCode() {
    JsonObject cloudcodeConfig = config.getJsonObject("cloudcode");
    JsonObject webConfig = config.getJsonObject("web");
    Map<String, Object> cloudcodeConfigMap = new HashMap<>();
    cloudcodeConfigMap.put("port", cloudcodeConfig.getInteger("port"));//CloudCode对外提供的http服务端口
    cloudcodeConfigMap.put("baasUrl", "http://" + webConfig.getString("host") + ":" + webConfig.getInteger("port") + "/2.0");
    Async.fromRunnable(() -> {
      CloudCodeBootstrap.main(cloudcodeConfigMap);
      TaskAcquirer taskAcquirer = injector.getInstance(TaskAcquirer.class);
      taskAcquirer.acquire(CloudCodeContants.GLOBAL_CONFIG.getApplicationID());
    }, null).subscribe(aVoid -> {
      System.out.println("start CloudCode success... listen on " + cloudcodeConfig.getInteger("port"));
    }, throwable -> {
      System.err.println("start cloudcode error... " + throwable.getMessage());
    });

  }

  private void handleFailure(RoutingContext ctx, Throwable e) {
    int code = 1;
    String msg = "";
    if (e instanceof InvocationTargetException) {
      Throwable targetException = ((InvocationTargetException) e).getTargetException();
      if (targetException instanceof LASException) {
        code = ((LASException) targetException).getCode();
      }
      logger.error(targetException.getMessage(), targetException);
      msg = targetException.getMessage();
    } else {
      msg = e.getMessage();
      logger.error(e.getMessage(), e);
    }
    String jsonString = JsonStringBuilder.create().writeNumber("errorCode", code).writeString("errorMessage", msg).build();
    ctx.response().setStatusCode(400);
    ctx.response().putHeader("Content-Length", String.valueOf(jsonString.getBytes().length));
    ctx.response().end(jsonString, "utf-8");
  }

  private void handlePermission(Method method, LASPrincipal principal) {
    if (method.isAnnotationPresent(Permission.class)) {
      if (principal == null || principal.getPermissions() == null || principal.getPermissions().isEmpty()) {
        throw new LASException(LASException.Unauthorized.NO_PERMISSION, "No permission!");
      }

      Set<PermissionType> userPermissions = principal.getPermissions();

      Set<PermissionType> methodPermissions;
      if (methodPermissionsMap.containsKey(method)) {
        methodPermissions = methodPermissionsMap.get(method);
      } else {
        Permission permission = method.getAnnotation(Permission.class);
        methodPermissions = new HashSet<>();
        for (PermissionType type : permission.type()) {
          do {
            methodPermissions.add(type);
            type = type.getParent();
          } while (type != null);
        }

        methodPermissionsMap.put(method, methodPermissions);
      }

      boolean hasPermission = false;
      for (PermissionType type : methodPermissions) {
        if (userPermissions.contains(type)) {
          hasPermission = true;
          break;
        }
      }

      if (!hasPermission) {
        throw new LASException(LASException.Unauthorized.NO_PERMISSION, "Not enough permission.");
      }
    }
  }
}

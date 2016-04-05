package com.maxleap.code.impl;

import com.maxleap.code.*;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.VertxFactoryImpl;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class CloudCodeBootstrap {

  private final Vertx vertx;
  private final HttpServer httpServer;
  private final Executor executor;
  private GlobalConfig globalConfig;
  private final CloudCodeMetrics metrics;
  private String apiUrl;
  private final static Logger logger = LoggerFactory.getLogger(CloudCodeBootstrap.class);

  public CloudCodeBootstrap(Vertx vertx, HttpServer httpServer,String apiUrl) {
    this.vertx = vertx;
    this.httpServer = httpServer;
    this.apiUrl = apiUrl;
    BlockingQueue<Runnable> workerQueue = new LinkedBlockingQueue<Runnable>();
    this.executor = new ThreadPoolExecutor(10, 200, 30, TimeUnit.SECONDS, workerQueue, new CloudCodeThreadFactory(), new CloudCodeRejectHandler());
    this.metrics = new CloudCodeMetrics(workerQueue);
  }

  private class CloudCodeThreadFactory implements ThreadFactory {
    private final AtomicInteger threadCount = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable runnable) {
      Thread thread = new Thread(runnable, "CloudCodeThread-" + threadCount.getAndIncrement());
      thread.setUncaughtExceptionHandler((t, e) -> logger.error(e.getMessage(), e));
      return thread;
    }
  }

  private class CloudCodeRejectHandler implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
      HttpRequest httpRequest = (HttpRequest)r;
      httpRequest.getHttpRequest().response().end("the request is rejected");
      metrics.getRejectedTaskCount().inc();
      logger.error("task pool have full, so the request will be reject.");
    }
  }

  void start() {
    //init contants
    CloudCodeContants.init();
    this.globalConfig = CloudCodeContants.GLOBAL_CONFIG;
    if (apiUrl != null) CloudCodeContants.DEFAULT_API_ADDRESS_PREFIX = apiUrl;
    //init data
    final Map<String, JobRunner> jobs = new ConcurrentHashMap<>();
    final Map<String, Definer> definers = new ConcurrentHashMap<>();
    //start metric
    this.metrics.start();

    try {
      @SuppressWarnings("unchecked")
      Class<LoaderBase> clazz = (Class<LoaderBase>) Class.forName(globalConfig.getCodeMain());
      final Loader loader = new CloudLoaderProxy().newProxyInstance(clazz.newInstance());
      loader.main(globalConfig);
      for (Map.Entry<String, Definer> entry : loader.definers().entrySet())
        definers.put(entry.getKey(), entry.getValue());

      httpServer
          .requestHandler(request -> request.bodyHandler(buffer -> executor.execute(new HttpRequest(request, metrics, executor, jobs, definers, buffer.toString("UTF-8")))))
          .listen(httpServerAsyncResult -> {
            logger.info(globalConfig.getApplicationName() + " deploy success, waiting request.");
          });
    } catch (Exception e) {
      logger.error("load defined function failed.", e);
    }
  }

  void stop() {
    this.metrics.stop();
    httpServer.close(asyncResult -> {
      if (asyncResult.failed()) logger.error(asyncResult.cause().getMessage(), asyncResult.cause());
      else logger.info("close http server success.");
    });
  }

  public static void main(Map<String,Object> config){
    VertxFactoryImpl vertxFactory = new VertxFactoryImpl();
    Vertx vertx = vertxFactory.vertx();

    HttpServerOptions httpServerOptions = new HttpServerOptions();
    httpServerOptions.setPort(config.get("port") == null ? 8080: ((Integer)config.get("port")));
    httpServerOptions.setIdleTimeout(5);
    HttpServer httpServer = vertx.createHttpServer(httpServerOptions);
    final CloudCodeBootstrap bootstrap = new CloudCodeBootstrap(vertx, httpServer,(String)config.get("baasUrl"));
    bootstrap.start();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      bootstrap.stop();
    }, "shutdownHooker"));
  }

}

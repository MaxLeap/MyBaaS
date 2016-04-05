package com.maxleap.cloudcode;

import com.maxleap.cloudcode.processors.CCodeSimpleProcessor;
import com.maxleap.domain.auth.LASPrincipal;
import io.vertx.core.http.HttpServerResponse;
import org.apache.http.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * User: yuyangning
 * Date: 8/11/14
 * Time: 2:48 PM
 */
@Singleton
public class CCodeExecutor {
  private static final Logger logger = LoggerFactory.getLogger(CCodeExecutor.class);

  private CCodeProcessor simpleProcessor;
  private final Scheduler scheduler;
  private CloudCodeRestClient cloudCodeRestClient;

  @Inject
  public CCodeExecutor(CloudCodeRestClient cloudCodeRestClient) {
    this.cloudCodeRestClient = cloudCodeRestClient;
    this.simpleProcessor = new CCodeSimpleProcessor();
    scheduler = Schedulers.from(new ThreadPoolExecutor(80, 500, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(2000)));
  }

  public <T extends Object> T execute(CCodeEntity codeEntity,LASPrincipal userPrincipal) {
    return execute(codeEntity, simpleProcessor, null,userPrincipal);
  }

  public <T extends Object> T execute(CCodeEntity codeEntity,HttpServerResponse response,LASPrincipal userPrincipal) {
    return execute(codeEntity, simpleProcessor, response,userPrincipal);
  }

  /**
   * execute cloud code.
   *
   * @param codeEntity
   * @param processor
   * @return
   */
  public <T extends Object> T execute(CCodeEntity codeEntity, CCodeProcessor processor,LASPrincipal userPrincipal) {
    return this.execute(codeEntity, processor,null,userPrincipal);
  }

  public <T extends Object> T execute(CCodeEntity codeEntity, CCodeProcessor processor,HttpServerResponse response,LASPrincipal userPrincipal) {
    validate(codeEntity);//validate input data.
    String result = cloudCodeRestClient.doPost("/" + codeEntity.getCategory().alias() + "/" + codeEntity.getName(), codeEntity.getParameters(), response, userPrincipal);
    return (T)processor.process(result);
  }

  /**
   * validate the input data.
   *
   * @param codeEntity
   */
  public void validate(CCodeEntity codeEntity) {
    Asserts.notNull(codeEntity, "The codeEntity can't be null.");
    Asserts.notNull(codeEntity.getAppId(), "The codeEntity's appId can't be empty.");
    Asserts.notNull(codeEntity.getName(), "The codeEntity's name can't be empty.");
    Asserts.notNull(codeEntity.getCategory(), "The codeEntity's category can't be null.");
  }


}
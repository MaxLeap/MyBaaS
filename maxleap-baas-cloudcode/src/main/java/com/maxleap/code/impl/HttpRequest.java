package com.maxleap.code.impl;

import com.maxleap.code.*;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.util.*;
import java.util.concurrent.Executor;

/**
 *
 */
class HttpRequest implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResponseHandler.class);
  private HttpServerRequest httpRequest;
  private Map<String, JobRunner> jobs;
  private Map<String, Definer> definers;
  private Executor executor;
  private CloudCodeMetrics metrics;
  private ResponseHandler responseHandler;
  private String params;

  public HttpRequest(HttpServerRequest httpRequest, CloudCodeMetrics metrics, Executor executor, Map<String, JobRunner> jobs, Map<String, Definer> definers, String params) {
    this.httpRequest = httpRequest;
    this.jobs = jobs;
    this.definers = definers;
    this.executor = executor;
    this.metrics = metrics;
    this.responseHandler = new ResponseHandler();
    this.params = params;
  }

  public HttpServerRequest getHttpRequest() {
    return httpRequest;
  }

  private class CloudcodeRequest{
    private String category;
    private String name;
    private String params;
    private UserPrincipal userPrincipal;

    public String getCategory() {
      return category;
    }

    public void setCategory(String category) {
      this.category = category;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getParams() {
      return params;
    }

    public void setParams(String params) {
      this.params = params;
    }

    public UserPrincipal getUserPrincipal() {
      return userPrincipal;
    }

    public void setUserPrincipal(UserPrincipal userPrincipal) {
      this.userPrincipal = userPrincipal;
    }
  }

  private CloudcodeRequest parseCloudcodeRequest() throws MLException {
    String[] pathSplits = httpRequest.path().split("/");
    if (pathSplits.length <= 1) {
      throw new MLException("malformation request.");
    }
    CloudcodeRequest cloudcodeRequest = new CloudcodeRequest();

    LOGGER.debug("receive an request[" + httpRequest.path() + "]:" + params);

    cloudcodeRequest.setCategory(pathSplits[1]);
    if (pathSplits.length > 2) {
      cloudcodeRequest.setName(pathSplits[2]);
    }
    cloudcodeRequest.setParams(params);
    String userPrincipalStr = httpRequest.getHeader("ML-User-Principal");
    if (userPrincipalStr != null && userPrincipalStr.length() > 0) {
      cloudcodeRequest.setUserPrincipal(MLJsonParser.asObject(userPrincipalStr, UserPrincipal.class));
    }
    return cloudcodeRequest;
  }

  /**
   * 请求路径
   * POST /function/name  比如  function/hello,  参数放到body里,以json字符串方式,或者整形
   * POST /job/name
   * POST /entityManager/name
   * GET  /console/config
   * GET  /health
   */
  @Override
  public void run() {
    CloudcodeRequest cloudcodeRequest;
    try {
      cloudcodeRequest = parseCloudcodeRequest();
    } catch (Exception e) {
      responseMalformationMsg(e.getMessage());
      return;
    }
    String category = cloudcodeRequest.getCategory();

    if ("health".equals(category)) {
      httpRequest.response().setStatusCode(200).end();
    } else if ("console".equals(category)) {
      processConsoleRequest(cloudcodeRequest.getName());
    } else {
      Definer definer = definers.get(cloudcodeRequest.getCategory());
      MLHandler<Request, Response> lasHandler;
      if (definer == null || isBlank(cloudcodeRequest.getName()) || (lasHandler = definer.getHandler(cloudcodeRequest.getName())) == null) {
        responseMalformationMsg("invalid request.caused by no handler for this uri");
        return;
      }
      if ("function".equals(category)) {
        processFunctionRequest(lasHandler,cloudcodeRequest);
      } else if ("job".equals(category)) {
        processJobRequest(lasHandler,cloudcodeRequest);
      } else if ("entityManager".equals(category)) {
        processEntityManagerRequest(lasHandler, cloudcodeRequest);
      } else {
        responseMalformationMsg("invalid request category.");
      }
    }
  }

  private void processFunctionRequest(MLHandler<Request, Response> lasHandler,CloudcodeRequest cloudcodeRequest){
    MLRequest functionRequest = new MLRequest(params,cloudcodeRequest.getUserPrincipal());
    MLResponse<String> response;
    try {
      response = (MLResponse) lasHandler.handle(functionRequest);
    } catch (Throwable e) {
      e.printStackTrace();
      response = new MLResponse<>(String.class);
      StackTraceElement[] elements = e.getStackTrace();
      if (elements != null && elements.length > 0) {
        response.setError(e.toString() + " at " + elements[0]);
      } else {
        response.setError(e.toString());
      }
    }
    responseHandler.handle(response, httpRequest.response());
  }

  private void processJobRequest(MLHandler<Request, Response> lasHandler,CloudcodeRequest cloudcodeRequest){
    MLRequest jobRequest = new MLRequest(params,cloudcodeRequest.getUserPrincipal());
    JobRunner jobRunner = new JobRunner(lasHandler, jobRequest, httpRequest.response(), responseHandler);
    jobs.put(cloudcodeRequest.getName(), jobRunner);
    executor.execute(jobRunner);
  }

  private void processEntityManagerRequest(MLHandler<Request, Response> lasHandler,CloudcodeRequest cloudcodeRequest){
    MLResponse<String> response;
    try {
      MLClassManagerRequest entityManagerRequest = MLJsonParser.asObject(params, MLClassManagerRequest.class);
      entityManagerRequest.setUserPrincipal(cloudcodeRequest.getUserPrincipal());
      response = (MLResponse) lasHandler.handle(entityManagerRequest);
    } catch (Throwable e) {
      e.printStackTrace();
      response = new MLResponse<>(String.class);
      StackTraceElement[] elements = e.getStackTrace();
      if (elements != null && elements.length > 0) {
        response.setError(e.toString() + " at " + elements[0]);
      } else {
        response.setError(e.toString());
      }
    }
    responseHandler.handle(response, httpRequest.response());
  }

  private void processConsoleRequest(String name) {
    MLResponse<String> response = new MLResponse<>(String.class);
    if ("config".equals(name)) {
      response.setResult(CloudCodeContants.GLOBAL_CONFIG.getConfigJsonStr());
    } else if ("jobNames".equals(name)) {
      Set<String> jobNames = definers.get(RequestCategory.JOB.alias()).getHandlerNames();
      List<String> list = new ArrayList<>();
      if (jobNames != null && jobNames.size() > 0) {
        for (String jobName:jobNames) {
          list.add(jobName);
        }
      }
      response.setResult(MLJsonParser.asJson(list));
    } else if ("functionNames".equals(name)) {
      Set<String> functionNames = definers.get(RequestCategory.FUNCTION.alias()).getHandlerNames();
      List<String> list = new ArrayList<>();
      if (functionNames != null && functionNames.size() > 0) {
        for (String functionName : functionNames) {
          list.add(functionName);
        }
      }
      response.setResult(MLJsonParser.asJson(list));
    } else if ("threadStats".equals(name)) {
      Map<String,Object> threadStats = new HashMap<>();
      threadStats.put("queueSize",metrics.queueSize());
      threadStats.put("rejectCount",metrics.getRejectedTaskCount().getCount());
      response.setResult(MLJsonParser.asJson(threadStats));
    } else {
      response.setError("invalid request param:console");
    }
    responseHandler.handle(response, httpRequest.response());
  }

  private void responseMalformationMsg(String errorMsg) {
    HttpServerResponse httpResponse = httpRequest.response();
    httpResponse.setStatusCode(545);
    httpResponse.setStatusMessage(errorMsg);
    httpResponse.end(errorMsg,"UTF-8");
  }

  private boolean isBlank(String str) {
    return (str == null || str.trim().equals(""));
  }
}

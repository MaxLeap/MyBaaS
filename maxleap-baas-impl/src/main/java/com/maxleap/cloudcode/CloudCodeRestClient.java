package com.maxleap.cloudcode;

import com.maxleap.cloudcode.utils.ZJsonParser;
import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.exception.LASException;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * User：David Young
 * Date：16/1/26
 */
@Singleton
public class CloudCodeRestClient {

  @Inject
  @Named("cloudCodeAddr")
  private String cloudcodeAddr;

  public String doPost(String uri,Iterable params){
    return doPost(uri,params,null,null);
  }

  public String doPost(String uri, Iterable params,HttpServerResponse apiResponse,LASPrincipal userPrincipal) {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost(cloudcodeAddr + uri);
    httpPost.addHeader("Content-Type",MediaType.APPLICATION_JSON);
    if (userPrincipal != null) httpPost.addHeader("ML-User-Principal",ZJsonParser.asJson(userPrincipal));
    HttpEntity entity = EntityBuilder.create()
        .setContentType(ContentType.APPLICATION_JSON)
        .setText(params == null ? "" : (params instanceof JsonArray ? ((JsonArray) params).encode() : ((JsonObject) params).encode())).build();
    httpPost.setEntity(entity);

    RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(10000).build();//设置请求和传输超时时间
    httpPost.setConfig(requestConfig);

    try {
      CloseableHttpResponse response = httpClient.execute(httpPost);
      if (apiResponse != null) checkHeader(response, apiResponse);
      int statusCode = response.getStatusLine().getStatusCode();
//      String reason = response.getStatusLine().getReasonPhrase();
      HttpEntity responseEntity = response.getEntity();
      String result = EntityUtils.toString(responseEntity, "UTF-8");
      EntityUtils.consume(responseEntity);
      if (statusCode != 200) {
        throw new LASException(109998,result);
      }

      return result;
    } catch (LASException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new LASException(109997,e.getMessage());
    } finally {
      httpPost.releaseConnection();
      try {
        httpClient.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void checkHeader(CloseableHttpResponse response,HttpServerResponse apiResponse){
    Header[] headers = response.getAllHeaders();
    boolean hasContentType = false;
    if (apiResponse != null && headers != null && headers.length > 0) {
      for (Header header : headers) {
        apiResponse.putHeader(header.getName(), header.getValue());
        if ("Content-Type".equals(header.getName())) hasContentType = true;
      }
    }

    if (apiResponse != null && (headers == null || !hasContentType)) {
      apiResponse.putHeader("Content-Type", MediaType.APPLICATION_JSON);
    }
  }
}

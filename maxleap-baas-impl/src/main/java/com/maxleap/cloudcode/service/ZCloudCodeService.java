package com.maxleap.cloudcode.service;

import com.maxleap.cloudcode.CCodeCategory;
import com.maxleap.cloudcode.CCodeEntity;
import com.maxleap.cloudcode.CCodeExecutor;
import com.maxleap.cloudcode.CCodeMethod;
import com.maxleap.cloudcode.processors.CCodeCreateProcessor;
import com.maxleap.cloudcode.processors.CCodeUpdateProcessor;
import com.maxleap.domain.auth.LASPrincipal;
import com.maxleap.domain.base.LASObject;
import com.maxleap.pandora.core.lasdata.LASUpdate;
import com.maxleap.utils.Assert;
import com.maxleap.las.baas.Constants;
import com.maxleap.las.sdk.DeleteMsg;
import com.maxleap.las.sdk.SaveMsg;
import com.maxleap.las.sdk.UpdateMsg;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: yuyangning
 * Date: 7/10/14
 * Time: 3:56 PM
 */
@Singleton
public class ZCloudCodeService {

  private final CCodeExecutor codeExecutor;
  private final CCodeCreateProcessor cCodeCreateProcessor;
  private final CCodeUpdateProcessor cCodeUpdateProcessor;

  @Inject
  public ZCloudCodeService(CCodeExecutor codeExecutor) {
    this.codeExecutor = codeExecutor;
    this.cCodeCreateProcessor = new CCodeCreateProcessor();
    this.cCodeUpdateProcessor = new CCodeUpdateProcessor();
  }

  public String invokeFunc(String appId, String name, Object params, CCodeCategory category,LASPrincipal userPrincipal) {
    return this.invokeFunc(appId, name, params, category,null);
  }

  public String invokeFunc(String appId, String name, Object params, CCodeCategory category, HttpServerResponse response,LASPrincipal userPrincipal) {

    Assert.hasLength(appId);
    Assert.hasLength(name);

    CCodeEntity entity = new CCodeEntity();
    entity.setAppId(appId);
    entity.setName(name);
    entity.setCategory(category);
    if (params != null) {
      if (params instanceof List) {
        entity.setParameters(new JsonArray((List)params));
      } else if (params instanceof Map) {
        entity.setParameters(new JsonObject((Map)params));
      } else {
        entity.setParameters(new JsonObject(params.toString()));
      }
    }


    return codeExecutor.execute(entity,response,userPrincipal);
  }

  public String invokeConsole(String appId, String name, Map<String, Object> params,LASPrincipal userPrincipal) {

    Assert.hasLength(appId);
    Assert.hasLength(name);

    CCodeEntity entity = new CCodeEntity();
    entity.setAppId(appId);
    entity.setName(name);
    entity.setCategory(CCodeCategory.Console);
    if (params != null)
      entity.setParameters(new JsonObject(params));

    return codeExecutor.execute(entity,userPrincipal);
  }

  public Object invokeJob() {

    return null;
  }

  public SaveMsg invokeCreate(String appId, String className, LASObject object,LASPrincipal userPrincipal) {

    Assert.hasLength(appId);
    Assert.hasLength(className);
    Assert.notNull(object);

    Map<String, Object> params = new HashMap<>();
    params.put(Constants.CLOUD_CODE_PARAMS_METHOD, CCodeMethod.Create.alias());
    params.put(Constants.CLOUD_CODE_PARAMS_PARAMS, object.getMap());

    CCodeEntity codeEntity = mappingCCodeEntity(appId, className, params);

    return codeExecutor.execute(codeEntity, cCodeCreateProcessor,userPrincipal);
  }

  public UpdateMsg invokeUpdate(String appId, String className, String objectId, LASUpdate object,LASPrincipal userPrincipal) {

    Assert.hasLength(appId);
    Assert.hasLength(className);
    Assert.hasLength(objectId);
    Assert.notNull(object);


    Map<String, Object> iparams = new HashMap<>();
    iparams.put("objectId", objectId);
    iparams.put("update", object.getModifierOps());

    Map<String, Object> params = new HashMap<>();
    params.put(Constants.CLOUD_CODE_PARAMS_METHOD, CCodeMethod.Update.alias());
    params.put(Constants.CLOUD_CODE_PARAMS_PARAMS, iparams);

    CCodeEntity codeEntity = mappingCCodeEntity(appId, className, params);

    return codeExecutor.execute(codeEntity, cCodeUpdateProcessor,userPrincipal);
  }

  public DeleteMsg invokeDelete(String appId, String className, String objectId,LASPrincipal userPrincipal) {

    Map<String, Object> params = new HashMap<>();
    params.put(Constants.CLOUD_CODE_PARAMS_METHOD, CCodeMethod.Delete.alias());
    params.put(Constants.CLOUD_CODE_PARAMS_PARAMS, objectId);

    CCodeEntity codeEntity = mappingCCodeEntity(appId, className, params);

    return codeExecutor.execute(codeEntity, userPrincipal);

  }

  public DeleteMsg invokeDelete(String appId, String className, List ids,LASPrincipal userPrincipal) {

    Map<String, Object> params = new HashMap<>();
    params.put(Constants.CLOUD_CODE_PARAMS_METHOD, CCodeMethod.DeleteBatch.alias());
    params.put(Constants.CLOUD_CODE_PARAMS_PARAMS, ids);

    CCodeEntity codeEntity = mappingCCodeEntity(appId, className, params);

    return codeExecutor.execute(codeEntity,userPrincipal);

  }

  private CCodeEntity mappingCCodeEntity(String appId, String className, Map<String, Object> params) {

    CCodeEntity codeEntity = new CCodeEntity();
    codeEntity.setAppId(appId);
    codeEntity.setName(className);
    codeEntity.setCategory(CCodeCategory.Mapping);
    codeEntity.setParameters(new JsonObject(params));

    return codeEntity;
  }
}
package com.maxleap.code.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maxleap.code.*;
import com.maxleap.code.MLException;
import com.maxleap.las.sdk.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class MLClassManagerImpl<T> implements MLClassManager<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MLClassManagerImpl.class);
  private MLClassManagerHook<T> hook;
  private Class<T> entityClazz;
  private String apiAddress;

  public MLClassManagerImpl(MLClassManagerHook<T> hook, Class<T> entityClazz) {
    this.hook = hook;
    this.entityClazz = entityClazz;
    this.apiAddress = CloudCodeContants.DEFAULT_API_ADDRESS_PREFIX + "/classes/" + entityClazz.getSimpleName();
  }

  @Override
  public SaveResult<T> create(T object, UserPrincipal userPrincipal) throws MLException {
    try {
      BeforeResult<T> beforeResult = hook == null ? new BeforeResult<>(object, true) : hook.beforeCreate(object, userPrincipal);
      if (!beforeResult.isResult()) return new SaveResult<>(beforeResult.getFailMessage());
      String response = WebUtils.doPost(apiAddress, CloudCodeContants.getHeaders(userPrincipal), MLJsonParser.asJson(object), CloudCodeContants.DEFAULT_TIMEOUT, CloudCodeContants.DEFAULT_READ_TIMEOUT);
      LOGGER.info("get response of create[" + apiAddress + "]:" + response);
      SaveMsg saveMsg = MLJsonParser.asObject(response, SaveMsg.class);
      SaveResult saveResult = new SaveResult<>(beforeResult, saveMsg);
      if (hook == null) return saveResult;
      AfterResult afterResult = hook.afterCreate(beforeResult, saveMsg, userPrincipal);
      if (!afterResult.isSuccess()) saveResult.setFailMessage(afterResult.getFailMessage());
      return saveResult;
    } catch (Exception e) {
      throw new MLException(e);
    }
  }

  @Override
  public FindMsg<T> find(MLQuery query, UserPrincipal userPrincipal) {
    return find(query, false, userPrincipal);
  }

  @Override
  public FindMsg<T> find(MLQuery query, boolean count, UserPrincipal userPrincipal) throws MLException {
    try {
      String postQuery = serializeLasQueryForPostQuest(query);
      String response = WebUtils.doPost(apiAddress + "/query", CloudCodeContants.getHeaders(userPrincipal), postQuery, CloudCodeContants.DEFAULT_TIMEOUT, CloudCodeContants.DEFAULT_READ_TIMEOUT);
      LOGGER.info("get response of find[" + apiAddress + "/query](" + postQuery + "):" + response);
      JsonNode responseJson = MLJsonParser.asJsonNode(response);
      ArrayNode results = (ArrayNode) responseJson.get("results");
      List<T> r = new ArrayList<T>();
      if (results == null || results.size() == 0) return new FindMsg<T>();
      for (JsonNode result : results) {
        r.add(MLJsonParser.asObject(result.toString(), entityClazz));
      }
      return new FindMsg<T>(count ? results.size() : 0, r);
    } catch (Exception e) {
      throw new MLException(e);
    }
  }

  @Override
  public T findById(String id, UserPrincipal userPrincipal) throws MLException {
    try {
      String response = WebUtils.doGet(apiAddress + "/" + id, CloudCodeContants.getHeaders(userPrincipal), null);
      LOGGER.info("get response of findById[" + apiAddress + "/" + id + "]:" + response);
      if ("{}".equals(response)) return null;
      return MLJsonParser.asObject(response, entityClazz);
    } catch (IOException e) {
      throw new MLException(e);
    }
  }

  @Override
  public UpdateMsg update(String id, MLUpdate update, UserPrincipal userPrincipal) throws MLException {
    BeforeResult<MLUpdate> beforeResult = hook == null ? new BeforeResult<>(update, true) : hook.beforeUpdate(id, update, userPrincipal);
    if (!beforeResult.isResult()) throw new MLException(beforeResult.getFailMessage());
    try {
      String response = WebUtils.doPut(apiAddress + "/" + id, CloudCodeContants.getHeaders(userPrincipal), MLJsonParser.asJson(update.update()), CloudCodeContants.DEFAULT_TIMEOUT, CloudCodeContants.DEFAULT_READ_TIMEOUT);
      LOGGER.info("get response of update[" + apiAddress + "/" + id + "](" + update.update() + "):" + response);
      UpdateMsg updateMsg = MLJsonParser.asObject(response, UpdateMsg.class);
      if (hook != null) hook.afterUpdate(id,beforeResult, updateMsg, userPrincipal);
      return updateMsg;
    } catch (IOException e) {
      throw new MLException(e);
    }
  }

  @Override
  public DeleteResult delete(String id, UserPrincipal userPrincipal) throws MLException {
    BeforeResult<String> beforeResult = hook == null ? new BeforeResult<>(id, true) : hook.beforeDelete(id, userPrincipal);
    if (!beforeResult.isResult()) return new DeleteResult(beforeResult.getFailMessage());
    try {
      String response = WebUtils.doDelete(apiAddress + "/" + id, CloudCodeContants.getHeaders(userPrincipal), null);
      LOGGER.info("get response of delete[" + apiAddress + "/" + id + "]:" + response);
      DeleteMsg deleteMsg = MLJsonParser.asObject(response, DeleteMsg.class);
      DeleteResult deleteResult = new DeleteResult(beforeResult, deleteMsg);
      if (hook == null) return deleteResult;
      AfterResult afterResult = hook.afterDelete(beforeResult, deleteMsg, userPrincipal);
      if (!afterResult.isSuccess()) deleteResult.setFailMessage(afterResult.getFailMessage());
      return deleteResult;
    } catch (Exception e) {
      throw new MLException(e);
    }
  }

  @Override
  public DeleteResult delete(String[] ids, UserPrincipal userPrincipal) {
    if (ids != null && ids.length > 50) throw new MLException("delete bach max limit 50.");
    try {
      BeforeResult<String[]> beforeResult = hook == null ? new BeforeResult<>(ids, true) : hook.beforeDelete(ids, userPrincipal);
      if (!beforeResult.isResult()) return new DeleteResult(beforeResult.getFailMessage());
      ArrayNode arrays = JsonNodeFactory.instance.arrayNode();
      for (String id : ids) arrays.add(id);
      ObjectNode params = JsonNodeFactory.instance.objectNode();
      params.put("objectIds", arrays);
      String response = WebUtils.doPost(apiAddress + "/delete", CloudCodeContants.getHeaders(userPrincipal), params.toString(), CloudCodeContants.DEFAULT_TIMEOUT, CloudCodeContants.DEFAULT_READ_TIMEOUT);
      LOGGER.info("get response of deleteBatch[" + apiAddress + "/delete](" + ids + "):" + response);
      return new DeleteResult<>(beforeResult, MLJsonParser.asObject(response, DeleteMsg.class));
    } catch (Exception e) {
      throw new MLException(e);
    }
  }

  @Override
  public SaveResult<T> create(T object) throws MLException {
    return this.create(object, null);
  }

  @Override
  public FindMsg<T> find(MLQuery query) throws MLException {
    return this.find(query, null);
  }

  @Override
  public FindMsg<T> find(MLQuery query, boolean count) throws MLException {
    return this.find(query, count, null);
  }

  @Override
  public T findById(String id) throws MLException {
    return this.findById(id, null);
  }

  @Override
  public UpdateMsg update(String id, MLUpdate update) throws MLException {
    return this.update(id, update, null);
  }

  @Override
  public DeleteResult delete(String id) throws MLException {
    return this.delete(id, null);
  }

  @Override
  public DeleteResult delete(String[] ids) throws MLException {
    return this.delete(ids, null);
  }

  String serializeLasQueryForPostQuest(MLQuery lasQuery) {
    Map<String, Object> map = new HashMap<>();
    if (lasQuery.query() != null) map.put("where", MLJsonParser.asJson(lasQuery.query()));
    if (lasQuery.sort() != null) map.put("order", lasQuery.sort());
    if (lasQuery.keys() != null) map.put("keys", lasQuery.keys());
    if (lasQuery.includes() != null) map.put("include", lasQuery.includes());
    map.put("limit", lasQuery.limit());
    map.put("skip", lasQuery.skip());
//    map.put("excludeKeys", null); Unsupported.
    return MLJsonParser.asJson(map);
  }

}

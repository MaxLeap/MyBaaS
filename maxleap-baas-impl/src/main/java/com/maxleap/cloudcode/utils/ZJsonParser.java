package com.maxleap.cloudcode.utils;

import com.maxleap.exception.LASException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by stream.
 */
public class ZJsonParser {

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final TypeFactory typeFactory = TypeFactory.defaultInstance();

  public static <T> T asObject(String source, Class<T> clazz) {
    try {
      return mapper.readValue(source, typeFactory.uncheckedSimpleType(clazz));
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static <T> T asObject(String source, JavaType type) {
    try {
      return mapper.readValue(source, type);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static JsonNode asJsonNode(String source) {
    try {
      return mapper.readTree(source);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static Map<String, Object> objectToMap(Object object) {
    if (object == null) return null;
    try {
      String jsonStr = mapper.writeValueAsString(object);
      return mapper.readValue(jsonStr, Map.class);
    } catch (Exception e) {
      throw new LASException(109999,e.getMessage());
    }
  }

  public static Map<String, Object> jsonNodeToMap(JsonNode jsonNode) {
    if (jsonNode == null) {
      return null;
    }
    Iterator<String> paramsIterator = jsonNode.fieldNames();
    Map<String, Object> map = new HashMap<String, Object>();
    while (paramsIterator.hasNext()) {
      String paramName = paramsIterator.next();
      JsonNode jsonSonNode = jsonNode.get(paramName);
      if (jsonSonNode.isInt()) {
        map.put(paramName, jsonSonNode.asInt());
      } else if (jsonSonNode.isLong()) {
        map.put(paramName, jsonSonNode.asLong());
      } else if (jsonSonNode.isTextual()) {
        map.put(paramName, jsonSonNode.asText());
      } else if (jsonSonNode.isObject()) {
        Map<String, Object> tempMap = jsonNodeToMap(jsonSonNode);
        if (tempMap != null) {
          map.put(paramName, tempMap);
        }
      } else if (jsonSonNode.isArray()) {
        Iterator<JsonNode> nodeIterator = jsonSonNode.elements();
        ArrayList<Map<String, Object>> mapArrayList = new ArrayList<Map<String, Object>>();
        while (nodeIterator.hasNext()) {
          JsonNode tempJsonNode = nodeIterator.next();
          Map<String, Object> tempM = jsonNodeToMap(tempJsonNode);
          if (tempM != null) {
            mapArrayList.add(tempM);
          }
        }
        map.put(paramName, mapArrayList);
      }
    }
    return map;
  }

  public static <T> String asJson(T obj) {
    try {
      return mapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }
  }
}

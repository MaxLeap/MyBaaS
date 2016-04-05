package com.maxleap.pandora.data.support.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sneaky
 */
public class Jsons {
    private static Logger logger = LoggerFactory.getLogger(Jsons.class);

    private static final ObjectMapper objectMapper;
    private static final ObjectMapper objectIgnoreNullMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE);
//        SimpleModule module = new SimpleModule();
//        module.addSerializer(ObjectId.class, new ObjectIdSerializer());
//
//        module.addDeserializer(ObjectId.class, new ObjectIdDeserialier());
//        objectMapper.registerModule(module);

        objectIgnoreNullMapper = new ObjectMapper();
        objectIgnoreNullMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("parse exception: " + e.getMessage());
        }
    }

    public static String serializeIgnoreNull(Object obj) {
        try {
            return objectIgnoreNullMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("parse exception: " + e.getMessage());
        }
    }

    public static String serializePretty(Object obj) {
        if (obj == null)
            return null;
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("Failed objectToPrettyJSONStr object is : " + obj, e);
            return null;
        }
    }

    public static <T> T deserialize(String jsonString, Class<T> type) {
        if (jsonString == null || jsonString.length() == 0)
            return null;
        try {
            return objectMapper.readValue(jsonString, type);
        } catch (Exception e) {
            throw new IllegalArgumentException("serialize exception: " + e.getMessage());
        }
    }

}

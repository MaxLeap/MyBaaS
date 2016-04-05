package com.maxleap.pandora.core.utils;

import com.maxleap.domain.base.ObjectId;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.List;

/**
 * @author sneaky
 * @since 3.0.0
 */
public class LASObjectJsons {
    private static final ObjectMapper objectMapper;

    static {
        SimpleModule module2 = new SimpleModule();
        module2.addSerializer(ObjectId.class, new ObjectIdSerializer3());

        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(module2);
    }

    public static String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("serialize exception: " + e.getMessage());
        }
    }

    public static String serializePretty(Object obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("serialize exception: " + e.getMessage());
        }
    }

    public static <T> T deserialize(String jsonString, Class<T> type) {
        try {
            return objectMapper.readValue(jsonString, type);
        } catch (Exception e) {
            throw new IllegalArgumentException("deserialize exception: " + e.getMessage());
        }
    }

    public static <T> T deserializeList(String jsonString, Class<T> type) {
        try {
            return objectMapper.readValue(jsonString, objectMapper.getTypeFactory().constructCollectionType(List.class, type));
        } catch (Exception e) {
            throw new IllegalArgumentException("deserialize exception: " + e.getMessage());
        }
    }

    public static class ObjectIdSerializer3 extends JsonSerializer<ObjectId> {
        @Override
        public void serialize(ObjectId value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            if (value == null) {
                jgen.writeObject(null);
            } else {
                jgen.writeObject(value.toHexString());
            }
        }
    }
}

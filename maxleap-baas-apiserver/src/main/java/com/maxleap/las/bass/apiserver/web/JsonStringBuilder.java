package com.maxleap.las.bass.apiserver.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class JsonStringBuilder {
  private final static ObjectMapper objectMapper = new ObjectMapper();
  private StringWriter writer = new StringWriter();
  private JsonGenerator generator;

  public JsonStringBuilder() {
    try {
      SimpleModule module2 = new SimpleModule();
      module2.addSerializer(org.bson.types.ObjectId.class, new ObjectIdSerializer2());
      module2.addSerializer(com.maxleap.domain.base.ObjectId.class, new ObjectIdSerializer3());
      objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
      objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      objectMapper.registerModule(module2);
      generator = objectMapper.getFactory().createGenerator(writer);
      generator.writeStartObject();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static JsonStringBuilder create() {
    return new JsonStringBuilder();
  }

  public JsonStringBuilder writeObject(String key, Object val) {
    try {
      generator.writeObjectField(key, val);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return this;
  }

  public JsonStringBuilder writeString(String key, String val) {
    try {
      generator.writeStringField(key, val);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return this;
  }

  public JsonStringBuilder writeNumber(String key, Number val) {
    try {
      if (val instanceof Long) {
        generator.writeNumberField(key, (Long) val);
      } else if (val instanceof Integer) {
        generator.writeNumberField(key, (Integer) val);
      } else if (val instanceof Double) {
        generator.writeNumberField(key, (Double) val);
      } else if (val instanceof Float) {
        generator.writeNumberField(key, (Float) val);
      } else if (val instanceof BigDecimal) {
        generator.writeNumberField(key, (BigDecimal) val);
      } else {
        throw new IllegalArgumentException("mismatch type: " + val.getClass().getName());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return this;
  }

  public String build() {
    try {
      generator.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return writer.toString();
  }


  public static class ObjectIdSerializer3 extends JsonSerializer<com.maxleap.domain.base.ObjectId> {
    @Override
    public void serialize(com.maxleap.domain.base.ObjectId value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
      if (value == null) {
        jgen.writeObject(null);
      } else {
        jgen.writeObject(value.toHexString());
      }
    }
  }

  public static class ObjectIdSerializer2 extends JsonSerializer<ObjectId> {
    @Override
    public void serialize(org.bson.types.ObjectId value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
      if (value == null) {
        jgen.writeObject(null);
      } else {
        jgen.writeObject(value.toHexString());
      }
    }
  }

}

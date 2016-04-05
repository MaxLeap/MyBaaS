package com.maxleap;

import com.maxleap.domain.base.ObjectId;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class CustomJsonObjectIdSerializer extends JsonSerializer<Object>
{
  @Override
  public void serialize(Object objectId, JsonGenerator aJsonGenerator, SerializerProvider aSerializerProvider)
      throws IOException {
    if (objectId instanceof ObjectId) {
      aJsonGenerator.writeString(objectId.toString());
    } else {
      aJsonGenerator.writeObject(objectId);
    }
  }
}

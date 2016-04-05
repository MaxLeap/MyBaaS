package com.maxleap;

import com.maxleap.domain.mongo.DateUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Date;

/**
 * @author sneaky
 * @since 1.0.0
 */
public class CustomJsonDateSerializer extends JsonSerializer<Date>
{
  @Override
  public void serialize(Date date, JsonGenerator aJsonGenerator, SerializerProvider aSerializerProvider)
      throws IOException {
    aJsonGenerator.writeString(DateUtils.encodeDate(date));
  }
}

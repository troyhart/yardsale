package com.myco.jpa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

@Converter public abstract class JacksonAttributeConverter<T> implements AttributeConverter<T, String> {

  private static ObjectMapper objectMapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .registerModule(new KotlinModule())
      .registerModule(new ParameterNamesModule())
      .registerModule(new Jdk8Module())
      .registerModule(new JavaTimeModule())
      ;

  private Class<T> type;

  public JacksonAttributeConverter(Class<T> type) {
    this.type = type;
  }

  @Override public String convertToDatabaseColumn(T attribute) {
    try {
      return objectMapper.writer().writeValueAsString(attribute);
    }
    catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override public T convertToEntityAttribute(String dbData) {
    try {
      return objectMapper.readerFor(type).readValue(dbData);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

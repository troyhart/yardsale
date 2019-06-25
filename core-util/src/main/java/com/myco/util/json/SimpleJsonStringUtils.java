package com.myco.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SimpleJsonStringUtils {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static String toJsonString(Object value) throws JsonProcessingException {
    return OBJECT_MAPPER.writeValueAsString(value);
  }
}

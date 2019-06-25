package com.myco;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myco.api.values.UserInfo;
import com.myco.auth.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController public class YardsaleApplication {

  public static void main(String[] args) {
    SpringApplication.run(YardsaleApplication.class, args);
  }

  @Autowired public void configure(ObjectMapper objectMapper) {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @GetMapping(path = "/userInfo", produces = {MediaType.APPLICATION_JSON_VALUE}) public UserInfo userInfo() {
    return AuthContext.authenticatedUserInfo();
  }
}

package com.myco.user.rest.api;

import com.myco.user.api.commands.CreateUser;
import com.myco.user.api.commands.UpdateExternalAuthInfo;
import com.myco.util.values.ObfuscatedToStringProperty;
import org.springframework.util.Assert;

public class ExternalAuthInfoDto {

  private String userId;
  private ObfuscatedToStringProperty<String> apiKey;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getApiKey() {
    return apiKey.getValue();
  }

  public void setApiKey(String apiKey) {
    this.apiKey = new ObfuscatedToStringProperty<>(apiKey);
  }

  public CreateUser toCreateUserCommand(String yardsaleUserId) {
    Assert.hasText(yardsaleUserId, "null/blank yardsale user identifier");
    Assert.hasText(userId, "Invalid/Uninitialized external user identifier");
    Assert.hasText(apiKey.getValue(), "Invalid/Uninitialized external user api key");
    return new CreateUser(yardsaleUserId.trim(), userId.trim(), apiKey);
  }

  public UpdateExternalAuthInfo toUpdateExternalAuthInfoCommand(String yardsaleUserId) {
    Assert.hasText(yardsaleUserId, "null/blank yardsale user identifier");
    Assert.hasText(apiKey.getValue(), "Invalid/Uninitialized external user api key");
    return new UpdateExternalAuthInfo(yardsaleUserId.trim(), userId == null ? null : userId.trim(), apiKey);
  }
}

package com.myco.user.command;

import com.myco.api.values.UserInfo;
import com.myco.user.api.commands.CreateUser;
import com.myco.user.api.commands.UpdateExternalAuthInfo;
import com.myco.user.api.commands.UpdateUserPreferences;
import com.myco.user.api.events.ExternalAuthInfoUpdated;
import com.myco.user.api.events.UserCreated;
import com.myco.user.api.events.UserPreferencesUpdated;
import com.myco.util.crypto.CryptoService;
import com.myco.util.v8n.V8NException;
import com.myco.util.values.ObfuscatedToStringProperty;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.annotation.MetaDataValue;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import static com.myco.api.AxonMessageMetadataKeys.USER_INFO;

@Aggregate
public class UserAggregate {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserAggregate.class);

  @AggregateIdentifier(routingKey = "userId")
  private String userId;
  private String externalUserId;

  public UserAggregate() {
  }

  @CommandHandler
  UserAggregate(
      CreateUser command, @MetaDataValue(USER_INFO) UserInfo userInfo, CryptoService encryptionService,
      @Qualifier("secureDataSecret") ObfuscatedToStringProperty<String> secureDataSecret
  ) {
    LOGGER.debug("Preparing to handle: {}", command);
    Assert.notNull(userInfo, "null userInfo");
    V8NException.ifError(command.validate());
    String encryptedKey = encryptionService.encryptString(secureDataSecret, command.getExternalApiKey());

    AggregateLifecycle.apply(new UserCreated(command.getUserId()));
    AggregateLifecycle
        .apply(new ExternalAuthInfoUpdated(command.getUserId(), encryptedKey, command.getExternalUserId()));
  }

  @CommandHandler
  void handle(
      UpdateExternalAuthInfo command, @MetaDataValue(USER_INFO) UserInfo userInfo, CryptoService encryptionService,
      @Qualifier("secureDataSecret") ObfuscatedToStringProperty<String> secureDataSecret
  ) {
    LOGGER.debug("Preparing to handle: {}", command);
    Assert.notNull(userInfo, "null userInfo");
    V8NException.ifError(command.validate());
    String encryptedKey = encryptionService.encryptString(secureDataSecret, command.getExternalApiKey());

    AggregateLifecycle.apply(
        new ExternalAuthInfoUpdated(command.getUserId(), encryptedKey, externalUserId(command.getExternalUserId())));
  }

  @CommandHandler
  void handle(UpdateUserPreferences command, @MetaDataValue(USER_INFO) UserInfo userInfo) {
    LOGGER.debug("Preparing to handle: {}", command);
    Assert.notNull(userInfo, "null userInfo");
    V8NException.ifError(command.validate());

    AggregateLifecycle
        .apply(new UserPreferencesUpdated(command.getUserId(), command.getDimUnits(), command.getWeightUnits()));
  }

  private String externalUserId(String commandValue) {
    return StringUtils.hasText(commandValue) ? commandValue.trim() : externalUserId;
  }

  @EventSourcingHandler
  void on(UserCreated event) {
    this.userId = event.getUserId();
  }

  @EventSourcingHandler
  void on(ExternalAuthInfoUpdated event) {
    this.externalUserId = event.getExternalUserId();
  }

  @EventSourcingHandler
  void on(UserPreferencesUpdated event) {
    // no-op
  }
}

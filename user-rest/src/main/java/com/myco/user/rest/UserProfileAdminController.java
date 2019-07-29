package com.myco.user.rest;

import com.myco.rest.ControllerAdviceSupport;
import com.myco.rest.DeferredResultSupport;
import com.myco.user.api.UserProfile;
import com.myco.user.api.commands.CreateUser;
import com.myco.user.api.commands.UpdateExternalAuthInfo;
import com.myco.user.rest.api.ExternalAuthInfoDto;
import com.myco.util.v8n.V8NException;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping(path = "/users/{userId}")
public class UserProfileAdminController implements ControllerAdviceSupport {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileAdminController.class);

  private final CommandGateway commandGateway;
  private final QueryGateway queryGateway;

  @Autowired
  public UserProfileAdminController(CommandGateway commandGateway, QueryGateway queryGateway) {
    this.commandGateway = commandGateway;
    this.queryGateway = queryGateway;
  }

  @Override
  public Logger logger() {
    return LOGGER;
  }

  @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
  public DeferredResult<? extends UserProfile> getUserProfile(@PathVariable String userId) {
    return UserProfileController.doGetUserProfile(queryGateway, userId);
  }

  @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
  public DeferredResult<Map<String, String>> createUserProfile(
      @PathVariable String userId, @RequestBody ExternalAuthInfoDto request
  ) {
    CreateUser command = request.toCreateUserCommand(userId);
    V8NException.ifError(command.validate());
    return DeferredResultSupport.from(commandGateway.send(command)
        .thenApply(userProfileId -> Collections.singletonMap("userProfileId", userProfileId.toString())));
  }

  @PutMapping(consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
  public DeferredResult<Void> updateExternalAuthInfo(
      @PathVariable String userId, @RequestBody ExternalAuthInfoDto request
  ) {
    UpdateExternalAuthInfo command = request.toUpdateExternalAuthInfoCommand(userId);
    V8NException.ifError(command.validate());
    return DeferredResultSupport.from(commandGateway.send(command));
  }
}

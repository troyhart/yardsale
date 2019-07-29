package com.myco.user.rest;

import com.myco.api.values.UserInfo;
import com.myco.auth.AuthContext;
import com.myco.rest.ControllerAdviceSupport;
import com.myco.rest.DeferredResultSupport;
import com.myco.rest.ServerSentEventProducerSupport;
import com.myco.user.api.UserProfile;
import com.myco.user.api.commands.UpdateUserPreferences;
import com.myco.user.api.queries.UserProfileByIdQuery;
import com.myco.user.query.UserProfileImpl;
import com.myco.user.rest.api.UserPreferencesDto;
import com.myco.util.v8n.V8NException;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.core.publisher.Flux;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(path = "/user")
public class UserProfileController implements ControllerAdviceSupport, ServerSentEventProducerSupport {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileController.class);

  private final CommandGateway commandGateway;
  private final QueryGateway queryGateway;

  @Autowired
  public UserProfileController(CommandGateway commandGateway, QueryGateway queryGateway) {
    this.commandGateway = commandGateway;
    this.queryGateway = queryGateway;
  }

  static DeferredResult<? extends UserProfile> doGetUserProfile(QueryGateway queryGateway, String userId) {
    UserProfileByIdQuery q = new UserProfileByIdQuery(userId);
    return DeferredResultSupport.from(queryGateway.query(q, ResponseTypes.instanceOf(UserProfileImpl.class))
        .whenComplete(DeferredResultSupport.completeQuery(q)));
  }

  static SubscriptionQueryResult<UserProfile, UserProfile> subscriptionQueryResult(
      QueryGateway queryGateway, String userProfileId
  ) {
    SubscriptionQueryResult<UserProfile, UserProfile> queryResult = queryGateway
        .subscriptionQuery(new UserProfileByIdQuery(userProfileId), ResponseTypes.instanceOf(UserProfile.class),
            ResponseTypes.instanceOf(UserProfile.class));
    return queryResult;
  }

  @Override
  public Logger logger() {
    return LOGGER;
  }

  @PutMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("hasRole('ROLE_USER')")
  public DeferredResult<Void> updatePreferences(@RequestBody UserPreferencesDto request) {
    UserInfo userInfo = AuthContext.authenticatedUserInfo();
    UpdateUserPreferences command = request.toUpdateUserPreferencesCommand(userInfo.getUserId());
    V8NException.ifError(command.validate());
    return DeferredResultSupport.from(commandGateway.send(command));
  }

  @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
  @PreAuthorize("hasRole('ROLE_USER')")
  public DeferredResult<? extends UserProfile> getUserProfile() {
    UserInfo userInfo = AuthContext.authenticatedUserInfo();
    return doGetUserProfile(queryGateway, userInfo.getUserId());
  }

  @GetMapping(path = "/subscription", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @PreAuthorize("hasRole('ROLE_USER')")
  public Flux<ServerSentEvent<?>> getUserProfileSubscription(HttpServletResponse response) {
    UserInfo userInfo = AuthContext.authenticatedUserInfo();
    LOGGER.debug("Subscribe to UserProfile for authenticated entity: {}", userInfo);
    return toSSEFlux(response, subscriptionQueryResult(queryGateway, userInfo.getUserId()), 15)/*.log()*/;
  }
}

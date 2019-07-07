package com.myco;

import com.myco.api.values.UserInfo;
import com.myco.auth.AuthContext;
import com.myco.util.values.ErrorMessage;
import com.myco.util.slf4j.MdcAutoClosable;
import org.axonframework.axonserver.connector.command.AxonServerCommandBus;
import org.axonframework.axonserver.connector.query.AxonServerQueryBus;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.queryhandling.QueryBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.Collections;

import static com.myco.api.AxonMessageMetadataKeys.USER_INFO;

@Configuration public class AxonMessageInterceptorsConfig {

  private Logger LOGGER = LoggerFactory.getLogger(AxonMessageInterceptorsConfig.class);

  @Autowired public void registerInterceptors(CommandBus commandBus, QueryBus queryBus) {
    Assert.notNull(commandBus, "Invalid configuration, commandBus is null!");
    Assert.notNull(queryBus, "Invalid configuration, queryBus is null!");

    if (AxonServerCommandBus.class.isAssignableFrom(commandBus.getClass())) {
      commandBus.registerDispatchInterceptor(authorizationDispatchInterceptor());
      commandBus.registerHandlerInterceptor(authorizationHandlerInterceptor());
    }
    if (AxonServerQueryBus.class.isAssignableFrom(queryBus.getClass())) {
      queryBus.registerDispatchInterceptor(authorizationDispatchInterceptor());
      queryBus.registerHandlerInterceptor(authorizationHandlerInterceptor());
    }
  }

  private MessageDispatchInterceptor<? super Message<?>> authorizationDispatchInterceptor() {
    return list -> {
      if (AuthContext.authenticatedUserInfo() != null) {
        return (index, message) -> {
          UserInfo userInfo = AuthContext.authenticatedUserInfo();
          ErrorMessage errorMessage = userInfo == null ? null : userInfo.validate();
          if (errorMessage != null || userInfo == null) {
            LOGGER.info("Can not dispatch for invalid user -> {}", userInfo);
            throw new SecurityException("Invalid user!");
          }
          LOGGER.debug("Authorization dispatch interceptor resolved user from security context -> {}", userInfo);
          LOGGER.debug("Message -> {}", message.getPayload());
          return message.andMetaData(Collections.singletonMap(USER_INFO, userInfo));
        };
      }
      return (index, message) -> {
        try (MdcAutoClosable mdc = new MdcAutoClosable()) {
          UserInfo userInfo = (UserInfo) message.getMetaData().get(USER_INFO);
          mdc.put("yardsaleUserId", userInfo == null ? "ANONYMOUS" : userInfo.getUserId());
          LOGGER
              .debug("Authorization dispatch interceptor resolved resolved user from message metadata -> {}", userInfo);
          return message;
        }
      };
    };
  }

  private MessageHandlerInterceptor<? super Message<?>> authorizationHandlerInterceptor() {
    return (unitOfWork, interceptorChain) -> {
      try (MdcAutoClosable mdc = new MdcAutoClosable()) {
        UserInfo userInfo = (UserInfo) unitOfWork.getMessage().getMetaData().get(USER_INFO);
        if (userInfo == null) {
          throw new SecurityException("Invalid user!");
        }
        mdc.put("yardsaleUserId", userInfo.getUserId());
        LOGGER.debug("Authorization handler interceptor resolved user -> {}", userInfo);
        return interceptorChain.proceed();
      }
    };
  }
}

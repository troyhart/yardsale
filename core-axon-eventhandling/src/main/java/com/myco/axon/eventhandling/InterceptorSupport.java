package com.myco.axon.eventhandling;

import com.myco.api.values.UserInfo;
import com.myco.auth.AuthContext;
import com.myco.util.slf4j.MdcAutoClosable;
import com.myco.util.values.ErrorMessage;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import static com.myco.api.AxonMessageMetadataKeys.USER_INFO;

public class InterceptorSupport {
  private static final Logger LOGGER = LoggerFactory.getLogger(InterceptorSupport.class);

  public static MessageDispatchInterceptor<? super Message<?>> authorizationDispatchInterceptor() {
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

  public static MessageHandlerInterceptor<? super Message<?>> authorizationHandlerInterceptor() {
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

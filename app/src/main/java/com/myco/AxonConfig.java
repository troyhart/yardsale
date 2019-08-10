package com.myco;

import com.myco.axon.eventhandling.ErrorHandler;
import com.myco.axon.eventhandling.EventHandlingFailureRepository;
import com.myco.axon.eventhandling.InterceptorSupport;
import com.thoughtworks.xstream.XStream;
import org.axonframework.axonserver.connector.command.AxonServerCommandBus;
import org.axonframework.axonserver.connector.query.AxonServerQueryBus;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.gateway.DefaultEventGateway;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventhandling.interceptors.EventLoggingInterceptor;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.messaging.correlation.MessageOriginProvider;
import org.axonframework.messaging.correlation.SimpleCorrelationDataProvider;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.xml.XStreamSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

import static com.myco.api.AxonMessageMetadataKeys.USER_INFO;

@Configuration
public class AxonConfig {

  /**
   * Correctly configuring the XStream serializer to avoid security warnings.
   */
  @Autowired
  public void configureXStream(Serializer serializer) {
    if (serializer instanceof XStreamSerializer) {
      XStream xStream = ((XStreamSerializer) serializer).getXStream();
      XStream.setupDefaultSecurity(xStream);
      xStream.allowTypesByWildcard(new String[] {"com.myco.**", "org.axonframework.**"});
    }
  }

  @Autowired
  public void configureEventProcessors(
      EventProcessingConfigurer configurer, PlatformTransactionManager platformTransactionManager,
      EventHandlingFailureRepository handlerEventHandlingFailureRepository
  ) {
    // TODO: handle tracking event processor initialization. See the axon mailing list thread:
    // *****************************************************************************************************************
    // https://groups.google.com/forum/#!topic/axonframework/eyw0rRiSzUw
    // In that thread there is a discussion about properly initializing the token store to avoid recreating query models.
    // I still need to understand more about this...
    // *****************************************************************************************************************
  }

  @Autowired
  public void configureErrorHandling(
      EventProcessingConfigurer configurer, ErrorHandler errorHandler
  ) {
    configurer.registerDefaultListenerInvocationErrorHandler(c -> errorHandler);
  }

  @Autowired
  public void registerInterceptors(EventBus eventBus, CommandBus commandBus, QueryBus queryBus) {
    Assert.notNull(commandBus, "Invalid configuration, commandBus is null!");
    Assert.notNull(queryBus, "Invalid configuration, queryBus is null!");

    // Authorization Interceptors
    if (AxonServerCommandBus.class.isAssignableFrom(commandBus.getClass())) {
      commandBus.registerDispatchInterceptor(InterceptorSupport.authorizationDispatchInterceptor());
      commandBus.registerHandlerInterceptor(InterceptorSupport.authorizationHandlerInterceptor());
    }
    if (AxonServerQueryBus.class.isAssignableFrom(queryBus.getClass())) {
      queryBus.registerDispatchInterceptor(InterceptorSupport.authorizationDispatchInterceptor());
      queryBus.registerHandlerInterceptor(InterceptorSupport.authorizationHandlerInterceptor());
    }

    // Event Logging on dispatch!
    eventBus.registerDispatchInterceptor(new EventLoggingInterceptor("com.myco.axon"));
  }

  @Bean
  public EventGateway eventGateway(EventBus eventBus) {
    return DefaultEventGateway.builder().eventBus(eventBus).build();
  }

  @Bean
  public CorrelationDataProvider messageOriginProvider() {
    return new MessageOriginProvider();
  }

  @Bean
  public CorrelationDataProvider authCorrelationDataProvider() {
    return new SimpleCorrelationDataProvider(USER_INFO);
  }
}

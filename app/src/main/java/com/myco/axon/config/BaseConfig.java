package com.myco.axon.config;

import com.myco.axon.eventhandling.ErrorHandler;
import com.myco.axon.eventhandling.EventLogger;
import com.myco.axon.eventhandling.FailureRecordRepository;
import com.thoughtworks.xstream.XStream;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.gateway.DefaultEventGateway;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.messaging.correlation.MessageOriginProvider;
import org.axonframework.messaging.correlation.SimpleCorrelationDataProvider;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.xml.XStreamSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import static com.myco.api.AxonMessageMetadataKeys.USER_INFO;

@Configuration
public class BaseConfig {

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
      FailureRecordRepository handlerFailureRecordRepository
  ) {
    // TODO: handle tracking event processor initialization. See the axon mailing list thread:
    // *****************************************************************************************************************
    // https://groups.google.com/forum/#!topic/axonframework/eyw0rRiSzUw
    // In that thread there is a discussion about properly initializing the token store to avoid recreating query models.
    // I still need to understand more about this...
    // *****************************************************************************************************************


    // By default all event handlers are managed by a tracking processor.
    // Select event handlers will be configured as subscribing processors.
    // *****************************************************************************************************************
    configurer.registerSubscribingEventProcessor(EventLogger.class.getPackage().getName());
  }

  @Autowired
  public void configureErrorHandling(
      EventProcessingConfigurer configurer, ErrorHandler errorHandler
  ) {
    configurer.registerDefaultListenerInvocationErrorHandler(c -> errorHandler);
    // TODO: consolidate error handling...I want to define the EventHandler interceptor that handles sending blacklisted events to dead letter queue...presently this will be defined in com.myco.axon.config.MessageInterceptorsConfig
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

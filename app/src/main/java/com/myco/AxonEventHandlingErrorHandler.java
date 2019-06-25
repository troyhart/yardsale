package com.myco;

import com.myco.util.slf4j.MdcAutoClosable;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventMessageHandler;
import org.axonframework.eventhandling.ListenerInvocationErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AxonEventHandlingErrorHandler implements ListenerInvocationErrorHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(AxonEventHandlingErrorHandler.class);

  public static final ListenerInvocationErrorHandler INSTANCE = new AxonEventHandlingErrorHandler();

  private AxonEventHandlingErrorHandler() {
  }

  @Override public void onError(Exception e, EventMessage<?> eventMessage, EventMessageHandler eventMessageHandler) {

    try (MdcAutoClosable mdc = new MdcAutoClosable()) {
      mdc.put("trackingProcessorErrorClass", eventMessageHandler.getTargetType().getSimpleName());
      mdc.put("trackingProcessorErrorMessageId", eventMessage.getIdentifier());
      mdc.put("trackingProcessorErrorPayloadType", eventMessage.getPayloadType().getName());

      LOGGER.error("Tracking Processor Error", e);
    }

    // By throwing an exception we will put the tracking processor into an error mode, where it will try to re-process
    // this event until it succeeds, in perpetuity. By not throwing the exception, the state represented in the
    // exception will not be retried and the event handler will not therefore have the opportunity to apply it's
    // state and so this view will need to be rebuilt for the particular aggregate's view to come up to date...
    //throw e;
  }
}

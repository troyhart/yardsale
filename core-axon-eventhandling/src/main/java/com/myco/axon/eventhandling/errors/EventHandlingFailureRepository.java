package com.myco.axon.eventhandling.errors;

import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventMessageHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventHandlingFailureRepository extends JpaRepository<EventHandlingFailure, String> {
  @NotNull
  static String toEventProcessorGroupName(EventMessageHandler eventMessageHandler) {
    return toEventProcessorGroupName(eventMessageHandler.getTargetType());
  }

  @NotNull
  static String toEventProcessorGroupName(Class<?> eventMessageHandlerTargetType) {
    // TODO: address me! Here I'm assuming usage of the default strategy for configuring tracking processor group IDs.
    //  This assumption is very fragile as the configuration occurs at the application, and this is a reusable module!
    return eventMessageHandlerTargetType.getPackage().getName();
  }

  @NotNull
  static String toEventHandlingFailureId(EventMessageHandler eventMessageHandler, EventMessage<?> eventMessage) {
    return String.format("%s::%s", toEventProcessorGroupName(eventMessageHandler), eventMessage.getIdentifier());
  }
}

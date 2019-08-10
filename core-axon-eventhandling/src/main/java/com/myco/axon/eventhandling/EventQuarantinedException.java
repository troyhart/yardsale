package com.myco.axon.eventhandling;

public class EventQuarantinedException extends RuntimeException {
  /**
   * @param sequenceIdentifier the identifier of the event store sequence, typically the aggregate identifier
   */
  public EventQuarantinedException(Class<?> eventMessageHandlerTargetType, String sequenceIdentifier) {
    super(String.format("Event sequence quarantined: %s::%s", eventMessageHandlerTargetType.getPackage().getName(),
        sequenceIdentifier));
  }
}

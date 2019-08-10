package com.myco.axon.eventhandling;

import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventMessageHandler;
import org.axonframework.eventhandling.async.SequentialPerAggregatePolicy;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.NoSuchElementException;
import java.util.Optional;
@Repository
public interface BlacklistedEventSequenceRepository extends JpaRepository<BlacklistedEventSequence, String> {
  /**
   * <p>
   * See: {@link #isEventSequenceIdentifiable(EventMessage)}
   *
   * @param eventMessage an event message
   * @return an optional sequence identifier value
   */
  @NotNull
  static String toEventSequenceIdentifier(EventMessage<?> eventMessage) {
    Object eventSequenceIdentifier = SequentialPerAggregatePolicy.instance().getSequenceIdentifierFor(eventMessage);
    if (eventSequenceIdentifier == null) {
      throw new NoSuchElementException("No element could be found because the event sequence is unidentifiable");
    }
    return eventSequenceIdentifier.toString();
    // TODO: address me! Problems may lurk here because I'm just taking the String value of
    //  the org.axonframework.eventhandling.async.SequencingPolicy.getSequenceIdentifierFor(...) method.
    //  I have established the pattern within the recover application so far to use the String representation
    //  of UUID for aggregate identifiers. So, since primarily the sequence identifier is going to be the
    //  aggregate identifier this may not be an issue.......???????
  }

  /**
   * The sequence identifier will only be present for domain events and the identifier will be the aggregate identifier.
   *
   * @param eventMessage
   * @return true if the event message has an identifiable sequence (an aggregate identifier).
   */
  @NotNull
  static boolean isEventSequenceIdentifiable(EventMessage<?> eventMessage) {
    return SequentialPerAggregatePolicy.instance().getSequenceIdentifierFor(eventMessage) != null;
  }

  @NotNull
  static String toBlacklistedSequenceId(EventMessageHandler eventMessageHandler, EventMessage eventMessage) {
    return String.format("%s::%s", EventHandlingFailureRepository.toEventProcessorGroupName(eventMessageHandler),
        toEventSequenceIdentifier(eventMessage));
  }

  default Optional<BlacklistedEventSequence> findByEventMessageHandlerTargetTypeAndEventSequenceId(
      Class<?> eventMessageHandlerTargetType, String eventSequenceId
  ) {
    return findById(String
        .format("%s::%s", EventHandlingFailureRepository.toEventProcessorGroupName(eventMessageHandlerTargetType), eventSequenceId));
  }

  default Optional<BlacklistedEventSequence> findByEventMessageHandlerAndEventMessage(
      EventMessageHandler eventMessageHandler, EventMessage<?> eventMessage
  ) {
    return findById(toBlacklistedSequenceId(eventMessageHandler, eventMessage));
  }
}

package com.myco.axon.eventhandling;

import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventMessageHandler;
import org.axonframework.eventhandling.async.SequentialPerAggregatePolicy;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Optional;

@Entity(name = "eventhandling_sequence_blacklist")
public class SequenceBlacklistRecord {

  @Id
  private String id;

  public SequenceBlacklistRecord() {
  }

  SequenceBlacklistRecord(String id) {
    this.id = id;
  }

  /**
   * The sequence identifier will only be present for domain events and the identifier will be the aggregate identifier.
   *
   * @param eventMessage an event message
   * @return an optional sequence identifier value
   */
  @NotNull
  static Optional<Object> toSequenceIdentifier(EventMessage<?> eventMessage) {
    return Optional.ofNullable(SequentialPerAggregatePolicy.instance().getSequenceIdentifierFor(eventMessage));
  }

  /**
   * The optional result will be present when there is a sequence identifier that can be resolved for the event message.
   * See: {@link #toSequenceIdentifier(EventMessage)}.
   *
   * @param eventMessageHandler the event message handler
   * @param eventMessage        the event message
   * @return an optional primary key.
   */
  @NotNull
  static Optional<String> toPrimaryKey(EventMessageHandler eventMessageHandler, EventMessage<?> eventMessage) {
    // TODO: fix me! Problems may lurk here because I'm just using the string representation of the eventMessageIdentifier...
    Optional<Object> sequenceIdentifier = toSequenceIdentifier(eventMessage);
    return sequenceIdentifier.isPresent() ?
        Optional.of(String
            .format("%s::%s", FailureRecord.toEventProcessorGroupName(eventMessageHandler.getTargetType()),
                sequenceIdentifier.get())) :
        Optional.empty();
  }

  @NotNull
  static String toPrimaryKey(Class<?> eventMessageHandlerTargetType, String aggregateIdentifier) {
    return String
        .format("%s::%s", FailureRecord.toEventProcessorGroupName(eventMessageHandlerTargetType), aggregateIdentifier);
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return "SequenceBlacklistRecord{" + "id='" + id + '\'' + '}';
  }
}

package com.myco.axon.eventhandling;

import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.async.SequentialPerAggregatePolicy;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "eventhandling_sequence_blacklist")
public class SequenceBlacklistRecord {

  @Id
  private String id;

  public SequenceBlacklistRecord() {
  }

  public SequenceBlacklistRecord(EventMessage<?> eventMessage, Class<?> handlerType) {
    // TODO: determine if I can use the string value of the identifier along with the handler type for a unique blacklist identifier
    this.id = toPrimaryKey(SequentialPerAggregatePolicy.instance().getSequenceIdentifierFor(eventMessage).toString(),
        handlerType);
  }

  @NotNull
  static String toPrimaryKey(String eventMessageIdentifier, Class<?> handlerType) {
    return FailureRecord.toPrimaryKey(eventMessageIdentifier, handlerType);
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return "SequenceBlacklistRecord{" + "id='" + id + '\'' + '}';
  }
}

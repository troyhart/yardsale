package com.myco.axon.eventhandling;

import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "eventhandling_sequence_blacklist")
public class SequenceBlacklistRecord {

  @Id
  private String id;

  public SequenceBlacklistRecord() {
  }

  public SequenceBlacklistRecord(String id) {
    this.id = id;
  }

  @NotNull
  static String toPrimaryKey(Class<?> handlerType, Object eventSequenceIdentifier) {
    // TODO: fix me! Problems may lurk here because I'm just using the string representation of the eventMessageIdentifier...
    return String.format("%s::%s", ErrorHandler.toEventProcessorGroupName(handlerType), eventSequenceIdentifier);
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return "SequenceBlacklistRecord{" + "id='" + id + '\'' + '}';
  }
}

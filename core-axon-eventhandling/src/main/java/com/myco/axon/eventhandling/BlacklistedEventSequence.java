package com.myco.axon.eventhandling;

import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventMessageHandler;
import org.axonframework.eventhandling.async.SequentialPerAggregatePolicy;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Optional;

@Entity
public class BlacklistedEventSequence {

  @Id
  private String id;

  public BlacklistedEventSequence() {
  }

  BlacklistedEventSequence(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return "BlacklistedEventSequence{" + "id='" + id + '\'' + '}';
  }
}

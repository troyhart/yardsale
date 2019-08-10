package com.myco.axon.eventhandling;

import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventMessageHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity(name = "eventhandling_failures")
public class EventHandlingFailure {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventHandlingFailure.class);

  @Id
  private String id;
  @Column
  private Integer failureCount;
  @Column
  private boolean attemptsExhausted;
  @Column
  private Instant createdInstant;
  @Column
  private Instant lastRetryInstant;

  public EventHandlingFailure() {
  }

  EventHandlingFailure(String id) {
    this.id = id;
    this.failureCount = 1;
    this.createdInstant = this.lastRetryInstant = Instant.now();
  }

  @NotNull
  static String toEventProcessorGroupName(Class<?> handlerType) {
    return handlerType.getPackage().getName();
  }

  @NotNull
  static String toPrimaryKey(EventMessageHandler eventMessageHandler, EventMessage<?> eventMessage) {
    // TODO: fix me! Problems may lurk here because I'm just using the string representation of the eventMessageIdentifier...
    return String
        .format("%s::%s", toEventProcessorGroupName(eventMessageHandler.getTargetType()), eventMessage.getIdentifier());
  }

  @NotNull
  public String getId() {
    return id;
  }

  @NotNull
  public Integer getFailureCount() {
    return failureCount;
  }

  public boolean attemptsExhausted() {
    return attemptsExhausted;
  }

  @NotNull
  public Instant getCreatedInstant() {
    return createdInstant;
  }

  @NotNull
  public Instant getLastRetryInstant() {
    return lastRetryInstant;
  }

  @NotNull
  EventHandlingFailure recordFailure() {
    if (attemptsExhausted) {
      // This should never happen because the event shouldn't have even been retried when the state is already "attemptsExhausted"
      LOGGER.warn("Already exhausted record asked to handler another failure. No state will change in this case!");
    }
    else {
      //TODO: determine the right value for retry count...7 wasn't really researched or anything...
      if (getFailureCount() > 7) {
        markAttemptsExhausted();
      }
      else {
        bumpFailureCount();
      }
    }
    return this;
  }

  protected EventHandlingFailure bumpFailureCount() {
    if (!attemptsExhausted) {
      failureCount += 1;
      lastRetryInstant = Instant.now();
    }
    return this;
  }

  protected EventHandlingFailure markAttemptsExhausted() {
    if (!attemptsExhausted) {
      lastRetryInstant = Instant.now();
      attemptsExhausted = true;
    }
    return this;
  }

  @Override
  public String toString() {
    return "EventHandlingFailure{" + "id='" + id + '\'' + ", failureCount=" + failureCount + ", attemptsExhausted="
        + attemptsExhausted + ", createdInstant=" + createdInstant + ", lastRetryInstant=" + lastRetryInstant + '}';
  }
}
package com.myco.axon.eventhandling.errors;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
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

  EventHandlingFailure(String id, boolean retryable) {
    this.id = id;
    this.failureCount = 1;
    this.createdInstant = this.lastRetryInstant = Instant.now();
    if (!retryable) {
      lastRetryInstant = Instant.now();
      attemptsExhausted = true;
    }
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
  EventHandlingFailure recordFailure(boolean retryable) {
    if (attemptsExhausted) {
      // This should never happen because the event shouldn't have even been retried when the state is already "attemptsExhausted"
      LOGGER.warn("Already exhausted record asked to handler another failure. No state will change in this case!");
    }
    else {
      //TODO: determine the upper limit. 10 is arbitrary!
      if (getFailureCount() >= 10 || !retryable) {
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

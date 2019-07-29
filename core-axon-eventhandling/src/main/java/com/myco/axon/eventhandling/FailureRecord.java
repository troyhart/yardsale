package com.myco.axon.eventhandling;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity(name = "eventhandling_failures")
public class FailureRecord {
  private static final Logger LOGGER = LoggerFactory.getLogger(FailureRecord.class);

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

  public FailureRecord() {
  }

  public FailureRecord(String eventMessageIdentifier, Class<?> handlerType) {
    this.id = toPrimaryKey(eventMessageIdentifier, handlerType);
    this.failureCount = 1;
    this.createdInstant = this.lastRetryInstant = Instant.now();
  }

  @NotNull
  static String toPrimaryKey(String eventMessageIdentifier, Class<?> handlerType) {
    String eventHandlerName = toEventHandlerName(handlerType);
    return String.format("%s::%s", eventHandlerName, eventMessageIdentifier);
  }

  @NotNull
  static String toEventHandlerName(Class<?> handlerType) {
    String rawName = handlerType.getTypeName();
    // TODO: consider alternatives to cropping at the first '$' character. It just seems like there should be a better way.
    int idx = rawName.indexOf('$');
    return (idx > 0) ? rawName.substring(0, idx) : rawName;
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
  FailureRecord recordFailure() {
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

  protected FailureRecord bumpFailureCount() {
    if (!attemptsExhausted) {
      failureCount += 1;
      lastRetryInstant = Instant.now();
    }
    return this;
  }

  protected FailureRecord markAttemptsExhausted() {
    if (!attemptsExhausted) {
      lastRetryInstant = Instant.now();
      attemptsExhausted = true;
    }
    return this;
  }

  @Override
  public String toString() {
    return "FailureRecord{" + "id='" + id + '\'' + ", failureCount=" + failureCount + ", attemptsExhausted="
        + attemptsExhausted + ", createdInstant=" + createdInstant + ", lastRetryInstant=" + lastRetryInstant + '}';
  }
}

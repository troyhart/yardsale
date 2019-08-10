package com.myco.axon.eventhandling;

public interface BlacklistedEventSequenceAware {
  BlacklistedEventSequenceRepository getBlacklistedEventSequenceRepository();

  /**
   * An event sequence (aggregate) is black listed for a specific processing group. This default
   * implementation takes the package of the implementing class (see: {@link #getClass()}) as
   * the processing group.
   *
   * @param eventSequenceId the identifier of a unique event sequence, typically an aggregate identifier.
   * @throws EventQuarantinedException if the identified aggregate is black listed.
   */
  default void throwIfBlacklisted(String eventSequenceId) throws EventQuarantinedException {
    if (getBlacklistedEventSequenceRepository().findByEventMessageHandlerTargetTypeAndEventSequenceId(getClass(), eventSequenceId)
        .isPresent()) {
      throw new EventQuarantinedException(getClass(), eventSequenceId);
    }
  }
}

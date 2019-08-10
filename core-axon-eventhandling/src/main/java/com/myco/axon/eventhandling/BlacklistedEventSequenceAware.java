package com.myco.axon.eventhandling;

public interface BlacklistedEventSequenceAware {
  BlacklistedEventSequenceRepository getBlacklistedEventSequenceRepository();

  /**
   * An aggregate is black listed for a specific processing group. This default implementation takes the package of the
   * implementing class (see: {@link #getClass()}) as the processing group.
   *
   * @param aggregateId an aggregate identifer
   * @throws EventQuarantinedException if the identified aggregate is black listed.
   */
  default void throwIfBlacklisted(String aggregateId) throws EventQuarantinedException {
    if (getBlacklistedEventSequenceRepository().findById(BlacklistedEventSequence.toPrimaryKey(getClass(), aggregateId))
        .isPresent()) {
      throw new EventQuarantinedException();
    }
  }
}

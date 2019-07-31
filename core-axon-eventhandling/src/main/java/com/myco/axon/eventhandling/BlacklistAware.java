package com.myco.axon.eventhandling;

public interface BlacklistAware {
  SequenceBlacklistRecordRepository getSequenceBlacklistRecordRepository();

  /**
   * @param aggregateId an aggregate identifer
   * @throws EventQuarantinedException if the identified aggregate is quarantined for this {@link BlacklistAware} event processor.
   */
  default void throwIfBlacklisted(String aggregateId) throws EventQuarantinedException {
    // TODO: determine if this is a problem to assume the sequencing policy will be the aggregate identifier.
    if (getSequenceBlacklistRecordRepository()
        .findById(SequenceBlacklistRecord.toPrimaryKey(getClass(), aggregateId)).isPresent()) {
      throw new EventQuarantinedException();
    }
  }
}

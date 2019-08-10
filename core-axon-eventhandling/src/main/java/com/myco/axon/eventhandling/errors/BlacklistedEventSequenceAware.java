package com.myco.axon.eventhandling.errors;

import com.myco.axon.eventhandling.QueryGatewayAware;
import org.slf4j.Logger;

import java.util.concurrent.ExecutionException;

public interface BlacklistedEventSequenceAware extends QueryGatewayAware {

  Logger logger();

  /**
   * An event sequence (aggregate) is black listed for a specific processing group. This default
   * implementation takes the package of the implementing class (see: {@link #getClass()}) as
   * the processing group.
   *
   * @param eventSequenceId the identifier of a unique event sequence, typically an aggregate identifier.
   * @throws EventQuarantinedException if the identified aggregate is black listed.
   */
  default void throwIfBlacklistedEventSequence(String eventSequenceId) throws EventQuarantinedException {
    BlacklistedEventSequence blacklistedEventSequence;
    BlacklistedEventSequenceByEventMessageHandlerTargetTypeAndEventSequenceId query =
        new BlacklistedEventSequenceByEventMessageHandlerTargetTypeAndEventSequenceId(getClass(), eventSequenceId);

    try {
      blacklistedEventSequence = queryGateway().query(query, BlacklistedEventSequence.class).get();
    }
    catch (InterruptedException | ExecutionException e) {
      blacklistedEventSequence = null;
      logger().info(String
          .format("Unable to determine if event sequence has been blacklisted; blacklistedEventSequenceId: %s",
              query.blacklistedEventSequenceId()), e);
    }

    if (blacklistedEventSequence != null) {
      throw new EventQuarantinedException(getClass(), eventSequenceId);
    }
  }
}

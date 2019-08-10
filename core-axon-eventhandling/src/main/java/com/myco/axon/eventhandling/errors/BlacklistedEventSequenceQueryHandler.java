package com.myco.axon.eventhandling.errors;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BlacklistedEventSequenceQueryHandler {

  private final BlacklistedEventSequenceRepository repository;

  @Autowired
  public BlacklistedEventSequenceQueryHandler(BlacklistedEventSequenceRepository repository) {
    this.repository = repository;
  }

  @QueryHandler
  BlacklistedEventSequence handle(BlacklistedEventSequenceByEventMessageHandlerTargetTypeAndEventSequenceId query) {
    Optional<BlacklistedEventSequence> result = repository.findById(query.blacklistedEventSequenceId());
    return result.isPresent() ? result.get() : null;
  }
}

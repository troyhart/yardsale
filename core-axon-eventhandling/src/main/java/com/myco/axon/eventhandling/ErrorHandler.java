package com.myco.axon.eventhandling;

import com.myco.util.slf4j.MdcAutoClosable;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventMessageHandler;
import org.axonframework.eventhandling.ListenerInvocationErrorHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

@Component
public class ErrorHandler implements ListenerInvocationErrorHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

  private EventHandlingFailureRepository eventHandlingFailureRepository;
  private BlacklistedEventSequenceRepository blacklistedEventSequenceRepository;
  private TransactionTemplate transactionTemplate;
  private EventGateway eventGateway;

  @Autowired
  private ErrorHandler(
      PlatformTransactionManager platformTransactionManager,
      EventHandlingFailureRepository eventHandlingFailureRepository,
      BlacklistedEventSequenceRepository blacklistedEventSequenceRepository, EventGateway eventGateway
  ) {
    transactionTemplate = new TransactionTemplate(platformTransactionManager);
    transactionTemplate.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
    this.eventHandlingFailureRepository = eventHandlingFailureRepository;
    this.blacklistedEventSequenceRepository = blacklistedEventSequenceRepository;
    this.eventGateway = eventGateway;
  }

  private void handleBlacklisting(
      EventMessage eventMessage, EventMessageHandler eventMessageHandler
  ) {
    transactionTemplate.execute(transactionStatus -> {
      // always deliver to the dead letter queue.
      deliverDeadLetter(eventMessage, eventMessageHandler);

      // If there is an identifiable sequence then we can black list it, otherwise we can not
      if (BlacklistedEventSequenceRepository.isEventSequenceIdentifiable(eventMessage)) {
        Optional<BlacklistedEventSequence> sequenceBlacklistRecord = blacklistedEventSequenceRepository
            .findByEventMessageHandlerAndEventMessage(eventMessageHandler, eventMessage);
        if (sequenceBlacklistRecord.isPresent()) {
          // Processors which are NOT BlacklistedEventSequenceAware will continue to attempt to process events from a blacklisted sequence.
          // subsequent failures for a given sequence (aggregate) will. BlacklistedEventSequenceAware processors may also choose when to
          // quarantine...
          LOGGER.debug("Sequence already blacklisted: {}", sequenceBlacklistRecord.get().getId());
        }
        else {
          BlacklistedEventSequence blacklistedEventSequence = blacklistedEventSequenceRepository.save(
              new BlacklistedEventSequence(
                  BlacklistedEventSequenceRepository.toBlacklistedSequenceId(eventMessageHandler, eventMessage)));
          LOGGER.warn("Sequence blacklisted: {}", blacklistedEventSequence.getId());
          // Raise event so processors can respond and mark the aggregate record...
          eventGateway.publish(new EventSequenceBlacklisted(
              EventHandlingFailureRepository.toEventProcessorGroupName(eventMessageHandler),
              BlacklistedEventSequenceRepository.toEventSequenceIdentifier(eventMessage)));
        }
      }
      else {
        // The given EventMessage is not a domain event because it doesn't have a sequence identifier
        // So this event message can not be blacklisted. However, we did deliver the event message
        // to the dead letter queue...
        LOGGER.warn("Unidentifiable sequence can not be blacklisted: {}", eventMessage);
      }
      // nothing to return
      return null;
    });
  }

  /*
   * Update state for existing records or create a new persistent record and do it in a
   */
  private void handlerFailure(
      Exception e, EventMessage<?> eventMessage, EventMessageHandler eventMessageHandler
  ) throws Exception {
    EventHandlingFailure eventHandlingFailure = transactionTemplate.execute(transactionStatus -> {
      String eventHandlingFailureId =
          EventHandlingFailureRepository.toEventHandlingFailureId(eventMessageHandler, eventMessage);
      Optional<EventHandlingFailure> optionalHandlerFailureRecord =
          eventHandlingFailureRepository.findById(eventHandlingFailureId);
      return optionalHandlerFailureRecord.isPresent() ?
          // update existing record
          eventHandlingFailureRepository.save(optionalHandlerFailureRecord.get().recordFailure(isRetryable(e))) :
          // a new failure record
          eventHandlingFailureRepository.save(new EventHandlingFailure(eventHandlingFailureId, isRetryable(e)));
    });

    LOGGER.info("Handling failure record: {}", eventHandlingFailure);
    if (eventHandlingFailure.attemptsExhausted()) {
      handleBlacklisting(eventMessage, eventMessageHandler);
    }
    else {
      LOGGER.info("Will retry failed event message.");
      throw e;
    }
  }

  private boolean isRetryable(Exception e) {
    return !(e instanceof IllegalArgumentException || e instanceof NullPointerException);
  }

  private void deliverDeadLetter(EventMessage<?> eventMessage, EventMessageHandler eventMessageHandler) {
    // ************************************************************************
    // ************************************************************************
    // ************************************************************************
    // TODO: implement me....
    //
    // Thoughts: it seems like I shouldn't need an actual queue because I can identify every event message
    // and so it seems like I should simply be able to select events from the event store to replay as needed.
    // I will just need to record the value of EventHandlingFailure.toPrimaryKey(eventMessageHandler, eventMessage) in
    // order to identify both the processing group and the event message.
    // ************************************************************************
    // ************************************************************************
    // ************************************************************************

    try (MdcAutoClosable mdc = new MdcAutoClosable()) {
      if (BlacklistedEventSequenceRepository.isEventSequenceIdentifiable(eventMessage)) {
        mdc.put("blacklistedSequenceId",
            BlacklistedEventSequenceRepository.toBlacklistedSequenceId(eventMessageHandler, eventMessage));
      }
      else {
        mdc.put("blacklistedSequenceId", "unidentifiable");
      }
      LOGGER.info("Dead letter: {}", eventMessage);
    }
  }

  @Override
  public void onError(Exception e, EventMessage<?> eventMessage, EventMessageHandler eventMessageHandler)
      throws Exception {

    try (MdcAutoClosable mdc = new MdcAutoClosable()) {
      mdc.put("eventMessageIdentifier", eventMessage.getIdentifier());
      mdc.put("eventMessagePayloadType", eventMessage.getPayloadType());
      mdc.put("eventMessageProcessorGroup",
          EventHandlingFailureRepository.toEventProcessorGroupName(eventMessageHandler));
      LOGGER.error(String.format("Tracking processor error: %s", eventMessage), e);

      if (e instanceof EventQuarantinedException) {
        deliverDeadLetter(eventMessage, eventMessageHandler);
      }
      else {
        handlerFailure(e, eventMessage, eventMessageHandler);
      }
    }
  }
}

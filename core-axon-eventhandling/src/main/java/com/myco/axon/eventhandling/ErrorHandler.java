package com.myco.axon.eventhandling;

import com.myco.util.slf4j.MdcAutoClosable;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventMessageHandler;
import org.axonframework.eventhandling.ListenerInvocationErrorHandler;
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

  private FailureRecordRepository failureRecordRepository;
  private SequenceBlacklistRecordRepository sequenceBlacklistRecordRepository;
  private TransactionTemplate transactionTemplate;

  @Autowired
  private ErrorHandler(
      PlatformTransactionManager platformTransactionManager, FailureRecordRepository failureRecordRepository,
      SequenceBlacklistRecordRepository sequenceBlacklistRecordRepository
  ) {
    transactionTemplate = new TransactionTemplate(platformTransactionManager);
    transactionTemplate.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
    this.failureRecordRepository = failureRecordRepository;
    this.sequenceBlacklistRecordRepository = sequenceBlacklistRecordRepository;
  }

  private void handleBlacklisting(
      EventMessage eventMessage, EventMessageHandler eventMessageHandler
  ) {
    transactionTemplate.execute(transactionStatus -> {
      // always deliver to the dead letter queue.
      deliverDeadLetter(eventMessage, eventMessageHandler);

      // If there is an identifiable sequence then we can black list it, otherwise we can not
      Optional<Object> aggregateIdentifier = SequenceBlacklistRecord.toSequenceIdentifier(eventMessage);
      if (aggregateIdentifier.isPresent()) {
        // There is an identifiable sequence (aggregateIdentifier), so handle blacklisting it.
        deliverDeadLetter(eventMessage, eventMessageHandler);
        // blindly de-referencing the primary key below because I know it will be present, because
        // the aggregateIdentifier was present above.
        String blacklistPK = SequenceBlacklistRecord.toPrimaryKey(eventMessageHandler, eventMessage).get();
        Optional<SequenceBlacklistRecord> sequenceBlacklistRecord =
            sequenceBlacklistRecordRepository.findById(blacklistPK);
        if (sequenceBlacklistRecord.isPresent()) {
          // Processors which are NOT BlacklistAware will continue to attempt to process events from a blacklisted sequence.
          // subsequent failures for a given sequence (aggregate) will. BlacklistAware processors may also choose when to
          // quarantine...
          LOGGER.debug("Sequence already blacklisted: {}", blacklistPK);
        }
        else {
          LOGGER.warn("Sequence blacklisted: {}", blacklistPK);
        }
        sequenceBlacklistRecordRepository.save(new SequenceBlacklistRecord(blacklistPK));
      }
      else {
        // The given EventMessage is not a domain event because it doesn't have a sequence identifier
        // So this event message can not be black listed. However, we did deliver the event message
        // to the dead letter queue...
        LOGGER.warn("Unidentifiable sequence can not be black listed: {}", eventMessage);
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
    FailureRecord failureRecord = transactionTemplate.execute(transactionStatus -> {
      String failureRecordPK = FailureRecord.toPrimaryKey(eventMessageHandler, eventMessage);
      Optional<FailureRecord> optionalHandlerFailureRecord = failureRecordRepository.findById(failureRecordPK);
      return optionalHandlerFailureRecord.isPresent() ?
          // update existing record
          failureRecordRepository.saveAndFlush(optionalHandlerFailureRecord.get().recordFailure()) :
          // a new failure record
          failureRecordRepository.saveAndFlush(new FailureRecord(failureRecordPK));
    });

    LOGGER.info("Handling failure record: {}", failureRecord);
    if (failureRecord.attemptsExhausted()) {
      handleBlacklisting(eventMessage, eventMessageHandler);
    }
    else {
      LOGGER.info("Will retry failed event message.");
      throw e;
    }
  }

  private void deliverDeadLetter(EventMessage<?> eventMessage, EventMessageHandler eventMessageHandler) {
    // ************************************************************************
    // ************************************************************************
    // ************************************************************************
    // TODO: implement me....
    //
    // Thoughts: it seems like I shouldn't need an actual queue because I can identify every event message
    // and so it seems like I should simply be able to select events from the event store to replay as needed.
    // I will just need to record the value of FailureRecord.toPrimaryKey(eventMessageHandler, eventMessage) in
    // order to identify both the processing group and the event message.
    // ************************************************************************
    // ************************************************************************
    // ************************************************************************

    try (MdcAutoClosable mdc = new MdcAutoClosable()) {
      mdc.put("deadLetterId", FailureRecord.toPrimaryKey(eventMessageHandler, eventMessage));
      Optional<String> sequenceRecordPK = SequenceBlacklistRecord.toPrimaryKey(eventMessageHandler, eventMessage);
      if (sequenceRecordPK.isPresent()) {
        mdc.put("deadLetterSequenceId", sequenceRecordPK.get());
      }
      else {
        mdc.put("deadLetterSequenceId", "non-domain-event");
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
          FailureRecord.toEventProcessorGroupName(eventMessageHandler.getTargetType()));
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

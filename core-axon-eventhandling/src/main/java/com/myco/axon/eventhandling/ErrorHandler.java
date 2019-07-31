package com.myco.axon.eventhandling;

import com.myco.util.slf4j.MdcAutoClosable;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventMessageHandler;
import org.axonframework.eventhandling.ListenerInvocationErrorHandler;
import org.axonframework.eventhandling.async.SequentialPerAggregatePolicy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

@Component
public class ErrorHandler implements ListenerInvocationErrorHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

  private FailureRecordRepository handlerFailureRecordRepository;
  private SequenceBlacklistRecordRepository sequenceBlacklistRecordRepository;
  private TransactionTemplate transactionTemplate;

  @Autowired
  private ErrorHandler(
      PlatformTransactionManager platformTransactionManager, FailureRecordRepository handlerFailureRecordRepository,
      SequenceBlacklistRecordRepository sequenceBlacklistRecordRepository
  ) {
    transactionTemplate = new TransactionTemplate(platformTransactionManager);
    transactionTemplate.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
    this.handlerFailureRecordRepository = handlerFailureRecordRepository;
    this.sequenceBlacklistRecordRepository = sequenceBlacklistRecordRepository;
  }

  @NotNull
  static String toEventProcessorGroupName(Class<?> handlerType) {
    return handlerType.getPackage().getName();
  }

  private SequenceBlacklistRecord handleBlacklisting(
      EventMessage eventMessage, EventMessageHandler eventMessageHandler
  ) {
    Optional<Object> aggregateIdentifier = toSequenceIdentifier(eventMessage);
    SequenceBlacklistRecord sequenceBlacklistRecord;
    if (aggregateIdentifier.isPresent()) {
      // handling black listing will persist a SequenceBlacklistRecord AND will deliver the message to the dead letter queue
      // Subsequent processing of a blacklisted sequence
      deliverDeadLetter(eventMessage);// TODO: fix me!!! Does this need to be done in the transaction???

      String blacklistPK =
          SequenceBlacklistRecord.toPrimaryKey(eventMessageHandler.getTargetType(), aggregateIdentifier.get());

      sequenceBlacklistRecord = transactionTemplate.execute(new TransactionCallback<SequenceBlacklistRecord>() {
        @Override
        public SequenceBlacklistRecord doInTransaction(TransactionStatus status) {
          Optional<SequenceBlacklistRecord> sequenceBlacklistRecord =
              sequenceBlacklistRecordRepository.findById(blacklistPK);
          if (sequenceBlacklistRecord.isPresent()) {
            // TODO: there shouldn't be persistent SequenceBlacklistRecord already...what should I do with this???
            LOGGER.error("Unexpected Blacklisting Scenario! This should be researched!");
          }
          return sequenceBlacklistRecord.isPresent() ?
              sequenceBlacklistRecord.get() :
              sequenceBlacklistRecordRepository.save(new SequenceBlacklistRecord(blacklistPK));
        }
      });

      LOGGER.warn("Failure attempts exhausted; Sequence blacklisted: {}", blacklistPK);
    }
    else {
      sequenceBlacklistRecord = null;
      // The given EventMessage is not a domain event because it doesn't have a sequence identifier
      // So this event message can not be black listed AND will not be quarantined because of the
      // lack of a sequence identifier....
      LOGGER.warn("Failure attempts exhausted; no identifiable sequence present (non-DomainEvent message): {}",
          eventMessage);
    }

    return sequenceBlacklistRecord;
  }

  /*
   * Update state for existing records or create a new persistent record and do it in a
   */
  private FailureRecord handleFailureState(
      EventMessage<?> eventMessage, EventMessageHandler eventMessageHandler
  ) {
    return transactionTemplate.execute(new TransactionCallback<FailureRecord>() {
      @Override
      public FailureRecord doInTransaction(TransactionStatus status) {
        Optional<FailureRecord> optionalHandlerFailureRecord = handlerFailureRecordRepository
            .findById(FailureRecord.toPrimaryKey(eventMessageHandler.getTargetType(), eventMessage.getIdentifier()));

        return optionalHandlerFailureRecord.isPresent() ?
            // update existing record
            handlerFailureRecordRepository.saveAndFlush(optionalHandlerFailureRecord.get().recordFailure()) :
            // a new failure record
            handlerFailureRecordRepository.saveAndFlush(new FailureRecord(
                FailureRecord.toPrimaryKey(eventMessageHandler.getTargetType(), eventMessage.getIdentifier())));
      }
    });
  }

  private Optional<Object> toSequenceIdentifier(EventMessage<?> eventMessage) {
    return Optional.ofNullable(SequentialPerAggregatePolicy.instance().getSequenceIdentifierFor(eventMessage));
  }

  private void deliverDeadLetter(EventMessage<?> eventMessage) {
    // ************************************************************************
    // ************************************************************************
    // ************************************************************************
    // TODO: implement me....do delivery to the dead letter queue...
    // ************************************************************************
    // ************************************************************************
    // ************************************************************************

    LOGGER.info("Dead letter: {}", eventMessage);
  }

  @Override
  public void onError(Exception e, EventMessage<?> eventMessage, EventMessageHandler eventMessageHandler)
      throws Exception {

    try (MdcAutoClosable mdc = new MdcAutoClosable()) {
      mdc.put("eventMessageIdentifier", eventMessage.getIdentifier());
      mdc.put("eventMessagePayloadType", eventMessage.getPayloadType());
      mdc.put("eventMessageProcessorGroup", toEventProcessorGroupName(eventMessageHandler.getTargetType()));
      LOGGER.error("Tracking Processor Error", e);

      if (e instanceof EventQuarantinedException) {
        deliverDeadLetter(eventMessage);
        return; // short circuit because already blacklisted and attempts exhausted.
      }

      FailureRecord failureRecord = handleFailureState(eventMessage, eventMessageHandler);
      LOGGER.info("Handling failure record: {}", failureRecord);

      if (failureRecord.attemptsExhausted()) {
        handleBlacklisting(eventMessage, eventMessageHandler);
      }
      else {
        LOGGER.debug("Will retry event processing");
        throw e;// forces retry
      }
    }
  }
}

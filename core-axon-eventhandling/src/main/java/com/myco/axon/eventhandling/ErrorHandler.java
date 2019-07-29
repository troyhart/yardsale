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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

@Component
public class ErrorHandler implements ListenerInvocationErrorHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);
  private static ErrorHandler INSTANCE;

  private FailureRecordRepository handlerFailureRecordRepository;
  private SequenceBlacklistRecordRepository sequenceBlacklistRecordRepository;
  private TransactionTemplate transactionTemplate;

  @Autowired
  private ErrorHandler(
      PlatformTransactionManager platformTransactionManager, FailureRecordRepository handlerFailureRecordRepository,
      SequenceBlacklistRecordRepository sequenceBlacklistRecordRepository
  ) {
    transactionTemplate = new TransactionTemplate(platformTransactionManager);
    transactionTemplate.setPropagationBehavior(Propagation.REQUIRED.value());
    this.handlerFailureRecordRepository = handlerFailureRecordRepository;
    this.sequenceBlacklistRecordRepository = sequenceBlacklistRecordRepository;
  }

  public static synchronized ListenerInvocationErrorHandler instance(
      PlatformTransactionManager platformTransactionManager, FailureRecordRepository handlerFailureRecordRepository,
      SequenceBlacklistRecordRepository sequenceBlacklistRecordRepository
  ) {
    return INSTANCE == null ?
        INSTANCE = new ErrorHandler(platformTransactionManager, handlerFailureRecordRepository,
            sequenceBlacklistRecordRepository) :
        INSTANCE;
  }

  private void handleBlacklisting(
      FailureRecord failureRecord, EventMessage<?> eventMessage, EventMessageHandler eventMessageHandler
  ) {
    SequenceBlacklistRecord sequenceBlacklistRecord = sequenceBlacklistRecordRepository
        .save(new SequenceBlacklistRecord(eventMessage, eventMessageHandler.getTargetType()));
    LOGGER.error("Blacklisting Sequence {}", sequenceBlacklistRecord.getId());
    // TODO: deliver the event message to the dead letter queue
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
            .findById(FailureRecord.toPrimaryKey(eventMessage.getIdentifier(), eventMessageHandler.getTargetType()));
        return optionalHandlerFailureRecord.isPresent() ?
            // update existing record
            handlerFailureRecordRepository.saveAndFlush(optionalHandlerFailureRecord.get().recordFailure()) :
            // a new failure record
            handlerFailureRecordRepository
                .saveAndFlush(new FailureRecord(eventMessage.getIdentifier(), eventMessageHandler.getTargetType()));
      }
    });
  }

  @Override
  public void onError(Exception e, EventMessage<?> eventMessage, EventMessageHandler eventMessageHandler)
      throws Exception {
    try (MdcAutoClosable mdc = new MdcAutoClosable()) {
      FailureRecord failureRecord = handleFailureState(eventMessage, eventMessageHandler);
      mdc.put("trackingProcessorFailureRecordId", failureRecord.getId())
          .put("trackingProcessorFailureRecordAttempts", failureRecord.getFailureCount());
      LOGGER.error("Tracking Processor Error", e);
      if (failureRecord.attemptsExhausted()) {
        handleBlacklisting(failureRecord, eventMessage, eventMessageHandler);
      }
      else {
        LOGGER.debug("Will retry event processing");
        throw e;// forces retry
      }
    }
  }
}

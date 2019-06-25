package com.myco;

import com.myco.api.values.UserInfo;
import com.myco.util.slf4j.MdcAutoClosable;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.SequenceNumber;
import org.axonframework.eventhandling.Timestamp;
import org.axonframework.messaging.annotation.MetaDataValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static com.myco.api.AxonMessageMetadataKeys.USER_INFO;

@Component
public class EventLogger {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventLogger.class);

  @EventHandler
  public void on(Object event, @SequenceNumber long aggregateVersion, @Timestamp Instant occurrenceInstant,
      @MetaDataValue(USER_INFO) UserInfo userInfo) {
    try(MdcAutoClosable mdc = new MdcAutoClosable()) {
      mdc.put("yardsaleUserId", userInfo == null ? "ANONYMOUS" : userInfo.getUserId());
      LOGGER.info("Published -> {}", event);
    }
  }
}

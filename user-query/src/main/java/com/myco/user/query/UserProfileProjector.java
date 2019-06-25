package com.myco.user.query;

import com.myco.api.values.UserInfo;
import com.myco.user.api.events.ExternalAuthInfoUpdated;
import com.myco.user.api.events.UserCreated;
import com.myco.user.api.events.UserPreferencesUpdated;
import com.myco.user.api.queries.UserProfileByIdQuery;
import com.myco.util.slf4j.MdcAutoClosable;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.SequenceNumber;
import org.axonframework.eventhandling.Timestamp;
import org.axonframework.messaging.annotation.MetaDataValue;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Optional;

import static com.myco.api.AxonMessageMetadataKeys.USER_INFO;

@Component class UserProfileProjector {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileProjector.class);

  private UserProfileContainerRepository userProfileContainerRepository;
  private QueryUpdateEmitter queryUpdateEmitter;

  @Autowired UserProfileProjector(UserProfileContainerRepository userProfileContainerRepository,
      QueryUpdateEmitter queryUpdateEmitter) {
    this.userProfileContainerRepository = userProfileContainerRepository;
    this.queryUpdateEmitter = queryUpdateEmitter;
  }

  @EventHandler void on(UserCreated event, @SequenceNumber long aggregateVersion, @Timestamp Instant occurrenceInstant,
      @MetaDataValue(USER_INFO) UserInfo userInfo) {

    try (MdcAutoClosable mdc = new MdcAutoClosable()) {
      mdc(event.getUserId(), aggregateVersion, mdc);
      handleDebug(event);

      UserProfileImpl userProfile = new UserProfileImpl();
      userProfile.setUserId(event.getUserId());

      save(new UserProfileContainer(userProfile), userInfo, occurrenceInstant, aggregateVersion);
    }
  }

  @EventHandler void on(ExternalAuthInfoUpdated event, @SequenceNumber long aggregateVersion,
      @Timestamp Instant occurrenceInstant, @MetaDataValue(USER_INFO) UserInfo userInfo) {

    try (MdcAutoClosable mdc = new MdcAutoClosable()) {
      mdc(event.getUserId(), aggregateVersion, mdc);
      handleDebug(event);

      UserProfileContainer userProfileContainer = userProfileContainerRepository.findById(event.getUserId()).get();
      userProfileContainer.getUserProfile().setExternalApiKeyEncrypted(event.getExternalApiKeyEncrypted())
          .setExternalUserId(event.getExternalUserId());

      save(userProfileContainer, userInfo, occurrenceInstant, aggregateVersion);
    }
  }

  @EventHandler void on(UserPreferencesUpdated event, @SequenceNumber long aggregateVersion,
      @Timestamp Instant occurrenceInstant, @MetaDataValue(USER_INFO) UserInfo userInfo) {
    try (MdcAutoClosable mdc = new MdcAutoClosable()) {
      mdc(event.getUserId(), aggregateVersion, mdc);
      handleDebug(event);

      UserProfileContainer userProfileContainer = userProfileContainerRepository.findById(event.getUserId()).get();
      userProfileContainer.getUserProfile()
          .setDimUnits(event.getDimUnits())
          .setWeightUnits(event.getWeightUnits());

      save(userProfileContainer, userInfo, occurrenceInstant, aggregateVersion);
    }
  }

  @QueryHandler UserProfileImpl handle(UserProfileByIdQuery query, @MetaDataValue(USER_INFO) UserInfo userInfo) {
    assertUserCanQuery(query, userInfo);
    Optional<UserProfileContainer> record = userProfileContainerRepository.findById(query.getId());
    return record.isPresent() ? record.get().getUserProfile() : null;
  }


  private void save(UserProfileContainer userProfileContainer, UserInfo userInfo, Instant occurrenceInstant,
      long aggregateVersion) {

    userProfileContainer.getUserProfile().setLastModifiedBy(userInfo.getName())
        .setLastModifiedById(userInfo.getUserId()).setLastModifiedInstant(occurrenceInstant)
        .setAggregateVersion(aggregateVersion);

    userProfileContainerRepository.save(userProfileContainer);

    LOGGER.debug("emitting update: {}", userProfileContainer.getUserProfile());
    queryUpdateEmitter.emit(UserProfileByIdQuery.class, query -> query.getId().equals(userProfileContainer.getId()),
        userProfileContainer.getUserProfile());
  }


  /**
   * @param query
   * @param userInfo The {@link UserInfo} instance from the query MetaData (see: {@link MetaDataValue}).
   */
  private void assertUserCanQuery(Object query, UserInfo userInfo) {
    Assert.notNull(userInfo, "null userInfo");
    Assert.notNull(query, "null query");
  }

  private void mdc(String userId, long aggregateVersion, MdcAutoClosable mdc) {
    mdc.put("UserId", userId);
    mdc.put("AggregateVersion", aggregateVersion);
  }

  private void handleDebug(Object event) {
    LOGGER.debug("Projecting state change from: {}", event);
  }
}

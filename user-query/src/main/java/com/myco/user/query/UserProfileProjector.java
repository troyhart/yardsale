package com.myco.user.query;

import com.myco.api.values.UserInfo;
import com.myco.axon.eventhandling.BlacklistedEventSequenceAware;
import com.myco.axon.eventhandling.BlacklistedEventSequenceRepository;
import com.myco.user.api.events.ExternalAuthInfoUpdated;
import com.myco.user.api.events.UserCreated;
import com.myco.user.api.events.UserEvent;
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

@Component
class UserProfileProjector implements BlacklistedEventSequenceAware {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileProjector.class);

  private UserProfileRepository userProfileRepository;
  private QueryUpdateEmitter queryUpdateEmitter;
  private BlacklistedEventSequenceRepository blacklistedEventSequenceRepository;

  @Autowired
  UserProfileProjector(
      UserProfileRepository userProfileRepository, QueryUpdateEmitter queryUpdateEmitter,
      BlacklistedEventSequenceRepository blacklistedEventSequenceRepository
  ) {
    this.userProfileRepository = userProfileRepository;
    this.queryUpdateEmitter = queryUpdateEmitter;
    this.blacklistedEventSequenceRepository = blacklistedEventSequenceRepository;
  }

  @EventHandler
  void on(
      UserCreated event, @SequenceNumber long aggregateVersion, @Timestamp Instant occurrenceInstant,
      @MetaDataValue(USER_INFO) UserInfo userInfo
  ) {
    // Opt-in to blacklisting...
    throwIfBlacklisted(event.getUserId());

    try (MdcAutoClosable mdc = new MdcAutoClosable()) {
      mdc(event, userInfo, aggregateVersion, mdc);
      UserProfileImpl userProfile = new UserProfileImpl();
      userProfile.setUserId(event.getUserId());
      save(userProfile, userInfo, occurrenceInstant, aggregateVersion);
    }
  }

  @EventHandler
  void on(
      ExternalAuthInfoUpdated event, @SequenceNumber long aggregateVersion, @Timestamp Instant occurrenceInstant,
      @MetaDataValue(USER_INFO) UserInfo userInfo
  ) {
    // Opt-in to blacklisting...
    throwIfBlacklisted(event.getUserId());

    if (event.getExternalUserId().endsWith("z")) {
      // TODO: fix me....every time the external auth information is updated to value that ends with the character 'z' a failure will be triggered below...
      // This is merely a predictable way to produce a failure!!!!!!!!!!!!!!!
      throw new RuntimeException(("This is a test exception............"));
    }
    try (MdcAutoClosable mdc = new MdcAutoClosable()) {
      mdc(event, userInfo, aggregateVersion, mdc);
      UserProfileImpl userProfile = userProfileRepository.findById(event.getUserId()).get();
      userProfile.setExternalApiKeyEncrypted(event.getExternalApiKeyEncrypted())
          .setExternalUserId(event.getExternalUserId());
      save(userProfile, userInfo, occurrenceInstant, aggregateVersion);
    }
  }

  @EventHandler
  void on(
      UserPreferencesUpdated event, @SequenceNumber long aggregateVersion, @Timestamp Instant occurrenceInstant,
      @MetaDataValue(USER_INFO) UserInfo userInfo
  ) {
    // Opt-in to blacklisting...
    throwIfBlacklisted(event.getUserId());

    try (MdcAutoClosable mdc = new MdcAutoClosable()) {
      mdc(event, userInfo, aggregateVersion, mdc);
      UserProfileImpl userProfile = userProfileRepository.findById(event.getUserId()).get();
      userProfile.setDimUnits(event.getDimUnits()).setWeightUnits(event.getWeightUnits());
      save(userProfile, userInfo, occurrenceInstant, aggregateVersion);
    }
  }

  @QueryHandler
  UserProfileImpl handle(UserProfileByIdQuery query, @MetaDataValue(USER_INFO) UserInfo userInfo) {
    assertUserCanQuery(query, userInfo);
    Optional<UserProfileImpl> record = userProfileRepository.findById(query.getId());
    return record.get();
  }


  private void save(
      UserProfileImpl userProfile, UserInfo userInfo, Instant occurrenceInstant, long aggregateVersion
  ) {
    userProfile.setLastModifiedBy(userInfo.getName()).setLastModifiedById(userInfo.getUserId())
        .setLastModifiedInstant(occurrenceInstant).setAggregateVersion(aggregateVersion);
    userProfileRepository.save(userProfile);
    LOGGER.debug("emitting update: {}", userProfile);
    queryUpdateEmitter
        .emit(UserProfileByIdQuery.class, query -> query.getId().equals(userProfile.getUserId()), userProfile);
  }


  /**
   * @param query
   * @param userInfo The {@link UserInfo} instance from the query MetaData (see: {@link MetaDataValue}).
   */
  private void assertUserCanQuery(Object query, UserInfo userInfo) {
    Assert.notNull(userInfo, "null userInfo");
    Assert.notNull(query, "null query");
  }

  private void mdc(
      UserEvent event, UserInfo userInfo, long aggregateVersion, MdcAutoClosable mdc
  ) {
    mdc.put("userAggregateId", event.getUserId());
    mdc.put("userAggregateVersion", aggregateVersion);
    mdc.put("eventType", event.getClass().getName());
    mdc.put("authorizedUserSubjectId", userInfo.getUserId());
    mdc.put("authorizedUserName", userInfo.getUserName());
    LOGGER.debug("Materializing view from: {}", event);
  }

  @Override
  public BlacklistedEventSequenceRepository getBlacklistedEventSequenceRepository() {
    return blacklistedEventSequenceRepository;
  }
}

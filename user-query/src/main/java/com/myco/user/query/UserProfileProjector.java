package com.myco.user.query;

import com.myco.api.values.UserInfo;
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

@Component class UserProfileProjector {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileProjector.class);

  private MaterializedUserProfileRepository materializedUserProfileRepository;
  private QueryUpdateEmitter queryUpdateEmitter;

  @Autowired UserProfileProjector(MaterializedUserProfileRepository materializedUserProfileRepository,
      QueryUpdateEmitter queryUpdateEmitter) {
    this.materializedUserProfileRepository = materializedUserProfileRepository;
    this.queryUpdateEmitter = queryUpdateEmitter;
  }

  @EventHandler void on(UserCreated event, @SequenceNumber long aggregateVersion, @Timestamp Instant occurrenceInstant,
      @MetaDataValue(USER_INFO) UserInfo userInfo) {

    try (MdcAutoClosable mdc = new MdcAutoClosable()) {
      materializedViewHandlingMdc(event, userInfo, aggregateVersion, mdc);

      UserProfileImpl userProfile = new UserProfileImpl();
      userProfile.setUserId(event.getUserId());

      save(new MaterializedUserProfile(userProfile), userInfo, occurrenceInstant, aggregateVersion);
    }
  }

  @EventHandler void on(ExternalAuthInfoUpdated event, @SequenceNumber long aggregateVersion,
      @Timestamp Instant occurrenceInstant, @MetaDataValue(USER_INFO) UserInfo userInfo) {

    try (MdcAutoClosable mdc = new MdcAutoClosable()) {
      materializedViewHandlingMdc(event, userInfo, aggregateVersion, mdc);

      MaterializedUserProfile materializedUserProfile = materializedUserProfileRepository.findById(event.getUserId()).get();
      materializedUserProfile.getUserProfile().setExternalApiKeyEncrypted(event.getExternalApiKeyEncrypted())
          .setExternalUserId(event.getExternalUserId());

      save(materializedUserProfile, userInfo, occurrenceInstant, aggregateVersion);
    }
  }

  @EventHandler void on(UserPreferencesUpdated event, @SequenceNumber long aggregateVersion,
      @Timestamp Instant occurrenceInstant, @MetaDataValue(USER_INFO) UserInfo userInfo) {
    try (MdcAutoClosable mdc = new MdcAutoClosable()) {
      materializedViewHandlingMdc(event, userInfo, aggregateVersion, mdc);

      MaterializedUserProfile materializedUserProfile = materializedUserProfileRepository.findById(event.getUserId()).get();
      materializedUserProfile.getUserProfile()
          .setDimUnits(event.getDimUnits())
          .setWeightUnits(event.getWeightUnits());

      save(materializedUserProfile, userInfo, occurrenceInstant, aggregateVersion);
    }
  }

  @QueryHandler UserProfileImpl handle(UserProfileByIdQuery query, @MetaDataValue(USER_INFO) UserInfo userInfo) {
    assertUserCanQuery(query, userInfo);
    Optional<MaterializedUserProfile> record = materializedUserProfileRepository.findById(query.getId());
    return record.get().getUserProfile();
  }


  private void save(MaterializedUserProfile materializedUserProfile, UserInfo userInfo, Instant occurrenceInstant,
      long aggregateVersion) {

    materializedUserProfile.getUserProfile().setLastModifiedBy(userInfo.getName())
        .setLastModifiedById(userInfo.getUserId()).setLastModifiedInstant(occurrenceInstant)
        .setAggregateVersion(aggregateVersion);

    materializedUserProfileRepository.save(materializedUserProfile);

    LOGGER.debug("emitting update: {}", materializedUserProfile.getUserProfile());
    queryUpdateEmitter.emit(UserProfileByIdQuery.class, query -> query.getId().equals(materializedUserProfile.getId()),
        materializedUserProfile.getUserProfile());
  }


  /**
   * @param query
   * @param userInfo The {@link UserInfo} instance from the query MetaData (see: {@link MetaDataValue}).
   */
  private void assertUserCanQuery(Object query, UserInfo userInfo) {
    Assert.notNull(userInfo, "null userInfo");
    Assert.notNull(query, "null query");
  }

  private void materializedViewHandlingMdc(UserEvent event, UserInfo userInfo, long aggregateVersion, MdcAutoClosable mdc) {
    mdc.put("userAggregateId", event.getUserId());
    mdc.put("userAggregateVersion", aggregateVersion);
    mdc.put("eventType", event.getClass().getName());
    mdc.put("authorizedUserSubjectId", userInfo.getUserId());
    mdc.put("authorizedUserName", userInfo.getUserName());
    materializedViewHandlingDebug(event);
  }

  private void materializedViewHandlingDebug(Object event) {
    LOGGER.debug("Materializing view from: {}", event);
  }
}

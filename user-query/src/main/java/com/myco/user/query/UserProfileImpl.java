package com.myco.user.query;

import com.myco.api.UnitsDim;
import com.myco.api.UnitsWeight;
import com.myco.user.api.UserProfile;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Objects;

public class UserProfileImpl implements UserProfile {

  private String userId;

  private String externalUserId;
  private String externalApiKeyEncrypted;

  private UnitsDim dimUnits;
  private UnitsWeight weightUnits;

  private String lastModifiedBy;
  private String lastModifiedById;

  private Instant setLastModifiedInstant;

  private long setAggregateVersion;

  public String getUserId() {
    return userId;
  }

  UserProfileImpl setUserId(String userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public String getExternalUserId() {
    return externalUserId;
  }

  UserProfileImpl setExternalUserId(String externalUserId) {
    this.externalUserId = externalUserId;
    return this;
  }

  @Override
  public String getExternalApiKeyEncrypted() {
    return externalApiKeyEncrypted;
  }

  UserProfileImpl setExternalApiKeyEncrypted(String encryptedKey) {
    this.externalApiKeyEncrypted = encryptedKey;
    return this;
  }

  @Override public UnitsDim getDimUnits() {
    return dimUnits;
  }

  UserProfileImpl setDimUnits(UnitsDim dimUnits) {
    this.dimUnits = dimUnits;
    return this;
  }

  @Override public UnitsWeight getWeightUnits() {
    return weightUnits;
  }

  UserProfileImpl setWeightUnits(UnitsWeight weightUnits) {
    this.weightUnits = weightUnits;
    return this;
  }

  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  UserProfileImpl setLastModifiedBy(String personName) {
    this.lastModifiedBy = personName;
    return this;
  }

  public String getLastModifiedById() {
    return lastModifiedById;
  }

  UserProfileImpl setLastModifiedById(String userId) {
    this.lastModifiedById = userId;
    return this;
  }

  public Instant getSetLastModifiedInstant() {
    return setLastModifiedInstant;
  }

  UserProfileImpl setLastModifiedInstant(Instant instant) {
    this.setLastModifiedInstant = instant;
    return this;
  }

  public long getSetAggregateVersion() {
    return setAggregateVersion;
  }

  UserProfileImpl setAggregateVersion(long version) {
    this.setAggregateVersion = version;
    return this;
  }

  @Override public boolean equals(Object object) {
    if (this == object) return true;
    if (!(object instanceof UserProfileImpl)) return false;
    if (!super.equals(object)) return false;
    UserProfileImpl that = (UserProfileImpl) object;
    return Objects.equals(userId, that.userId);
  }

  @Override public int hashCode() {
    return Objects.hash(super.hashCode(), userId);
  }

  @Override public String toString() {
    return "UserProfileImpl{" + "userId='" + userId + '\'' + ", externalUserId=" + externalUserId + ", externalApiKeyEncrypted='"
        + externalApiKeyEncrypted + '\'' + ", lastModifiedBy='" + lastModifiedBy + '\'' + ", lastModifiedById='"
        + lastModifiedById + '\'' + ", setLastModifiedInstant=" + setLastModifiedInstant + ", setAggregateVersion="
        + setAggregateVersion + '}';
  }
}

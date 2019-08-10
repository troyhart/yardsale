package com.myco.user.query;

import com.myco.api.UnitsDim;
import com.myco.api.UnitsWeight;
import com.myco.user.api.UserProfile;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

@Entity(name = "yardsale_user_profiles")
public class UserProfileImpl implements UserProfile {

  @Id
  private String userId;

  @Column
  private String externalUserId;
  @Column
  private String externalApiKeyEncrypted;
  @Column
  // TODO: add converter to store enum name rather than the ordinal!!!
  private UnitsDim dimUnits;
  @Column
  // TODO: add converter to store enum name rather than the ordinal!!!
  private UnitsWeight weightUnits;
  @Column
  private String lastModifiedBy;
  @Column
  private String lastModifiedById;
  @Column
  private Instant lastModifiedInstant;
  @Column
  private long aggregateVersion;

  @Convert(converter = StatusSetConverter.class)
  @Column(columnDefinition = "text")
  private StatusSet status;

  public UserProfileImpl() {
  }

  public UserProfileImpl(String userId) {
    this.userId = userId;
    status = new StatusSet();
  }

  public String getUserId() {
    return userId;
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

  @Override
  public UnitsDim getDimUnits() {
    return dimUnits;
  }

  UserProfileImpl setDimUnits(UnitsDim dimUnits) {
    this.dimUnits = dimUnits;
    return this;
  }

  @Override
  public UnitsWeight getWeightUnits() {
    return weightUnits;
  }

  UserProfileImpl setWeightUnits(UnitsWeight weightUnits) {
    this.weightUnits = weightUnits;
    return this;
  }

  @Override
  public Set<Status> getStatus() {
    return status.getSet();
  }

  UserProfileImpl addStatus(Status status) {
    this.status.getSet().add(status);
    return this;
  }

  UserProfileImpl removeStatus(Status status) {
    this.status.getSet().remove(status);
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

  public Instant getLastModifiedInstant() {
    return lastModifiedInstant;
  }

  UserProfileImpl setLastModifiedInstant(Instant instant) {
    this.lastModifiedInstant = instant;
    return this;
  }

  public long getAggregateVersion() {
    return aggregateVersion;
  }

  UserProfileImpl setAggregateVersion(long version) {
    this.aggregateVersion = version;
    return this;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (!(object instanceof UserProfileImpl)) return false;
    if (!super.equals(object)) return false;
    UserProfileImpl that = (UserProfileImpl) object;
    return Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), userId);
  }

  @Override
  public String toString() {
    return "UserProfileImpl{" + "userId='" + userId + '\'' + ", externalUserId=" + externalUserId
        + ", externalApiKeyEncrypted='" + externalApiKeyEncrypted + '\'' + ", lastModifiedBy='" + lastModifiedBy + '\''
        + ", lastModifiedById='" + lastModifiedById + '\'' + ", setLastModifiedInstant=" + lastModifiedInstant
        + ", setAggregateVersion=" + aggregateVersion + '}';
  }
}

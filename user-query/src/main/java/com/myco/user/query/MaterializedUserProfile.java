package com.myco.user.query;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

@Entity public class MaterializedUserProfile {

  @Id private String id;

  @Column(columnDefinition = "text") @Convert(converter = UserProfileConverter.class) private UserProfileImpl userProfile;

  MaterializedUserProfile() {
  }

  MaterializedUserProfile(UserProfileImpl userProfile) {
    this.id = userProfile.getUserId();
    this.userProfile = userProfile;
  }

  public String getId() {
    return id;
  }

  void setId(String id) {
    this.id = id;
  }

  public UserProfileImpl getUserProfile() {
    return userProfile;
  }

  void setUserProfile(UserProfileImpl userProfile) {
    this.userProfile = userProfile;
  }

  @Override public String toString() {
    return "MaterializedUserProfile{" + "id='" + id + '\'' + ", userProfile=" + userProfile + '}';
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MaterializedUserProfile)) return false;
    MaterializedUserProfile that = (MaterializedUserProfile) o;
    return Objects.equals(id, that.id) && Objects.equals(userProfile, that.userProfile);
  }

  @Override public int hashCode() {
    return Objects.hash(id, userProfile);
  }
}


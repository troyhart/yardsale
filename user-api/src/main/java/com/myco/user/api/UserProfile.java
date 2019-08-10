package com.myco.user.api;

import com.myco.api.UnitsDim;
import com.myco.api.UnitsWeight;
import org.springframework.util.CollectionUtils;

import java.util.Set;

public interface UserProfile {

  String getUserId();

  String getExternalUserId();

  String getExternalApiKeyEncrypted();

  UnitsDim getDimUnits();

  UnitsWeight getWeightUnits();

  Set<Status> getStatus();

  default boolean isInError() {
    return !CollectionUtils.isEmpty(getStatus()) && getStatus().stream().anyMatch(Status::isError);
  }

  public static enum Status {
    PROJECTION_FAILURE(true), PROFILE_UNDERSPECIFIED(true), PROFILE_APPROVED;

    private boolean error;

    Status(boolean error) {
      this.error = error;
    }

    Status() {
      error = false;
    }

    public boolean isError() {
      return error;
    }
  }
}

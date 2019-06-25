package com.myco.user.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.myco.api.UnitsDim;
import com.myco.api.UnitsWeight;

@JsonSerialize(as = UserProfile.class)
public interface UserProfile {

  String getUserId();

  String getExternalUserId();

  String getExternalApiKeyEncrypted();

  UnitsDim getDimUnits();

  UnitsWeight getWeightUnits();
}

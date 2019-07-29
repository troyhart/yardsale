package com.myco.user.api;

import com.myco.api.UnitsDim;
import com.myco.api.UnitsWeight;

public interface UserProfile {

  String getUserId();

  String getExternalUserId();

  String getExternalApiKeyEncrypted();

  UnitsDim getDimUnits();

  UnitsWeight getWeightUnits();
}

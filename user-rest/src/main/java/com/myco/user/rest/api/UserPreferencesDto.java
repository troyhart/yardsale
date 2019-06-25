package com.myco.user.rest.api;

import com.myco.api.UnitsDim;
import com.myco.api.UnitsWeight;
import com.myco.user.api.commands.UpdateUserPreferences;
import org.springframework.util.Assert;

public class UserPreferencesDto {

  private UnitsDim dimUnits;
  private UnitsWeight weightUnits;

  public UnitsDim getDimUnits() {
    return dimUnits;
  }

  public void setDimUnits(UnitsDim dimUnits) {
    this.dimUnits = dimUnits;
  }

  public UnitsWeight getWeightUnits() {
    return weightUnits;
  }

  public void setWeightUnits(UnitsWeight weightUnits) {
    this.weightUnits = weightUnits;
  }

  public UpdateUserPreferences toUpdateUserPreferencesCommand(String yardsaleUserId) {
    Assert.hasText(yardsaleUserId, "null/blank yardsale user identifier");
    return new UpdateUserPreferences(yardsaleUserId, dimUnits, weightUnits);
  }
}

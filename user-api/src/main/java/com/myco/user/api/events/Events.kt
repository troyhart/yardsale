package com.myco.user.api.events

import com.myco.api.UnitsDim
import com.myco.api.UnitsWeight

interface UserEvent {
  val userId: String
}

data class UserCreated(
  override val userId: String
) : UserEvent


data class ExternalAuthInfoUpdated(
    override val userId: String,
    val externalApiKeyEncrypted: String,
    val externalUserId: String
) : UserEvent

data class UserPreferencesUpdated(
    override val userId: String,
    val dimUnits: UnitsDim,
    val weightUnits: UnitsWeight
) : UserEvent

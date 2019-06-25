package com.myco.user.api.commands

import com.myco.api.UnitsDim
import com.myco.api.UnitsWeight
import com.myco.util.v8n.V8NAssert
import com.myco.utils.values.ErrorMessage
import com.myco.utils.values.ObfuscatedToStringProperty
import com.myco.utils.values.Validatable
import org.axonframework.modelling.command.TargetAggregateIdentifier


interface UserCommand : Validatable {
  val userId: String
}


data class CreateUser(

    @TargetAggregateIdentifier
    override val userId: String,
    val externalUserId: String,
    val externalApiKey: ObfuscatedToStringProperty<String>

) : UserCommand {

  override fun validate(): ErrorMessage? {
    return ErrorMessage.Builder().code("CreateUser")
        .detail(V8NAssert.requiredInput(userId, "userId"))
        .detail(
            V8NAssert.requiredInput(externalUserId, "externalUserId")
        )
        .detail(V8NAssert.requiredInput(externalApiKey.value, "externalApiKey"))
        .buildIfDetailsPresent()
  }
}


data class UpdateExternalAuthInfo(

    @TargetAggregateIdentifier
    override val userId: String,
    val externalUserId: String?,
    val externalApiKey: ObfuscatedToStringProperty<String>

) : UserCommand {

  override fun validate(): ErrorMessage? {
    val emb: ErrorMessage.Builder = ErrorMessage.Builder().code("UpdateExternalAuthInfo")
        .detail(V8NAssert.requiredInput(userId, "userId"))
        .detail(V8NAssert.optionalInput(externalUserId, "externalUserId"))
        .detail(V8NAssert.requiredInput(externalApiKey.value, "externalApiKey"))

    return emb.buildIfDetailsPresent()
  }
}


data class UpdateUserPreferences(
    @TargetAggregateIdentifier
    override val userId: String,
    val dimUnits: UnitsDim,
    val weightUnits: UnitsWeight
) : UserCommand {

  override fun validate(): ErrorMessage? {
    return ErrorMessage.Builder().code("UpdateUserPreferences")
        .detail(V8NAssert.requiredInput(userId, "userId"))
        .detail(V8NAssert.notNull(dimUnits, "dimUnits"))
        .detail(V8NAssert.notNull(weightUnits, "weightUnits"))
        .buildIfDetailsPresent()
  }
}


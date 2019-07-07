package com.myco.api.values

import com.myco.api.UnitsDim
import com.myco.api.UnitsWeight
import com.myco.util.v8n.V8NAssert
import com.myco.util.values.ErrorMessage
import com.myco.util.values.Validatable


data class UserInfo(
    val userId: String,
    val userName: String,
    val email: String?,
    val name: String,
    val authorities: Collection<String>
) : Validatable {
  private constructor(builder: Builder) : this(
      builder.userId,
      builder.userName,
      builder.email,
      builder.name,
      builder.authorities
  )

  class Builder {
    lateinit var userId: String
      private set
    lateinit var userName: String
      private set
    var email: String = ""
      private set
    lateinit var name: String
      private set
    lateinit var authorities: Collection<String>
      private set

    fun userId(userId: String) = apply { this.userId = userId }
    fun userName(userName: String) = apply { this.userName = userName }
    fun email(email: String) = apply { this.email = email }
    fun name(name: String) = apply { this.name = name }
    fun authorities(authorities: Collection<String>) = apply { this.authorities = authorities }
    fun build() = UserInfo(this)
  }

  override fun validate(): ErrorMessage? {
    val emb: ErrorMessage.Builder = ErrorMessage.Builder()
        .code("Dimensions")
        .detail(V8NAssert.requiredInput(userId, "userId"))
        .detail(V8NAssert.requiredInput(userName, "userName"))
        .detail(V8NAssert.optionalInput(email, "email"))
        .detail(V8NAssert.requiredInput(name, "name"))
        .detail(
            V8NAssert.requiredInput(authorities, "authorities")
        )
    return emb.buildIfDetailsPresent()
  }
}


data class Address(
    val street: String,
    val city: String,
    val state: String,
    val zip: String,
    val country: String?
) : Validatable {

  init {
    raiseV8NExceptionIfNotValid()
  }

  private constructor(builder: Builder) : this(
      builder.street,
      builder.city,
      builder.state,
      builder.zip,
      builder.country
  )

  class Builder {
    lateinit var street: String
      private set
    lateinit var city: String
      private set
    lateinit var state: String
      private set
    lateinit var zip: String
      private set
    lateinit var country: String
      private set

    fun street(street: String) = apply { this.street = street }
    fun city(city: String) = apply { this.city = city }
    fun state(state: String) = apply { this.state = state }
    fun zip(zip: String) = apply { this.zip = zip }
    fun country(country: String) = apply { this.country = country }
    fun build() = Address(this)
  }

  override fun validate(): ErrorMessage? {
    val emb: ErrorMessage.Builder = ErrorMessage.Builder()
        .code("Address")
        .detail(V8NAssert.requiredInput(street, "street"))
        .detail(V8NAssert.requiredInput(city, "city"))
        .detail(V8NAssert.requiredInput(state, "state"))
        .detail(V8NAssert.requiredInput(zip, "zip"))
        .detail(V8NAssert.requiredInput(country, "country"))
    return emb.buildIfDetailsPresent()
  }
}

data class Weight(
    val value: Double,
    val units: UnitsWeight
) : Validatable {

  init {
    raiseV8NExceptionIfNotValid()
  }

  override fun validate(): ErrorMessage? {
    val emb: ErrorMessage.Builder = ErrorMessage.Builder()
        .code("Weight")
        .detail(
            V8NAssert.notNull(units, "units")
        )
        .detail(
            V8NAssert.isTrue(
                value > 0,
                "value",
                "must be greater than zero."
            )
        )
    return emb.buildIfDetailsPresent()
  }
}

data class Dimension(
    val value: Double,
    val units: UnitsDim
) : Validatable {

  init {
    raiseV8NExceptionIfNotValid()
  }

  override fun validate(): ErrorMessage? {
    val emb: ErrorMessage.Builder = ErrorMessage.Builder()
        .code("Dimension")
        .detail(
            V8NAssert.notNull(units, "units")
        )
        .detail(
            V8NAssert.isTrue(
                value >= 0,
                "value",
                "must be greater than or equal to zero."
            )
        )
    return emb.buildIfDetailsPresent()
  }
}

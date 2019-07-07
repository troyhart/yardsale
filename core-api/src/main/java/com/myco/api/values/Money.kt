package com.myco.api.values

import com.myco.util.v8n.V8NAssert
import com.myco.util.v8n.V8NException
import com.myco.util.values.ErrorMessage
import com.myco.util.values.Validatable
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*

data class Money(
    val amount: BigDecimal,
    val currency: Currency
) : Validatable {

  init {
    raiseV8NExceptionIfNotValid()
    amount.setScale(scale())
  }

  private constructor(builder: Builder) : this(
      builder.amount,
      builder.currency
  )

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Money) return false
    return amountNormalizedForComparison() == other.amountNormalizedForComparison()
  }

  override fun hashCode(): Int {
    return Objects.hashCode(amountNormalizedForComparison())
  }

  override fun toString(): String {
    val currencyFormat = NumberFormat.getCurrencyInstance()
    currencyFormat.minimumFractionDigits = currency.defaultFractionDigits
    currencyFormat.maximumFractionDigits = currency.defaultFractionDigits
    return String.format("%s (%s)", currencyFormat.format(amountNormalizedForComparison().toDouble()), currency.currencyCode)
  }

  class Builder {
    lateinit var amount: BigDecimal
      private set
    var currency: Currency = Currency.getInstance("USD")
      private set

    fun amount(amount: String) = apply {
      // TODO: fix me!!! Some locales use period for thousands separater and comma for decimal place...
      // the validation below will obviously fail miserably if such numbers are encountered since it removes commas.
      val parsedValue = amount.trim().removePrefix("$").replace(",", "").replace(" ", "")
      this.amount = BigDecimal(parsedValue)
    }
    fun amount(amount: Int) = apply { this.amount = BigDecimal(amount) }
    fun amount(amount: BigDecimal) = apply { this.amount = amount }
    fun currency(currency: Currency) = apply { this.currency = currency }
    fun build() = Money(this)
  }

  override fun validate(): ErrorMessage? {
    return ErrorMessage.Builder()
        .code("Money")
        .detail(V8NAssert.notNull(amount, "amount"))
        .detail(V8NAssert.notNull(currency, "currency"))
        .buildIfDetailsPresent()
  }

  fun add(augend: Money): Money {
    throwIfCurrencyMismatch(augend.currency)
    return instance(amount.add(augend.amount), currency)
  }

  fun subtract(subtrahend: Money): Money {
    throwIfCurrencyMismatch(subtrahend.currency)
    return instance(amount.subtract(subtrahend.amount), currency)
  }

  fun multiply(multiplicand: Int): Money {
    return instance(amount.multiply(BigDecimal(multiplicand), mathContext()), currency)
  }

  fun multiply(multiplicand: BigDecimal): Money {
    return instance(amount.multiply(multiplicand, mathContext()), currency)
  }

  fun divide(divisor: Int): Money {
    return instance(amount.divide(BigDecimal(divisor), mathContext()), currency)
  }

  fun divide(divisor: BigDecimal): Money {
    return instance(amount.divide(divisor, mathContext()), currency)
  }

  private fun instance(amount: BigDecimal, currency: Currency): Money {
    return Money.Builder().amount(amount).currency(currency).build()
  }

  private fun throwIfCurrencyMismatch(otherCurrency: Currency) {
    if (currency != otherCurrency) {
      V8NException.ifError(ErrorMessage.Builder().code("currency").message("currency mismatch").build())
    }
  }

  private fun amountNormalizedForComparison(): BigDecimal {
    return amount.setScale(currency.defaultFractionDigits, roundingMode())
  }

  private fun mathContext(): MathContext? {
    return MathContext(scale(), roundingMode())
  }

  private fun roundingMode(): RoundingMode {
    return RoundingMode.HALF_UP
  }

  private fun scale(): Int {
    return defaultScale(currency)
  }
}

private fun defaultScale(currency: Currency): Int {
  return currency.defaultFractionDigits + 2
}

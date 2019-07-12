package com.myco.api.values

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

  class Builder {
    lateinit var amount: BigDecimal
      private set
    var currency: Currency = Currency.getInstance("USD")
      private set

    fun scaledAmount(): BigDecimal {
      return amount.setScale(scaleFor(currency))
    }

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

  companion object {
    fun scaleFor(currency: Currency): Int {
      return currency.defaultFractionDigits + 2
    }
  }

  init {
    raiseV8NExceptionIfNotValid()
  }

  private constructor(builder: Builder) : this(
      builder.scaledAmount(),
      builder.currency
  )

  override fun validate(): ErrorMessage? {
    val emb = ErrorMessage.Builder().code("Money").message("errors detected")
    if (amount.scale() != scaleFor(currency)) {
      emb.detail(ErrorMessage.Builder().code("amount").message("The given Money amount has an invalid scale").build())
    }
    return emb.buildIfDetailsPresent()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Money) return false
    return Objects.equals(this.roundedAmount(), other.roundedAmount())
        && Objects.equals(this.currency, other.currency)
  }

  override fun hashCode(): Int {
    return Objects.hash(this.roundedAmount(), this.currency)
  }

  override fun toString(): String {
    return String.format("%s[%s]", this.roundedAmount(), currency.currencyCode)
  }

  fun toString(currencyFormat: NumberFormat): String {
    return String.format("%s[%s]", currencyFormat.format(this.roundedAmount().toDouble()), currency.currencyCode)
  }

  fun add(augend: Money): Money {
    throwIfCurrencyMismatch(augend.currency)
    return Money(amount.add(augend.amount), currency)
  }

  fun subtract(subtrahend: Money): Money {
    throwIfCurrencyMismatch(subtrahend.currency)
    return Money(amount.subtract(subtrahend.amount), currency)
  }

  fun multiply(multiplicand: Int): Money {
    return Money(amount.multiply(BigDecimal(multiplicand), mathContext()), currency)
  }

  fun multiply(multiplicand: BigDecimal): Money {
    return Money(amount.multiply(multiplicand, mathContext()), currency)
  }

  fun divide(divisor: Int): Money {
    return Money(amount.divide(BigDecimal(divisor), mathContext()), currency)
  }

  fun divide(divisor: BigDecimal): Money {
    return Money(amount.divide(divisor, mathContext()), currency)
  }

  fun round(): Money {
    return Money(roundedAmount(), currency)
  }

  private fun roundedAmount(): BigDecimal {
    return amount.setScale(currency.defaultFractionDigits, roundingMode())
  }

  private fun mathContext(): MathContext {
    return MathContext.DECIMAL64
  }

  private fun roundingMode(): RoundingMode {
    return RoundingMode.HALF_UP
  }

  private fun throwIfCurrencyMismatch(otherCurrency: Currency) {
    if (currency != otherCurrency) {
      V8NException.ifError(ErrorMessage.Builder().code("currency").message("currency mismatch").build())
    }
  }
}

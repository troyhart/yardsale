package com.myco.util.values

import com.myco.util.v8n.V8NException

interface Validatable {
  fun validate(): ErrorMessage?

  fun raiseV8NExceptionIfNotValid() {
    V8NException.ifError(validate())
  }
}

data class ErrorMessage(
    val code: String?,
    val index: Any?,
    val message: String,
    val details: List<ErrorMessage>
) {

  private constructor(builder: Builder) : this(
      builder.code,
      builder.index,
      builder.message,
      builder.details
  )

  fun toBuilder(): ErrorMessage.Builder {
    val builder: ErrorMessage.Builder = ErrorMessage.Builder()
        .code(code).index(index).message(message)
    details.forEach { detail ->
      run {
        builder.detail(detail)
      }
    }
    return builder
  }

  class Builder {
    var code: String? = ""
      private set
    var index: Any? = ""
      private set
    var message: String = "input errors detected"
      private set
    val details: MutableList<ErrorMessage> = mutableListOf()

    fun code(code: String?) = apply { this.code = code }
    fun index(index: Any?) = apply { this.index = index }
    fun message(message: String) = apply { this.message = message }
    fun detail(detail: ErrorMessage?) = apply { if (detail != null) this.details.add(detail) }

    fun buildIfDetailsPresent() = if (this.details.isEmpty()) null else ErrorMessage(this)
    fun build() = ErrorMessage(this)
  }
}

class ObfuscatedToStringProperty<T>(val value: T) {
  override fun toString() = "███████████"
}

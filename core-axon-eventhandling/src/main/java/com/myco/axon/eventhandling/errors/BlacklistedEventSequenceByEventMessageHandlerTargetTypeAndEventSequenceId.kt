package com.myco.axon.eventhandling.errors

data class BlacklistedEventSequenceByEventMessageHandlerTargetTypeAndEventSequenceId(
    val eventMessageHandlerTargetType: Class<*>,
    val eventSequenceId: String
) {
  fun blacklistedEventSequenceId(): String {
    val processingGroup = eventMessageHandlerTargetType.getPackage().name
    return "$processingGroup::$eventSequenceId"
  }
}

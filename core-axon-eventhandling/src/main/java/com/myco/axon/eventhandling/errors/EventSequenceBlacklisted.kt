package com.myco.axon.eventhandling.errors

/**
 * Raised when failure attempts are exhausted for a given eventSequenceId (typically an aggregate identifier)
 * being handled by a given processingGroup.
 */
data class EventSequenceBlacklisted(
    val processingGroup: String,
    val eventSequenceId: String
) {
  /**
   * @param eventMessageHandlerTargetType the EventMessageHandlerTarget
   * @return true if the eventMessageHandlerTargetType identifies `this.processingGroup`.
   */
  fun forEventMessageHandlerTargetType(eventMessageHandlerTargetType: Class<*>): Boolean {
    return processingGroup == eventMessageHandlerTargetType.getPackage().name
  }
}

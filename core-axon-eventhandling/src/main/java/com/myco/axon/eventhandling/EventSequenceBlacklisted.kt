package com.myco.axon.eventhandling

data class EventSequenceBlacklisted(
    val processingGroup: String,
    val eventSequenceId: String
)

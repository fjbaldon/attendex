package com.github.fjbaldon.attendex.scanner.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class TimeSlotResponse(
    val startTime: String,
    val endTime: String,
    val type: String
)

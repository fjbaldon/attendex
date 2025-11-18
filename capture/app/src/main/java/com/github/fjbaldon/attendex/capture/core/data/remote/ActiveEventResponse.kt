package com.github.fjbaldon.attendex.capture.core.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ActiveEventResponse(
    val id: Long,
    val eventName: String,
    val startDate: String,
    val endDate: String,
    val timeSlots: List<TimeSlotResponse>
)

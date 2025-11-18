package com.github.fjbaldon.attendex.capture.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class TimeSlotResponse(
    val activityName: String,
    val targetTime: String,
    val type: String
)

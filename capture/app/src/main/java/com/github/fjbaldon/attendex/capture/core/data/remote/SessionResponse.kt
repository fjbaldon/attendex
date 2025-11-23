package com.github.fjbaldon.attendex.capture.core.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class SessionResponse(
    val id: Long,
    val activityName: String,
    val targetTime: String,
    val intent: String
)

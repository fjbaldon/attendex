package com.github.fjbaldon.attendex.capture.core.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ActiveEventResponse(
    val id: Long,
    val name: String,
    val startDate: String,
    val endDate: String,
    val sessions: List<SessionResponse>
)

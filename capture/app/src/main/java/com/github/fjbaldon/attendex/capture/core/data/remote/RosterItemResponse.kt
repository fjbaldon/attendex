package com.github.fjbaldon.attendex.capture.core.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class RosterItemResponse(
    val attendeeId: Long,
    val identity: String,
    val firstName: String,
    val lastName: String,
)

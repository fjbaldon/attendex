package com.github.fjbaldon.attendex.capture.core.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class EventAttendeeSyncResponse(
    val attendeeId: Long,
    val uniqueIdentifier: String,
    val qrCodeHash: String,
    val firstName: String,
    val lastName: String,
)

package com.github.fjbaldon.attendex.scanner.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ActiveEventResponse(
    val id: Long,
    val eventName: String
)

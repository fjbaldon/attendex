package com.github.fjbaldon.attendex.capture.core.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ActiveEventResponse(
    val id: Long,
    val name: String,
    val startDate: String,
    val endDate: String,
    val sessions: List<SessionResponse>,
    // The backend sends 'roster' here now, but your app fetches it separately.
    // You can ignore it here or refactor the app to use it.
    // For now, ignore it to prevent parsing errors if your JSON config is strict.
    val roster: List<RosterItemResponse> = emptyList()
)

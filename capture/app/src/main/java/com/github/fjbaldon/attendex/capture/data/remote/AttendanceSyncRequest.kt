package com.github.fjbaldon.attendex.capture.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceSyncRequest(
    val records: List<Record>
) {
    @Serializable
    data class Record(
        val eventId: Long,
        val attendeeId: Long,
        val checkInTimestamp: String
    )
}

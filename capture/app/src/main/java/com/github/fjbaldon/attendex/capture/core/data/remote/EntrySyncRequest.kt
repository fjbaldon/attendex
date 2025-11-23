package com.github.fjbaldon.attendex.capture.core.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class EntrySyncRequest(
    val records: List<EntryRecord>
) {
    @Serializable
    data class EntryRecord(
        // NEW: The Idempotency Key
        val scanUuid: String,

        val eventId: Long,
        val attendeeId: Long,
        val scanTimestamp: String

        // REMOVED: sessionId
    )
}

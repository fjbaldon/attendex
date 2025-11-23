package com.github.fjbaldon.attendex.capture.core.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class EntrySyncRequest(
    val records: List<EntryRecord>
) {
    @Serializable
    data class EntryRecord(
        val scanUuid: String,
        val eventId: Long,
        val attendeeId: Long,
        val scanTimestamp: String,

        val snapshotIdentity: String,
        val snapshotFirstName: String,
        val snapshotLastName: String
    )
}

package com.github.fjbaldon.attendex.capture.core.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.*

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // Idempotency Key: Unique ID generated on the device
    val scanUuid: String = UUID.randomUUID().toString(),

    val eventId: Long,
    val attendeeId: Long, // Kept for server link

    // Snapshot Data: Preserves display history even if Roster is wiped
    val snapshotIdentity: String,
    val snapshotFirstName: String,
    val snapshotLastName: String,

    // Time: The source of truth for "Late/Early" calc on backend
    val scanTimestamp: Instant,

    // State Machine
    var syncStatus: SyncStatus = SyncStatus.PENDING,
    var syncErrorMessage: String? = null
)

package com.github.fjbaldon.attendex.capture.core.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.*

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val scanUuid: String = UUID.randomUUID().toString(),
    val eventId: Long,
    val attendeeId: Long,
    val snapshotIdentity: String,
    val snapshotFirstName: String,
    val snapshotLastName: String,
    val scanTimestamp: Instant,
    var syncStatus: SyncStatus = SyncStatus.PENDING,
    var syncErrorMessage: String? = null,
    var retryCount: Int = 0
)

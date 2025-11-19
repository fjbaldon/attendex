package com.github.fjbaldon.attendex.capture.core.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventId: Long,
    val sessionId: Long,
    val attendeeId: Long,
    val scanTimestamp: Instant,
    var isSynced: Boolean = false
)

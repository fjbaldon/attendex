package com.github.fjbaldon.attendex.scanner.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "attendance_records")
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventId: Long,
    val attendeeId: Long,
    val checkInTimestamp: Instant,
    var isSynced: Boolean = false
)

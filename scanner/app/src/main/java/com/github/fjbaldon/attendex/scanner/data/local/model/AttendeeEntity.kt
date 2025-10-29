package com.github.fjbaldon.attendex.scanner.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendees",
    indices = [Index(value = ["eventId", "qrCodeHash"], unique = true)]
)
data class AttendeeEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val eventId: Long,
    val attendeeId: Long,
    val uniqueIdentifier: String,
    val qrCodeHash: String
)

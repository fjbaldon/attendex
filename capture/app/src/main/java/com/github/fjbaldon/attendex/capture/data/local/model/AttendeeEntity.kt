package com.github.fjbaldon.attendex.capture.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendees",
    indices = [
        Index(value = ["eventId", "uniqueIdentifier"], unique = true)
    ]
)
data class AttendeeEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val eventId: Long,
    val attendeeId: Long,
    val uniqueIdentifier: String,
    val qrCodeHash: String,
    val firstName: String,
    val lastName: String
)

package com.github.fjbaldon.attendex.capture.core.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendees",
    indices = [
        Index(value = ["eventId", "identity"], unique = true),
        Index(value = ["identity"]),
        Index(value = ["firstName"]),
        Index(value = ["lastName"])
    ]
)
data class AttendeeEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val eventId: Long,
    val attendeeId: Long,
    val identity: String,
    val firstName: String,
    val lastName: String
)

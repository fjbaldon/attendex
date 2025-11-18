package com.github.fjbaldon.attendex.capture.core.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.fjbaldon.attendex.capture.core.data.remote.TimeSlotResponse

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: Long,
    val eventName: String,
    val startDate: String,
    val endDate: String,
    val timeSlots: List<TimeSlotResponse>
)

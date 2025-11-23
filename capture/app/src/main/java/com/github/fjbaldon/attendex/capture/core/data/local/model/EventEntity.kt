package com.github.fjbaldon.attendex.capture.core.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.fjbaldon.attendex.capture.core.data.remote.SessionResponse

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val startDate: String,
    val endDate: String,
    val sessions: List<SessionResponse>
)

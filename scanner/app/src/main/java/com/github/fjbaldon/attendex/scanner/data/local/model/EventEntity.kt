package com.github.fjbaldon.attendex.scanner.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: Long,
    val eventName: String
)

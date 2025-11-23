package com.github.fjbaldon.attendex.capture.core.data.local

import androidx.room.TypeConverter
import com.github.fjbaldon.attendex.capture.core.data.remote.SessionResponse
import kotlinx.serialization.json.Json
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilli()
    }

    @TypeConverter
    fun fromSessionList(sessions: List<SessionResponse>): String {
        return Json.encodeToString(sessions)
    }

    @TypeConverter
    fun toSessionList(sessionsString: String): List<SessionResponse> {
        return Json.decodeFromString(sessionsString)
    }
}

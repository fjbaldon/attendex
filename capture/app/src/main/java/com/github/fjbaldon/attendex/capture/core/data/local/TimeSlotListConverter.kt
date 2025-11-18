package com.github.fjbaldon.attendex.capture.core.data.local

import androidx.room.TypeConverter
import com.github.fjbaldon.attendex.capture.core.data.remote.TimeSlotResponse
import kotlinx.serialization.json.Json

class TimeSlotListConverter {
    @TypeConverter
    fun fromTimeSlotList(timeSlots: List<TimeSlotResponse>): String {
        return Json.encodeToString(timeSlots)
    }

    @TypeConverter
    fun toTimeSlotList(timeSlotsString: String): List<TimeSlotResponse> {
        return Json.decodeFromString(timeSlotsString)
    }
}

package com.github.fjbaldon.attendex.capture.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.fjbaldon.attendex.capture.data.local.dao.AttendanceRecordDao
import com.github.fjbaldon.attendex.capture.data.local.dao.AttendeeDao
import com.github.fjbaldon.attendex.capture.data.local.dao.EventDao
import com.github.fjbaldon.attendex.capture.data.local.model.AttendanceRecordEntity
import com.github.fjbaldon.attendex.capture.data.local.model.AttendeeEntity
import com.github.fjbaldon.attendex.capture.data.local.model.EventEntity

@Database(
    entities = [
        EventEntity::class,
        AttendeeEntity::class,
        AttendanceRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class, TimeSlotListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun attendeeDao(): AttendeeDao
    abstract fun attendanceRecordDao(): AttendanceRecordDao
}

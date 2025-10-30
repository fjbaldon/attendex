package com.github.fjbaldon.attendex.scanner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.fjbaldon.attendex.scanner.data.local.dao.AttendanceRecordDao
import com.github.fjbaldon.attendex.scanner.data.local.dao.AttendeeDao
import com.github.fjbaldon.attendex.scanner.data.local.dao.EventDao
import com.github.fjbaldon.attendex.scanner.data.local.dao.UserCredentialsDao
import com.github.fjbaldon.attendex.scanner.data.local.model.AttendanceRecordEntity
import com.github.fjbaldon.attendex.scanner.data.local.model.AttendeeEntity
import com.github.fjbaldon.attendex.scanner.data.local.model.EventEntity
import com.github.fjbaldon.attendex.scanner.data.local.model.UserCredentialsEntity

@Database(
    entities = [
        EventEntity::class,
        AttendeeEntity::class,
        AttendanceRecordEntity::class,
        UserCredentialsEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class, TimeSlotListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun attendeeDao(): AttendeeDao
    abstract fun attendanceRecordDao(): AttendanceRecordDao
    abstract fun userCredentialsDao(): UserCredentialsDao
}

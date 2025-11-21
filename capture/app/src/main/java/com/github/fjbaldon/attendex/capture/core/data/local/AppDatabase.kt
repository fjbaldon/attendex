package com.github.fjbaldon.attendex.capture.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.fjbaldon.attendex.capture.core.data.local.dao.AttendeeDao
import com.github.fjbaldon.attendex.capture.core.data.local.dao.EntryDao
import com.github.fjbaldon.attendex.capture.core.data.local.dao.EventDao
import com.github.fjbaldon.attendex.capture.core.data.local.model.AttendeeEntity
import com.github.fjbaldon.attendex.capture.core.data.local.model.EntryEntity
import com.github.fjbaldon.attendex.capture.core.data.local.model.EventEntity

@Database(
    entities = [
        EventEntity::class,
        AttendeeEntity::class,
        EntryEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun attendeeDao(): AttendeeDao
    abstract fun entryDao(): EntryDao
}

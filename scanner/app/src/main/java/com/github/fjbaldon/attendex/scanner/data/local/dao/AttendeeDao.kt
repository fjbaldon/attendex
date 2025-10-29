package com.github.fjbaldon.attendex.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.fjbaldon.attendex.scanner.data.local.model.AttendeeEntity

@Dao
interface AttendeeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attendees: List<AttendeeEntity>)

    @Query("SELECT * FROM attendees WHERE eventId = :eventId AND qrCodeHash = :qrCodeHash LIMIT 1")
    suspend fun findAttendeeByQrCode(eventId: Long, qrCodeHash: String): AttendeeEntity?

    @Query("DELETE FROM attendees WHERE eventId = :eventId")
    suspend fun clearAttendeesForEvent(eventId: Long)
}

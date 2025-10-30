package com.github.fjbaldon.attendex.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.fjbaldon.attendex.scanner.data.local.model.AttendeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendeeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attendees: List<AttendeeEntity>)

    @Query("SELECT * FROM attendees WHERE eventId = :eventId AND uniqueIdentifier = :identifier LIMIT 1")
    suspend fun findAttendeeByIdentifier(eventId: Long, identifier: String): AttendeeEntity?

    @Query("DELETE FROM attendees WHERE eventId = :eventId")
    suspend fun clearAttendeesForEvent(eventId: Long)

    @Query("SELECT * FROM attendees WHERE attendeeId IN (:attendeeIds)")
    fun getAttendeesByIds(attendeeIds: List<Long>): Flow<List<AttendeeEntity>>
}

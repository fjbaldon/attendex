package com.github.fjbaldon.attendex.capture.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.github.fjbaldon.attendex.capture.core.data.local.model.AttendeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendeeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attendees: List<AttendeeEntity>)

    @Query("SELECT * FROM attendees WHERE eventId = :eventId AND identity = :identity LIMIT 1")
    suspend fun findAttendeeByIdentity(eventId: Long, identity: String): AttendeeEntity?

    @Transaction
    suspend fun replaceRosterForEvent(eventId: Long, newRoster: List<AttendeeEntity>) {
        clearAttendeesForEvent(eventId)
        insertAll(newRoster)
    }

    @Query("DELETE FROM attendees WHERE eventId = :eventId")
    suspend fun clearAttendeesForEvent(eventId: Long)

    @Query("DELETE FROM attendees")
    suspend fun clearAll()

    @Query("""
        SELECT * FROM attendees 
        WHERE eventId = :eventId 
        AND (
            identity LIKE '%' || :query || '%' 
            OR firstName LIKE '%' || :query || '%' 
            OR lastName LIKE '%' || :query || '%'
        ) 
        ORDER BY lastName, firstName 
        LIMIT 50
    """)
    fun searchAttendees(eventId: Long, query: String): Flow<List<AttendeeEntity>>
}

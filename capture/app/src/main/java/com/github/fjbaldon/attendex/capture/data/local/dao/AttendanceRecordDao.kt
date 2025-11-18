package com.github.fjbaldon.attendex.capture.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.fjbaldon.attendex.capture.data.local.model.AttendanceRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AttendanceRecordEntity)

    @Query("SELECT * FROM attendance_records WHERE eventId = :eventId ORDER BY checkInTimestamp DESC")
    fun getRecordsForEventStream(eventId: Long): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE isSynced = 0")
    suspend fun getUnsyncedRecords(): List<AttendanceRecordEntity>

    @Query("SELECT * FROM attendance_records WHERE isSynced = 0 LIMIT :limit")
    suspend fun getUnsyncedRecordsBatch(limit: Int): List<AttendanceRecordEntity>

    @Query("UPDATE attendance_records SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Int>)

    @Query("SELECT COUNT(id) FROM attendance_records WHERE isSynced = 0")
    fun getUnsyncedRecordCount(): Flow<Int>

    @Query("SELECT * FROM attendance_records WHERE eventId = :eventId ORDER BY checkInTimestamp DESC LIMIT 1")
    suspend fun findMostRecentRecordForEvent(eventId: Long): AttendanceRecordEntity?
}

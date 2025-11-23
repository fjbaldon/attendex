package com.github.fjbaldon.attendex.capture.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.fjbaldon.attendex.capture.core.data.local.model.EntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: EntryEntity)

    @Query("SELECT * FROM entries WHERE eventId = :eventId ORDER BY scanTimestamp DESC")
    fun getEntriesForEventStream(eventId: Long): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE syncStatus = 'PENDING' LIMIT :limit")
    suspend fun getPendingEntriesBatch(limit: Int): List<EntryEntity>

    @Query("UPDATE entries SET syncStatus = 'SYNCED', syncErrorMessage = null WHERE scanUuid IN (:uuids)")
    suspend fun markAsSyncedByUuid(uuids: List<String>)

    @Query("UPDATE entries SET syncStatus = 'FAILED', syncErrorMessage = 'Server Rejected' WHERE scanUuid IN (:uuids)")
    suspend fun markAsFailedByUuid(uuids: List<String>)

    @Query("UPDATE entries SET syncStatus = 'PENDING', syncErrorMessage = null WHERE syncStatus = 'FAILED' AND eventId = :eventId")
    suspend fun resetFailedToPendingForEvent(eventId: Long)

    @Query("SELECT COUNT(id) FROM entries WHERE syncStatus = 'PENDING'")
    fun getUnsyncedEntryCount(): Flow<Int>

    @Query("SELECT * FROM entries WHERE eventId = :eventId ORDER BY scanTimestamp DESC LIMIT 1")
    suspend fun findMostRecentEntryForEvent(eventId: Long): EntryEntity?

    @Query("SELECT COUNT(id) FROM entries WHERE syncStatus = 'PENDING'")
    suspend fun getUnsyncedCountSnapshot(): Int

    @Query("DELETE FROM entries WHERE syncStatus = 'SYNCED' AND scanTimestamp < :threshold")
    suspend fun deleteSyncedEntriesOlderThan(threshold: Long): Int

    @Query("DELETE FROM entries")
    suspend fun clearAll()
}

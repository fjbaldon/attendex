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

    // FIX: Changed 'LIMIT 100' to 'LIMIT :limit'
    @Query("SELECT * FROM entries WHERE eventId = :eventId ORDER BY scanTimestamp DESC LIMIT :limit")
    fun getEntriesForEventStream(eventId: Long, limit: Int): Flow<List<EntryEntity>>

    @Query("""
        SELECT * FROM entries 
        WHERE eventId = :eventId 
        AND (
            snapshotFirstName LIKE '%' || :query || '%' 
            OR snapshotLastName LIKE '%' || :query || '%' 
            OR snapshotIdentity LIKE '%' || :query || '%'
        )
        ORDER BY scanTimestamp DESC LIMIT 50
    """)
    fun searchEntries(eventId: Long, query: String): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE syncStatus IN ('PENDING', 'FAILED') AND retryCount < 5 ORDER BY scanTimestamp ASC LIMIT :limit")
    suspend fun getPendingEntriesBatch(limit: Int): List<EntryEntity>

    @Query("UPDATE entries SET syncStatus = 'SYNCED', syncErrorMessage = null WHERE scanUuid IN (:uuids)")
    suspend fun markAsSyncedByUuid(uuids: List<String>)

    @Query("UPDATE entries SET syncStatus = 'FAILED', syncErrorMessage = :error, retryCount = retryCount + 1 WHERE scanUuid IN (:uuids)")
    suspend fun markAsFailedByUuid(uuids: List<String>, error: String)

    @Query("UPDATE entries SET syncStatus = 'PENDING', syncErrorMessage = null, retryCount = 0 WHERE syncStatus = 'FAILED' AND eventId = :eventId")
    suspend fun resetFailedToPendingForEvent(eventId: Long)

    @Query("SELECT COUNT(id) FROM entries WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED'")
    fun getUnsyncedEntryCount(): Flow<Int>

    @Query("SELECT * FROM entries WHERE eventId = :eventId ORDER BY scanTimestamp DESC LIMIT 1")
    suspend fun findMostRecentEntryForEvent(eventId: Long): EntryEntity?

    @Query("SELECT COUNT(id) FROM entries WHERE eventId = :eventId")
    suspend fun countEntriesForEvent(eventId: Long): Long

    @Query("SELECT COUNT(id) FROM entries WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED'")
    suspend fun getUnsyncedCountSnapshot(): Int

    @Query("DELETE FROM entries WHERE syncStatus = 'SYNCED' AND scanTimestamp < :threshold")
    suspend fun deleteSyncedEntriesOlderThan(threshold: Long): Int

    @Query("DELETE FROM entries")
    suspend fun clearAll()
}

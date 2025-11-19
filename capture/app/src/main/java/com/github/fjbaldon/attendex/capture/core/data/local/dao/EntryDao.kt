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

    @Query("SELECT * FROM entries WHERE isSynced = 0 LIMIT :limit")
    suspend fun getUnsyncedEntriesBatch(limit: Int): List<EntryEntity>

    @Query("UPDATE entries SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Int>)

    @Query("SELECT COUNT(id) FROM entries WHERE isSynced = 0")
    fun getUnsyncedEntryCount(): Flow<Int>

    @Query("SELECT * FROM entries WHERE eventId = :eventId ORDER BY scanTimestamp DESC LIMIT 1")
    suspend fun findMostRecentEntryForEvent(eventId: Long): EntryEntity?
}

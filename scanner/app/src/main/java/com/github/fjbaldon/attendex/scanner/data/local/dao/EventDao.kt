package com.github.fjbaldon.attendex.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.fjbaldon.attendex.scanner.data.local.model.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EventEntity>)

    @Query("SELECT * FROM events ORDER BY eventName ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("DELETE FROM events")
    suspend fun clearAll()
}

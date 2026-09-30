package com.moneo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moneo.app.data.local.entity.InboxEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InboxEventDao {
    @Query("SELECT * FROM inbox_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<InboxEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: InboxEventEntity): Long

    @Query("DELETE FROM inbox_events WHERE id = :id")
    suspend fun deleteEvent(id: Long)

    @Query("DELETE FROM inbox_events")
    suspend fun deleteAllEvents()
}

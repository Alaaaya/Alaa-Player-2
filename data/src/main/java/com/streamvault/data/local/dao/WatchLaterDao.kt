package com.streamvault.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.streamvault.data.local.entity.WatchLaterEntity
import com.streamvault.domain.model.ContentType
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchLaterDao {
    @Query("SELECT * FROM watch_later ORDER BY added_at DESC")
    fun observeAll(): Flow<List<WatchLaterEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watch_later WHERE provider_id = :providerId AND content_id = :contentId AND content_type = :contentType)")
    fun observeContains(providerId: Long, contentId: Long, contentType: ContentType): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WatchLaterEntity): Long

    @Query("DELETE FROM watch_later WHERE provider_id = :providerId AND content_id = :contentId AND content_type = :contentType")
    suspend fun delete(providerId: Long, contentId: Long, contentType: ContentType): Int
}

package com.streamvault.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.streamvault.domain.model.ContentType

/** Separate "watch later" list, independent from favorites. Added in schema v78. */
@Entity(
    tableName = "watch_later",
    foreignKeys = [ForeignKey(
        entity = ProviderEntity::class,
        parentColumns = ["id"],
        childColumns = ["provider_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["provider_id", "content_id", "content_type"], unique = true),
        Index(value = ["added_at"])
    ]
)
data class WatchLaterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "provider_id") val providerId: Long,
    @ColumnInfo(name = "content_id") val contentId: Long,
    @ColumnInfo(name = "content_type") val contentType: ContentType,
    val title: String,
    @ColumnInfo(name = "poster_url") val posterUrl: String? = null,
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis()
)

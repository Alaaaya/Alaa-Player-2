package com.streamvault.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.streamvault.data.local.entity.PluginProviderOwnershipEntity

@Dao
interface PluginProviderOwnershipDao {
    @Query("""
        SELECT * FROM plugin_provider_ownership
        WHERE package_name = :packageName
          AND service_class_name = :serviceClassName
          AND manifest_id = :manifestId
        LIMIT 1
    """)
    suspend fun get(
        packageName: String,
        serviceClassName: String,
        manifestId: String
    ): PluginProviderOwnershipEntity?

    @Query("SELECT * FROM plugin_provider_ownership")
    suspend fun getAll(): List<PluginProviderOwnershipEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(ownership: PluginProviderOwnershipEntity)

    @Query("""
        DELETE FROM plugin_provider_ownership
        WHERE package_name = :packageName
          AND service_class_name = :serviceClassName
          AND manifest_id = :manifestId
    """)
    suspend fun delete(packageName: String, serviceClassName: String, manifestId: String)
}

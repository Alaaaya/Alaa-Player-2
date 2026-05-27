package com.streamvault.domain.repository

import com.streamvault.domain.model.DownloadItem
import com.streamvault.domain.model.DownloadRequest
import com.streamvault.domain.model.DownloadStorageConfig
import com.streamvault.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing downloads across the app.
 */
interface DownloadManager {

    /**
     * Observe all downloads.
     */
    fun observeAllDownloads(): Flow<List<DownloadItem>>

    /**
     * Observe a single download by [id].
     */
    fun observeDownload(id: String): Flow<DownloadItem?>

    /**
     * Observe the current storage configuration state.
     */
    fun observeStorageState(): Flow<DownloadStorageConfig>

    /**
     * Enqueue a new download with the given [request].
     */
    suspend fun enqueueDownload(request: DownloadRequest): Result<DownloadItem>

    /**
     * Cancel a download by [id].
     */
    suspend fun cancelDownload(id: String): Result<Unit>

    /**
     * Delete a download by [id], removing its persisted state.
     */
    suspend fun deleteDownload(id: String): Result<Unit>

    /**
     * Update the download storage configuration.
     */
    suspend fun updateStorageConfig(
        treeUri: String?,
        displayName: String?
    ): Result<DownloadStorageConfig>
}

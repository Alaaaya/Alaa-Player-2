package com.streamvault.data.sync

import com.streamvault.data.local.dao.XtreamIndexJobDao
import com.streamvault.data.local.dao.StalkerIndexJobDao
import com.streamvault.domain.manager.ProviderSyncStateReader
import com.streamvault.domain.model.StalkerIndexState
import com.streamvault.domain.model.StalkerReadinessSnapshot
import com.streamvault.domain.model.SyncState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@Singleton
class ProviderSyncStateReaderImpl @Inject constructor(
    private val syncManager: SyncManager,
    private val readinessTracker: StalkerReadinessTracker,
    private val xtreamIndexJobDao: XtreamIndexJobDao,
    private val stalkerIndexJobDao: StalkerIndexJobDao
) : ProviderSyncStateReader {
    override fun currentSyncState(providerId: Long): SyncState = syncManager.currentSyncState(providerId)

    override fun currentStalkerReadiness(providerId: Long): StalkerReadinessSnapshot? =
        readinessTracker.current(providerId)

    override fun observeStalkerReadiness(providerId: Long): Flow<StalkerReadinessSnapshot?> =
        readinessTracker.observe(providerId)

    override fun observeBackgroundIndexingActive(providerId: Long): Flow<Boolean> =
        combine(
            xtreamIndexJobDao.observeForProvider(providerId),
            stalkerIndexJobDao.observeForProvider(providerId)
        ) { xtreamJobs, stalkerJobs ->
            xtreamJobs.any { job ->
                job.section in setOf("MOVIE", "SERIES") &&
                    job.state in setOf("QUEUED", "RUNNING", "PARTIAL", "STALE", "FAILED_RETRYABLE")
            } || stalkerJobs.any { job ->
                job.state in setOf(
                    StalkerIndexState.QUEUED,
                    StalkerIndexState.RUNNING,
                    StalkerIndexState.RETRY_WAIT,
                    StalkerIndexState.PARTIAL
                )
            }
        }
}

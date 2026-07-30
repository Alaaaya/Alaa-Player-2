package com.streamvault.data.sync

import com.streamvault.data.local.dao.XtreamIndexJobDao
import com.streamvault.data.local.dao.ProviderWorkflowDao
import com.streamvault.data.local.entity.ProviderWorkflowPhase
import com.streamvault.data.local.entity.ProviderWorkflowState
import com.streamvault.domain.manager.ProviderSyncStateReader
import com.streamvault.domain.model.SyncState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine

@Singleton
class ProviderSyncStateReaderImpl @Inject constructor(
    private val syncManager: SyncManager,
    private val xtreamIndexJobDao: XtreamIndexJobDao,
    private val providerWorkflowDao: ProviderWorkflowDao
) : ProviderSyncStateReader {
    override fun currentSyncState(providerId: Long): SyncState = syncManager.currentSyncState(providerId)

    override fun observeBackgroundIndexingActive(providerId: Long): Flow<Boolean> =
        combine(
            xtreamIndexJobDao.observeForProvider(providerId),
            providerWorkflowDao.observeWorkflow(providerId),
            providerWorkflowDao.observeActivePhases(providerId)
        ) { jobs, workflow, activePhases ->
            val legacyIndexActive = jobs.any { job ->
                job.section in setOf("MOVIE", "SERIES") &&
                    job.state in setOf("QUEUED", "RUNNING", "PARTIAL", "STALE", "FAILED_RETRYABLE")
            }
            val workflowIndexActive = workflow?.let {
                it.state in setOf(ProviderWorkflowState.PENDING, ProviderWorkflowState.RUNNING) &&
                    activePhases.any { phase ->
                        phase in setOf(
                        ProviderWorkflowPhase.CONTENT_INDEX,
                        ProviderWorkflowPhase.MOVIE_INDEX,
                        ProviderWorkflowPhase.SERIES_INDEX
                    )
                    }
            } == true
            legacyIndexActive || workflowIndexActive
        }
}

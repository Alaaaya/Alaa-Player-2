package com.streamvault.data.sync

import androidx.room.Room
import com.google.common.truth.Truth.assertThat
import com.streamvault.data.local.StreamVaultDatabase
import com.streamvault.data.local.entity.ProviderEntity
import com.streamvault.data.local.entity.ProviderWorkflowPhase
import com.streamvault.data.local.entity.ProviderWorkflowPhaseState
import com.streamvault.data.local.entity.ProviderWorkflowReason
import com.streamvault.data.local.entity.ProviderWorkflowState
import com.streamvault.domain.model.ProviderType
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ProviderWorkflowRunnerTest {
    private lateinit var database: StreamVaultDatabase
    private lateinit var runner: ProviderWorkflowRunner

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            StreamVaultDatabase::class.java
        ).allowMainThreadQueries().build()
        runner = ProviderWorkflowRunner(database.providerWorkflowDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `successful execution publishes the terminal phase and workflow`() = runTest {
        insertProvider()

        val disposition = runner.execute(
            providerId = PROVIDER_ID,
            phase = ProviderWorkflowPhase.EPG,
            reason = ProviderWorkflowReason.PERIODIC
        ) {
            ProviderWorkflowOutcome.Success(checkpoint = "window=complete")
        }

        assertThat(disposition).isEqualTo(ProviderWorkflowDisposition.SUCCEEDED)
        val workflow = database.providerWorkflowDao().getWorkflow(PROVIDER_ID)!!
        assertThat(workflow.state).isEqualTo(ProviderWorkflowState.SUCCEEDED)
        val phase = database.providerWorkflowDao()
            .getPhases(PROVIDER_ID, workflow.generation)
            .single()
        assertThat(phase.state).isEqualTo(ProviderWorkflowPhaseState.SUCCEEDED)
        assertThat(phase.checkpoint).isEqualTo("window=complete")
    }

    @Test
    fun `retryable execution publishes durable retry state`() = runTest {
        insertProvider()

        val disposition = runner.execute(
            providerId = PROVIDER_ID,
            phase = ProviderWorkflowPhase.CONTENT_INDEX,
            reason = ProviderWorkflowReason.PERIODIC
        ) {
            ProviderWorkflowOutcome.Failure(
                code = "NETWORK",
                message = "offline",
                cause = IOException("offline")
            )
        }

        assertThat(disposition).isEqualTo(ProviderWorkflowDisposition.RETRY)
        val workflow = database.providerWorkflowDao().getWorkflow(PROVIDER_ID)!!
        assertThat(workflow.state).isEqualTo(ProviderWorkflowState.PENDING)
        assertThat(workflow.lastErrorCode).isEqualTo("NETWORK")
        assertThat(
            database.providerWorkflowDao()
                .getPhases(PROVIDER_ID, workflow.generation)
                .single()
                .state
        ).isEqualTo(ProviderWorkflowPhaseState.FAILED_RETRYABLE)
    }

    @Test
    fun `supersession during execution prevents old completion publication`() = runTest {
        insertProvider()
        val dao = database.providerWorkflowDao()

        val disposition = runner.execute(
            providerId = PROVIDER_ID,
            phase = ProviderWorkflowPhase.PRIMARY_CATALOG,
            reason = ProviderWorkflowReason.PERIODIC
        ) {
            dao.request(
                providerId = PROVIDER_ID,
                phase = ProviderWorkflowPhase.PREPARE,
                reason = ProviderWorkflowReason.CONFIG_CHANGE,
                now = System.currentTimeMillis(),
                supersede = true,
                force = true
            )
            ProviderWorkflowOutcome.Success()
        }

        assertThat(disposition).isEqualTo(ProviderWorkflowDisposition.SUPERSEDED)
        val workflow = dao.getWorkflow(PROVIDER_ID)!!
        assertThat(workflow.generation).isEqualTo(2L)
        assertThat(workflow.state).isEqualTo(ProviderWorkflowState.PENDING)
    }

    @Test
    fun `overlapping execution is told to retry while current owner retains lease`() = runTest {
        insertProvider()
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val first = async {
            runner.execute(
                providerId = PROVIDER_ID,
                phase = ProviderWorkflowPhase.EPG,
                reason = ProviderWorkflowReason.PERIODIC
            ) {
                started.complete(Unit)
                release.await()
                ProviderWorkflowOutcome.Success()
            }
        }
        started.await()

        val overlapping = runner.execute(
            providerId = PROVIDER_ID,
            phase = ProviderWorkflowPhase.EPG,
            reason = ProviderWorkflowReason.MANUAL,
            priority = 50
        ) {
            ProviderWorkflowOutcome.Success()
        }

        assertThat(overlapping).isEqualTo(ProviderWorkflowDisposition.BUSY)
        release.complete(Unit)
        assertThat(first.await()).isEqualTo(ProviderWorkflowDisposition.SUCCEEDED)
    }

    private suspend fun insertProvider() {
        database.providerDao().insert(
            ProviderEntity(
                id = PROVIDER_ID,
                name = "Provider",
                type = ProviderType.M3U,
                serverUrl = "https://example.com"
            )
        )
    }

    private companion object {
        const val PROVIDER_ID = 1L
    }
}

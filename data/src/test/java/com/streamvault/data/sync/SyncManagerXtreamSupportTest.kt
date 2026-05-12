package com.streamvault.data.sync

import com.google.common.truth.Truth.assertThat
import java.io.IOException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Test

class SyncManagerXtreamSupportTest {

    private val adaptiveSyncPolicy = XtreamAdaptiveSyncPolicy()

    private val support = SyncManagerXtreamSupport(
        adaptiveSyncPolicy = adaptiveSyncPolicy,
        shouldRememberSequentialPreference = { false },
        sanitizeThrowableMessage = { it?.message.orEmpty() },
        progress = { _, _, _ -> },
        movieRequestTimeoutMillis = 60_000L,
        seriesRequestTimeoutMillis = 60_000L,
        recoveryAbortWarningSuffix = "aborted"
    )

    @Test
    fun `executeXtreamRequest converts timeout cancellations into io failures`() = runTest {
        val providerId = 7L

        val failure = runCatching {
            support.executeXtreamRequest(providerId, XtreamAdaptiveSyncPolicy.Stage.CATEGORY) {
                withTimeout(1) {
                    delay(10)
                }
            }
        }.exceptionOrNull()

        assertThat(failure).isInstanceOf(IOException::class.java)
        assertThat(failure).hasMessageThat().contains("35 seconds")
        assertThat(
            adaptiveSyncPolicy.concurrencyFor(
                providerId = providerId,
                workloadSize = 10,
                preferSequential = false,
                stage = XtreamAdaptiveSyncPolicy.Stage.CATEGORY
            )
        ).isEqualTo(1)
    }
}
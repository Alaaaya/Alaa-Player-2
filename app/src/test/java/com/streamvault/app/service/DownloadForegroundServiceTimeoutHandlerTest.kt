package com.streamvault.app.service

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

class DownloadForegroundServiceTimeoutHandlerTest {

    @Test
    fun timeoutReleasesQuotaBeforeCleanup() {
        val events = mutableListOf<String>()
        val handler = DownloadForegroundServiceTimeoutHandler(
            releaseQuotaLease = { events += "release" },
            pauseDownload = { events += "pause" },
            stopForeground = { events += "stop_foreground" },
            stopSelf = { startId -> events += "stop_self_$startId" }
        )

        handler.handle(
            downloadId = "probe",
            startId = 7,
            launch = { block -> runBlocking { block() } }
        )

        assertThat(events).containsExactly(
            "release",
            "pause",
            "stop_foreground",
            "stop_self_7"
        ).inOrder()
    }
    @Test
    fun quotaIsReleasedBeforeCleanupIsScheduled() {
        val events = mutableListOf<String>()
        var pending: (suspend () -> Unit)? = null
        val handler = DownloadForegroundServiceTimeoutHandler(
            releaseQuotaLease = { events += "release" },
            pauseDownload = { events += "pause" },
            stopForeground = { events += "stop_foreground" },
            stopSelf = { events += "stop_self_$it" }
        )

        handler.handle("probe", 8) { pending = it; events += "scheduled" }
        assertThat(events).containsExactly("release", "scheduled").inOrder()
        runBlocking { requireNotNull(pending).invoke() }
        assertThat(events).containsExactly(
            "release", "scheduled", "pause", "stop_foreground", "stop_self_8"
        ).inOrder()
    }

    @Test
    fun pauseFailureStillStopsForegroundAndService() {
        val events = mutableListOf<String>()
        val failure = IllegalStateException("pause failed")
        val handler = DownloadForegroundServiceTimeoutHandler(
            releaseQuotaLease = { events += "release" },
            pauseDownload = { throw failure },
            stopForeground = { events += "stop_foreground" },
            stopSelf = { events += "stop_self_$it" }
        )

        val thrown = org.junit.Assert.assertThrows(IllegalStateException::class.java) {
            handler.handle("probe", 9) { block -> runBlocking { block() } }
        }
        assertThat(thrown).isSameInstanceAs(failure)
        assertThat(events).containsExactly("release", "stop_foreground", "stop_self_9").inOrder()
    }

    @Test
    fun missingDownloadSkipsPauseButStillCleansUp() {
        for (downloadId in listOf(null, "", "  ")) {
            val events = mutableListOf<String>()
            val handler = DownloadForegroundServiceTimeoutHandler(
                releaseQuotaLease = { events += "release" },
                pauseDownload = { events += "pause" },
                stopForeground = { events += "stop_foreground" },
                stopSelf = { events += "stop_self_$it" }
            )
            handler.handle(downloadId, 10) { block -> runBlocking { block() } }
            assertThat(events).containsExactly("release", "stop_foreground", "stop_self_10").inOrder()
        }
    }

}

package com.streamvault.app.panel

import com.streamvault.domain.model.PanelArtworkPolicyRegistry as Registry
import kotlinx.coroutines.Job
import org.junit.Assert.*
import org.junit.Test

/** Adapter/lease tests, not a claim of real Coil network/decode instrumentation. */
class PanelArtworkInterceptorTest {
    @Test fun lateDenialCancelsAdapterJobAndRejectsOldResult() {
        Registry.completeStartup()
        val url = "https://adapter.invalid/late-denial"
        Registry.record(83001, url, "vod:1", true)
        val job = Job()
        val lease = RegistryArtworkAccess.acquire(url, job)
        lease.requireAllowed()
        Registry.record(83001, url, "vod:1", false)
        assertTrue(job.isCancelled)
        assertThrows(PanelArtworkDenied::class.java) { lease.requireAllowed() }
        lease.close()
    }
    @Test fun deniedCacheOnlyRequestCannotAcquireAdapterLease() {
        Registry.completeStartup()
        val url = "https://adapter.invalid/already-cached"
        Registry.record(83002, url, "live:1", false)
        assertThrows(PanelArtworkDenied::class.java) { RegistryArtworkAccess.acquire(url, Job()) }
    }
    @Test fun sharedVisibleAndManualJobsUnaffectedByUnrelatedTransition() {
        Registry.completeStartup()
        val url = "https://adapter.invalid/shared"
        Registry.record(83003, url, "vod:1", true)
        Registry.record(83004, url, "vod:2", true)
        val sharedJob = Job()
        val shared = RegistryArtworkAccess.acquire(url, sharedJob)
        val manualJob = Job()
        val manual = RegistryArtworkAccess.acquire("https://manual.invalid/cached-adapter", manualJob)
        Registry.beginTransition(83003)
        shared.requireAllowed(); manual.requireAllowed()
        assertFalse(sharedJob.isCancelled); assertFalse(manualJob.isCancelled)
        assertFalse(manual.tracked)
        shared.close(); manual.close()
    }
}

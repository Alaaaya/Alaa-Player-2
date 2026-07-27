package com.streamvault.data.sync

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.sync.Section
import com.streamvault.domain.sync.SyncProgress
import org.junit.Test

class SyncProgressBusTest {
    private fun progress(section: Section = Section.LIVE) = SyncProgress(section, 3, 10, "Sport", 42)

    @Test
    fun finishingOneProvider_doesNotClearAnotherProvidersProgress() {
        val bus = SyncProgressBus()
        val providerA = bus.begin(1L)
        val providerB = bus.begin(2L)
        bus.emit(providerA, progress())
        val providerBProgress = progress(Section.VOD)
        bus.emit(providerB, providerBProgress)

        bus.finish(providerA)

        assertThat(bus.progressByProvider.value).containsKey(2L)
        assertThat(bus.progressByProvider.value[2L]?.progress).isEqualTo(providerBProgress)
        assertThat(bus.aggregate.value?.activeProviderCount).isEqualTo(1)
    }

    @Test
    fun staleSession_cannotClearOrPublishOverReplacementSession() {
        val bus = SyncProgressBus()
        val stale = bus.begin(1L)
        bus.emit(stale, progress())
        val replacement = bus.begin(1L)
        val replacementProgress = progress(Section.SERIES)
        bus.emit(replacement, replacementProgress)

        bus.emit(stale, progress(Section.VOD))
        bus.finish(stale)

        assertThat(bus.progressByProvider.value[1L]?.session).isEqualTo(replacement)
        assertThat(bus.progressByProvider.value[1L]?.progress).isEqualTo(replacementProgress)
    }

    @Test
    fun aggregate_isDerivedFromAllActiveProviders() {
        val bus = SyncProgressBus()
        val providerA = bus.begin(1L)
        val providerB = bus.begin(2L)
        bus.emit(providerA, progress())
        bus.emit(providerB, progress(Section.VOD))

        assertThat(bus.aggregate.value?.activeProviderCount).isEqualTo(2)
        assertThat(bus.aggregate.value?.representative?.session).isEqualTo(providerB)
    }
}

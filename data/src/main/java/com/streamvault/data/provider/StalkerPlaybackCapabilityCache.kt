package com.streamvault.data.provider

import com.streamvault.domain.model.ProviderSnapshot
import com.streamvault.domain.provider.CapabilityResolution
import com.streamvault.domain.provider.PlaybackResolver
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/** Temporary ARCH-004 compatibility boundary for Stalker playback client lifetime. */
@Singleton
class StalkerPlaybackCapabilityCache @Inject constructor(
    private val clients: TypedProviderClientFactory
) {
    private data class Entry(
        val generation: Long,
        val resolver: PlaybackResolver
    )

    private val entries = ConcurrentHashMap<Long, Entry>()

    fun resolve(snapshot: ProviderSnapshot): CapabilityResolution<PlaybackResolver> {
        entries[snapshot.provider.id]
            ?.takeIf { it.generation == snapshot.configurationGeneration }
            ?.let { return CapabilityResolution.Available(it.resolver) }
        val resolver = when (val result = clients.stalker(snapshot)) {
            is CapabilityResolution.Available -> result.capability
            is CapabilityResolution.ConfigurationError -> return result
            is CapabilityResolution.Restricted -> return result
            is CapabilityResolution.Unsupported -> return result
        }
        entries[snapshot.provider.id] = Entry(snapshot.configurationGeneration, resolver)
        trimToBound()
        return CapabilityResolution.Available(resolver)
    }

    fun invalidate(providerId: Long) {
        entries.remove(providerId)
    }

    private fun trimToBound() {
        if (entries.size <= MAX_ENTRIES) return
        entries.keys.sorted().take(entries.size - MAX_ENTRIES).forEach(entries::remove)
    }

    private companion object {
        const val MAX_ENTRIES = 32
    }
}

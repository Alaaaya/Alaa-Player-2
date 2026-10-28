package com.streamvault.app.panel

import com.streamvault.domain.model.StreamInfo
import com.streamvault.domain.model.PanelPlaybackAuthorization
import com.streamvault.player.PlayerEngine
import com.streamvault.player.timeshift.TimeshiftConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

/** Guards every main/preview/multiview engine, including previously resolved stream URLs. */
@Singleton
class PanelPlaybackGuard @Inject constructor() {
    @Volatile private var loading = true
    private val engines = mutableListOf<WeakReference<GuardedEngine>>()
    @Synchronized fun wrap(engine: PlayerEngine): PlayerEngine = GuardedEngine(engine, this).also {
        engines.removeAll { ref -> ref.get() == null }; engines += WeakReference(it)
    }
    fun ready() { loading = false }
    fun failClosed() { loading = true }
    suspend fun failClosedAndStop() {
        loading = true
        withContext(Dispatchers.Main.immediate) {
            synchronized(this@PanelPlaybackGuard) {
                engines.mapNotNull { it.get() }.forEach { it.revokeCurrent() }
            }
        }
    }
    fun allows(info: StreamInfo) = !loading && PanelPlaybackAuthorization.allows(info)
    suspend fun revoke(providerIds: Set<Long>) {
        if (providerIds.isEmpty()) return // Pending install must never stop manual playback.
        PanelPlaybackAuthorization.revoke(providerIds)
        withContext(Dispatchers.Main.immediate) {
            synchronized(this@PanelPlaybackGuard) {
                engines.mapNotNull { it.get() }.forEach { it.revokeProviders(providerIds) }
            }
        }
    }
    fun authorize(providerIds: Set<Long>) {
        PanelPlaybackAuthorization.authorize(providerIds)
        loading = false
    }
    private class GuardedEngine(private val delegate: PlayerEngine, private val guard: PanelPlaybackGuard) : PlayerEngine by delegate {
        private var stream: StreamInfo? = null
        private var revoked = false
        private var preloaded: StreamInfo? = null
        fun revokeCurrent() { revoked = true; delegate.preload(null); delegate.stopLiveTimeshift(); delegate.stop() }
        fun revokeProviders(ids: Set<Long>) {
            // Preloads may contain the next provider even while the current stream is manual.
            if (preloaded?.providerId in ids) { delegate.preload(null); preloaded = null }
            if (stream?.providerId in ids) revokeCurrent()
        }
        private fun accept(info: StreamInfo): Boolean {
            if (!guard.allows(info)) { revokeCurrent(); return false }
            stream = info; revoked = false; return true
        }
        override fun prepare(streamInfo: StreamInfo) { if (accept(streamInfo)) delegate.prepare(streamInfo) }
        override fun renewStreamUrl(streamInfo: StreamInfo) { if (accept(streamInfo)) delegate.renewStreamUrl(streamInfo) }
        override fun preload(streamInfo: StreamInfo?) {
            if (streamInfo == null || guard.allows(streamInfo)) {
                preloaded = streamInfo; delegate.preload(streamInfo)
            }
        }
        override fun play() { if (!revoked && stream?.let { guard.allows(it) } == true) delegate.play() }
        override fun startLiveTimeshift(streamInfo: StreamInfo, channelKey: String, config: TimeshiftConfig) {
            if (accept(streamInfo)) delegate.startLiveTimeshift(streamInfo, channelKey, config)
        }
        override fun resumeTimeshift() { if (!revoked && stream?.let { guard.allows(it) } == true) delegate.resumeTimeshift() }
    }
}

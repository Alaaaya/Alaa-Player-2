package com.streamvault.app.panel

import com.streamvault.domain.model.StreamInfo
import com.streamvault.player.PlayerEngine
import com.streamvault.player.timeshift.TimeshiftConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

/** Guards every main/preview/multiview engine, including previously resolved stream URLs. */
@Singleton
class PanelPlaybackGuard @Inject constructor() {
    @Volatile private var loading = true
    @Volatile private var deniedOrigins: Set<String> = emptySet()
    private val engines = mutableListOf<WeakReference<GuardedEngine>>()
    @Synchronized fun wrap(engine: PlayerEngine): PlayerEngine = GuardedEngine(engine, this).also {
        engines.removeAll { ref -> ref.get() == null }; engines += WeakReference(it)
    }
    fun ready() { loading = false }
    fun allows(url: String) = !loading && origin(url) !in deniedOrigins
    suspend fun revoke(origins: Set<String>) {
        deniedOrigins = deniedOrigins + origins
        withContext(Dispatchers.Main.immediate) {
            synchronized(this@PanelPlaybackGuard) {
                // Stop all running engines conservatively: stream URLs may be redirected CDN URLs.
                engines.mapNotNull { it.get() }.forEach { it.revokeCurrent() }
            }
        }
    }
    fun authorize(origins: Set<String>) { deniedOrigins = deniedOrigins - origins; loading = false }
    companion object {
        fun origin(url: String): String = runCatching {
            val u = URI(url); "${u.scheme?.lowercase()}://${u.host?.lowercase()}:${if (u.port >= 0) u.port else if (u.scheme == "https") 443 else 80}"
        }.getOrDefault("invalid")
        fun origins(c: PanelConfig): Set<String> = c.playlists.mapNotNull { it.server ?: it.url }.map(::origin).toSet()
    }
    private class GuardedEngine(private val delegate: PlayerEngine, private val guard: PanelPlaybackGuard) : PlayerEngine by delegate {
        private var stream: StreamInfo? = null
        private var revoked = false
        fun revokeCurrent() { revoked = true; delegate.preload(null); delegate.stopLiveTimeshift(); delegate.stop() }
        private fun accept(info: StreamInfo): Boolean {
            if (!guard.allows(info.url)) { revokeCurrent(); return false }
            stream = info; revoked = false; return true
        }
        override fun prepare(streamInfo: StreamInfo) { if (accept(streamInfo)) delegate.prepare(streamInfo) }
        override fun renewStreamUrl(streamInfo: StreamInfo) { if (accept(streamInfo)) delegate.renewStreamUrl(streamInfo) }
        override fun preload(streamInfo: StreamInfo?) {
            if (streamInfo == null || guard.allows(streamInfo.url)) delegate.preload(streamInfo)
        }
        override fun play() { if (!revoked && stream?.let { guard.allows(it.url) } == true) delegate.play() }
        override fun startLiveTimeshift(streamInfo: StreamInfo, channelKey: String, config: TimeshiftConfig) {
            if (accept(streamInfo)) delegate.startLiveTimeshift(streamInfo, channelKey, config)
        }
        override fun resumeTimeshift() { if (!revoked && stream?.let { guard.allows(it.url) } == true) delegate.resumeTimeshift() }
    }
}

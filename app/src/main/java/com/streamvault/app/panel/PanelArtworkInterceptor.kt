package com.streamvault.app.panel

import com.streamvault.domain.model.PanelArtworkPolicyRegistry
import coil3.intercept.Interceptor
import coil3.request.CachePolicy
import coil3.request.ImageResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Must run before Coil's EngineInterceptor (including memory cache lookup).
 * URL ownership and startup barrier are supplied by the data/startup coordinators.
 * A network-only OkHttp interceptor cannot enforce memory/disk cache hits.
 */
internal class PanelArtworkInterceptor(private val access: PanelArtworkAccess) : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult = coroutineScope {
        val url = chain.request.data.toString()
        val lease = access.acquire(url, checkNotNull(currentCoroutineContext()[Job]))
        try {
            lease.requireAllowed()
            // Coil commits decoded/network results INSIDE proceed(), before our post-check.
            // Disable BOTH caches for tracked panel URLs, even with an empty hide policy.
            // This prevents an obsolete decoder/fetcher writing after a purge. Manual URLs
            // retain normal caching; shared tracked URLs conservatively bypass caches.
            val request = if (lease.tracked) chain.request.newBuilder()
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .placeholderMemoryCacheKey(null as coil3.memory.MemoryCache.Key?)
                .build() else chain.request
            val result = chain.withRequest(request).proceed()
            currentCoroutineContext().ensureActive()
            lease.requireAllowed()
            result
        } finally {
            lease.close()
        }
    }
}

/**
 * Data-owned adapter contract. acquire must atomically resolve URL owners, check the current
 * generation AND register the cancellable request. A new policy first denies the affected
 * scope and cancels leases; cache purge must finish before publication. Unknown membership
 * under hides denies tracked URLs. At least one allowed visible owner permits a shared URL.
 * Manual ownership must be explicit, not inferred from "not found in the remaining DB".
 * On cold start build ownership/tombstones before opening cached-catalog readers.
 */
internal fun interface PanelArtworkAccess {
    fun acquire(url: String, requestJob: Job): PanelArtworkLease
}

internal interface PanelArtworkLease : AutoCloseable {
    val tracked: Boolean
    /** Check before request/cache decode and after fetch/decode, without leaking URL in errors. */
    fun requireAllowed()
    override fun close()
}

internal class PanelArtworkDenied : CancellationException("Artwork policy changed")

internal object RegistryArtworkAccess : PanelArtworkAccess {
    override fun acquire(url: String, requestJob: Job): PanelArtworkLease {
        val lease = try {
            PanelArtworkPolicyRegistry.acquire(url) { requestJob.cancel(PanelArtworkDenied()) }
        } catch (_: IllegalStateException) { throw PanelArtworkDenied() }
        return object : PanelArtworkLease {
            override val tracked = lease.tracked
            override fun requireAllowed() { if (!lease.allowed()) throw PanelArtworkDenied() }
            override fun close() = lease.close()
        }
    }
}

/** Recheck at actual HTTP dispatch as Coil's fetch queue can outlive the initial check. */
internal class PanelArtworkNetworkFence : okhttp3.Interceptor {
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val lease = try {
            PanelArtworkPolicyRegistry.acquire(chain.request().url.toString()) { chain.call().cancel() }
        } catch (_: IllegalStateException) { throw java.io.IOException("Artwork unavailable") }
        try {
            if (!lease.allowed()) throw java.io.IOException("Artwork unavailable")
            val response = chain.proceed(chain.request())
            if (!lease.allowed()) {
                response.close()
                throw java.io.IOException("Artwork unavailable")
            }
            return response
        } finally { lease.close() }
    }
}

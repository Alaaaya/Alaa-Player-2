package com.streamvault.domain.model

/** URL ownership must be seeded before deleting legacy catalog rows. Denied URL tombstones
 * intentionally survive provider removal. This is a process-local fence, not persistence. */
object PanelArtworkPolicyRegistry {
    private data class Owner(val providerId: Long, val key: String)
    class Lease internal constructor(val url: String, val tracked: Boolean, internal val generation: Long,
        internal val cancel: () -> Unit) : AutoCloseable {
        fun allowed(): Boolean = synchronized(PanelArtworkPolicyRegistry) {
            generation == (urlVersions[url] ?: 0L) && allows(url)
        }
        override fun close() { synchronized(PanelArtworkPolicyRegistry) { leases.remove(this) } }
    }
    private val owners = mutableMapOf<String, MutableMap<Owner, Boolean>>()
    private val epochs = mutableMapOf<Long, Long>()
    private val leases = mutableSetOf<Lease>()
    private val listeners = mutableListOf<() -> Unit>()
    private val urlVersions = mutableMapOf<String, Long>()
    private var startupReady = false

    @Synchronized fun epoch(providerId: Long): Long = epochs[providerId] ?: 0
    @Synchronized fun addInvalidationListener(listener: () -> Unit) { listeners += listener }
    fun record(providerId: Long, url: String?, ownerKey: String, allowed: Boolean,
        expectedEpoch: Long = epoch(providerId)): Boolean {
        val work = synchronized(this) {
            if (url.isNullOrBlank() || expectedEpoch != epoch(providerId)) return false
            require(ownerKey.isNotBlank())
            val previouslyAllowed = allows(url)
            owners.getOrPut(url) { mutableMapOf() }[Owner(providerId, ownerKey)] = allowed
            // Invalidate under the same lock as ownership mutation, before callbacks run.
            if (previouslyAllowed && !allows(url)) invalidate(setOf(url)) else null
        }
        dispatch(work)
        return true
    }
    /** Deny affected URL ownership before callbacks; shared visible references survive.
     * Callbacks execute outside the registry monitor and may safely inspect registry state. */
    fun beginTransition(providerId: Long) {
        val work = synchronized(this) {
            epochs[providerId] = epoch(providerId) + 1
            val denied = mutableSetOf<String>()
            owners.forEach { (url, refs) ->
                val previouslyAllowed = allows(url)
                refs.keys.filter { it.providerId == providerId }.forEach { refs[it] = false }
                if (previouslyAllowed && !allows(url)) denied += url
            }
            invalidate(denied)
        }
        dispatch(work)
    }
    private fun invalidate(urls: Set<String>): Pair<List<() -> Unit>, List<() -> Unit>>? {
        if (urls.isEmpty()) return null
        urls.forEach { urlVersions[it] = (urlVersions[it] ?: 0L) + 1L }
        return leases.filter { it.url in urls }.map { it.cancel } to listeners.toList()
    }
    private fun dispatch(work: Pair<List<() -> Unit>, List<() -> Unit>>?) {
        if (work == null) return
        var failure: Throwable? = null
        (work.first + work.second).forEach { callback ->
            try { callback() } catch (error: Throwable) { if (failure == null) failure = error }
        }
        failure?.let { throw it }
    }
    /** Call only AFTER startup ownership seed, policy restore and cache invalidation. */
    @Synchronized fun completeStartup() { startupReady = true }
    @Synchronized fun acquire(url: String, cancel: () -> Unit): Lease {
        check(allows(url)) { "Artwork unavailable" }
        return Lease(url, owners[url]?.keys?.any { it.providerId != 0L } == true, urlVersions[url] ?: 0L, cancel)
            .also { leases += it }
    }
    @Synchronized internal fun resetForTests() {
        owners.clear(); epochs.clear(); leases.clear(); listeners.clear(); urlVersions.clear()
        startupReady = false
    }
    private fun allows(url: String): Boolean {
        val remote = url.startsWith("https://", true) || url.startsWith("http://", true)
        if (remote && !startupReady) return false
        val refs = owners[url] ?: return true // unowned manual/decorative URLs after startup purge
        return refs.values.any { it }
    }
}

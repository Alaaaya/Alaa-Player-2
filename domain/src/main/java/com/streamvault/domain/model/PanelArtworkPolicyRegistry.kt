package com.streamvault.domain.model

/** URL ownership must be seeded before deleting legacy catalog rows. Denied URL tombstones
 * intentionally survive provider removal. This is a process-local fence, not persistence. */
object PanelArtworkPolicyRegistry {
    private data class Owner(val providerId: Long, val key: String)
    class Lease internal constructor(val url: String, val tracked: Boolean, internal val generation: Long,
        internal val cancel: () -> Unit) : AutoCloseable {
        fun allowed(): Boolean = synchronized(PanelArtworkPolicyRegistry) {
            (!tracked || generation == revision) && allows(url)
        }
        override fun close() { synchronized(PanelArtworkPolicyRegistry) { leases.remove(this) } }
    }
    private val owners = mutableMapOf<String, MutableMap<Owner, Boolean>>()
    private val epochs = mutableMapOf<Long, Long>()
    private val leases = mutableSetOf<Lease>()
    private val listeners = mutableListOf<() -> Unit>()
    private var revision = 0L
    private var startupReady = false

    @Synchronized fun epoch(providerId: Long): Long = epochs[providerId] ?: 0
    @Synchronized fun addInvalidationListener(listener: () -> Unit) { listeners += listener }
    @Synchronized fun record(providerId: Long, url: String?, ownerKey: String, allowed: Boolean,
        expectedEpoch: Long = epoch(providerId)): Boolean {
        if (url.isNullOrBlank() || expectedEpoch != epoch(providerId)) return false
        require(ownerKey.isNotBlank())
        owners.getOrPut(url) { mutableMapOf() }[Owner(providerId, ownerKey)] = allowed
        return true
    }
    /** Caller serializes this with publication and registers no new visible owners until purge ends.
     * Clear callbacks must not call back into data/DB locks. Failure keeps provider denied. */
    fun beginTransition(providerId: Long) {
        val work = synchronized(this) {
            epochs[providerId] = epoch(providerId) + 1
            revision++
            owners.values.forEach { refs -> refs.keys.filter { it.providerId == providerId }.forEach { refs[it] = false } }
            leases.filter { it.tracked }.map { it.cancel } to listeners.toList()
        }
        work.first.forEach { it() }
        work.second.forEach { it() }
    }
    /** Call only AFTER startup ownership seed, policy restore and cache invalidation. */
    @Synchronized fun completeStartup() { startupReady = true }
    @Synchronized fun acquire(url: String, cancel: () -> Unit): Lease {
        check(allows(url)) { "Artwork unavailable" }
        return Lease(url, owners[url]?.keys?.any { it.providerId != 0L } == true, revision, cancel)
            .also { leases += it }
    }
    private fun allows(url: String): Boolean {
        val remote = url.startsWith("https://", true) || url.startsWith("http://", true)
        if (remote && !startupReady) return false
        val refs = owners[url] ?: return true // unowned manual/decorative URLs after startup purge
        return refs.values.any { it }
    }
}

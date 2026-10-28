package com.streamvault.domain.model

/** Exact, opaque panel scope. No policy is ever shared between provider IDs. */
data class PanelCategoryPolicy(
    val deviceScope: String,
    val playlistId: String,
    val providerGeneration: String,
    val revision: String,
    val hidden: Map<String, Set<String>>
)

/** Process-local request fence. A null policy can mean blocked; use [isTracked], not current alone. */
object PanelCategoryPolicyRegistry {
    data class Snapshot internal constructor(val providerId: Long, internal val epoch: Long)
    private data class State(val epoch: Long, val policy: PanelCategoryPolicy?)
    private val states = mutableMapOf<Long, State>()
    private val epochs = mutableMapOf<Long, Long>()
    private val membership = mutableMapOf<Triple<Long, String, Long>, Set<String>>()
    private val episodeParents = mutableMapOf<Pair<Long, Long>, MutableSet<Long>>()
    private val requests = mutableMapOf<Snapshot, MutableSet<() -> Unit>>()
    @Synchronized fun registerRequest(token: Snapshot, cancel: () -> Unit): AutoCloseable {
        requireCurrent(token)
        requests.getOrPut(token) { mutableSetOf() }.add(cancel)
        return AutoCloseable { synchronized(this) { requests[token]?.remove(cancel) } }
    }
    private fun takeRequests(providerId: Long): List<() -> Unit> {
        val keys = requests.keys.filter { it.providerId == providerId }
        val callbacks = keys.flatMap { requests.remove(it).orEmpty() }
        return callbacks
    }
    private fun dispatch(callbacks: List<() -> Unit>) {
        var failure: Throwable? = null
        callbacks.forEach { callback ->
            try { callback() } catch (error: Throwable) { if (failure == null) failure = error }
        }
        failure?.let { throw it }
    }
    private var startupReady = false
    private val startupRestored = kotlinx.coroutines.CompletableDeferred<Unit>()
    interface Persistence {
        fun membership(providerId: Long, media: String, contentId: Long, categories: Set<String>, allowed: Boolean)
        fun episodeParent(providerId: Long, seriesId: Long, episodeId: Long) {}
        fun artwork(providerId: Long, media: String, contentId: Long, urls: List<String?>, expectedEpoch: Long)
        fun categoryKey(providerId: Long, media: String, rawId: String): Long
        fun rawCategory(providerId: Long, media: String, key: Long): String?
    }
    private val persistence = mutableMapOf<Long, Persistence>()
    @Synchronized fun attach(providerId: Long, store: Persistence) { persistence[providerId] = store }
    fun recordArtwork(providerId: Long, media: String, contentId: Long, urls: List<String?>, expectedEpoch: Long) = PanelArtworkPolicyRegistry.deferInvalidationCallbacks { synchronized(this) {
        if (!isTracked(providerId)) { check(startupReady); return@synchronized }
        check(allowsContent(providerId,media,contentId)) { "Artwork membership changed" }
        checkNotNull(persistence[providerId]) { "Artwork ownership unavailable" }.artwork(providerId,media,contentId,urls,expectedEpoch)
    } }
    @Synchronized fun markStartupReady() { startupReady = true; startupRestored.complete(Unit) }
    /** Shared process barrier, completed only by successful application policy restoration. */
    suspend fun awaitStartupReady() { startupRestored.await() }
    @Synchronized fun startupReady(): Boolean = startupReady
    @Synchronized fun categoryKey(providerId: Long, media: String, rawId: String): Long? {
        if (!allowsCategory(providerId, media, rawId)) return null
        return persistence[providerId]?.categoryKey(providerId, media, rawId)
            ?: rawId.toLongOrNull()?.takeIf { it.toString() == rawId }
    }
    @Synchronized fun rawCategory(providerId: Long, media: String, key: Long?): String? =
        key?.let { persistence[providerId]?.rawCategory(providerId, media, it) }
    @Synchronized fun restoreMembership(providerId: Long, media: String, contentId: Long, ids: Set<String>) {
        membership[Triple(providerId, media, contentId)] = ids.toSet()
    }
    private fun next(id: Long): Long = ((epochs[id] ?: 0L) + 1).also { epochs[id] = it }

    fun install(providerId: Long, policy: PanelCategoryPolicy) {
        require(policy.deviceScope.isNotBlank() && policy.playlistId.isNotBlank() &&
            policy.providerGeneration.isNotBlank() && policy.revision.isNotBlank())
        require(policy.hidden.keys == setOf("live", "vod", "series"))
        require(policy.hidden.values.all { ids -> ids.size <= 10000 && ids.all { it.matches(Regex("[0-9]{1,64}")) } })
        val frozen = policy.copy(hidden = policy.hidden.mapValues { it.value.toSet() }.toMap())
        val callbacks = synchronized(this) {
            if (states[providerId]?.policy == frozen) return
            clearMembership(providerId)
            states[providerId] = State(next(providerId), frozen)
            takeRequests(providerId)
        }
        dispatch(callbacks)
    }
    fun block(providerId: Long) {
        val callbacks = synchronized(this) {
            clearMembership(providerId)
            states[providerId] = State(next(providerId), null)
            takeRequests(providerId)
        }
        dispatch(callbacks)
    }
    fun remove(providerId: Long) {
        val callbacks = synchronized(this) {
            clearMembership(providerId)
            next(providerId)
            states.remove(providerId)
            persistence.remove(providerId)
            takeRequests(providerId)
        }
        dispatch(callbacks)
    }
    @Synchronized fun current(providerId: Long): PanelCategoryPolicy? = states[providerId]?.policy
    @Synchronized fun isTracked(providerId: Long): Boolean = states.containsKey(providerId)
    @Synchronized fun version(providerId: Long): Long = epochs[providerId] ?: 0L
    @Synchronized fun snapshot(providerId: Long): Snapshot = Snapshot(providerId, epochs[providerId] ?: 0L)
    @Synchronized fun isCurrent(token: Snapshot): Boolean =
        (startupReady || isTracked(token.providerId)) && token.epoch == (epochs[token.providerId] ?: 0L) && (!isTracked(token.providerId) || current(token.providerId) != null)
    @Synchronized fun requireCurrent(token: Snapshot) {
        check(isCurrent(token)) { "Category policy changed" }
    }
    @Synchronized fun allowsCategory(providerId: Long, media: String, rawId: String?): Boolean {
        val state = states[providerId] ?: return startupReady
        val policy = state.policy ?: return false
        val hidden = policy.hidden[if (media == "episode") "series" else media] ?: return false
        return rawId != null && rawId.matches(Regex("[0-9]{1,64}")) && rawId !in hidden
    }
    fun recordMembership(token: Snapshot, media: String, contentId: Long, categoryIds: Set<String>): Boolean = PanelArtworkPolicyRegistry.deferInvalidationCallbacks { synchronized(this) {
        if (!isCurrent(token)) return@synchronized false
        if (!isTracked(token.providerId)) return@synchronized true
        val key = Triple(token.providerId, media, contentId)
        val union = membership[key].orEmpty() + categoryIds
        membership[key] = union
        val allowed = union.isNotEmpty() && union.all { allowsCategory(token.providerId, media, it) }
        persistence[token.providerId]?.membership(token.providerId, media, contentId, union, allowed)
        if (media == "series") {
            episodeParents.filterKeys { it.first == token.providerId }.filterValues { contentId in it }
                .keys.toList().forEach { (_, episodeId) ->
                    recordMembership(token, "episode", episodeId, union)
                }
        }
        return@synchronized allowed
    } }
    /** Typed persistent edges inherit the complete sticky parent union, including future discoveries. */
    fun recordEpisodeParent(token: Snapshot, seriesId: Long, episodeId: Long): Boolean = PanelArtworkPolicyRegistry.deferInvalidationCallbacks { synchronized(this) {
        if (!isCurrent(token)) return@synchronized false
        if (!isTracked(token.providerId)) return@synchronized true
        episodeParents.getOrPut(token.providerId to episodeId) { mutableSetOf() }.add(seriesId)
        persistence[token.providerId]?.episodeParent(token.providerId, seriesId, episodeId)
        return@synchronized recordMembership(token, "episode", episodeId, membership[Triple(token.providerId,"series",seriesId)].orEmpty()) &&
            allowsContent(token.providerId, "series", seriesId)
    } }
    @Synchronized fun restoreEpisodeParent(providerId: Long, seriesId: Long, episodeId: Long) {
        episodeParents.getOrPut(providerId to episodeId) { mutableSetOf() }.add(seriesId)
    }
    @Synchronized fun allowsContent(providerId: Long, media: String, contentId: Long?): Boolean {
        val state = states[providerId] ?: return startupReady
        val policy = state.policy ?: return false
        if (media == "episode") {
            val parents = episodeParents[providerId to contentId] ?: return false
            if (parents.isEmpty() || parents.any { !allowsContent(providerId,"series",it) }) return false
        }
        val categories = membership[Triple(providerId, media, contentId)] ?: return false
        return categories.isNotEmpty() && categories.all { allowsCategory(providerId, media, it) }
    }
    private fun clearMembership(id: Long) { membership.keys.removeAll { it.first == id }; episodeParents.keys.removeAll { it.first == id } }
    fun media(type: ContentType): String = when (type) {
        ContentType.LIVE -> "live"
        ContentType.MOVIE, ContentType.VOD -> "vod"
        ContentType.SERIES -> "series"
        ContentType.SERIES_EPISODE -> "episode"
    }
}

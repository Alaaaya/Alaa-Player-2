package com.streamvault.data.sync

import com.streamvault.domain.model.ContentType
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex

/**
 * Owns the narrower locks used by interactive hydration and incremental index work.
 *
 * The provider-wide execution lane lives in [ProviderWorkLockRegistry]. These locks deliberately
 * remain separate because category hydration and Stalker summary/index work can be concurrent
 * across independent categories while still requiring same-key serialization.
 */
@Singleton
class ProviderSyncLockRegistry @Inject constructor() {
    private val vodCategoryLocks = ConcurrentHashMap<String, Mutex>()
    private val stalkerSummaryLocks = ConcurrentHashMap<Long, Mutex>()
    private val stalkerIndexSectionLocks = ConcurrentHashMap<String, Mutex>()

    suspend fun <T> withVodCategoryLock(
        providerId: Long,
        categoryId: Long,
        splitCatalog: Boolean,
        block: suspend () -> T
    ): T = withMutexLock(
        lockFor(vodCategoryLocks, vodCategoryKey(providerId, categoryId, splitCatalog)),
        block
    )

    suspend fun <T> withStalkerSummaryLock(
        providerId: Long,
        block: suspend () -> T
    ): T = withMutexLock(lockFor(stalkerSummaryLocks, providerId), block)

    suspend fun <T> withStalkerIndexSectionLock(
        providerId: Long,
        section: ContentType,
        block: suspend () -> T
    ): T = withMutexLock(lockFor(stalkerIndexSectionLocks, "$providerId:${section.name}"), block)

    /** Removes idle keys after durable provider deletion. */
    fun forgetProvider(providerId: Long) {
        stalkerSummaryLocks.remove(providerId)
        vodCategoryLocks.keys.removeIf { it.startsWith("$providerId:") || it.startsWith("split:$providerId:") }
        stalkerIndexSectionLocks.keys.removeIf { it.startsWith("$providerId:") }
    }

    private fun vodCategoryKey(providerId: Long, categoryId: Long, splitCatalog: Boolean): String =
        if (splitCatalog) "split:$providerId:$categoryId" else "$providerId:$categoryId"

    private fun <K> lockFor(locks: ConcurrentHashMap<K, Mutex>, key: K): Mutex =
        locks.computeIfAbsent(key) { Mutex() }

    private suspend fun <T> withMutexLock(mutex: Mutex, block: suspend () -> T): T {
        mutex.lock()
        return try {
            block()
        } finally {
            mutex.unlock()
        }
    }
}

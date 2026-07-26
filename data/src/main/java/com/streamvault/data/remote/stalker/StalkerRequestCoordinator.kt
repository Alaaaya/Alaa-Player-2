package com.streamvault.data.remote.stalker

import com.streamvault.domain.model.StalkerRequestPriority
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class StalkerRequestSnapshot(
    val active: Int,
    val queued: Int,
    val concurrencyLimit: Int,
    val stressCooldownUntil: Long
)

data class StalkerRequestDescriptor(
    val contentType: String,
    val action: String,
    val categoryKey: String? = null,
    val itemKey: String? = null,
    val page: Int? = null,
    val workId: String? = null
) {
    internal fun dedupeKey(): String = listOf(
        contentType,
        action,
        categoryKey.orEmpty(),
        itemKey.orEmpty(),
        page?.toString().orEmpty()
    ).joinToString("\u001f")
}

data class StalkerResponseMetrics(
    val items: Int? = null,
    val pages: Int? = null,
    val advertisedTotal: Int? = null,
    val truncated: Boolean? = null,
    val terminationReason: String? = null
)

/** Provider-scoped priority, deduplication, and adaptive metadata admission. */
@Singleton
class StalkerRequestCoordinator @Inject constructor(
    private val portalStateStore: StalkerPortalStateStore?
) {
    /** Isolated constructor for pure unit tests; production uses the injected state store. */
    constructor() : this(null)

    private data class Waiter(
        val ticket: Long,
        val priority: StalkerRequestPriority
    )

    private class ProviderState {
        val mutex = Mutex()
        val waiters = mutableListOf<Waiter>()
        val inFlight = mutableMapOf<String, CompletableDeferred<Any?>>()
        var active = 0
        var activeBackground = 0
        var safeMetadataConcurrency = NORMAL_METADATA_CONCURRENCY
        var stressCooldownUntil = 0L
        var persistedStateLoaded = false
    }

    private val states = ConcurrentHashMap<Long, ProviderState>()
    private val tickets = AtomicLong()

    suspend fun <T> execute(
        providerId: Long,
        priority: StalkerRequestPriority,
        descriptor: StalkerRequestDescriptor,
        metricsOf: (T) -> StalkerResponseMetrics = { StalkerResponseMetrics() },
        block: suspend () -> T
    ): T {
        require(providerId > 0L)
        val state = states.computeIfAbsent(providerId) { ProviderState() }
        hydratePersistedState(providerId, state)
        val requestKey = descriptor.dedupeKey()
        val deferred = CompletableDeferred<Any?>()
        var owner = false
        val shared = state.mutex.withLock {
            state.inFlight[requestKey]?.also { return@withLock it }
            state.inFlight[requestKey] = deferred
            owner = true
            deferred
        }
        if (!owner) {
            @Suppress("UNCHECKED_CAST")
            return shared.await() as T
        }

        val waiter = Waiter(tickets.incrementAndGet(), priority)
        var acquired = false
        var responseMetrics = StalkerResponseMetrics()
        val startedAt = System.currentTimeMillis()
        var outcome = "success"
        try {
            state.mutex.withLock { state.waiters += waiter }
            while (!acquired) {
                acquired = state.mutex.withLock {
                    val now = System.currentTimeMillis()
                    val limit = state.currentLimit(now)
                    val first = state.waiters.minWithOrNull(
                        compareBy<Waiter>({ it.priority.ordinal }, { it.ticket })
                    )
                    val backgroundAllowed = priority != StalkerRequestPriority.BACKGROUND_INDEX ||
                        state.activeBackground == 0
                    if (first == waiter && state.active < limit && backgroundAllowed) {
                        state.waiters.remove(waiter)
                        state.active += 1
                        if (priority == StalkerRequestPriority.BACKGROUND_INDEX) state.activeBackground += 1
                        true
                    } else {
                        false
                    }
                }
                if (!acquired) delay(ADMISSION_POLL_MILLIS)
            }
            val result = block()
            responseMetrics = metricsOf(result)
            val probeNow = System.currentTimeMillis()
            val restored = state.mutex.withLock {
                if (state.safeMetadataConcurrency == 1 && state.stressCooldownUntil in 1..probeNow) {
                    state.safeMetadataConcurrency = NORMAL_METADATA_CONCURRENCY
                    state.stressCooldownUntil = 0L
                    true
                } else {
                    false
                }
            }
            if (restored) portalStateStore?.recordHealthyMetadataProbe(providerId, probeNow)
            deferred.complete(result)
            return result
        } catch (cancelled: CancellationException) {
            outcome = "cancelled"
            deferred.cancel(cancelled)
            throw cancelled
        } catch (error: Throwable) {
            outcome = error.telemetryOutcome()
            if (error.isProviderStressSignal()) {
                val cooldownUntil = System.currentTimeMillis() + STRESS_COOLDOWN_MILLIS
                state.mutex.withLock {
                    state.safeMetadataConcurrency = 1
                    state.stressCooldownUntil = cooldownUntil
                }
                portalStateStore?.recordStressCooldown(providerId, cooldownUntil)
            }
            deferred.completeExceptionally(error)
            throw error
        } finally {
            val finalSnapshot = state.mutex.withLock {
                state.waiters.remove(waiter)
                if (acquired) {
                    state.active = (state.active - 1).coerceAtLeast(0)
                    if (priority == StalkerRequestPriority.BACKGROUND_INDEX) {
                        state.activeBackground = (state.activeBackground - 1).coerceAtLeast(0)
                    }
                }
                if (state.inFlight[requestKey] === deferred) state.inFlight.remove(requestKey)
                val limit = state.currentLimit(System.currentTimeMillis())
                StalkerRequestSnapshot(state.active, state.waiters.size, limit, state.stressCooldownUntil)
            }
            StalkerTelemetry.requestCompleted(
                providerId = providerId,
                priority = priority,
                descriptor = descriptor,
                responseMetrics = responseMetrics,
                durationMillis = System.currentTimeMillis() - startedAt,
                active = finalSnapshot.active,
                queued = finalSnapshot.queued,
                concurrencyLimit = finalSnapshot.concurrencyLimit,
                stressCooldownUntil = finalSnapshot.stressCooldownUntil,
                outcome = outcome
            )
        }
    }

    suspend fun snapshot(providerId: Long): StalkerRequestSnapshot {
        val state = states[providerId] ?: return StalkerRequestSnapshot(0, 0, 2, 0L)
        return state.mutex.withLock {
            val limit = state.currentLimit(System.currentTimeMillis())
            StalkerRequestSnapshot(state.active, state.waiters.size, limit, state.stressCooldownUntil)
        }
    }

    suspend fun recordFailure(providerId: Long, error: Throwable?) {
        if (providerId <= 0L || error?.isProviderStressSignal() != true) return
        val state = states.computeIfAbsent(providerId) { ProviderState() }
        hydratePersistedState(providerId, state)
        val cooldownUntil = System.currentTimeMillis() + STRESS_COOLDOWN_MILLIS
        state.mutex.withLock {
            state.safeMetadataConcurrency = 1
            state.stressCooldownUntil = cooldownUntil
        }
        portalStateStore?.recordStressCooldown(providerId, cooldownUntil)
    }

    private suspend fun hydratePersistedState(providerId: Long, state: ProviderState) {
        if (state.mutex.withLock { state.persistedStateLoaded }) return
        val persisted = portalStateStore?.get(providerId)
        state.mutex.withLock {
            if (!state.persistedStateLoaded) {
                state.safeMetadataConcurrency = persisted?.safeMetadataConcurrency
                    ?.coerceIn(1, NORMAL_METADATA_CONCURRENCY)
                    ?: NORMAL_METADATA_CONCURRENCY
                state.stressCooldownUntil = persisted?.stressCooldownUntil ?: 0L
                state.persistedStateLoaded = true
            }
        }
    }

    private fun ProviderState.currentLimit(now: Long): Int =
        if (safeMetadataConcurrency <= 1 || stressCooldownUntil > now) 1 else NORMAL_METADATA_CONCURRENCY

    private fun Throwable.isProviderStressSignal(): Boolean = when (this) {
        is StalkerApiError.RateLimited -> true
        is StalkerApiError.Server -> httpStatus == 503
        is StalkerApiError.Transport -> cause is SocketTimeoutException ||
            message.orEmpty().contains("timeout", ignoreCase = true) ||
            message.orEmpty().contains("reset", ignoreCase = true)
        is SocketTimeoutException -> true
        else -> cause?.isProviderStressSignal() == true
    }

    private fun Throwable.telemetryOutcome(): String = when (this) {
        is StalkerApiError.Authorization -> "authorization"
        is StalkerApiError.RateLimited -> "rate_limited"
        is StalkerApiError.Server -> "server"
        is StalkerApiError.Transport -> "transport"
        is StalkerApiError.Malformed -> "malformed"
        is StalkerApiError.ResponseTooLarge -> "oversized"
        is StalkerApiError.BlockedOrConfiguration -> "blocked_configuration"
        else -> "failed"
    }

    private companion object {
        const val NORMAL_METADATA_CONCURRENCY = 2
        const val ADMISSION_POLL_MILLIS = 20L
        const val STRESS_COOLDOWN_MILLIS = 5L * 60L * 1000L
    }
}

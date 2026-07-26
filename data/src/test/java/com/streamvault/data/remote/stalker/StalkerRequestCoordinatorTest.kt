package com.streamvault.data.remote.stalker

import com.google.common.truth.Truth.assertThat
import com.streamvault.domain.model.StalkerRequestPriority
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StalkerRequestCoordinatorTest {
    @Test
    fun duplicateRequestsShareOneExecution() = runTest {
        val coordinator = StalkerRequestCoordinator()
        val executions = AtomicInteger()
        val release = CompletableDeferred<Unit>()

        val first = async {
            coordinator.execute(
                7L,
                StalkerRequestPriority.OPEN_CATEGORY,
                StalkerRequestDescriptor("MOVIE", "CATEGORY_PAGE", categoryKey = "42", page = 1)
            ) {
                executions.incrementAndGet()
                release.await()
                "done"
            }
        }
        val second = async {
            coordinator.execute(
                7L,
                StalkerRequestPriority.OPEN_CATEGORY,
                StalkerRequestDescriptor("MOVIE", "CATEGORY_PAGE", categoryKey = "42", page = 1)
            ) {
                executions.incrementAndGet()
                "duplicate"
            }
        }
        runCurrent()
        assertThat(executions.get()).isEqualTo(1)

        release.complete(Unit)
        advanceUntilIdle()
        assertThat(first.await()).isEqualTo("done")
        assertThat(second.await()).isEqualTo("done")
    }

    @Test
    fun metadataConcurrencyIsCappedAtTwo() = runTest {
        val coordinator = StalkerRequestCoordinator()
        val active = AtomicInteger()
        val peak = AtomicInteger()
        val release = CompletableDeferred<Unit>()

        val jobs = (1..4).map { page ->
            async {
                coordinator.execute(
                    9L,
                    StalkerRequestPriority.OPEN_CATEGORY,
                    StalkerRequestDescriptor("MOVIE", "CATEGORY_PAGE", categoryKey = "1", page = page)
                ) {
                    val current = active.incrementAndGet()
                    peak.updateAndGet { previous -> maxOf(previous, current) }
                    release.await()
                    active.decrementAndGet()
                }
            }
        }
        runCurrent()
        assertThat(peak.get()).isEqualTo(2)

        release.complete(Unit)
        advanceUntilIdle()
        jobs.forEach { it.await() }
        assertThat(peak.get()).isEqualTo(2)
    }
}

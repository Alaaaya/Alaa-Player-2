package com.streamvault.data.sync

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProviderWorkLockRegistryTest {
    @Test
    fun `same provider phases are serialized`() = runTest {
        val registry = ProviderWorkLockRegistry()
        val firstEntered = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val secondEntered = CompletableDeferred<Unit>()

        val first = async {
            registry.withProviderLock(7L) {
                firstEntered.complete(Unit)
                releaseFirst.await()
            }
        }
        firstEntered.await()

        val second = async {
            registry.withProviderLock(7L) {
                secondEntered.complete(Unit)
            }
        }
        runCurrent()

        assertThat(secondEntered.isCompleted).isFalse()
        assertThat(registry.isAnyWorkActiveOrWaiting()).isTrue()

        releaseFirst.complete(Unit)
        first.await()
        second.await()

        assertThat(secondEntered.isCompleted).isTrue()
        assertThat(registry.isAnyWorkActiveOrWaiting()).isFalse()
    }

    @Test
    fun `different providers retain independent execution lanes`() = runTest {
        val registry = ProviderWorkLockRegistry()
        val firstEntered = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val secondEntered = CompletableDeferred<Unit>()

        val first = async {
            registry.withProviderLock(7L) {
                firstEntered.complete(Unit)
                releaseFirst.await()
            }
        }
        firstEntered.await()

        val second = async {
            registry.withProviderLock(8L) {
                secondEntered.complete(Unit)
            }
        }
        runCurrent()

        assertThat(secondEntered.isCompleted).isTrue()

        releaseFirst.complete(Unit)
        first.await()
        second.await()
    }

    @Test
    fun `maintenance admission rejects active or queued provider work`() = runTest {
        val registry = ProviderWorkLockRegistry()
        val firstEntered = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()

        val providerWork = async {
            registry.withProviderLock(7L) {
                firstEntered.complete(Unit)
                releaseFirst.await()
            }
        }
        firstEntered.await()

        assertThat(registry.runWhenNoWorkActive { true }).isFalse()

        releaseFirst.complete(Unit)
        providerWork.await()

        assertThat(registry.runWhenNoWorkActive { true }).isTrue()
    }
}

package com.streamvault.data.security

import com.google.common.truth.Truth.assertThat
import java.security.GeneralSecurityException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import javax.crypto.spec.SecretKeySpec
import org.junit.Assert.assertThrows
import org.junit.Test

class CachingSecretKeyProviderTest {
    @Test
    fun concurrentRequestsLoadOneKey() {
        val calls = AtomicInteger()
        val key = SecretKeySpec(ByteArray(32) { 7 }, "AES")
        val provider = CachingSecretKeyProvider {
            calls.incrementAndGet()
            key
        }
        val executor = Executors.newFixedThreadPool(8)
        try {
            val results = executor.invokeAll(List(32) { Callable { provider.get() } })
            results.forEach { assertThat(it.get()).isSameInstanceAs(key) }
            assertThat(calls.get()).isEqualTo(1)
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun failuresAreNotCached() {
        var attempts = 0
        val key = SecretKeySpec(ByteArray(32) { 9 }, "AES")
        val provider = CachingSecretKeyProvider {
            if (++attempts == 1) throw GeneralSecurityException("temporarily unavailable")
            key
        }

        assertThrows(GeneralSecurityException::class.java) { provider.get() }
        assertThat(provider.get()).isSameInstanceAs(key)
        assertThat(provider.get()).isSameInstanceAs(key)
        assertThat(attempts).isEqualTo(2)
    }
}

package com.streamvault.data.security

import javax.crypto.SecretKey

/** Caches successful loads only. A transient storage failure remains retryable. */
internal class CachingSecretKeyProvider(private val load: () -> SecretKey) {
    @Volatile private var cached: SecretKey? = null

    fun get(): SecretKey = cached ?: synchronized(this) {
        cached ?: load().also { cached = it }
    }
}

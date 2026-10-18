package com.streamvault.data.security

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.security.GeneralSecurityException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SoftwareCredentialKeyStoreTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test
    fun createsAes256KeyThatSurvivesNewStoreInstances() {
        val file = File(temporary.root, "private/credential.key")
        val first = SoftwareCredentialKeyStore(file).loadOrCreate()
        val second = SoftwareCredentialKeyStore(file).loadOrCreate()

        assertThat(first.algorithm).isEqualTo("AES")
        assertThat(first.encoded).hasLength(32)
        assertThat(second.encoded).isEqualTo(first.encoded)
        assertThat(file.readBytes()).isEqualTo(first.encoded)
        assertThat(file.parentFile!!.list()!!.filter { it.endsWith(".tmp") }).isEmpty()
    }

    @Test
    fun concurrentStoreInstancesUseOnePersistedKey() {
        val file = File(temporary.root, "credential.key")
        val executor = Executors.newFixedThreadPool(8)
        try {
            val results = executor.invokeAll(List(32) {
                Callable { SoftwareCredentialKeyStore(file).loadOrCreate().encoded }
            }).map { it.get().toList() }
            assertThat(results.distinct()).hasSize(1)
            assertThat(file.readBytes().toList()).isEqualTo(results.first())
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun corruptExistingKeyIsNotSilentlyReplaced() {
        val file = temporary.newFile("credential.key").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        assertThrows(GeneralSecurityException::class.java) {
            SoftwareCredentialKeyStore(file).loadOrCreate()
        }
        assertThat(file.readBytes()).isEqualTo(byteArrayOf(1, 2, 3))
    }
}

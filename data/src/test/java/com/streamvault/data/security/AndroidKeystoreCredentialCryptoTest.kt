package com.streamvault.data.security

import android.content.Context
import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/** The local JVM has no AndroidKeyStore provider, exercising the broken-TV fallback. */
class AndroidKeystoreCredentialCryptoTest {
    @get:Rule val temporary = TemporaryFolder()

    private fun crypto(): AndroidKeystoreCredentialCrypto {
        val context: Context = mock()
        whenever(context.noBackupFilesDir).thenReturn(temporary.root)
        return AndroidKeystoreCredentialCrypto(context)
    }

    @Test
    fun unavailableKeystoreFallsBackToPersistentEncryptedStorage() {
        val encrypted = crypto().encryptIfNeeded("private-password")
        assertThat(encrypted).startsWith("enc:software:v1:")
        assertThat(encrypted).doesNotContain("private-password")
        assertThat(crypto().decryptIfNeeded(encrypted)).isEqualTo("private-password")
        assertThat(crypto().encryptIfNeeded(encrypted)).isEqualTo(encrypted)
    }

    @Test
    fun encryptionUsesFreshNonceForEachCredential() {
        val crypto = crypto()
        val first = crypto.encryptIfNeeded("secret")
        val second = crypto.encryptIfNeeded("secret")
        assertThat(second).isNotEqualTo(first)
        assertThat(crypto.decryptIfNeeded(first)).isEqualTo("secret")
        assertThat(crypto.decryptIfNeeded(second)).isEqualTo("secret")
    }

    @Test
    fun corruptKeyFailsClosedWithoutReplacingIt() {
        val file = File(temporary.root, "streamvault_credential_software.key")
        file.writeBytes(byteArrayOf(1))
        assertThrows(SecurityException::class.java) { crypto().encryptIfNeeded("secret") }
        assertThat(file.readBytes()).isEqualTo(byteArrayOf(1))
    }

    @Test
    fun modifiedCiphertextFailsAuthentication() {
        val crypto = crypto()
        val encrypted = crypto.encryptIfNeeded("secret")
        val bytes = java.util.Base64.getDecoder().decode(encrypted.removePrefix("enc:software:v1:"))
        bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()
        val corrupted = "enc:software:v1:" + java.util.Base64.getEncoder().encodeToString(bytes)
        assertThrows(CredentialDecryptionException::class.java) { crypto.decryptIfNeeded(corrupted) }
    }

    @Test
    fun hardwareCiphertextNeverCreatesOrUsesASoftwareReplacementKey() {
        // A valid-shaped legacy hardware payload on a JVM without AndroidKeyStore.
        val value = "enc:v1:" + java.util.Base64.getEncoder().encodeToString(ByteArray(32))
        assertThrows(CredentialDecryptionException::class.java) { crypto().decryptIfNeeded(value) }
        assertThat(temporary.root.listFiles()!!.toList()).isEmpty()
    }

    @Test
    fun blankAndLegacyValuesRemainCompatible() {
        val crypto = crypto()
        assertThat(crypto.encryptIfNeeded("")).isEmpty()
        assertThat(crypto.decryptIfNeeded("legacy")).isEqualTo("legacy")
        assertThat(temporary.root.listFiles()!!.toList()).isEmpty()
    }
}

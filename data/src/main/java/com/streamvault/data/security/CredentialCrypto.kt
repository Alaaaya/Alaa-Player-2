package com.streamvault.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Contract for AES-GCM credential encryption backed by Android Keystore.
 * Expressed as an interface so implementations can be replaced with test fakes.
 */
interface CredentialCrypto {
    fun encryptIfNeeded(value: String): String
    fun decryptIfNeeded(value: String): String
}

/**
 * Production implementation backed by the Android Keystore system.
 *
 * Hardware values retain enc:v1:<base64(iv + ciphertext)>.
 * Software values use enc:software:v1: so a fallback cannot strand older hardware values.
 */
@Singleton
class AndroidKeystoreCredentialCrypto @Inject constructor(
    @ApplicationContext context: Context
) : CredentialCrypto {
    private val TAG = "CredentialCrypto"
    private val KEYSTORE_TYPE = "AndroidKeyStore"
    private val KEY_ALIAS = "streamvault_credentials"
    private val TRANSFORMATION = "AES/GCM/NoPadding"
    private val IV_SIZE_BYTES = 12
    private val AUTH_TAG_BITS = 128
    private val PREFIX = "enc:v1:"
    private val SOFTWARE_PREFIX = "enc:software:v1:"
    private val softwareKeys = SoftwareCredentialKeyStore(
        File(context.noBackupFilesDir, "streamvault_credential_software.key")
    )
    private val hardwareKeyProvider = CachingSecretKeyProvider(::loadOrCreateHardwareSecretKey)
    private val softwareKeyProvider = CachingSecretKeyProvider(softwareKeys::loadOrCreate)
    @Volatile private var softwareEncryption = softwareKeys.exists()
    private val encryptionKeyProvider = CachingSecretKeyProvider {
        // Keep new writes on the same backend if Keymaster recovers after a restart.
        if (softwareEncryption) {
            softwareKeyProvider.get()
        } else {
            try {
                hardwareKeyProvider.get()
            } catch (e: Exception) {
                Log.w(TAG, "Android Keystore unavailable; using an app-private software key.")
                softwareKeyProvider.get().also { softwareEncryption = true }
            }
        }
    }

    override fun encryptIfNeeded(value: String): String {
        if (value.isBlank() || value.startsWith(PREFIX) || value.startsWith(SOFTWARE_PREFIX)) return value

        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKeyProvider.get())
            val iv = cipher.iv
            val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            val packed = iv + encrypted
            val prefix = if (softwareEncryption) SOFTWARE_PREFIX else PREFIX
            prefix + java.util.Base64.getEncoder().encodeToString(packed)
        } catch (e: Exception) {
            // Do NOT fall back to plaintext — rethrow so the caller can surface the failure.
            Log.e(TAG, "Keystore encryption failed. Credential will NOT be stored.", e)
            throw SecurityException("Failed to encrypt credential: ${e.message}", e)
        }
    }

    override fun decryptIfNeeded(value: String): String {
        val prefix = when {
            value.startsWith(SOFTWARE_PREFIX) -> SOFTWARE_PREFIX
            value.startsWith(PREFIX) -> PREFIX
            else -> return value
        }

        return try {
            val payload = value.removePrefix(prefix)
            val bytes = java.util.Base64.getDecoder().decode(payload)
            if (bytes.size <= IV_SIZE_BYTES) {
                throw CredentialDecryptionException(cause = IllegalArgumentException("Encrypted credential payload is truncated"))
            }

            val iv = bytes.copyOfRange(0, IV_SIZE_BYTES)
            val ciphertext = bytes.copyOfRange(IV_SIZE_BYTES, bytes.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                if (prefix == SOFTWARE_PREFIX) softwareKeyProvider.get() else hardwareKeyProvider.get(),
                GCMParameterSpec(AUTH_TAG_BITS, iv)
            )
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Keystore decryption failed. Stored credential is unreadable.", e)
            throw CredentialDecryptionException(cause = e)
        }
    }

    private fun loadOrCreateHardwareSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_TYPE).apply { load(null) }
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_TYPE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(false)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}

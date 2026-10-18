package com.streamvault.data.security

import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.security.GeneralSecurityException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Last-resort AES key for devices with an unusable Android Keystore.
 *
 * Lives in app-private, non-backed-up storage. This does not provide hardware protection,
 * but never writes credentials in plaintext. Invalid key files fail closed, not regenerated.
 */
internal class SoftwareCredentialKeyStore(private val file: File) {
    fun exists(): Boolean = file.exists()

    fun loadOrCreate(): SecretKey = synchronized(lock) {
        val directory = requireNotNull(file.parentFile)
        if (!directory.isDirectory && !directory.mkdirs()) {
            throw GeneralSecurityException("Cannot create private credential key directory")
        }
        // Serialize creators in other app processes as well as threads.
        RandomAccessFile(File(directory, file.name + ".lock"), "rw").use { lockFile ->
            lockFile.channel.lock().use {
                if (file.exists()) return@synchronized readKey()
                val key = KeyGenerator.getInstance("AES").apply { init(KEY_SIZE_BITS) }.generateKey()
                val temporary = File.createTempFile("credential-key-", ".tmp", directory)
                try {
                    if (!temporary.setReadable(false, false) ||
                        !temporary.setWritable(false, false) ||
                        !temporary.setReadable(true, true) ||
                        !temporary.setWritable(true, true)
                    ) {
                        throw GeneralSecurityException("Cannot restrict credential key permissions")
                    }
                    FileOutputStream(temporary).use { output ->
                        output.write(key.encoded)
                        output.fd.sync()
                    }
                    if (!temporary.renameTo(file)) {
                        throw GeneralSecurityException("Cannot persist credential key")
                    }
                } finally {
                    temporary.delete()
                }
                key
            }
        }
    }

    private fun readKey(): SecretKey {
        // Check before allocating so a corrupt file cannot trigger an unbounded read.
        if (file.length() != KEY_SIZE_BITS / 8L) {
            throw GeneralSecurityException("Invalid software credential key length")
        }
        val bytes = file.readBytes()
        if (bytes.size != KEY_SIZE_BITS / 8) {
            throw GeneralSecurityException("Invalid software credential key length")
        }
        return SecretKeySpec(bytes, "AES")
    }

    private companion object {
        const val KEY_SIZE_BITS = 256
        val lock = Any()
    }
}

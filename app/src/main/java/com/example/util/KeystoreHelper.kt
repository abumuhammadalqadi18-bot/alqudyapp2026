package com.example.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import java.security.SecureRandom

/**
 * Generates a random database passphrase on first run,
 * encrypts it with Android Keystore, and stores the encrypted
 * form in SharedPreferences for subsequent launches.
 */
object KeystoreHelper {

    private const val KEYSTORE_ALIAS = "alqadi_db_key"
    private const val PREFS_NAME = "alqadi_secure_prefs"
    private const val PREF_ENCRYPTED_PASSPHRASE = "encrypted_db_passphrase"
    private const val PREF_IV = "encrypted_db_passphrase_iv"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    /**
     * Retrieves or creates the database passphrase, backed by Android Keystore.
     * On first call, a random 32-byte passphrase is generated, encrypted with a
     * Keystore-backed AES key, and persisted in SharedPreferences.
     */
    fun getOrCreatePassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val existingEncrypted = prefs.getString(PREF_ENCRYPTED_PASSPHRASE, null)
        val existingIv = prefs.getString(PREF_IV, null)

        if (existingEncrypted != null && existingIv != null) {
            // Decrypt and return the existing passphrase
            val encryptedBytes = Base64.decode(existingEncrypted, Base64.DEFAULT)
            val iv = Base64.decode(existingIv, Base64.DEFAULT)
            return decryptWithKeystore(encryptedBytes, iv)
        }

        // Generate a new random passphrase (32 bytes = 256 bits)
        val rawPassphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }

        // Encrypt it with Keystore
        val (encryptedBytes, iv) = encryptWithKeystore(rawPassphrase)

        // Store encrypted form
        prefs.edit()
            .putString(PREF_ENCRYPTED_PASSPHRASE, Base64.encodeToString(encryptedBytes, Base64.DEFAULT))
            .putString(PREF_IV, Base64.encodeToString(iv, Base64.DEFAULT))
            .apply()

        return rawPassphrase
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        keyStore.getEntry(KEYSTORE_ALIAS, null)?.let {
            return (it as KeyStore.SecretKeyEntry).secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun encryptWithKeystore(plaintext: ByteArray): Pair<ByteArray, ByteArray> {
        val secretKey = getOrCreateSecretKey()
        val cipher = javax.crypto.Cipher.getInstance(TRANSFORMATION)
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plaintext)
        return Pair(encrypted, iv)
    }

    private fun decryptWithKeystore(ciphertext: ByteArray, iv: ByteArray): ByteArray {
        val secretKey = getOrCreateSecretKey()
        val cipher = javax.crypto.Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(ciphertext)
    }
}

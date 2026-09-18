package com.example.repartidor.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import android.util.Base64
import javax.crypto.spec.GCMParameterSpec

class SecureStorage(
    private val context: Context
) {

    companion object {
        private const val KEYSTORE_NAME = "AndroidKeyStore"
        private const val KEY_ALIAS = "OsmitDeviceCredential"
        private const val PREFS_NAME = "secure_device_storage"

        private const val CREDENTIAL_KEY = "device_credential"
        private const val IV_KEY = "device_credential_iv"
    }

    private val preferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private fun getOrCreateSecretKey(): SecretKey {
        val keystore = java.security.KeyStore.getInstance(KEYSTORE_NAME).apply { load(null) }
        val existingKey = keystore.getKey(KEY_ALIAS, null)

        if (existingKey is SecretKey) {
            return existingKey
        }
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_NAME
        )

        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build()
        keyGenerator.init(keySpec)

        return keyGenerator.generateKey()
    }

    fun saveCredential(credential: String) {

        val secretKey = getOrCreateSecretKey()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        cipher.init(
            Cipher.ENCRYPT_MODE,
            secretKey
        )

        val encryptedBytes = cipher.doFinal(
            credential.toByteArray(StandardCharsets.UTF_8)
        )

        val encryptedBase64 = Base64.encodeToString(
            encryptedBytes,
            Base64.NO_WRAP
        )

        val ivBase64 = Base64.encodeToString(
            cipher.iv,
            Base64.NO_WRAP
        )

        preferences.edit()
            .putString(CREDENTIAL_KEY, encryptedBase64)
            .putString(IV_KEY, ivBase64)
            .apply()
    }

    fun getCredential(): String? {

        val encryptedBase64 =
            preferences.getString(CREDENTIAL_KEY, null)
                ?: return null

        val ivBase64 =
            preferences.getString(IV_KEY, null)
                ?: return null

        val encryptedBytes = Base64.decode(
            encryptedBase64,
            Base64.NO_WRAP
        )

        val ivBytes = Base64.decode(
            ivBase64,
            Base64.NO_WRAP
        )

        val secretKey = getOrCreateSecretKey()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        val gcmSpec = GCMParameterSpec(
            128,
            ivBytes
        )

        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey,
            gcmSpec
        )

        val decryptedBytes = cipher.doFinal(
            encryptedBytes
        )

        return String(
            decryptedBytes,
            StandardCharsets.UTF_8
        )
    }

    fun clearCredential() {

        preferences.edit()
            .remove(CREDENTIAL_KEY)
            .remove(IV_KEY)
            .apply()
    }

}
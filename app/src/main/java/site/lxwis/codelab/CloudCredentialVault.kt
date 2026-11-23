package site.lxwis.codelab

import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Device-bound AES-GCM storage for the optional cloud compiler API token.
 * The key remains in Android Keystore; only IV + ciphertext are persisted.
 * A legacy plaintext preference is migrated once and then removed.
 */
internal class CloudCredentialVault(
    private val preferences: SharedPreferences,
    private val alias: String = "CodeLab.cloud-credential.v1",
    private val encryptedKey: String = "credential-encrypted-v1",
    private val legacyKey: String = "credential"
) {

    fun load(): String {
        val ciphertext = preferences.getString(encryptedKey, null)
        if (ciphertext != null) return runCatching { unseal(ciphertext) }.getOrDefault("")
        val legacy = preferences.getString(legacyKey, null) ?: return ""
        return try {
            save(legacy)
            legacy
        } catch (_: Exception) {
            // Never leave the old cleartext token on disk, even on a broken Keystore.
            preferences.edit().remove(legacyKey).commit()
            ""
        }
    }

    fun save(credential: String) {
        if (credential.isEmpty()) {
            check(preferences.edit().remove(encryptedKey).remove(legacyKey).commit()) {
                "Could not remove encrypted cloud credential."
            }
            return
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val iv = cipher.iv
        require(iv.size == 12) { "Unsupported Keystore IV size." }
        val sealed = iv + cipher.doFinal(credential.toByteArray(Charsets.UTF_8))
        val encoded = Base64.encodeToString(sealed, Base64.NO_WRAP)
        check(preferences.edit().putString(encryptedKey, encoded).remove(legacyKey).commit()) {
            "Could not store encrypted cloud credential."
        }
    }

    private fun unseal(encoded: String): String {
        val bytes = Base64.decode(encoded, Base64.NO_WRAP)
        require(bytes.size in 29..2048) { "Invalid encrypted credential." }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        return String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8)
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore")
        store.load(null)
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build())
        return generator.generateKey()
    }
}

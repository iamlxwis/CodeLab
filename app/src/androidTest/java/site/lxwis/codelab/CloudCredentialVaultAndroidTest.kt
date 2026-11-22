package site.lxwis.codelab

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class CloudCredentialVaultAndroidTest {
    @Test fun encryptsTokenAndRemovesPlaintext() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = ctx.getSharedPreferences("vault-test-" + UUID.randomUUID(), 0)
        try {
            val vault = CloudCredentialVault(prefs)
            vault.save("top-secret-token")
            assertNull(prefs.getString("credential", null))
            assertFalse(prefs.getString("credential-encrypted-v1", "").orEmpty().contains("top-secret"))
            assertEquals("top-secret-token", CloudCredentialVault(prefs).load())
            vault.save("")
            assertEquals("", vault.load())
            assertFalse(prefs.contains("credential-encrypted-v1"))
        } finally { prefs.edit().clear().commit() }
    }

    @Test fun migratesLegacyTokenWithoutPersistingPlaintext() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = ctx.getSharedPreferences("vault-test-" + UUID.randomUUID(), 0)
        try {
            prefs.edit().putString("credential", "legacy-token").commit()
            val vault = CloudCredentialVault(prefs)
            assertEquals("legacy-token", vault.load())
            assertFalse(prefs.contains("credential"))
            assertTrue(prefs.contains("credential-encrypted-v1"))
            assertEquals("legacy-token", CloudCredentialVault(prefs).load())
        } finally { prefs.edit().clear().commit() }
    }
}

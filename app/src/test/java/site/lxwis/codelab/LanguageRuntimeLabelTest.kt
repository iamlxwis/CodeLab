package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class LanguageRuntimeLabelTest {
    @Test fun installedTypeScriptWithoutBundledEngineNeverDereferencesNull() {
        assertNull(OfflineRuntimeRegistry.manifest("ts"))
        assertEquals("Offline · verified TypeScript module",
            LanguageRuntimeLabel.describe(true, null, "TypeScript", false))
        assertEquals("Offline · verified TypeScript module · Cloud ready",
            LanguageRuntimeLabel.describe(true, null, "TypeScript", true))
    }

    @Test fun notVerifiedOptionalCannotClaimOfflineExecution() {
        assertNull(LanguageRuntimeLabel.describe(true, null, null, false))
        assertNull(LanguageRuntimeLabel.describe(false, null, null, true))
    }

    @Test fun bundledAndCompanionRuntimeLabelsRemainDistinct() {
        val engine = requireNotNull(OfflineRuntimeRegistry.manifest("lua"))
        assertEquals("Offline · LuaJ 3.0.1",
            LanguageRuntimeLabel.describe(true, engine, null, false))
        assertEquals("Offline · verified Lua module",
            LanguageRuntimeLabel.describe(true, engine, "Lua", false))
    }
}

package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class RuntimePackCatalogTest {
    @Test fun onlyPackagedOptionalModulesAreListed() {
        assertEquals(setOf("Python", "JavaScript", "Lua", "Java"), RuntimePackCatalog.downloadableLanguages())
        assertTrue(RuntimePackCatalog.isDownloadable("Python"))
        assertTrue(RuntimePackCatalog.isDownloadable("JavaScript"))
        assertTrue(RuntimePackCatalog.isDownloadable("Lua"))
        assertTrue(RuntimePackCatalog.isDownloadable("Java"))
        for (name in listOf("TypeScript", "C++", "Rust")) {
            assertFalse(name, RuntimePackCatalog.isDownloadable(name))
        }
    }

    @Test fun neverAdvertiseUnshippedCoreEngines() {
        // Optional Python is not bundled; real pack presence is verified on Android.
        assertNull(OfflineRuntimeRegistry.manifest("py"))
        assertEquals(RuntimePackCatalog.State.PACK_PLANNED,
            RuntimePackCatalog.capability("Python").state)
        assertEquals(RuntimePackCatalog.State.CORE_PLANNED,
            RuntimePackCatalog.capability("TypeScript").state)
    }

    @Test fun nativeCompilerPacksRemainPlannedUntilActuallyInstalled() {
        for (language in listOf("C", "C++", "Kotlin", "PHP", "Ruby", "Go", "Rust", "C#")) {
            val entry = RuntimePackCatalog.capability(language)
            assertEquals(language, RuntimePackCatalog.State.PACK_PLANNED, entry.state)
            assertTrue(entry.label.contains("not installed"))
        }
    }

    @Test fun filesWithNoEngineStayEditableButNotExecutable() {
        val entry = RuntimePackCatalog.capability("Markdown")
        assertEquals(RuntimePackCatalog.State.EDIT_ONLY, entry.state)
    }
}

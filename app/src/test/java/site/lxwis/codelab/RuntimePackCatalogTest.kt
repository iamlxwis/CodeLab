package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class RuntimePackCatalogTest {
    @Test fun neverAdvertiseUnshippedCoreEngines() {
        // This JVM test cannot verify native CPython startup; device tests do that.
        assertNotNull(OfflineRuntimeRegistry.manifest("py"))
        if (OfflineRuntimeRegistry.locallyAvailable("py")) {
            assertEquals(RuntimePackCatalog.State.BUNDLED,
                RuntimePackCatalog.capability("Python").state)
        }
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

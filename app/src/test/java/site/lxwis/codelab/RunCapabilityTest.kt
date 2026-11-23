package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class RunCapabilityTest {
    @Test fun bundledLanguagesAreExplicit() {
        listOf("main.js", "main.lua", "Main.java", "main.sql").forEach {
            assertEquals(it, RunCapability.Mode.LOCAL, RunCapability.forPath(it).mode)
        }
        assertEquals(RunCapability.Mode.PREVIEW, RunCapability.forPath("site/index.HTML").mode)
        assertEquals(RunCapability.Mode.UNAVAILABLE, RunCapability.forPath("main.py").mode)
        assertEquals(RunCapability.Mode.LOCAL,
            RunCapability.forPath("main.py", installedModules = setOf("py")).mode)
        assertEquals(RunCapability.Mode.ONLINE,
            RunCapability.forPath("main.py", cloudExtensions = setOf("py")).mode)
    }
    @Test fun cloudExecutionRequiresVerifiedExtension() {
        assertEquals(RunCapability.Mode.UNAVAILABLE, RunCapability.forPath("main.ts").mode)
        assertEquals(RunCapability.Mode.ONLINE,
            RunCapability.forPath("main.ts", setOf("ts")).mode)
        assertEquals(RunCapability.Mode.UNAVAILABLE,
            RunCapability.forPath("main.cpp", setOf("ts")).mode)
    }
    @Test fun unsupportedFilesCannotFallThroughToJavaScript() {
        listOf("README.md", "Dockerfile", "settings.json", "main.kt",
            "main.ts", "main.rb", "main.php", "main").forEach {
            assertEquals(it, RunCapability.Mode.UNAVAILABLE, RunCapability.forPath(it).mode)
        }
    }
}

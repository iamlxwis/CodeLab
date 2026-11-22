package site.lxwis.codelab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RunTargetAndTabOrderTest {
    @Test fun preferredEntrypointWinsAndStaleEntrypointFallsBack() {
        val files = listOf("src/main.js", "scratch.js")
        assertEquals("src/main.js", resolveRunTarget("src/main.js", "scratch.js", files))
        assertEquals("scratch.js", resolveRunTarget("deleted.js", "scratch.js", files))
        assertNull(resolveRunTarget("deleted.js", "deleted.js", files))
    }

    @Test fun tabReorderMovesExactlyOneFile() {
        val tabs = listOf("a", "b", "c")
        assertEquals(listOf("b", "a", "c"), reorderOpenTab(tabs, "b", -1))
        assertEquals(listOf("a", "c", "b"), reorderOpenTab(tabs, "b", 1))
        assertEquals(tabs, reorderOpenTab(tabs, "missing", 1))
        assertEquals(tabs, reorderOpenTab(tabs, "a", -1))
    }
    @Test fun renamedPinnedFileAndFolderKeepThePin() {
        assertEquals(setOf("lib/core.kt"),
            remapPinnedPaths(setOf("src/core.kt"), "src", "lib", directory = true))
        assertEquals(setOf("entry.ts"),
            remapPinnedPaths(setOf("main.js"), "main.js", "entry.ts"))
        assertEquals(emptySet<String>(),
            remapPinnedPaths(setOf("entry.ts"), "entry.ts", null))
    }
}

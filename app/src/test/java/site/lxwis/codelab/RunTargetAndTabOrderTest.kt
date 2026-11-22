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
}

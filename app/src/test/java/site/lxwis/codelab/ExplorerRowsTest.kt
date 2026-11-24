package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class ExplorerRowsTest {
    @Test fun expansionPreservesHierarchyAndEmptyFolders() {
        val files = listOf("main.py", "src/a.js", "src/deep/b.js")
        val rows = explorerRows(files, listOf("empty/"), setOf("src/"))
        assertEquals(listOf("empty/", "src/", "src/deep/", "src/a.js", "main.py"), rows.map { it.path })
        assertEquals(listOf(0, 0, 1, 1, 0), rows.map { it.depth })
        assertTrue(explorerRows(files, emptyList(), setOf("src/", "src/deep/")).any { it.path == "src/deep/b.js" })
    }
    @Test fun searchIncludesFilesInCollapsedFolders() {
        assertEquals(listOf(ExplorerRow("src/deep/b.js", false, 0)),
            explorerRows(listOf("src/deep/b.js", "main.py"), emptyList(), emptySet(), "B.JS"))
    }
    @Test fun logicalLinesIgnoreVisualWrapping() {
        val lines = LogicalLines("abcdefghij\nsecond\n")
        assertEquals(0, lines.lineAt(9))
        assertEquals(1, lines.lineAt(11))
        assertEquals(2, lines.lineAt(18))
        assertEquals(11, lines.start(1))
    }
}

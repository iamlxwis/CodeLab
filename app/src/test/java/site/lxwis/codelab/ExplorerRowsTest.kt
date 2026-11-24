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
    @Test fun incrementalLineIndexMatchesRebuiltIndex() {
        var source = "one\ntwo\nthree\n"
        var index = LogicalLines(source)
        val edits = listOf(Triple(3, 1, ""), Triple(3, 0, "\n\n"), Triple(0, 3, "alpha"), Triple(2, 5, "X\nY"))
        edits.forEach { (start, removed, inserted) ->
            index = index.edited(start, removed, inserted)
            source = source.replaceRange(start, start + removed, inserted)
            val expected = LogicalLines(source)
            assertEquals(expected.count, index.count)
            for (offset in 0..source.length) assertEquals(expected.lineAt(offset), index.lineAt(offset))
            for (line in 0 until expected.count) assertEquals(expected.start(line), index.start(line))
        }
    }
    @Test fun logicalLinesIgnoreVisualWrapping() {
        val lines = LogicalLines("abcdefghij\nsecond\n")
        assertEquals(0, lines.lineAt(9))
        assertEquals(1, lines.lineAt(11))
        assertEquals(2, lines.lineAt(18))
        assertEquals(11, lines.start(1))
    }
}

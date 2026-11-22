package site.lxwis.codelab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LineDiffTest {
    @Test fun producesAddedAndRemovedLines() {
        val rows = LineDiff.compare("one\ntwo\nthree", "one\nnew\nthree")
        assertEquals(1, rows.count { it.kind == LineDiff.Kind.REMOVED })
        assertEquals(1, rows.count { it.kind == LineDiff.Kind.ADDED })
        assertEquals("two", rows.first { it.kind == LineDiff.Kind.REMOVED }.text)
        assertEquals("new", rows.first { it.kind == LineDiff.Kind.ADDED }.text)
    }

    @Test fun handlesEmptyAndUnchangedFiles() {
        assertTrue(LineDiff.compare("", "").all { it.kind == LineDiff.Kind.SAME })
        assertTrue(LineDiff.compare("abc\n", "abc\n").all { it.kind == LineDiff.Kind.SAME })
    }

    @Test fun boundsLargeInputs() {
        val value = (0..300).joinToString("\n") { "line " + it }
        assertTrue(LineDiff.compare(value, value + "\nlast").size <= 4)
    }
}

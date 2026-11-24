package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class CodeIndentationTest {
    private fun edit(text: String, a: Int, b: Int, outdent: Boolean = false,
                     unit: String = "    ", spaces: Int = 4): Pair<String, CodeIndentationEdit?> {
        val change = codeIndentationEdit(text, a, b, unit, spaces, outdent)
        if (change == null) return text to null
        return text.replaceRange(change.start, change.end, change.replacement) to change
    }

    @Test fun indentMultipleLogicalLinesWithTrailingSelectedNewline() {
        val before = "one\ntwo\nthree\n"
        val (result, change) = edit(before, 0, 8)
        assertEquals("    one\n    two\nthree\n", result)
        assertEquals(0, change!!.selectionStart)
        assertEquals(15, change.selectionEnd)
    }

    @Test fun selectionEndingInsideNextLineIncludesThatLine() {
        val (result, _) = edit("abc\nx\n", 1, 5, unit = "\t")
        assertEquals("\tabc\n\tx\n", result)
    }

    @Test fun indentPreservesBlankAndUnselectedFollowingLines() {
        val (result, _) = edit("a\n\nb\nc", 0, 5, unit = "\t")
        assertEquals("\ta\n\n\tb\nc", result)
    }

    @Test fun outdentRemovesMixedWhitespacePerLine() {
        val source = "\talpha\n  beta\n      gamma"
        val (result, change) = edit(source, 0, source.length, outdent = true)
        assertEquals("alpha\nbeta\n  gamma", result)
        assertEquals(result.length, change!!.selectionEnd)
    }

    @Test fun collapsedOutdentPreservesCaretRelativeToContent() {
        val (result, change) = edit("    val x = 1", 10, 10, outdent = true)
        assertEquals("val x = 1", result)
        assertEquals(6, change!!.selectionStart)
        assertEquals(6, change.selectionEnd)
    }

    @Test fun collapsedIndentMovesCaretWithInsertedPrefix() {
        val (result, change) = edit("val x = 1", 4, 4)
        assertEquals("    val x = 1", result)
        assertEquals(8, change!!.selectionStart)
        assertEquals(8, change.selectionEnd)
    }

    @Test fun refusesIndentationWithoutActualChange() {
        val (result, change) = edit("val x = 1", 4, 4, outdent = true)
        assertEquals("val x = 1", result)
        assertNull(change)
    }
}

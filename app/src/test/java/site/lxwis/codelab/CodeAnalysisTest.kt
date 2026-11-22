package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class CodeAnalysisTest {
    @Test fun matchesNestedBracketsAndIgnoresQuotedBrackets() {
        val content = "call(foo(\"[ignored]\"), bar)"
        val pair = CodeAnalysis.matchBracket(content, 5)
        assertNotNull(pair)
        assertEquals(4, pair!!.first)
        assertEquals(content.lastIndex, pair.second)
        assertNull(CodeAnalysis.matchBracket(content, 10))
    }

    @Test fun findsDeclarationsWithLineNumbers() {
        val result = CodeAnalysis.symbols("val x = 1\nfun hello() {}\nclass User {}")
        assertEquals(2, result.size)
        assertEquals(2, result[0].line)
        assertEquals("fun hello", result[0].title)
    }

    @Test fun reportsUnclosedBraces() {
        val hints = CodeAnalysis.bracketIssues("fun main() {\n println(\"ok\")")
        assertEquals(1, hints.size)
        assertEquals(1, hints[0].line)
    }

    @Test fun suggestsOnlyWordsPresentInSource() {
        val result = CodeAnalysis.wordCompletions("renderView()\nren", 16)
        assertTrue(result.contains("renderView"))
    }
}

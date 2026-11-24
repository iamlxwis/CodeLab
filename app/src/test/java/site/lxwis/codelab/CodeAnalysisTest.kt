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

    @Test fun findsAndRenamesIdentifierWithoutTouchingComments() {
        val text = "fun calculate() {\n val amount = 2 // amount\n print(amount)\n}"
        val found = CodeAnalysis.references(text, "amount")
        assertEquals(2, found.size)
        assertEquals(2, CodeAnalysis.definition(text, "amount")?.line)
        val renamed = CodeAnalysis.lexicalRename(text, "amount", "total")
        assertTrue(renamed.contains("val total = 2 // amount"))
        assertTrue(renamed.contains("print(total)"))
    }

    @Test fun findsDeclarationsWithLineNumbers() {
        val result = CodeAnalysis.symbols("val x = 1\nfun hello() {}\nclass User {}")
        assertEquals(3, result.size)
        assertEquals(1, result[0].line)
        assertEquals("val x", result[0].title)
        assertEquals(2, result[1].line)
        assertEquals("fun hello", result[1].title)
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

    @Test fun completionRanksNearbyWordsAndSkipsCommentsAndStrings() {
        val source = "val renderFar = 1\n" +
            "// renderComment\n" +
            "val message = \"renderString\"\n" +
            "val renderClose = 2\n" +
            "ren\n"
        val candidates = CodeAnalysis.wordCompletions(source, source.lastIndexOf("ren") + 3, "kt")
        assertEquals(listOf("renderClose", "renderFar"), candidates)
        assertEquals(2, CodeAnalysis.renameCount(source, "renderClose", "kt") +
            CodeAnalysis.renameCount(source, "renderFar", "kt"))
        assertEquals(0, CodeAnalysis.renameCount(source, "renderComment", "kt"))
    }

    @Test fun renameCountIsNotLimitedToReferenceListPage() {
        val source = "val item = 1\n" + "use(item)\n".repeat(650) +
            "\"item\" // item\n"
        assertEquals(651, CodeAnalysis.renameCount(source, "item", "kt"))
        assertEquals(120, CodeAnalysis.references(source, "item", language = "kt").size)
    }

    @Test fun renameUpdatesAllOccurrencesBeyondReferenceDisplayCap() {
        val source = buildString {
            append("val item = 0\n")
            repeat(750) { append("print(item)\n") }
            append("// item must remain unchanged\n")
            append("val message = \"item\"\n")
        }
        val updated = CodeAnalysis.lexicalRename(source, "item", "value")
        assertEquals(751, Regex("\\bvalue\\b").findAll(updated).count())
        assertFalse(updated.contains("print(item)"))
        assertTrue(updated.contains("// item must remain unchanged"))
        assertTrue(updated.contains("\"item\""))
        assertEquals(120, CodeAnalysis.references(source, "item").size)
    }

    @Test fun renameDoesNotChangePartialIdentifiersOrInvalidNames() {
        val source = "val item = 1; val itemized = item"
        assertEquals("val total = 1; val itemized = total",
            CodeAnalysis.lexicalRename(source, "item", "total"))
        assertEquals(source, CodeAnalysis.lexicalRename(source, "item!", "total"))
    }

    @Test fun pythonRenameSkipsHashCommentsAndTripleQuotedStrings() {
        val source = "amount = 1 # amount\nprint(amount // 2)\n" +
            "''' amount '''\nprint(amount)\n"
        val renamed = CodeAnalysis.lexicalRename(source, "amount", "total", "py")
        assertTrue(renamed.contains("total = 1 # amount"))
        assertTrue(renamed.contains("print(total // 2)"))
        assertTrue(renamed.contains("''' amount '''"))
        assertEquals(3, CodeAnalysis.references(source, "amount", language = "py").size)
    }

    @Test fun diagnosticsHandleLongFilesWithAccurateLineNumbers() {
        val source = "val x = 1\n".repeat(4000) + "fun broken() {\n"
        val issues = CodeAnalysis.bracketIssues(source)
        assertEquals(1, issues.size)
        assertEquals(4001, issues.single().line)
    }
}

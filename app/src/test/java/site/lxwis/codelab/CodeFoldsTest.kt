package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class CodeFoldsTest {
    @Test fun nestedMultilineBlocksCanBeCollapsedWithoutChangingSource() {
        val source = "fun main() {\n  val x = 1\n  if (x > 0) {\n    print(x)\n  }\n}\n"
        val regions = CodeFolds.regions(source)
        assertEquals(listOf(CodeFolds.Region(1, 6), CodeFolds.Region(3, 5)), regions)
        val folded = CodeFolds.preview(source, regions, setOf(1))
        assertEquals(listOf(1, 6, 7), folded.map { it.line })
        assertEquals(4, folded.first().foldedLines)
        assertEquals(source.split('\n').size,
            CodeFolds.preview(source, regions, emptySet()).size)
    }

    @Test fun pythonIndentationCreatesNonDestructiveFoldRegions() {
        val source = "def main():\n    if True:\n        print(1)\n        print(2)\n    print(3)\nprint(4)\n"
        val regions = CodeFolds.regions(source, "py")
        assertEquals(listOf(CodeFolds.Region(1, 5), CodeFolds.Region(2, 4)), regions)
        assertEquals(listOf(1, 5, 6, 7),
            CodeFolds.preview(source, regions, setOf(1)).map { it.line })
    }

    @Test fun pythonBlankAndCommentLinesDoNotCloseSuite() {
        val source = "def run():\n    first()\n\n    # comment\n    second()\nfinally_call()"
        assertEquals(listOf(CodeFolds.Region(1, 5)), CodeFolds.regions(source, "py"))
    }

    @Test fun bracesInStringsAndCommentsAreNotFoldable() {
        val source = "const str = \"{\\nnot a fold\\n}\";\n// {\n/* {\nmore\n} */\n"
        assertTrue(CodeFolds.regions(source).isEmpty())
    }

    @Test fun sameLineNestedBracesUseOutermostRegion() {
        val source = "function x() { if (true) {\n  print(1)\n} }\n"
        assertEquals(listOf(CodeFolds.Region(1, 3)), CodeFolds.regions(source))
    }
}

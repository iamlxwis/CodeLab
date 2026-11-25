package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class ScreenshotSyntaxTest {
    @Test fun colorsQuotedTextBeforeCommentMarkers() {
        val tokens = ScreenshotSyntax.tokenize("const url = 'https://domain.test'; // note")
        assertTrue(tokens.any { it.kind == ScreenshotSyntax.Kind.STRING &&
            it.text.contains("https://") })
        assertTrue(tokens.any { it.kind == ScreenshotSyntax.Kind.COMMENT &&
            it.text.startsWith("// note") })
    }

    @Test fun identifiesKeywordsNumbersAndReconstructsSource() {
        val input = "val answer = 42"
        val spans = ScreenshotSyntax.tokenize(input)
        assertEquals(input, spans.joinToString("") { it.text })
        assertTrue(spans.any { it.kind == ScreenshotSyntax.Kind.KEYWORD })
        assertTrue(spans.any { it.kind == ScreenshotSyntax.Kind.NUMBER })
    }

    @Test fun retainsMultilineStringAcrossSourceLines() {
        val source = "const text = `line one\n// part of the string\nlast`; // real comment"
        val tokens = ScreenshotSyntax.ranges(source, "file.js").toList()
        assertEquals(listOf(ScreenshotSyntax.Kind.KEYWORD, ScreenshotSyntax.Kind.STRING, ScreenshotSyntax.Kind.COMMENT),
            tokens.map { it.kind })
        assertTrue(source.substring(tokens[1].range).contains("// part of the string"))
    }
}

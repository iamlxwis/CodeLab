package site.lxwis.codelab

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenshotRendererAndroidTest {
    private fun rows(plan: SnippetRenderer.Plan) = plan.pages.flatten()
    private fun rowText(row: SnippetRenderer.Row) = row.paragraph.text.subSequence(row.start, row.end).toString()
    private fun snapshot(source: String, capture: EditorCapture? = null, run: SnippetExecution? = null) =
        SnippetSnapshot("Test project", "script.js", source, capture, run)

    @Test fun retainsExactSelectionInMultilineCommentAndVisibleRows() = runBlocking {
        val source = "const first = 1;\n/* begins\n\t中間 ✨ comment\n last */ value\nfinal\n"
        val start = source.indexOf("間")
        val end = source.indexOf(" */") + 1
        val capture = EditorCapture(start, end, 2, 4)
        val selected = SnippetRenderer.plan(snapshot(source, capture), SnippetContent.Code,
            SnippetScope.Selected, SnippetTheme.Dark, true, true)
        val code = rows(selected).filter { it.number != null }
        assertEquals(listOf(3, 4), code.map { it.number })
        assertEquals(source.substring(start, end), code.joinToString("\n", transform = ::rowText))
        for (row in code) {
            val line = row.paragraph.text as Spanned
            val span = line.getSpans(0, line.length, ForegroundColorSpan::class.java).single()
            assertEquals(0, line.getSpanStart(span))
            assertEquals(line.length, line.getSpanEnd(span))
            assertEquals(selected.colors.syntax[ScreenshotSyntax.Kind.COMMENT.ordinal - 1], span.foregroundColor)
        }
        val visible = SnippetRenderer.plan(snapshot(source, capture), SnippetContent.Code,
            SnippetScope.Visible, SnippetTheme.Light, true, false)
        assertEquals(source.lines().subList(1, 4), rows(visible).filter { it.number != null }.map(::rowText))
        assertNotEquals(selected.colors.page, visible.colors.page)
    }

    @Test fun wrapsLongLinesAndExportsEveryNumberedPageWithPreviewPixels() = runBlocking {
        val longLine = "\t" + "αβ🙂 repeated words ".repeat(120) + "WRAPPED_END"
        val source = "FIRST\n" + longLine + "\n" + (3..96).joinToString("\n") { "line $it" } + "\nLAST\n"
        val plan = SnippetRenderer.plan(snapshot(source), SnippetContent.Code, SnippetScope.Full, SnippetTheme.Light, true, true)
        val rows = rows(plan)
        assertTrue(plan.pages.size >= 3)
        assertEquals((1..source.lines().size).toList(), rows.mapNotNull { it.number })
        assertEquals("FIRST", rowText(rows.first { it.number == 1 }))
        assertEquals(longLine, rows.filter { it.paragraph.text.toString() == longLine }.joinToString("", transform = ::rowText))
        assertEquals("LAST", rowText(rows.first { it.number == source.lines().size - 1 }))
        assertEquals("", rowText(rows.last()))
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefix = "snippet-test-${System.nanoTime()}"
        val uris = exportSnippetPages(context, plan, prefix, null, false, plan.pages.indices) {}
        assertEquals(plan.pages.size, uris.size)
        assertEquals(ContentResolver.SCHEME_CONTENT, uris.first().scheme)
        assertTrue(uris.last().lastPathSegment.orEmpty().endsWith("-${plan.pages.size}.png"))
        val share = snippetShareIntent(uris)
        assertEquals(Intent.ACTION_SEND_MULTIPLE, share.action)
        assertEquals(uris.size, share.clipData?.itemCount)
        assertTrue(share.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        for (index in listOf(0, plan.pages.lastIndex)) {
            val preview = SnippetRenderer.render(plan, index)
            try {
                val png = checkNotNull(context.contentResolver.openInputStream(uris[index])).use {
                    checkNotNull(BitmapFactory.decodeStream(it))
                }
                try {
                    assertEquals(1080, png.width)
                    assertTrue(png.height <= 1660)
                    assertTrue("Export raster differs from the preview", preview.sameAs(png))
                } finally { png.recycle() }
            } finally { preview.recycle() }
        }
    }

    @Test fun showsActualFailedOrPartialOutputWithProvenance() = runBlocking {
        val run = SnippetExecution("old.js", "older", "Test project", "Offline", "Exit 1",
            "\u001b[31mERROR\u001b[0m\n\u001b]0;hidden title\u0007\t  detail\n" +
                (1..85).joinToString("\n") { "trace $it" }, running = true, omitted = true)
        val plan = SnippetRenderer.plan(snapshot("", run = run), SnippetContent.Both,
            SnippetScope.Full, SnippetTheme.Dark, true, true)
        val lines = rows(plan).map(::rowText)
        assertTrue(plan.pages.size > 1)
        assertTrue(lines.any { it.contains("different") || it.contains("another file") })
        assertTrue(lines.any { it.contains("Offline") && it.contains("Exit 1") })
        assertTrue(lines.any { it.contains("still") || it.contains("while the run was active") })
        assertEquals("ERROR", lines.first { it.contains("ERROR") })
        assertTrue(lines.contains("\t  detail"))
        assertFalse(lines.any { it.contains("hidden title") })
        assertEquals("trace 85", lines.last())

        val empty = SnippetRenderer.plan(snapshot(""), SnippetContent.Both,
            SnippetScope.Full, SnippetTheme.Light, true, false)
        assertEquals("", rowText(rows(empty).first { it.number == 1 }))
        assertTrue(rows(empty).any { rowText(it) == "No execution result is available." })
    }
}

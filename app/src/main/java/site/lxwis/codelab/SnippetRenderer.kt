package site.lxwis.codelab

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

data class EditorCapture(val selectionStart: Int, val selectionEnd: Int, val firstLine: Int, val lastLine: Int)

internal data class SnippetExecution(
    val file: String, val source: String, val project: String?, val mode: String,
    val status: String, val output: String, val running: Boolean, val omitted: Boolean, val cleared: Boolean = false
)

internal data class SnippetSnapshot(
    val project: String, val file: String, val source: String,
    val capture: EditorCapture?, val run: SnippetExecution?
) {
    val staleRun get() = run?.let { it.project != project || it.file != file || it.source != source } == true
}

internal enum class SnippetContent(val label: String) { Code("Code"), Result("Result"), Both("Code + Result") }
internal enum class SnippetTheme { Light, Dark }
internal enum class SnippetScope(val label: String) {
    Selected("Selected text"), Visible("Visible lines"), Full("Entire file")
}

internal object SnippetRenderer {
    private const val width = 1080
    private const val padding = 52
    private const val textLeft = 162
    private const val bodyTop = 190
    private const val pageCapacity = 1370
    private val mono = Typeface.MONOSPACE
    private val controls = Regex(
        """\x1b\[[0-?]*[ -/]*[@-~]|\u009b[0-?]*[ -/]*[@-~]|\x1b\][^\x07\x1b]*(?:\x07|\x1b\\|$)|\x1b[@-_]|[\x00-\x08\x0b\x0c\x0e-\x1f\x7f-\x9f\u202a-\u202e\u2066-\u2069]"""
    )

    internal data class Palette(val page: Int, val panel: Int, val ink: Int, val muted: Int, val syntax: List<Int>)
    internal data class Paragraph(val text: CharSequence, val left: Int, val heading: Boolean)
    internal data class Row(
        val paragraph: Paragraph, val line: Int, val number: Int?, val height: Int, val start: Int, val end: Int
    )
    internal data class Plan(val pages: List<List<Row>>, val title: String, val subtitle: String, val colors: Palette)

    private fun layout(paragraph: Paragraph, paint: TextPaint) =
        StaticLayout.Builder.obtain(paragraph.text, 0, paragraph.text.length, paint,
            width - paragraph.left - padding - 27)
            .setIncludePad(false).setLineSpacing(4f, 1f).build()

    private fun textPaint(colors: Palette, heading: Boolean) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (heading) colors.muted else colors.ink
        textSize = 27f
        typeface = if (heading) Typeface.create(mono, Typeface.BOLD) else mono
    }

    private fun palette(theme: SnippetTheme) = if (theme == SnippetTheme.Light) {
        Palette(0xffe9edf0.toInt(), Color.WHITE, 0xff232930.toInt(), 0xff526171.toInt(),
            listOf(0xff174d82.toInt(), 0xff166646.toInt(), 0xff536274.toInt(), 0xff7c3a19.toInt()))
    } else {
        Palette(0xff141719.toInt(), 0xff21262b.toInt(), 0xffedf0f3.toInt(), 0xffa6b3bd.toInt(),
            listOf(0xff9dcaff.toInt(), 0xff9ad7b0.toInt(), 0xffa7b4bc.toInt(), 0xfff1b88c.toInt()))
    }

    fun cleanOutput(output: String): String = controls.replace(output, "")
        .replace("\r\n", "\n").replace('\r', '\n')

    suspend fun plan(snapshot: SnippetSnapshot, content: SnippetContent, scope: SnippetScope,
        theme: SnippetTheme, numbers: Boolean, metadata: Boolean): Plan {
        val colors = palette(theme)
        val body = textPaint(colors, false)
        val label = textPaint(colors, true)
        val blankCode = Paragraph("", if (numbers) textLeft else padding + 30, false)
        val blankResult = Paragraph("", padding + 30, false)
        val blankCodeLayout = layout(blankCode, body)
        val blankResultLayout = layout(blankResult, body)
        val pages = mutableListOf<List<Row>>()
        var rows = mutableListOf<Row>()
        var used = 0

        fun addLine(line: CharSequence, number: Int? = null, heading: Boolean = false, code: Boolean = false) {
            val paragraph = when {
                line.isNotEmpty() || heading -> Paragraph(line, if (code && numbers) textLeft else padding + 30, heading)
                code -> blankCode
                else -> blankResult
            }
            val layout = when {
                paragraph === blankCode -> blankCodeLayout
                paragraph === blankResult -> blankResultLayout
                else -> layout(paragraph, if (heading) label else body)
            }
            for (i in 0 until layout.lineCount) {
                val row = Row(paragraph, i, number.takeIf { i == 0 },
                    layout.getLineTop(i + 1) - layout.getLineTop(i), layout.getLineStart(i), layout.getLineEnd(i))
                if (used + row.height > pageCapacity && rows.isNotEmpty()) {
                    pages += rows
                    rows = mutableListOf()
                    used = 0
                }
                rows += row
                used += row.height
            }
        }

        if (content != SnippetContent.Result) {
            val source = snapshot.source
            val lines = LogicalLines(source)
            val capture = snapshot.capture
            val first = when (scope) {
                SnippetScope.Selected -> lines.lineAt(capture?.selectionStart?.coerceIn(0, source.length) ?: 0)
                SnippetScope.Visible -> (capture?.firstLine?.minus(1) ?: 0).coerceIn(0, lines.count - 1)
                SnippetScope.Full -> 0
            }
            val last = when (scope) {
                SnippetScope.Selected -> lines.lineAt((capture?.selectionEnd ?: 0).coerceIn(0, source.length))
                SnippetScope.Visible -> (capture?.lastLine?.minus(1) ?: first).coerceIn(first, lines.count - 1)
                SnippetScope.Full -> lines.count - 1
            }
            addLine("CODE · ${scope.label}", heading = true)
            val tokens = ScreenshotSyntax.ranges(source, snapshot.file).iterator()
            var token = if (tokens.hasNext()) tokens.next() else null
            for (line in first..last) {
                currentCoroutineContext().ensureActive()
                val start = lines.start(line)
                val end = if (line + 1 < lines.count) lines.start(line + 1) - 1 else source.length
                val from = if (scope == SnippetScope.Selected) maxOf(start, capture?.selectionStart ?: start) else start
                val to = if (scope == SnippetScope.Selected) minOf(end, capture?.selectionEnd ?: end) else end
                val text = source.substring(from.coerceAtMost(to), to)
                var styled: SpannableString? = null
                while (token != null && token.range.first < to) {
                    val colorStart = maxOf(token.range.first, from)
                    val colorEnd = minOf(token.range.last + 1, to)
                    if (colorStart < colorEnd) {
                        val span = styled ?: SpannableString(text).also { styled = it }
                        span.setSpan(ForegroundColorSpan(colors.syntax[token.kind.ordinal - 1]),
                            colorStart - from, colorEnd - from, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    if (token.range.last + 1 > to) break
                    token = if (tokens.hasNext()) tokens.next() else null
                }
                addLine(styled ?: text, (line + 1).takeIf { numbers }, code = true)
            }
        }
        if (content != SnippetContent.Code) {
            val run = snapshot.run
            if (content == SnippetContent.Both) addLine("")
            addLine("RESULT", heading = true)
            if (run == null) addLine("No execution result is available.")
            else {
                if (metadata || snapshot.staleRun) addLine(run.file, heading = true)
                if (metadata) {
                    val language = LanguageCatalog.entries.firstOrNull { it.extension == languageExtension(run.file) }?.name
                    addLine(listOfNotNull(language, run.mode, run.status).joinToString(" · "), heading = true)
                }
                if (run.running) addLine("Output captured while the run was active.", heading = true)
                if (snapshot.staleRun) addLine("This result belongs to another file or revision.", heading = true)
                if (run.omitted) addLine("Earlier output exceeded the console history limit.", heading = true)
                if (run.cleared) addLine("Earlier output was cleared in the console.", heading = true)
                if (run.output.isEmpty() && !run.cleared) addLine("No output recorded.")
                if (run.output.isNotEmpty()) cleanOutput(run.output).lineSequence().forEach {
                    currentCoroutineContext().ensureActive()
                    addLine(it)
                }
            }
        }
        if (rows.isNotEmpty()) pages += rows
        val titleFile = if (content == SnippetContent.Result) snapshot.run?.file ?: snapshot.file else snapshot.file
        return Plan(pages, titleFile.substringAfterLast('/'),
            if (metadata) "${snapshot.project} · CodeLab" else "CodeLab", colors)
    }

    fun render(plan: Plan, page: Int): Bitmap {
        val rows = plan.pages[page]
        val colors = plan.colors
        val height = bodyTop + rows.sumOf(Row::height) + 95
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        fun text(value: String, x: Float, y: Float, size: Float, color: Int, typeface: Typeface = mono) {
            paint.color = color
            paint.textSize = size
            paint.typeface = typeface
            for (start in value.indices) {
                if (paint.measureText(value, 0, start + 1) > width - x - padding - 20) {
                    canvas.drawText(value.take(start).dropLast(1) + "…", x, y, paint)
                    return
                }
            }
            canvas.drawText(value, x, y, paint)
        }
        canvas.drawColor(colors.page)
        paint.color = colors.panel
        canvas.drawRoundRect(padding.toFloat(), 44f, (width - padding).toFloat(),
            (height - 44).toFloat(), 27f, 27f, paint)
        text(plan.title, 82f, 105f, 36f, colors.ink, Typeface.DEFAULT_BOLD)
        text(plan.subtitle, 82f, 146f, 23f, colors.muted, Typeface.DEFAULT)
        var y = bodyTop
        val body = textPaint(colors, false)
        val label = textPaint(colors, true)
        var lastParagraph: Paragraph? = null
        var lastLayout: StaticLayout? = null
        for (row in rows) {
            val paragraph = row.paragraph
            val layout = if (paragraph === lastParagraph) checkNotNull(lastLayout)
                else layout(paragraph, if (paragraph.heading) label else body)
            lastParagraph = paragraph
            lastLayout = layout
            val top = layout.getLineTop(row.line)
            row.number?.let { text(it.toString(), 82f,
                (y + layout.getLineBaseline(row.line) - top).toFloat(), 21f, colors.muted) }
            canvas.save()
            canvas.clipRect(paragraph.left, y, width - padding - 23, y + row.height)
            canvas.translate(paragraph.left.toFloat(), (y - top).toFloat())
            layout.draw(canvas)
            canvas.restore()
            y += row.height
        }
        text("${page + 1} / ${plan.pages.size}", 82f, (height - 61).toFloat(), 21f, colors.muted)
        return bitmap
    }
}

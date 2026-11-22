package site.lxwis.codelab

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import java.util.Locale
import kotlin.math.min

internal enum class ScreenshotAspect(val label: String, val height: Int) {
    SQUARE("Square · 1:1", 1080),
    LANDSCAPE("Landscape · 16:9", 608),
    PORTRAIT("Portrait · 4:5", 1350)
}

internal data class ScreenshotOptions(
    val palette: String = "Obsidian",
    val aspect: ScreenshotAspect = ScreenshotAspect.SQUARE,
    val background: String = "Gradient",
    val lineNumbers: Boolean = true,
    val firstLine: Int = 1,
    val selectedLines: Int = 18,
    val title: String = "",
    val footer: String = "",
    val highlightLine: Int = 0
)

/** A bounded, fully local code-to-image renderer. Text is never uploaded. */
internal object ScreenshotRenderer {
    private data class Palette(
        val backdrop: Int, val backdropEnd: Int, val window: Int, val toolbar: Int,
        val text: Int, val muted: Int, val keyword: Int, val string: Int,
        val comment: Int, val number: Int, val accent: Int
    )

    private fun colors(name: String): Palette = when (name) {
        "Paper" -> Palette(0xffd5e2dc.toInt(), 0xffebeee8.toInt(),
            0xfff8f9f6.toInt(), 0xffe7eae7.toInt(), 0xff25352d.toInt(),
            0xff76827d.toInt(), 0xff5361b8.toInt(), 0xffa15a21.toInt(),
            0xff75877a.toInt(), 0xff2d7b80.toInt(), 0xff35745e.toInt())
        "Nocturne" -> Palette(0xff313c66.toInt(), 0xff111525.toInt(),
            0xff151b30.toInt(), 0xff232d47.toInt(), 0xffe9effb.toInt(),
            0xff8c9bb4.toInt(), 0xffbca3fa.toInt(), 0xff8bdad5.toInt(),
            0xff6f8197.toInt(), 0xfff4bf87.toInt(), 0xffa3b6ff.toInt())
        else -> Palette(0xff24644c.toInt(), 0xff11191d.toInt(),
            0xff151e22.toInt(), 0xff253238.toInt(), 0xffe5efed.toInt(),
            0xff869e9b.toInt(), 0xff91c2ff.toInt(), 0xffc5e4a6.toInt(),
            0xff77918c.toInt(), 0xffefbc8d.toInt(), 0xff8fe1ba.toInt())
    }

    /** Actual visible text line capacity; no silently clipped code lines. */
    fun lineCapacity(aspect: ScreenshotAspect): Int =
        ((aspect.height - 270) / 37).coerceIn(1, 55)

    fun render(filename: String, source: String, options: ScreenshotOptions): Bitmap {
        val palette = colors(options.palette)
        val width = 1080
        val height = options.aspect.height
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        if (options.background == "Transparent") {
            canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
        } else if (options.background == "Gradient") {
            paint.shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(),
                palette.backdrop, palette.backdropEnd, Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.shader = null
        } else canvas.drawColor(palette.backdropEnd)

        val frameLeft = 55f
        val frameRight = 1025f
        val frameTop = 50f
        val frameBottom = height - 68f
        val shadow = if (options.background == "Transparent") 0 else 26
        paint.color = palette.window
        paint.setShadowLayer(shadow.toFloat(), 0f, 14f, 0x65000000)
        canvas.drawRoundRect(frameLeft, frameTop, frameRight, frameBottom, 28f, 28f, paint)
        paint.clearShadowLayer()
        val top = frameTop + 82f
        paint.color = palette.toolbar
        canvas.save()
        val clip = android.graphics.Path().apply {
            addRoundRect(frameLeft, frameTop, frameRight, frameBottom,
                28f, 28f, android.graphics.Path.Direction.CW)
        }
        canvas.clipPath(clip)
        canvas.drawRect(frameLeft, frameTop, frameRight, top, paint)
        canvas.restore()

        paint.color = palette.accent
        listOf(0f, 29f, 58f).forEach { dx ->
            paint.alpha = if (dx == 0f) 255 else 130
            canvas.drawCircle(frameLeft + 35f + dx, frameTop + 40f, 8f, paint)
        }
        paint.alpha = 255
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 25f
        paint.color = palette.text
        val title = (options.title.ifBlank { filename.substringAfterLast('/') }).take(55)
        val titleWidth = paint.breakText(title, true, 670f, null)
        canvas.drawText(title.take(titleWidth), frameLeft + 146f, frameTop + 49f, paint)
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        paint.textSize = 27f

        val codeTop = top + 52f
        val lineHeight = 37f
        val left = if (options.lineNumbers) frameLeft + 104f else frameLeft + 42f
        val lines = source.lineSequence()
            .drop((options.firstLine - 1).coerceAtLeast(0))
            .take(min(options.selectedLines.coerceIn(1, 55), lineCapacity(options.aspect)))
            .toList()
            .ifEmpty { listOf("") }
        val font = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textSize = 27f
        }
        canvas.save()
        canvas.clipRect(frameLeft + 15f, top + 12f, frameRight - 17f, frameBottom - 42f)
        for ((index, raw) in lines.withIndex()) {
            val baseline = codeTop + lineHeight * index
            val number = options.firstLine + index
            if (options.highlightLine == number) {
                paint.color = palette.accent
                paint.alpha = 24
                canvas.drawRoundRect(frameLeft + 19f, baseline - 28f,
                    frameRight - 20f, baseline + 9f, 6f, 6f, paint)
                paint.alpha = 255
            }
            if (options.lineNumbers) {
                font.color = palette.muted
                font.textAlign = Paint.Align.RIGHT
                canvas.drawText(number.toString(), frameLeft + 78f, baseline, font)
            }
            font.textAlign = Paint.Align.LEFT
            val line = raw.replace("\t", "    ").take(220)
            val maxWidth = frameRight - left - 42f
            val fits = font.breakText(line, true, maxWidth, null)
            val visible = line.take(fits)
            var cursor = 0
            for (segment in ScreenshotSyntax.tokenize(visible)) {
                font.color = when (segment.kind) {
                    ScreenshotSyntax.Kind.KEYWORD -> palette.keyword
                    ScreenshotSyntax.Kind.STRING -> palette.string
                    ScreenshotSyntax.Kind.COMMENT -> palette.comment
                    ScreenshotSyntax.Kind.NUMBER -> palette.number
                    ScreenshotSyntax.Kind.PLAIN -> palette.text
                }
                canvas.drawText(segment.text, left + cursor, baseline, font)
                cursor += font.measureText(segment.text).toInt()
            }
        }
        canvas.restore()

        if (options.footer.isNotBlank()) {
            paint.color = palette.muted
            paint.textSize = 21f
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            val footer = options.footer.trim().take(62)
            canvas.drawText(footer, frameLeft + 36f, frameBottom - 20f, paint)
        }
        return image
    }
}

/** Minimal lexical colors only; not a parser and never presented as semantic highlighting. */
internal object ScreenshotSyntax {
    enum class Kind { PLAIN, KEYWORD, STRING, COMMENT, NUMBER }
    data class Segment(val text: String, val kind: Kind)

    private val tokens = Regex(
        """("(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*')|(//.*$|#.*$)|"""+
        """(\b(?:fun|val|var|let|const|class|interface|public|private|return|if|else|for|while|"""+
        """function|async|await|def|import|from|print|package|static|void|new|true|false|null|"""+
        """SELECT|FROM|WHERE|CREATE|TABLE|INSERT|INTO|UPDATE|DELETE|local|end)\b)|"""+
        """(\b\d+(?:\.\d+)?\b)""")

    fun tokenize(line: String): List<Segment> {
        val result = mutableListOf<Segment>()
        var at = 0
        for (match in tokens.findAll(line)) {
            if (match.range.first > at) result += Segment(line.substring(at, match.range.first), Kind.PLAIN)
            val kind = when {
                match.groups[1] != null -> Kind.STRING
                match.groups[2] != null -> Kind.COMMENT
                match.groups[3] != null -> Kind.KEYWORD
                else -> Kind.NUMBER
            }
            result += Segment(match.value, kind)
            at = match.range.last + 1
        }
        if (at < line.length) result += Segment(line.substring(at), Kind.PLAIN)
        return result
    }
}

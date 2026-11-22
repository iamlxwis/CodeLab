package site.lxwis.codelab

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight

/** ANSI SGR formatting for trusted console rendering; control sequences are not executed. */
internal object ConsoleAnsi {
    private val sgr = Regex("\u001B\\[([0-9;]*)m")
    private val basic = listOf(
        Color(0xff3b4654), Color(0xffe56e6e), Color(0xff7ed69b), Color(0xfff0c674),
        Color(0xff86bfff), Color(0xffc8a7ff), Color(0xff7ed4da), Color(0xffe4e8ec)
    )

    fun format(input: String, fallback: Color): AnnotatedString {
        val builder = AnnotatedString.Builder()
        var foreground = fallback
        var bold = false
        var index = 0
        fun appendRange(from: Int, to: Int) {
            if (to <= from) return
            builder.pushStyle(SpanStyle(color = foreground,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal))
            builder.append(input.substring(from, to))
            builder.pop()
        }
        for (match in sgr.findAll(input)) {
            appendRange(index, match.range.first)
            val codes = match.groupValues[1].split(";").mapNotNull(String::toIntOrNull)
                .ifEmpty { listOf(0) }
            for (code in codes) {
                when (code) {
                    0 -> { foreground = fallback; bold = false }
                    1 -> bold = true
                    22 -> bold = false
                    39 -> foreground = fallback
                    in 30..37 -> foreground = basic[code - 30]
                    in 90..97 -> foreground = basic[code - 90].copy(alpha = .85f)
                }
            }
            index = match.range.last + 1
        }
        appendRange(index, input.length)
        return builder.toAnnotatedString()
    }
}

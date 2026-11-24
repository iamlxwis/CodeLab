package site.lxwis.codelab

/**
 * A single atomic edit for indent/outdent, independent of Android's Editable.
 * Only complete logical lines are modified; a trailing selected newline never
 * causes indentation to leak onto the following unselected line.
 */
internal data class CodeIndentationEdit(
    val start: Int,
    val end: Int,
    val replacement: String,
    val selectionStart: Int,
    val selectionEnd: Int
)

internal fun codeIndentationEdit(
    content: String,
    selectionStart: Int,
    selectionEnd: Int,
    unit: String,
    spaces: Int,
    outdent: Boolean
): CodeIndentationEdit? {
    require(unit.isNotEmpty() && unit.length <= 8 && unit.all { it == '\t' || it == ' ' })
    val lower = minOf(selectionStart, selectionEnd).coerceIn(0, content.length)
    val upper = maxOf(selectionStart, selectionEnd).coerceIn(0, content.length)
    val start = content.lastIndexOf('\n', (lower - 1).coerceAtLeast(0))
        .let { if (it < 0) 0 else it + 1 }
    // The next line is excluded when a multi-line selection ends after '\n'.
    val effectiveEnd = if (upper > lower && upper > 0 && content[upper - 1] == '\n') {
        upper - 1
    } else upper
    val end = content.indexOf('\n', effectiveEnd).let {
        if (it < 0) content.length else it
    }
    val original = content.substring(start, end)
    val replacement = original.split("\n").joinToString("\n") { line ->
        if (!outdent) {
            if (line.isBlank()) line else unit + line
        } else {
            val remove = if (line.startsWith("\t")) 1
                else line.takeWhile { it == ' ' }.length.coerceAtMost(spaces.coerceIn(1, 8))
            line.drop(remove)
        }
    }
    if (replacement == original) return null
    val collapsed = lower == upper
    val caret = if (collapsed) {
        // Preserve the logical column for an indent or outdent of the current line.
        val originalLine = original.substringBefore('\n')
        val replacementLine = replacement.substringBefore('\n')
        val delta = replacementLine.length - originalLine.length
        (lower + delta).coerceIn(start, start + replacement.length)
    } else start
    return CodeIndentationEdit(
        start, end, replacement,
        caret, if (collapsed) caret else start + replacement.length
    )
}

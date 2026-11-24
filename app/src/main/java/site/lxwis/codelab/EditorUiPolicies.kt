package site.lxwis.codelab

internal object EditorChromePolicy {
    fun afterScroll(collapsed: Boolean, delta: Int): Boolean = when {
        delta > 0 -> true
        delta < 0 -> false
        else -> collapsed
    }
}
internal enum class WorkspaceSizeClass {
    Compact, Medium, Expanded;
    companion object {
        fun editorPercentRange(availableDp: Float): IntRange {
            val width = availableDp.coerceAtLeast(1f)
            val max = ((width - 180f) / width * 100f).toInt().coerceIn(35, 80)
            val min = kotlin.math.ceil(260f / width * 100f).toInt().coerceIn(35, max)
            return min..max
        }
        fun of(widthDp: Int) = when {
            widthDp < 600 -> Compact
            widthDp < 840 -> Medium
            else -> Expanded
        }
    }
}
internal object EditorSyntaxPolicy {
    fun identifierKind(source: String, start: Int, end: Int): Int? {
        if (start < 0 || end <= start || end > source.length) return null
        val first = source[start]
        if (!first.isLetter() && first != '_') return null
        var next = end
        while (next < source.length && source[next].isWhitespace()) next++
        return when {
            first.isUpperCase() -> 5
            next < source.length && source[next] == '(' -> 4
            start > 0 && source[start - 1] == '.' -> 6
            else -> null
        }
    }
    fun contrast(a: Int, b: Int): Double {
        fun luminance(c: Int): Double {
            fun channel(shift: Int): Double {
                val v = ((c ushr shift) and 255) / 255.0
                return if (v <= 0.04045) v / 12.92 else Math.pow((v + 0.055) / 1.055, 2.4)
            }
            return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
        }
        val x = luminance(a); val y = luminance(b)
        return (maxOf(x, y) + 0.05) / (minOf(x, y) + 0.05)
    }
}

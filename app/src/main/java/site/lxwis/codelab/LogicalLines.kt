package site.lxwis.codelab

/** Document line starts; visual wrapping never changes numbering. */
internal class LogicalLines private constructor(private val starts: IntArray) {
    constructor(source: String) : this(buildStarts(source))
    val count: Int get() = starts.size
    fun lineAt(offset: Int): Int {
        val index = starts.binarySearch(offset.coerceAtLeast(0))
        return if (index >= 0) index else (-index - 2).coerceAtLeast(0)
    }
    fun start(line: Int): Int = starts[line.coerceIn(starts.indices)]

    /** Apply a TextWatcher delta without scanning the full document on each keystroke. */
    fun edited(start: Int, removed: Int, inserted: CharSequence): LogicalLines {
        val end = start + removed
        val delta = inserted.length - removed
        val prefix = lineAt(start) + 1
        val suffix = lineAt(end) + 1
        val insertedLines = inserted.count { it == '\n' }
        if (prefix == suffix && insertedLines == 0 && delta == 0) return this
        val next = IntArray(prefix + insertedLines + starts.size - suffix)
        starts.copyInto(next, endIndex = prefix)
        var index = prefix
        inserted.forEachIndexed { offset, c -> if (c == '\n') next[index++] = start + offset + 1 }
        for (i in suffix until starts.size) next[index++] = starts[i] + delta
        return LogicalLines(next)
    }

    companion object {
        private fun buildStarts(source: String): IntArray {
            val result = IntArray(source.count { it == '\n' } + 1)
            var index = 1
            source.forEachIndexed { offset, c -> if (c == '\n') result[index++] = offset + 1 }
            return result
        }
    }
}

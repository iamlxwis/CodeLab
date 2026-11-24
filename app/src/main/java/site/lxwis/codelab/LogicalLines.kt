package site.lxwis.codelab

/** Map visual wrapping offsets to document lines without scanning on every draw. */
internal class LogicalLines(source: String) {
    private val starts = IntArray(source.count { it == '\n' } + 1)
    init {
        var index = 1
        source.forEachIndexed { offset, c -> if (c == '\n') starts[index++] = offset + 1 }
    }
    fun lineAt(offset: Int): Int {
        val index = starts.binarySearch(offset.coerceAtLeast(0))
        return if (index >= 0) index else (-index - 2).coerceAtLeast(0)
    }
    fun start(line: Int): Int = starts[line.coerceIn(starts.indices)]
}

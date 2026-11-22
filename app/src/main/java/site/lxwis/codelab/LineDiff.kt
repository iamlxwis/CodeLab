package site.lxwis.codelab

/**
 * Line-oriented diff for file-history comparison. Bounded dynamic programming
 * prevents large edited files from causing UI stalls.
 */
internal object LineDiff {
    enum class Kind { SAME, ADDED, REMOVED }
    data class Row(val kind: Kind, val text: String)

    fun compare(before: String, after: String, maxLines: Int = 180): List<Row> {
        val oldLines = before.split('\n')
        val newLines = after.split('\n')
        val n = oldLines.size
        val m = newLines.size
        if (n > maxLines || m > maxLines) {
            val oldHead = oldLines.take(maxLines / 2).joinToString("\n")
            val newHead = newLines.take(maxLines / 2).joinToString("\n")
            return listOf(
                Row(Kind.REMOVED, "[Large file: earlier section]"),
                Row(Kind.REMOVED, oldHead),
                Row(Kind.ADDED, "[Large file: current section]"),
                Row(Kind.ADDED, newHead)
            )
        }
        val lcs = Array(n + 1) { IntArray(m + 1) }
        for (i in n - 1 downTo 0) for (j in m - 1 downTo 0) {
            lcs[i][j] = if (oldLines[i] == newLines[j]) lcs[i + 1][j + 1] + 1
                else maxOf(lcs[i + 1][j], lcs[i][j + 1])
        }
        val result = mutableListOf<Row>()
        var i = 0
        var j = 0
        while (i < n || j < m) {
            when {
                i < n && j < m && oldLines[i] == newLines[j] -> {
                    result += Row(Kind.SAME, oldLines[i++]); j++
                }
                i < n && (j == m || lcs[i + 1][j] >= lcs[i][j + 1]) ->
                    result += Row(Kind.REMOVED, oldLines[i++])
                else -> result += Row(Kind.ADDED, newLines[j++])
            }
        }
        return result
    }
}

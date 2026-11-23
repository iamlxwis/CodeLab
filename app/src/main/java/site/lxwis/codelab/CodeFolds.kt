package site.lxwis.codelab

/** Non-destructive, brace-based folding model for the read-only fold preview. */
internal object CodeFolds {
    data class Region(val startLine: Int, val endLine: Int)
    data class Row(val line: Int, val code: String, val region: Region?, val foldedLines: Int)

    fun regions(source: String, language: String = ""): List<Region> {
        if (source.length > 2_000_000) return emptyList()
        val visible = CodeAnalysis.visibleCode(source, language)
        val stack = ArrayDeque<Pair<Int, Int>>()
        val found = mutableListOf<Region>()
        var line = 1
        for (index in source.indices) {
            val ch = source[index]
            if (visible[index]) {
                when (ch) {
                    '{' -> stack.addLast(index to line)
                    '}' -> if (stack.isNotEmpty()) {
                        val start = stack.removeLast()
                        if (line - start.second >= 2) {
                            found.add(Region(start.second, line))
                        }
                    }
                }
            }
            if (ch == '\n') line++
        }
        // Multiple opening braces on one line map to the outermost fold.
        return found.groupBy { it.startLine }
            .values.map { group -> group.maxBy { it.endLine } }
            .sortedBy { it.startLine }
            .take(300)
    }

    fun preview(source: String, regions: List<Region>, collapsed: Set<Int>): List<Row> {
        val lines = source.split('\n')
        val starts = regions.associateBy { it.startLine }
        val rows = ArrayList<Row>(lines.size)
        var line = 1
        while (line <= lines.size) {
            val region = starts[line]
            val folded = if (region != null && line in collapsed)
                (region.endLine - line - 1).coerceAtLeast(0) else 0
            rows.add(Row(line, lines[line - 1].take(400), region, folded))
            line = if (folded > 0) region!!.endLine else line + 1
        }
        return rows
    }
}

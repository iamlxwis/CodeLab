package site.lxwis.codelab

/** Non-destructive, brace-based folding model for the read-only fold preview. */
internal object CodeFolds {
    data class Region(val startLine: Int, val endLine: Int)
    data class Row(val line: Int, val code: String, val region: Region?, val foldedLines: Int)

    fun regions(source: String, language: String = ""): List<Region> {
        if (source.length > 2_000_000) return emptyList()
        if (language.lowercase() in setOf("py", "python")) return pythonRegions(source)
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

    /** Indentation-aware preview for Python suites; never edits the source text. */
    private fun pythonRegions(source: String): List<Region> {
        data class Open(val line: Int, val indent: Int)
        val stack = ArrayDeque<Open>()
        val regions = mutableListOf<Region>()
        var previousCodeLine = 0
        val lines = source.split('\n')
        fun indentOf(line: String): Int {
            var width = 0
            for (ch in line) {
                if (ch == ' ') width++
                else if (ch == '\t') width += 4
                else break
            }
            return width
        }
        lines.forEachIndexed { index, raw ->
            val line = index + 1
            val trimmed = raw.trim()
            if (trimmed.isBlank() || trimmed.startsWith("#")) return@forEachIndexed
            val indent = indentOf(raw)
            while (stack.isNotEmpty() && indent <= stack.last().indent) {
                val open = stack.removeLast()
                if (previousCodeLine - open.line >= 2) regions.add(Region(open.line, previousCodeLine))
            }
            if (trimmed.endsWith(":") && !trimmed.startsWith("#")) {
                stack.addLast(Open(line, indent))
            }
            previousCodeLine = line
        }
        while (stack.isNotEmpty()) {
            val open = stack.removeLast()
            if (previousCodeLine - open.line >= 2) regions.add(Region(open.line, previousCodeLine))
        }
        return regions.groupBy { it.startLine }.values.map { group ->
            group.maxBy { it.endLine }
        }.sortedBy { it.startLine }.take(300)
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

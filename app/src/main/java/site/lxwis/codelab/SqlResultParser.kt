package site.lxwis.codelab

/** Converts the last tab-delimited SQL query in the console into an optional read-only result grid. */
internal data class SqlTable(val columns: List<String>, val rows: List<List<String>>)

internal object SqlResultParser {
    private val summary = Regex("""^(\d+) rows?$""")

    fun parse(output: String): SqlTable? {
        val lines = output.trimEnd('\r', '\n').lines()
        val index = lines.indexOfLast { summary.matches(it.trim()) }
        if (index < 0) return null
        val count = summary.matchEntire(lines[index].trim())?.groupValues?.get(1)?.toIntOrNull() ?: return null
        if (count !in 0..300) return null
        val header = index - count - 1
        if (header < 0) return null
        val columns = lines[header].split('\t')
        if (columns.isEmpty() || columns.size > 30 || columns.any { it.isEmpty() }) return null
        val rows = lines.subList(header + 1, index).map { it.split('\t') }
        if (rows.any { it.size != columns.size }) return null
        return SqlTable(columns, rows)
    }
}

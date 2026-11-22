package site.lxwis.codelab

/**
 * Fast, offline text-level editor assistance.
 * Not a language server or compiler: no semantic type checking is implied.
 */
internal object CodeAnalysis {
    data class Symbol(val line: Int, val title: String)
    data class Issue(val line: Int, val description: String)

    private val declarations = Regex(
        """^(?:export\s+|default\s+|public\s+|private\s+|protected\s+|static\s+|async\s+|internal\s+|open\s+)*""" +
        """(class|interface|object|enum|struct|trait|fun|function|def|type)\s+([A-Za-z_][A-Za-z_0-9]*)"""
    )

    fun symbols(source: String): List<Symbol> =
        source.lineSequence().mapIndexedNotNull { index, line ->
            val match = declarations.find(line.trimStart()) ?: return@mapIndexedNotNull null
            Symbol(index + 1, match.groupValues[1] + " " + match.groupValues[2])
        }.take(120).toList()

    private fun visibleCode(source: String): BooleanArray {
        val allowed = BooleanArray(source.length)
        var mode = 0
        var i = 0
        while (i < source.length) {
            val ch = source[i]
            val next = source.getOrNull(i + 1)
            when (mode) {
                0 -> when {
                    ch == '/' && next == '/' -> { mode = 4; i++ }
                    ch == '/' && next == '*' -> { mode = 5; i++ }
                    ch == '\'' -> mode = 1
                    ch == '"' -> mode = 2
                    ch.code == 96 -> mode = 3
                    else -> allowed[i] = true
                }
                1, 2, 3 -> {
                    if (ch == '\\') i++
                    else if ((mode == 1 && ch == '\'') ||
                             (mode == 2 && ch == '"') ||
                             (mode == 3 && ch.code == 96)) mode = 0
                }
                4 -> if (ch == '\n') { mode = 0; allowed[i] = true }
                5 -> if (ch == '*' && next == '/') { mode = 0; i++ }
            }
            i++
        }
        return allowed
    }

    private val openers = mapOf('(' to ')', '[' to ']', '{' to '}')
    private val closers = openers.entries.associate { (start, end) -> end to start }

    fun matchBracket(source: String, caret: Int): Pair<Int, Int>? {
        if (source.isEmpty()) return null
        val visible = visibleCode(source)
        val indices = listOf(caret, caret - 1).filter { it in source.indices }
        val index = indices.firstOrNull { i ->
            visible[i] && (source[i] in openers || source[i] in closers)
        } ?: return null
        val ch = source[index]
        val forward = ch in openers
        val opposite = if (forward) openers.getValue(ch) else closers.getValue(ch)
        var depth = 0
        val positions = if (forward) index until source.length else index downTo 0
        for (position in positions) {
            if (!visible[position]) continue
            when (source[position]) {
                ch -> depth++
                opposite -> {
                    depth--
                    if (depth == 0) return if (forward) index to position else position to index
                }
            }
        }
        return null
    }

    /** Structural hints only; these are not compiler diagnostics. */
    fun bracketIssues(source: String): List<Issue> {
        val visible = visibleCode(source)
        val stack = ArrayDeque<Pair<Char, Int>>()
        val issues = mutableListOf<Issue>()
        fun line(at: Int): Int = source.take(at).count { it == '\n' } + 1
        for (index in source.indices) {
            if (!visible[index]) continue
            val ch = source[index]
            if (ch in openers) stack.addLast(ch to index)
            else if (ch in closers) {
                if (stack.isEmpty() || stack.last().first != closers[ch]) {
                    issues += Issue(line(index), "Unexpected '" + ch + "'")
                } else stack.removeLast()
            }
            if (issues.size >= 30) return issues
        }
        for ((ch, at) in stack.takeLast(30 - issues.size)) {
            issues += Issue(line(at), "Unclosed '" + ch + "'")
        }
        return issues
    }

    /** Suggestions are words actually used in the current buffer, never invented symbols. */
    fun wordCompletions(source: String, caret: Int): List<String> {
        val position = caret.coerceIn(0, source.length)
        var start = position
        while (start > 0 && (source[start - 1].isLetterOrDigit() || source[start - 1] == '_')) start--
        val prefix = source.substring(start, position)
        if (prefix.length < 2) return emptyList()
        return Regex("""[A-Za-z_][A-Za-z_0-9]{2,}""")
            .findAll(source).map { it.value }.filter { it != prefix && it.startsWith(prefix) }
            .distinct().take(25).toList()
    }
}

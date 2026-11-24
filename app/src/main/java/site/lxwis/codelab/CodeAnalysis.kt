package site.lxwis.codelab

/**
 * Fast, offline text-level editor assistance.
 * Not a language server or compiler: no semantic type checking is implied.
 */
internal object CodeAnalysis {
    data class Symbol(val line: Int, val title: String)
    data class Issue(val line: Int, val description: String)
    data class Reference(val index: Int, val line: Int, val preview: String)

    private val declarations = Regex(
        """^(?:export\s+|default\s+|public\s+|private\s+|protected\s+|static\s+|async\s+|internal\s+|open\s+)*""" +
        """(class|interface|object|enum|struct|trait|fun|function|def|type|val|var|let|const)\s+([A-Za-z_][A-Za-z_0-9]*)"""
    )

    fun symbols(source: String): List<Symbol> =
        source.lineSequence().mapIndexedNotNull { index, line ->
            val match = declarations.find(line.trimStart()) ?: return@mapIndexedNotNull null
            Symbol(index + 1, match.groupValues[1] + " " + match.groupValues[2])
        }.take(120).toList()

    fun identifierAt(source: String, caret: Int): String? {
        if (source.isEmpty()) return null
        val position = caret.coerceIn(0, source.length)
        val at = listOf(position, position - 1).firstOrNull {
            it in source.indices && (source[it].isLetterOrDigit() || source[it] == '_')
        } ?: return null
        var first = at
        var last = at + 1
        while (first > 0 && (source[first - 1].isLetterOrDigit() || source[first - 1] == '_')) first--
        while (last < source.length && (source[last].isLetterOrDigit() || source[last] == '_')) last++
        return source.substring(first, last).takeIf {
            it.isNotEmpty() && (it[0].isLetter() || it[0] == '_')
        }
    }

    /** Searches only visible lexical occurrences in this buffer, not semantic references. */
    fun references(source: String, identifier: String, cap: Int = 120, language: String = ""): List<Reference> {
        if (!identifier.matches(Regex("[A-Za-z_][A-Za-z_0-9]*"))) return emptyList()
        val allowed = visibleCode(source, language)
        val pattern = Regex("(?<![A-Za-z_0-9])" + Regex.escape(identifier) + "(?![A-Za-z_0-9])")
        var line = 1
        var scanned = 0
        return pattern.findAll(source).filter { allowed[it.range.first] }
            .take(cap.coerceIn(1, 500)).map { match ->
                while (scanned < match.range.first) {
                    if (source[scanned] == '\n') line++
                    scanned++
                }
                val before = source.lastIndexOf('\n', (match.range.first - 1).coerceAtLeast(0)) + 1
                val next = source.indexOf('\n', match.range.last).let {
                    if (it < 0) source.length else it
                }
                Reference(match.range.first, line,
                    source.substring(before, next).trim().take(100))
            }.toList()
    }

    fun definition(source: String, identifier: String): Reference? {
        if (!identifier.matches(Regex("[A-Za-z_][A-Za-z_0-9]*"))) return null
        // The outline is capped at 120 entries for the UI; definition lookup must not be.
        var offset = 0
        source.lineSequence().forEachIndexed { index, line ->
            val match = declarations.find(line.trimStart())
            if (match?.groupValues?.get(2) == identifier) {
                return Reference(offset + line.indexOfFirst { !it.isWhitespace() }.coerceAtLeast(0),
                    index + 1, match.groupValues[1] + " " + identifier)
            }
            offset += line.length + 1
        }
        return null
    }

    /**
     * Lexical rename: skips strings/comments. Does not resolve scopes or imports.
     * Intended as a transparent file-local edit rather than LSP rename.
     */
    fun lexicalRename(source: String, old: String, replacement: String, language: String = ""): String {
        require(replacement.matches(Regex("[A-Za-z_][A-Za-z_0-9]*"))) {
            "New name must be a valid identifier."
        }
        if (old == replacement) return source
        if (!old.matches(Regex("[A-Za-z_][A-Za-z_0-9]*"))) return source
        // Rename every visible occurrence. The UI reference list is capped for
        // responsiveness, but applying that cap here silently corrupts large edits.
        val allowed = visibleCode(source, language)
        val pattern = Regex("(?<![A-Za-z_0-9])" + Regex.escape(old) + "(?![A-Za-z_0-9])")
        val builder = StringBuilder(source.length)
        var copiedThrough = 0
        for (match in pattern.findAll(source)) {
            if (!allowed[match.range.first]) continue
            builder.append(source, copiedThrough, match.range.first)
            builder.append(replacement)
            copiedThrough = match.range.last + 1
        }
        if (copiedThrough == 0) return source
        builder.append(source, copiedThrough, source.length)
        return builder.toString()
    }

    /** File-local lexical mask. Handles common comment syntaxes, not a full parser. */
    internal fun visibleCode(source: String, language: String = ""): BooleanArray {
        val allowed = BooleanArray(source.length)
        val ext = language.lowercase()
        val hashComments = ext in setOf("py", "rb", "sh", "bash", "yaml", "yml", "toml", "r")
        val dashComments = ext in setOf("lua", "sql")
        val slashComments = !hashComments && !dashComments
        var mode = 0
        var i = 0
        while (i < source.length) {
            val ch = source[i]
            val next = source.getOrNull(i + 1)
            when (mode) {
                0 -> when {
                    hashComments && ch == '#' -> mode = 4
                    dashComments && ch == '-' && next == '-' -> { mode = 4; i++ }
                    slashComments && ch == '/' && next == '/' -> { mode = 4; i++ }
                    slashComments && ch == '/' && next == '*' -> { mode = 5; i++ }
                    ch == '\'' && source.startsWith("'''", i) -> { mode = 6; i += 2 }
                    ch == '"' && source.startsWith("\"\"\"", i) -> { mode = 7; i += 2 }
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
                6, 7 -> {
                    val closing = if (mode == 6) "'''" else "\"\"\""
                    if (source.startsWith(closing, i)) { mode = 0; i += 2 }
                    else if (ch == '\\') i++
                }
            }
            i++
        }
        return allowed
    }

    private val openers = mapOf('(' to ')', '[' to ']', '{' to '}')
    private val closers = openers.entries.associate { (start, end) -> end to start }

    fun matchBracket(source: String, caret: Int, language: String = ""): Pair<Int, Int>? {
        if (source.isEmpty()) return null
        val visible = visibleCode(source, language)
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
    fun bracketIssues(source: String, language: String = ""): List<Issue> {
        val visible = visibleCode(source, language)
        val stack = ArrayDeque<Pair<Char, Int>>()
        val issues = mutableListOf<Issue>()
        var line = 1
        for (index in source.indices) {
            val ch = source[index]
            if (visible[index]) {
                if (ch in openers) stack.addLast(ch to line)
                else if (ch in closers) {
                    if (stack.isEmpty() || stack.last().first != closers[ch]) {
                        issues += Issue(line, "Unexpected '" + ch + "'")
                    } else stack.removeLast()
                }
                if (issues.size >= 30) return issues
            }
            if (ch == '\n') line++
        }
        for ((ch, atLine) in stack.takeLast(30 - issues.size)) {
            issues += Issue(atLine, "Unclosed '" + ch + "'")
        }
        return issues
    }

    /** Ranked file-local completions, excluding tokens inside comments and quoted strings. */
    fun wordCompletions(source: String, caret: Int, language: String = ""): List<String> {
        val position = caret.coerceIn(0, source.length)
        var start = position
        while (start > 0 && (source[start - 1].isLetterOrDigit() || source[start - 1] == '_')) start--
        val prefix = source.substring(start, position)
        if (prefix.length < 2 || !prefix[0].isLetter() && prefix[0] != '_') return emptyList()
        // Keep suggestion lookup bounded for huge documents; prioritize nearby context.
        val windowStart = (position - 120_000).coerceAtLeast(0)
        val windowEnd = (position + 40_000).coerceAtMost(source.length)
        val visible = visibleCode(source.substring(windowStart, windowEnd), language)
        val candidates = LinkedHashMap<String, Triple<Int, Int, Boolean>>()
        // When completing obj.met, prioritize properties/methods seen after a dot.
        // This remains lexical: it does not infer object types or promise semantic accuracy.
        val memberContext = start > 0 && source[start - 1] == '.'
        val pattern = Regex("""[A-Za-z_][A-Za-z_0-9]{2,}""")
        for (match in pattern.findAll(source, windowStart)) {
            if (match.range.first >= windowEnd) break
            val word = match.value
            if (word == prefix || !word.startsWith(prefix) ||
                !visible[match.range.first - windowStart]) continue
            val distance = kotlin.math.abs(match.range.first - position)
            val current = candidates[word]
            // Sort by closest occurrence, breaking ties by repeated use and name.
            candidates[word] = Triple(
                minOf(distance, current?.first ?: Int.MAX_VALUE),
                (current?.second ?: 0) + 1,
                (current?.third ?: false) || source.getOrNull(match.range.first - 1) == '.'
            )
        }
        return candidates.entries.sortedWith(
            compareByDescending<Map.Entry<String, Triple<Int, Int, Boolean>>> {
                memberContext && it.value.third
            }.thenBy { it.value.first }
                .thenByDescending { it.value.second }
                .thenBy { it.key }
        ).take(25).map { it.key }
    }

    /** Show nearby lexical hits first, without including strings or comments. */
    fun nearbyReferences(source: String, identifier: String, caret: Int,
        cap: Int = 120, language: String = ""): List<Reference> =
        references(source, identifier, cap = 500, language = language)
            .sortedWith(compareBy<Reference> {
                kotlin.math.abs(it.index - caret.coerceIn(0, source.length))
            }.thenBy { it.index }).take(cap.coerceIn(1, 500))

    /** Bounded preview of the exact lines that lexical rename would affect. */
    fun renamePreview(source: String, identifier: String, language: String = "",
        limit: Int = 8): List<Reference> =
        references(source, identifier, cap = limit.coerceIn(1, 500), language = language)

    /** Complete-file count for explicit user confirmation, not the capped reference UI. */
    fun renameCount(source: String, identifier: String, language: String = ""): Int {
        if (!identifier.matches(Regex("[A-Za-z_][A-Za-z_0-9]*"))) return 0
        val visible = visibleCode(source, language)
        val pattern = Regex("(?<![A-Za-z_0-9])" + Regex.escape(identifier) + "(?![A-Za-z_0-9])")
        return pattern.findAll(source).count { visible[it.range.first] }
    }
}

package site.lxwis.codelab

/**
 * Small local formatter: no network, native dependency or runtime initialization.
 * Preserves quoted strings; not intended to replace a full language parser.
 */
internal object CodeFormatter {
    fun format(filename: String, source: String): String? {
        if (source.isBlank()) return source
        return when (filename.substringAfterLast('.', "").lowercase()) {
            "json" -> json(source)
            "js", "mjs", "jsx", "ts", "tsx", "css" -> braces(source)
            "html", "htm" -> html(source)
            "sql" -> sql(source)
            else -> null
        }
    }

    private fun json(source: String): String {
        val out = StringBuilder(source.length + 64)
        var indent = 0
        var quoted = false
        var escaped = false
        fun newline() {
            while (out.isNotEmpty() && out.last() == ' ') out.deleteCharAt(out.lastIndex)
            if (out.isNotEmpty() && out.last() != '\n') out.append('\n')
            repeat(indent.coerceIn(0, 32)) { out.append("  ") }
        }
        for (c in source.trim()) {
            if (quoted) {
                out.append(c)
                if (escaped) escaped = false
                else if (c == '\\') escaped = true
                else if (c == '"') quoted = false
            } else when (c) {
                '"' -> { quoted = true; out.append(c) }
                '{', '[' -> { out.append(c); indent++; newline() }
                '}', ']' -> {
                    indent = (indent - 1).coerceAtLeast(0)
                    while (out.isNotEmpty() && out.last().isWhitespace()) out.deleteCharAt(out.lastIndex)
                    if (out.isNotEmpty() && out.last() != '{' && out.last() != '[') newline()
                    out.append(c)
                }
                ',' -> { out.append(c); newline() }
                ':' -> out.append(": ")
                else -> if (!c.isWhitespace()) out.append(c)
            }
        }
        return out.toString().trimEnd() + "\n"
    }

    private fun braces(source: String): String {
        val lines = mutableListOf<String>()
        val current = StringBuilder()
        var depth = 0
        var parens = 0
        var quote: Char? = null
        var escaped = false
        var lineComment = false
        fun flush() {
            val value = current.toString().trim()
            if (value.isNotBlank()) lines.add("  ".repeat(depth.coerceIn(0, 32)) + value)
            current.setLength(0)
        }
        for (c in source) {
            if (quote != null) {
                current.append(c)
                if (escaped) escaped = false
                else if (c == '\\') escaped = true
                else if (c == quote) quote = null
            } else if (lineComment) {
                if (c == '\n') { flush(); lineComment = false } else current.append(c)
            } else when (c) {
                '"', '\'' -> { quote = c; current.append(c) }
                '(' -> { parens++; current.append(c) }
                ')' -> { parens = (parens - 1).coerceAtLeast(0); current.append(c) }
                '{' -> { current.append(if (current.isNotEmpty() && current.last() != ' ') " {" else "{"); flush(); depth++ }
                '}' -> {
                    flush()
                    depth = (depth - 1).coerceAtLeast(0)
                    current.append('}')
                    flush()
                }
                ';' -> {
                    current.append(c)
                    if (parens == 0) flush()
                }
                '\n' -> flush()
                else -> {
                    if (c.isWhitespace()) {
                        if (current.isNotEmpty() && current.last() != ' ') current.append(' ')
                    } else current.append(c)
                }
            }
            // Keep line-comment detection lightweight and do not touch strings.
            if (quote == null && current.length >= 2 && current.endsWith("//")) lineComment = true
        }
        flush()
        return lines.joinToString("\n").trimEnd() + "\n"
    }

    private fun html(source: String): String {
        val out = mutableListOf<String>()
        val voidTags = setOf("area", "base", "br", "col", "hr", "img", "input", "link", "meta", "source")
        var depth = 0
        for (match in Regex("(?s)<!--.*?-->|<[^>]+>|[^<]+").findAll(source)) {
            val token = match.value.trim()
            if (token.isBlank()) continue
            val closing = token.startsWith("</")
            if (closing) depth = (depth - 1).coerceAtLeast(0)
            out.add("  ".repeat(depth.coerceIn(0, 32)) + token)
            if (token.startsWith("<") && !closing && !token.startsWith("<!") &&
                !token.startsWith("<?") && !token.endsWith("/>")) {
                val tag = Regex("^<([A-Za-z][\\w:-]*)").find(token)?.groupValues?.get(1)?.lowercase()
                if (tag != null && tag !in voidTags) depth++
            }
        }
        return out.joinToString("\n").trimEnd() + "\n"
    }

    private fun sql(source: String): String {
        val keywords = setOf("select", "from", "where", "insert", "into", "values",
            "update", "set", "delete", "create", "table", "join", "on", "and", "or",
            "order", "group", "by", "limit", "as", "distinct", "primary", "key")
        val out = StringBuilder(source.length + 32)
        var i = 0
        var quote: Char? = null
        while (i < source.length) {
            val c = source[i]
            if (quote != null) {
                out.append(c)
                if (c == quote) {
                    if (source.getOrNull(i + 1) == quote) { out.append(c); i++ }
                    else quote = null
                }
            } else if (c == '\'' || c == '"') { quote = c; out.append(c) }
            else if (c.isLetter()) {
                val begin = i
                while (i < source.length && (source[i].isLetter() || source[i] == '_')) i++
                val word = source.substring(begin, i)
                out.append(if (word.lowercase() in keywords) word.uppercase() else word)
                continue
            } else if (c == ';') {
                out.append(';')
                if (source.drop(i + 1).isNotBlank()) out.append('\n')
            } else if (c == '\r' || c == '\n') {
                if (out.isNotEmpty() && out.last() != '\n') out.append('\n')
            } else out.append(c)
            i++
        }
        return out.toString().trimEnd() + "\n"
    }
}

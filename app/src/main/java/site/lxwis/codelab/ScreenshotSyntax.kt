package site.lxwis.codelab

/** Minimal lexical colors only; not a parser and never presented as semantic highlighting. */
internal object ScreenshotSyntax {
    enum class Kind { PLAIN, KEYWORD, STRING, COMMENT, NUMBER }
    data class Segment(val text: String, val kind: Kind)

    data class Token(val range: IntRange, val kind: Kind)

    private val hashComments = setOf("", "py", "rb", "sh", "toml", "yaml", "yml", "r", "pl")
    private val dashComments = setOf("lua", "sql")
    private val compiled = java.util.concurrent.ConcurrentHashMap<String, Regex>()
    private val strings = """["]{3}[\s\S]*?(?:["]{3}|$)|'{3}[\s\S]*?(?:'{3}|$)|""" +
        """`(?:\\.|[^`\\])*+`?|"(?:\\.|[^"\\\n])*+"?|'(?:\\.|[^'\\\n])*+'?"""
    private val keywords = """\b(?:fun|val|var|let|const|class|interface|public|private|return|if|else|for|while|""" +
        """function|async|await|def|import|from|print|package|static|void|new|true|false|null|""" +
        """SELECT|FROM|WHERE|CREATE|TABLE|INSERT|INTO|UPDATE|DELETE|local|end)\b"""
    private val numbers = """\b(?:0[xX][0-9a-fA-F]+|\d+(?:\.\d+)?)\b"""

    fun ranges(source: String, filename: String): Sequence<Token> {
        val extension = filename.substringAfterLast('.', "").lowercase()
        val group = when {
            extension == "lua" -> "lua"
            extension in hashComments -> "hash"
            extension in dashComments -> "dash"
            else -> "ordinary"
        }
        val matcher = compiled.getOrPut(group) {
            val comments = buildList {
                if (group == "lua") add("""--\[\[[\s\S]*?(?:\]\]|$)""")
                if (group == "lua" || group == "dash") add("""--[^\n]*""")
                if (group == "hash") add("""\#[^\n]*""")
                add("""<!--[\s\S]*?(?:-->|$)""")
                add("""/\*[\s\S]*?(?:\*/|$)""")
                add("""//[^\n]*""")
            }.joinToString("|")
            Regex("($comments)|($strings)|($keywords)|($numbers)")
        }
        return matcher.findAll(source).map { match ->
            val kind = when {
                match.groups[1] != null -> Kind.COMMENT
                match.groups[2] != null -> Kind.STRING
                match.groups[3] != null -> Kind.KEYWORD
                else -> Kind.NUMBER
            }
            Token(match.range, kind)
        }
    }

    fun tokenize(line: String): List<Segment> {
        val result = mutableListOf<Segment>()
        var at = 0
        for (token in ranges(line, "")) {
            if (token.range.first > at) result += Segment(line.substring(at, token.range.first), Kind.PLAIN)
            result += Segment(line.substring(token.range), token.kind)
            at = token.range.last + 1
        }
        if (at < line.length) result += Segment(line.substring(at), Kind.PLAIN)
        return result
    }
}

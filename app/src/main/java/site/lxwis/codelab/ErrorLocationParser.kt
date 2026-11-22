package site.lxwis.codelab

import java.util.Locale

internal data class SourceLineReference(val path: String, val line: Int)

/**
 * Recognize source positions emitted by Rhino, Lua, BeanShell and common compilers.
 * Never guess a file from an unrecognized filename.
 */
internal fun parseErrorLocations(
    output: String,
    active: String?,
    files: List<String>
): List<SourceLineReference> {
    val filePosition = Regex("""([A-Za-z0-9_./-]+\.[A-Za-z0-9]+)[:#](\d{1,6})""")
    val linePosition = Regex("""(?i)\bline\s*:?\s*(\d{1,6})\b""")
    val luaPosition = Regex("""\[string "[^"]*"]:(\d{1,6}):""")
    val references = LinkedHashSet<SourceLineReference>()
    for (line in output.lineSequence()) {
        if (references.size >= 6) break
        val found = filePosition.find(line)
        if (found != null) {
            val named = found.groupValues[1].removePrefix("./")
            val exact = files.firstOrNull { it == named }
            val matched = exact ?: files.singleOrNull { it.endsWith("/" + named) }
            val number = found.groupValues[2].toIntOrNull()
            if (matched != null && number != null && number > 0)
                references.add(SourceLineReference(matched, number))
        } else if (active != null && active in files &&
            listOf("error", "exception", "syntax", "trace", "failed").any {
                it in line.lowercase(Locale.ROOT)
            }) {
            val number = (linePosition.find(line) ?: luaPosition.find(line))
                ?.groupValues?.getOrNull(1)?.toIntOrNull()
            if (number != null && number > 0)
                references.add(SourceLineReference(active, number))
        }
    }
    return references.toList()
}

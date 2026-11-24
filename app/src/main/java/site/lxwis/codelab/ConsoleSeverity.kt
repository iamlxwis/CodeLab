package site.lxwis.codelab

/** Runtime errors carry `! `; warning writers carry an explicit readable prefix. */
internal enum class ConsoleSeverity { Output, Warning, Error }
internal fun consoleSeverity(line: String): ConsoleSeverity = when {
    line.startsWith("! ") -> ConsoleSeverity.Error
    line.startsWith("Warning: ") -> ConsoleSeverity.Warning
    else -> ConsoleSeverity.Output
}

internal fun filterConsoleOutput(transcript: String, query: String, severity: String): String {
    if (query.isBlank() && severity == "All") return transcript
    return transcript.lineSequence().filter { line ->
        (query.isBlank() || line.contains(query, ignoreCase = true)) && when (severity) {
            "Errors" -> consoleSeverity(line) == ConsoleSeverity.Error
            "Warnings" -> consoleSeverity(line) == ConsoleSeverity.Warning
            else -> true
        }
    }.joinToString("\n")
}

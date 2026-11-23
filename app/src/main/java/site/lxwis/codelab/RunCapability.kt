package site.lxwis.codelab

/** Execution availability is distinct from syntax editing support. */
internal object RunCapability {
    enum class Mode { LOCAL, PREVIEW, ONLINE, UNAVAILABLE }
    data class Target(val mode: Mode, val description: String)
    fun forPath(path: String, cloudExtensions: Set<String> = emptySet()): Target {
        val extension = path.substringAfterLast('/').substringAfterLast('.', "").lowercase()
        if (extension in setOf("html", "htm"))
            return Target(Mode.PREVIEW, "Offline web preview")
        val local = mapOf("js" to "Rhino JavaScript", "py" to "CPython",
            "lua" to "LuaJ", "java" to "Java interpreter beta", "sql" to "SQLite")
        local[extension]?.let { return Target(Mode.LOCAL, "Offline · " + it) }
        if (extension.isNotEmpty() && extension in cloudExtensions)
            return Target(Mode.ONLINE, "Online · requires approval")
        val planned = when (extension) {
            "ts", "tsx" -> "TypeScript compiler not installed"
            "c", "cpp", "cc", "cxx" -> "C/C++ compiler pack not installed"
            "kt", "kts" -> "Kotlin compiler pack not installed"
            "php" -> "PHP interpreter pack not installed"
            "rb" -> "Ruby interpreter pack not installed"
            else -> "No compatible execution engine"
        }
        return Target(Mode.UNAVAILABLE, planned)
    }
}

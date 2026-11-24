package site.lxwis.codelab

/** Execution availability is distinct from syntax editing support. */
internal object RunCapability {
    enum class Mode { LOCAL, PREVIEW, ONLINE, UNAVAILABLE }
    data class Target(val mode: Mode, val description: String)
    fun forPath(path: String, cloudExtensions: Set<String> = emptySet(),
        installedModules: Set<String> = emptySet()): Target {
        val extension = path.substringAfterLast('/').substringAfterLast('.', "").lowercase()
        if (extension in setOf("html", "htm"))
            return Target(Mode.PREVIEW, "Offline web preview")
        if (extension == "py") {
            if ("py" in installedModules) return Target(Mode.LOCAL, "Offline · Python module")
            if ("py" in cloudExtensions) return Target(Mode.ONLINE,
                "Online · Python offline module not installed")
            return Target(Mode.UNAVAILABLE, "Python module not installed")
        }
        if (extension == "ts") {
            if ("ts" in installedModules) return Target(Mode.LOCAL, "Offline · TypeScript module")
            if ("ts" in cloudExtensions) return Target(Mode.ONLINE, "Online · TypeScript compiler")
            return Target(Mode.UNAVAILABLE, "TypeScript module not installed")
        }
        val local = mapOf("js" to "JavaScript",
            "lua" to "Lua", "java" to "Java interpreter beta", "sql" to "SQLite")
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

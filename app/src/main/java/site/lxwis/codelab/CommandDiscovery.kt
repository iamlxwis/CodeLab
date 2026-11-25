package site.lxwis.codelab

import java.util.Locale

internal data class PaletteCommand(
    val id: String, val label: String,
    val shortcut: String? = null,
    val requiresProject: Boolean = false,
    val requiresFile: Boolean = false
)

internal val workspaceCommandCatalog = listOf(
    PaletteCommand("newproject", "Create project"),
    PaletteCommand("newfile", "Create file", requiresProject = true),
    PaletteCommand("importzip", "Import project ZIP"),
    PaletteCommand("importmany", "Import multiple files", requiresProject = true),
    PaletteCommand("exportproject", "Export project backup", requiresProject = true),
    PaletteCommand("switchproject", "Switch project", requiresProject = true),
    PaletteCommand("workspacecommands", "Offline workspace commands", requiresProject = true),
    PaletteCommand("newfolder", "Create folder", requiresProject = true),
    PaletteCommand("files", "Browse files", requiresProject = true),
    PaletteCommand("run-active", "Run active file", "Ctrl+Enter", requiresFile = true),
    PaletteCommand("save", "Save file", "Ctrl+S", requiresFile = true),
    PaletteCommand("projectsearch", "Search project", requiresProject = true),
    PaletteCommand("projectreplace", "Replace across project", requiresProject = true),
    PaletteCommand("screenshot", "Snippet Studio", requiresFile = true),
    PaletteCommand("history", "File history", requiresFile = true),
    PaletteCommand("git", "Git version control", requiresProject = true),
    PaletteCommand("commands", "Command palette", "Ctrl+Shift+P"),
    PaletteCommand("devtools", "Developer tools"),
    PaletteCommand("languages", "Language manager"),
    PaletteCommand("appearance", "Appearance"),
    PaletteCommand("accessibility", "Accessibility"),
    PaletteCommand("performance", "Performance diagnostics", requiresProject = true),
    PaletteCommand("projectsettings", "Project settings", requiresProject = true),
    PaletteCommand("panels", "Panel layout", requiresProject = true),
    PaletteCommand("trash", "Recently deleted")
)

private fun fuzzyActionScore(query: String, candidate: String): Int? {
    val needle = query.lowercase(Locale.ROOT).filterNot(Char::isWhitespace)
    if (needle.isEmpty()) return 0
    val haystack = candidate.lowercase(Locale.ROOT)
    if (haystack.startsWith(needle)) return 1000 - haystack.length
    val contiguous = haystack.indexOf(needle)
    if (contiguous >= 0) return 800 - contiguous
    var index = 0
    var score = 0
    var previous = -2
    for ((position, character) in haystack.withIndex()) {
        if (index >= needle.length) break
        if (character != needle[index]) continue
        score += 20
        if (position == previous + 1) score += 8
        if (position == 0 || !haystack[position - 1].isLetterOrDigit()) score += 14
        previous = position
        index++
    }
    return if (index == needle.length) score else null
}

internal fun rankWorkspaceCommands(
    query: String,
    commands: List<PaletteCommand>,
    recent: List<String>
): List<PaletteCommand> = commands.mapNotNull { command ->
    val score = listOfNotNull(
        fuzzyActionScore(query.trim(), command.label),
        fuzzyActionScore(query.trim(), command.id.replace('-', ' ')),
        command.shortcut?.let { fuzzyActionScore(query.trim(), it) }
    ).maxOrNull() ?: return@mapNotNull null
    val index = recent.indexOf(command.id)
    val bonus = if (index < 0) 0 else 80 - index.coerceAtMost(7) * 10
    command to score + bonus
}.sortedWith(compareByDescending<Pair<PaletteCommand, Int>> { it.second }
    .thenBy { it.first.label }).map { it.first }

internal fun updatedQuickActionIds(existing: List<String>, id: String): List<String> =
    if (id in existing) existing.filterNot { it == id }
    else if (existing.size < 6) existing + id else existing

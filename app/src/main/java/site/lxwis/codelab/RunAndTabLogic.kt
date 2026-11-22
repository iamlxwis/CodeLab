package site.lxwis.codelab

/** Prefer configured entrypoints only when the file still exists. */
internal fun resolveRunTarget(
    preferred: String,
    active: String?,
    files: List<String>
): String? = preferred.takeIf { it.isNotBlank() && it in files }
    ?: active?.takeIf { it in files }

/** Move one tab without losing the order of its neighbors. */
internal fun reorderOpenTab(tabs: List<String>, path: String, offset: Int): List<String> {
    val from = tabs.indexOf(path)
    if (from < 0 || offset == 0) return tabs
    val to = (from + offset).coerceIn(0, tabs.lastIndex)
    if (from == to) return tabs
    return tabs.toMutableList().apply { removeAt(from); add(to, path) }
}

/** Keep pinned paths attached to their files when renaming or moving. */
internal fun remapPinnedPaths(
    pinned: Set<String>,
    oldPath: String,
    replacement: String?,
    directory: Boolean = false
): Set<String> {
    val old = oldPath.removeSuffix("/")
    val next = replacement?.removeSuffix("/")
    return pinned.mapNotNull { path ->
        if (directory && path.startsWith("$old/")) {
            next?.let { "$it/" + path.removePrefix("$old/") }
        } else if (!directory && path == oldPath) replacement
        else path
    }.toSet()
}

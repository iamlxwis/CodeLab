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

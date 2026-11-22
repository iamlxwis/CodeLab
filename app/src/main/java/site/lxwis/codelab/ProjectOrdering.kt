package site.lxwis.codelab

/**
 * Never filter by favorites when the user selects Favorites: sorting is
 * reversible and all projects must remain discoverable.
 */
internal fun orderedProjects(
    projects: List<String>,
    favorites: Set<String>,
    sort: String
): List<String> = when (sort) {
    "Favorites" -> projects.sortedWith(
        compareBy<String> { it !in favorites }.thenBy(String.CASE_INSENSITIVE_ORDER) { it }
    )
    "A–Z" -> projects.sortedWith(String.CASE_INSENSITIVE_ORDER)
    "Z–A" -> projects.sortedWith(String.CASE_INSENSITIVE_ORDER.reversed())
    else -> projects.sortedWith(compareBy<String> { it !in favorites })
}

package site.lxwis.codelab

/**
 * ProjectStore supplies newest-first order. Favorites is a true filter, not
 * a sort that leaves unstarred projects visible.
 */
internal fun orderedProjects(
    projects: List<String>,
    favorites: Set<String>,
    sort: String
): List<String> = when (sort) {
    "Favorites" -> projects.filter { it in favorites }
    "A–Z" -> projects.sortedWith(String.CASE_INSENSITIVE_ORDER)
    "Z–A" -> projects.sortedWith(String.CASE_INSENSITIVE_ORDER.reversed())
    else -> projects // Recent follows storage order, not pinned favorites.
}

/** The switcher keeps recent order within each group without O(n²) index lookups. */
internal fun projectSwitcherOrder(projects: List<String>, favorites: Set<String>): List<String> {
    val (starred, others) = projects.partition { it in favorites }
    return starred + others
}

package site.lxwis.codelab

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectOrderingTest {
    private val names = listOf("Zulu", "Alpha", "Bravo")
    private val favorites = setOf("Bravo")

    @Test fun favoritesSortKeepsNonFavoriteProjects() {
        assertEquals(listOf("Bravo", "Alpha", "Zulu"),
            orderedProjects(names, favorites, "Favorites"))
    }

    @Test fun recentPreservesExistingOrderWithinFavoriteGroups() {
        assertEquals(listOf("Bravo", "Zulu", "Alpha"),
            orderedProjects(names, favorites, "Recent"))
    }

    @Test fun alphabeticalSortsAreCaseInsensitive() {
        assertEquals(listOf("Alpha", "Bravo", "Zulu"),
            orderedProjects(names, favorites, "A–Z"))
        assertEquals(listOf("Zulu", "Bravo", "Alpha"),
            orderedProjects(names, favorites, "Z–A"))
    }
}

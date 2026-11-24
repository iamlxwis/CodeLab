package site.lxwis.codelab

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectOrderingTest {
    private val names = listOf("Zulu", "Alpha", "Bravo")
    private val favorites = setOf("Bravo")

    @Test fun favoritesOnlyShowsStarredProjects() {
        assertEquals(listOf("Bravo"), orderedProjects(names, favorites, "Favorites"))
        assertEquals(emptyList<String>(), orderedProjects(names, emptySet(), "Favorites"))
    }

    @Test fun unstarImmediatelyRemovesProjectFromFavoritesView() {
        assertEquals(listOf("Zulu", "Bravo"),
            orderedProjects(names, setOf("Zulu", "Bravo"), "Favorites"))
        assertEquals(listOf("Zulu"),
            orderedProjects(names, setOf("Zulu"), "Favorites"))
    }

    @Test fun switcherGroupsFavoritesWithoutLosingRecentOrder() {
        assertEquals(listOf("Bravo", "Zulu", "Alpha"),
            projectSwitcherOrder(names, favorites))
        assertEquals(names, projectSwitcherOrder(names, emptySet()))
        assertEquals(listOf("Zulu", "Bravo", "Alpha"),
            projectSwitcherOrder(names, setOf("Zulu", "Bravo")))
    }

    @Test fun recentKeepsNewFirstOrderWithoutPinningFavorites() {
        assertEquals(names, orderedProjects(names, favorites, "Recent"))
    }

    @Test fun alphabeticalSortsAreCaseInsensitive() {
        assertEquals(listOf("Alpha", "Bravo", "Zulu"),
            orderedProjects(names, favorites, "A–Z"))
        assertEquals(listOf("Zulu", "Bravo", "Alpha"),
            orderedProjects(names, favorites, "Z–A"))
    }
}

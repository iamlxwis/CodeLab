package site.lxwis.codelab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandDiscoveryTest {
    @Test fun fuzzySearchMatchesAbbreviations() {
        val ranked = rankWorkspaceCommands("srch prj", workspaceCommandCatalog, emptyList())
        assertEquals("projectsearch", ranked.first().id)
        assertTrue(rankWorkspaceCommands("zzzzzz", workspaceCommandCatalog, emptyList()).isEmpty())
    }

    @Test fun recentsRiseToTopForEmptyQuery() {
        val ranked = rankWorkspaceCommands("", workspaceCommandCatalog, listOf("git"))
        assertEquals("git", ranked.first().id)
    }

    @Test fun shortcutsAndProjectBackupActionsAreDiscoverable() {
        assertEquals("save", rankWorkspaceCommands("Ctrl+S",
            workspaceCommandCatalog, emptyList()).first().id)
        assertEquals("exportproject", rankWorkspaceCommands("project backup",
            workspaceCommandCatalog, emptyList()).first().id)
        assertTrue(workspaceCommandCatalog.any { it.id == "importmany" && it.requiresProject })
    }

    @Test fun quickActionsAreUniqueAndLimitedToSix() {
        val initial = listOf("a", "b")
        assertEquals(listOf("a", "b", "c"), updatedQuickActionIds(initial, "c"))
        assertEquals(listOf("b"), updatedQuickActionIds(initial, "a"))
        assertEquals(listOf("a","b","c","d","e","f"),
            updatedQuickActionIds(listOf("a","b","c","d","e","f"), "g"))
    }
}

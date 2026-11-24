package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject

class WorkspaceLayoutTest {
    @Test fun roundTripRestoresToolbarAndExplorer() {
        val original = WorkspaceLayout("My layout", true, 45, 70, false, "Compact",
            listOf("files", "commands"), listOf("wrap", "undo", "run"), false)
        assertEquals(listOf(original), WorkspaceLayout.decode(WorkspaceLayout.encode(listOf(original))))
    }
    @Test fun legacyLayoutPreservesCurrentToolbarAndDefaultsExplorer() {
        val old = JSONObject("""{"name":"Old","split":true,"phone":50,"tablet":70,"status":true,"density":"Comfortable","actions":["files"]}""")
        val layout = requireNotNull(WorkspaceLayout.from(old))
        assertNull(layout.toolbar)
        assertTrue(layout.explorer)
    }
    @Test fun emptyToolbarIsDifferentFromLegacyAndInvalidActionsAreRejected() {
        val empty = WorkspaceLayout("Empty", false, 60, 60, true, "Comfortable", emptyList(), emptyList(), true)
        assertEquals(emptyList<String>(), WorkspaceLayout.from(empty.json())!!.toolbar)
        val corrupted = JSONObject("""{"name":"Imported","toolbar":["undo","bad","run","undo"],"phone":0,"tablet":100}""")
        val decoded = requireNotNull(WorkspaceLayout.from(corrupted))
        assertEquals(listOf("undo", "run"), decoded.toolbar)
        assertEquals(35, decoded.phonePercent)
        assertEquals(80, decoded.tabletPercent)
    }
    @Test fun focusPreferencePersistsWithoutChangingLegacyLayouts() {
        val focus = WorkspaceLayout("Focus", false, 60, 60, true, "Compact", emptyList(), focus = true)
        assertEquals(true, WorkspaceLayout.decode(WorkspaceLayout.encode(listOf(focus))).single().focus)
        assertNull(WorkspaceLayout.from(JSONObject("""{"name":"Old"}"""))?.focus)
        assertEquals(false, WorkspaceLayout.from(focus.copy(focus = false).json())?.focus)
    }
}

package site.lxwis.codelab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UndoDeletionTest {
    private fun item(id: String, project: String, path: String) =
        ProjectStore.TrashItem(id, project, path, "file", 0)

    @Test fun onlyNewMatchingTrashRecordCanBeUndone() {
        val old = item("old", "Demo", "readme.md")
        val ours = item("new", "Demo", "src/main.js")
        val unrelated = item("newer", "Elsewhere", "src/main.js")
        assertEquals(ours, newTrashItem(listOf(old), listOf(unrelated, ours, old),
            "Demo", "src/main.js"))
        assertNull(newTrashItem(listOf(old), listOf(old), "Demo", "src/main.js"))
    }

    @Test fun failsClosedOnAmbiguousProjectOrPath() {
        assertNull(newTrashItem(emptyList(), listOf(item("x", "A", "b")), "B", "b"))
        assertNull(newTrashItem(emptyList(), listOf(item("x", "A", "b")), "A", "c"))
    }
}

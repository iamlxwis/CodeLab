package site.lxwis.codelab
import org.junit.Assert.*
import org.junit.Test

class EditorToolbarOrderTest {
    @Test fun storedOrderFiltersInvalidEntriesAndPreservesExplicitEmptySelection() {
        assertEquals(listOf("run", "undo"), EditorToolbarOrder.decode("run,bad,undo,run"))
        assertEquals(emptyList<String>(), EditorToolbarOrder.decode(""))
        assertEquals(EditorToolbarOrder.default, EditorToolbarOrder.decode(null))
    }
    @Test fun movesDoNotLoseActions() {
        assertEquals(listOf("redo","undo","find"), EditorToolbarOrder.move(listOf("undo","redo","find"),"redo",-1))
        assertEquals(listOf("undo"), EditorToolbarOrder.move(listOf("undo"),"undo",1))
    }
}

package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class EditorUiPoliciesTest {
    @Test fun chromeTracksScrollDirection() {
        assertTrue(EditorChromePolicy.afterScroll(false, 15))
        assertFalse(EditorChromePolicy.afterScroll(true, -15))
        assertTrue(EditorChromePolicy.afterScroll(true, 0))
    }
    @Test fun widthThresholds() {
        assertEquals(WorkspaceSizeClass.Compact, WorkspaceSizeClass.of(599))
        assertEquals(WorkspaceSizeClass.Medium, WorkspaceSizeClass.of(600))
        assertEquals(WorkspaceSizeClass.Medium, WorkspaceSizeClass.of(839))
        assertEquals(WorkspaceSizeClass.Expanded, WorkspaceSizeClass.of(840))
    }
    @Test fun splitWidths() {
        val medium = WorkspaceSizeClass.editorPercentRange(552f)
        assertTrue(medium.first * 552f / 100f >= 260f)
        assertTrue((100 - medium.last) * 552f / 100f >= 180f)
        assertTrue(!WorkspaceSizeClass.editorPercentRange(1f).isEmpty())
    }
    @Test fun identifiers() {
        assertEquals(4, EditorSyntaxPolicy.identifierKind("render (x)", 0, 6))
        assertEquals(5, EditorSyntaxPolicy.identifierKind("Widget", 0, 6))
        assertEquals(6, EditorSyntaxPolicy.identifierKind("obj.name", 4, 8))
        assertNull(EditorSyntaxPolicy.identifierKind("value", 0, 5))
    }
    @Test fun contrastFormula() {
        assertEquals(21.0, EditorSyntaxPolicy.contrast(0xff000000.toInt(), 0xffffffff.toInt()), 0.001)
        assertEquals(1.0, EditorSyntaxPolicy.contrast(0xff101517.toInt(), 0xff101517.toInt()), 0.001)
    }
}

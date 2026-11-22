package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class VisibleHighlightInvalidationTest {
    @Test fun editsBeforeOrWithinVisibleWindowInvalidate() {
        assertTrue(shouldRehighlightVisible(1000, 8000, 0))
        assertTrue(shouldRehighlightVisible(1000, 8000, 1000))
        assertTrue(shouldRehighlightVisible(1000, 8000, 7999))
    }
    @Test fun editsAfterVisibleWindowDoNotRequeueHighlighting() {
        assertFalse(shouldRehighlightVisible(1000, 8000, 8000))
        assertFalse(shouldRehighlightVisible(1000, 8000, 256000))
    }
    @Test fun unknownOrInvalidViewportAlwaysRecomputes() {
        assertTrue(shouldRehighlightVisible(-1, -1, 25000))
        assertTrue(shouldRehighlightVisible(10, 5, 100))
        assertTrue(shouldRehighlightVisible(1000, 2000, -1))
    }
}

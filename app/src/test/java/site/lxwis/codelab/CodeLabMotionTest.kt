package site.lxwis.codelab

import org.junit.Assert.assertEquals
import org.junit.Test

class CodeLabMotionTest {
    @Test fun reducedAlwaysDisablesMotion() {
        assertEquals(0, CodeLabMotion(resolveMotionScale("Reduced", 100, 1f)).millis(170))
        assertEquals(0, CodeLabMotion(resolveMotionScale("Reduced", 100, 3f)).millis(170))
    }
    @Test fun systemAccessibilitySettingCannotBeOverridden() {
        assertEquals(0, CodeLabMotion(resolveMotionScale("Full", 100, 0f)).millis(170))
        assertEquals(0, CodeLabMotion(resolveMotionScale("System", 100, 0f)).millis(170))
        assertEquals(0, CodeLabMotion(resolveMotionScale("Full", 0, 1f)).millis(170))
    }
    @Test fun intensityAndGlobalScaleControlDuration() {
        val slow = CodeLabMotion(resolveMotionScale("Full", 100, 2f)).millis(170)
        val normal = CodeLabMotion(resolveMotionScale("Full", 100, 1f)).millis(170)
        val restrained = CodeLabMotion(resolveMotionScale("System", 50, 1f)).millis(170)
        assertEquals(340, slow)
        assertEquals(170, normal)
        assert(slow > normal && normal > restrained)
        assertEquals(0, CodeLabMotion(1f).millis(-5))
        assertEquals(900, CodeLabMotion(4f).millis(300))
    }
    @Test fun unusualSettingsFailSafe() {
        assertEquals(0f, resolveMotionScale("System", 100, Float.NaN))
        assertEquals(0f, resolveMotionScale("Full", 100, -1f))
        assertEquals(3f, resolveMotionScale("Full", 100, 10f))
    }
}

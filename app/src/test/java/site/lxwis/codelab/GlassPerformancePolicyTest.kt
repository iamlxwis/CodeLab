package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class GlassPerformancePolicyTest {
    @Test fun respectsPlatformPowerAndManualFallbackAcrossQualities() {
        for (quality in listOf("Auto", "High", "Lightweight")) {
            assertFalse(glassBlurEnabled(30, quality, false, false))
            assertFalse(glassBlurEnabled(35, quality, false, true))
        }
        assertFalse(glassBlurEnabled(35, "Auto", true, false))
        assertFalse(glassBlurEnabled(35, "Lightweight", false, false))
        assertTrue(glassBlurEnabled(35, "Auto", false, false))
        assertTrue(glassBlurEnabled(35, "High", true, false))
    }
}

package site.lxwis.codelab

import org.junit.Assert.assertEquals
import org.junit.Test

class GlassPerformancePolicyTest {
    @Test fun androidApiAndPerformanceControlsChooseSupportedRendering() {
        assertEquals(GlassRendering.Frosted, render(api = 30))
        assertEquals(GlassRendering.Blur, render(api = 31))
        assertEquals(GlassRendering.Refraction, render(api = 33))
        assertEquals(GlassRendering.Frosted, render(powerSave = true))
        assertEquals(GlassRendering.Frosted, render(reduced = true))
        assertEquals(GlassRendering.Frosted, render(lowRam = true))
        assertEquals(GlassRendering.Refraction, render(lowRam = true, optimize = false))
        assertEquals(GlassRendering.Frosted, render(accelerated = false))
        assertEquals(GlassRendering.Solid, render(highContrast = true))
        assertEquals(GlassRendering.Solid, render(enabled = false))
    }

    private fun render(
        api: Int = 35, enabled: Boolean = true, highContrast: Boolean = false, accelerated: Boolean = true,
        lowRam: Boolean = false, powerSave: Boolean = false, reduced: Boolean = false, optimize: Boolean = true
    ) = glassRendering(api, enabled, highContrast, accelerated, lowRam, powerSave, reduced, optimize)
}

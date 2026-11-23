package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class OfflineRuntimeRegistryTest {
    @Test fun onlyAdvertisesExistingBundledEngines() {
        assertNotNull(OfflineRuntimeRegistry.manifest("js"))
        assertNotNull(OfflineRuntimeRegistry.manifest("lua"))
        assertNotNull(OfflineRuntimeRegistry.manifest("java"))
        assertNotNull(OfflineRuntimeRegistry.manifest("sql"))
        assertNotNull(OfflineRuntimeRegistry.manifest("py"))
        assertNull(OfflineRuntimeRegistry.manifest("rust"))
    }
    @Test fun capabilitiesAreExplained() {
        val js = OfflineRuntimeRegistry.manifest("js")!!
        assertEquals("Mozilla Rhino", js.engine)
        assertTrue(js.limitations.contains("Node.js"))
    }
}

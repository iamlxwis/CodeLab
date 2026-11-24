package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class InstalledRuntimeStateTest {
    @Test fun onlyVerifiedCompanionsEnableOptionalExecution() {
        val ready = PythonModuleLocator.Status(PythonModuleLocator.State.READY, "verified")
        val rejected = PythonModuleLocator.Status(PythonModuleLocator.State.UNTRUSTED, "untrusted")
        val state = InstalledRuntimeState(ready, mapOf(
            OptionalRuntimeLocator.Pack.TYPESCRIPT to ready,
            OptionalRuntimeLocator.Pack.JAVASCRIPT to rejected))
        assertEquals(setOf("py", "ts"), state.installedExtensions)
        assertEquals(RunCapability.Mode.LOCAL,
            RunCapability.forPath("main.ts", installedModules = state.installedExtensions).mode)
        assertTrue(InstalledRuntimeState(rejected, emptyMap()).installedExtensions.isEmpty())
    }
}

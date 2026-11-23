package site.lxwis.codelab

import org.junit.Assert.assertEquals
import org.junit.Test

class PythonModuleVersionPolicyTest {
    @Test fun firstInstallationAndGenuineUpdate() {
        assertEquals(PythonModuleVersionPolicy.Decision.NEW_INSTALL,
            PythonModuleVersionPolicy.decide(1288, null))
        assertEquals(PythonModuleVersionPolicy.Decision.UPDATE,
            PythonModuleVersionPolicy.decide(1288, 1286))
    }

    @Test fun equalVersionMayBeReinstalledButNeverAdvertisedAsAnUpdate() {
        assertEquals(PythonModuleVersionPolicy.Decision.REINSTALL,
            PythonModuleVersionPolicy.decide(1288, 1288))
    }

    @Test fun olderDownloadMustNotReplaceNewerInstalledModule() {
        assertEquals(PythonModuleVersionPolicy.Decision.DOWNGRADE,
            PythonModuleVersionPolicy.decide(1286, 1288))
    }

    @Test fun zeroAndNegativeVersionCodesAreRejected() {
        for (downloaded in listOf(0L, -1L)) {
            assertEquals(PythonModuleVersionPolicy.Decision.INVALID,
                PythonModuleVersionPolicy.decide(downloaded, null))
        }
        assertEquals(PythonModuleVersionPolicy.Decision.INVALID,
            PythonModuleVersionPolicy.decide(1L, 0L))
    }

    @Test fun longVersionCodeDoesNotTruncate() {
        assertEquals(PythonModuleVersionPolicy.Decision.UPDATE,
            PythonModuleVersionPolicy.decide(4294967299L, 4294967297L))
        assertEquals(PythonModuleVersionPolicy.Decision.DOWNGRADE,
            PythonModuleVersionPolicy.decide(4294967297L, 4294967299L))
    }
}

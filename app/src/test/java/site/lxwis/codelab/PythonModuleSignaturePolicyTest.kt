package site.lxwis.codelab

import org.junit.Assert.assertEquals
import org.junit.Test

class PythonModuleSignaturePolicyTest {
    @Test fun freshMatchingPackIsTrusted() {
        assertEquals(PythonModuleSignaturePolicy.State.TRUSTED,
            PythonModuleSignaturePolicy.check(setOf("permanent"), setOf("permanent"), null))
    }

    @Test fun existingSameSignerPackMayBeUpdated() {
        assertEquals(PythonModuleSignaturePolicy.State.TRUSTED,
            PythonModuleSignaturePolicy.check(setOf("permanent"), setOf("permanent"), setOf("permanent")))
    }

    @Test fun installedDebugPackMustBeRemovedBeforeProductionInstall() {
        assertEquals(PythonModuleSignaturePolicy.State.INSTALLED_UNTRUSTED,
            PythonModuleSignaturePolicy.check(setOf("permanent"), setOf("permanent"), setOf("debug")))
    }

    @Test fun alteredDownloadOrAbsentHostCertificateFailsClosed() {
        assertEquals(PythonModuleSignaturePolicy.State.DOWNLOAD_UNTRUSTED,
            PythonModuleSignaturePolicy.check(setOf("permanent"), setOf("different"), null))
        assertEquals(PythonModuleSignaturePolicy.State.DOWNLOAD_UNTRUSTED,
            PythonModuleSignaturePolicy.check(setOf("permanent"), emptySet(), null))
        assertEquals(PythonModuleSignaturePolicy.State.HOST_UNKNOWN,
            PythonModuleSignaturePolicy.check(emptySet(), emptySet(), null))
    }
}

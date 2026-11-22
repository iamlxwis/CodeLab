package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class DeveloperTransformsTest {
    @Test fun roundTripBase64AndUri() {
        val phrase = "CodeLab 2.0 ✓"
        val b64 = DeveloperTransforms.apply("Base64 encode", phrase)
        assertEquals(phrase, DeveloperTransforms.apply("Base64 decode", b64))
        val uri = DeveloperTransforms.apply("URL encode", phrase)
        assertEquals(phrase, DeveloperTransforms.apply("URL decode", uri))
    }
    @Test fun hashingIsDeterministic() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            DeveloperTransforms.apply("SHA-256 hash", "abc"))
    }
}

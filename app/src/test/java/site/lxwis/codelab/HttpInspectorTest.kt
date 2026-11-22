package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class HttpInspectorTest {
    @Test fun acceptsOnlyHttpsUrlWithoutEmbeddedCredentials() {
        assertEquals("api.example.com",
            HttpInspector.validate("https://api.example.com/v1/data").host)
        for (bad in listOf("http://example.com", "file:///tmp/a", "https://user:pass@example.com",
            "https://example.com/#x", "https://localhost/index")) {
            assertTrue(bad, runCatching { HttpInspector.validate(bad) }.isFailure)
        }
    }
}

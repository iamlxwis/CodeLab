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

    @Test fun rejectsLocalIpLiteralsAndLocalDomains() {
        for (bad in listOf("https://192.168.1.2/api", "https://10.0.0.1",
            "https://[::1]/", "https://router.local/", "https://host.localhost/")) {
            assertTrue(bad, runCatching { HttpInspector.validate(bad) }.isFailure)
        }
    }

    @Test fun acceptsValidHeadersAndRejectsSmugglingAndTransportOverrides() {
        assertEquals(listOf("Content-Type" to "application/json",
            "Authorization" to "Bearer example"),
            HttpInspector.parseHeaders("Content-Type: application/json\nAuthorization: Bearer example"))
        for (bad in listOf("Host: localhost", "Content-Length: 999",
            "X-Thing: a\r\nHost: localhost", "Broken Header",
            "X-Id: one\nx-id: two", "X-Token: ok\n".repeat(17))) {
            assertTrue(bad, runCatching { HttpInspector.parseHeaders(bad) }.isFailure)
        }
    }

    @Test fun requestValidatesBodyAndMethodBeforeNetworkAccess() {
        assertTrue(runCatching {
            HttpInspector.request(HttpInspector.Request("PATCH", "https://example.com"))
        }.isFailure)
        assertTrue(runCatching {
            HttpInspector.request(HttpInspector.Request("GET", "https://example.com",
                body = "secret"))
        }.isFailure)
        assertTrue(runCatching {
            HttpInspector.request(HttpInspector.Request("POST", "https://example.com",
                body = "x".repeat(65537)))
        }.isFailure)
    }
}

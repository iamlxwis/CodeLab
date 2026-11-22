package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class LanguageCatalogTest {
    @Test fun catalogIsLargeUniqueAndSearchable() {
        assertTrue(LanguageCatalog.entries.size >= 90)
        assertEquals(LanguageCatalog.entries.size, LanguageCatalog.entries.map { it.name }.distinct().size)
        assertEquals(LanguageCatalog.entries.size, LanguageCatalog.entries.map { it.extension }.distinct().size)
        assertEquals("zig", LanguageCatalog.extensionFor("Zig"))
        assertEquals("vue", LanguageCatalog.extensionFor("Vue"))
        assertEquals("py", LanguageCatalog.extensionFor("Python"))
        assertNull(LanguageCatalog.extensionFor("Unknown language"))
    }

    @Test fun editingDoesNotClaimOfflineExecution() {
        assertNull(LanguageCatalog.entries.first { it.name == "Zig" }.local)
        assertNotNull(LanguageCatalog.entries.first { it.name == "JavaScript" }.local)
    }

    @Test fun extraTemplatesProduceCorrectSourceFiles() {
        val root = java.nio.file.Files.createTempDirectory("language-catalog-test").toFile()
        try {
            val store = ProjectStore(root)
            store.createProject("Zig project", "Zig")
            assertEquals(listOf("main.zig"), store.files("Zig project"))
            store.createProject("Svelte project", "Svelte")
            assertEquals(listOf("main.svelte"), store.files("Svelte project"))
            store.createProject("Docker", "Dockerfile")
            assertEquals(listOf("Dockerfile"), store.files("Docker"))
        } finally {
            root.deleteRecursively()
        }
    }
}

package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class LanguageManagerPresentationTest {
    private fun row(
        downloadable: Boolean = false,
        moduleReady: Boolean = false,
        bundledReady: Boolean = false,
        onlineReady: Boolean = false,
        preview: Boolean = false,
        activeDownload: Boolean = false,
        moduleProblem: Boolean = false
    ) = LanguageManagerPresentation.describe(downloadable, moduleReady,
        bundledReady, onlineReady, preview, activeDownload, moduleProblem)

    @Test fun sixFrozenPacksSortFirstInExpectedOrder() {
        assertEquals(listOf("Python", "JavaScript", "Lua", "Java", "TypeScript", "SQLite"),
            listOf("Java", "Rust", "Lua", "SQLite", "Python", "TypeScript", "JavaScript")
                .sortedBy(LanguageManagerPresentation::priority).take(6))
    }

    @Test fun optionalPackDoesNotLookInstalledJustBecauseFallbackRunsOffline() {
        val entry = row(downloadable = true, bundledReady = true)
        assertEquals("Download", entry.label)
        assertTrue(entry.detail.contains("Built-in"))
        assertEquals("Installed", row(downloadable = true, moduleReady = true).label)
    }

    @Test fun failedVerificationAndActiveDownloadAreVisible() {
        assertEquals("Check", row(downloadable = true, moduleProblem = true).label)
        assertEquals("Downloading", row(downloadable = true, activeDownload = true).label)
    }

    @Test fun editingSupportIsNotPresentedAsOfflineExecution() {
        assertEquals("Edit only", row().label)
        assertEquals("Online", row(onlineReady = true).label)
        assertEquals("Preview", row(preview = true).label)
    }
}

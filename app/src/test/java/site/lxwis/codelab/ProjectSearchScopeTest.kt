package site.lxwis.codelab

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectSearchScopeTest {
    private val files = listOf("src/main.js", "src/site.html", "docs/readme.md",
        "src/assets/theme.css", "main.lua")

    @Test fun folderFilterMatchesPrefixWithoutBroadeningResults() {
        assertEquals(listOf("src/main.js", "src/site.html", "src/assets/theme.css"),
            scopedProjectFiles(files, "src/", null))
    }

    @Test fun typeAndFolderFiltersIntersect() {
        assertEquals(listOf("src/main.js"),
            scopedProjectFiles(files, "src/", ProjectFileKind.Code))
        assertEquals(emptyList<String>(),
            scopedProjectFiles(files, "docs/", ProjectFileKind.Code))
    }
}

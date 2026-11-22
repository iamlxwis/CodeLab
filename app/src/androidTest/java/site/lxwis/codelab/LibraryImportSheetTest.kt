package site.lxwis.codelab

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LibraryImportSheetTest {
    @get:Rule val compose = createComposeRule()

    @Test fun zipAndGithubRemainExplicitlyDiscoverable() {
        var source = ""
        compose.setContent {
            MaterialTheme {
                LibraryImportSheet(
                    onDismiss = {},
                    onZip = { source = "zip" },
                    onGitHub = { source = "github" }
                )
            }
        }
        compose.onNodeWithText("ZIP archive").performClick()
        assertEquals("zip", source)
        compose.onNodeWithText("GitHub repository").performClick()
        assertEquals("github", source)
    }
}

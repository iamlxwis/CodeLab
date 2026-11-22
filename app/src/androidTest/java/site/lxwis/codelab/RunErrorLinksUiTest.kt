package site.lxwis.codelab

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RunErrorLinksUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun errorChipOpensItsExactFileAndLine() {
        var location: SourceLineReference? = null
        compose.setContent {
            MaterialTheme {
                RunErrorLinks(listOf(SourceLineReference("src/main.js", 7))) { path, line ->
                    location = SourceLineReference(path, line)
                }
            }
        }
        compose.onNodeWithText("Open main.js · line 7").performClick()
        compose.runOnIdle {
            assertEquals(SourceLineReference("src/main.js", 7), location)
        }
    }
}

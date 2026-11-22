package site.lxwis.codelab

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SearchScopeUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun folderPickerDoesNotRequireFreeformPathTyping() {
        var folder = ""
        compose.setContent {
            MaterialTheme {
                SearchScopeFilters(listOf("src/", "docs/"), folder, null,
                    onFolder = { folder = it }, onKind = {})
            }
        }
        compose.onNodeWithText("All folders").performClick()
        compose.onNodeWithText("src/").performClick()
        compose.runOnIdle { assertEquals("src/", folder) }
    }
}

package site.lxwis.codelab

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class PreviewControlsUiTest {
    @get:Rule val compose = createComposeRule()
    @Test fun viewportSelectionAndLogToggleAreIndependent() {
        var viewport by mutableStateOf("Auto")
        var logs by mutableStateOf(true)
        compose.setContent {
            MaterialTheme { PreviewControlBar(viewport, { viewport = it }, logs, { logs = it }, 2) }
        }
        compose.onNodeWithText("Auto").assertIsSelected()
        compose.onNodeWithText("Phone").performClick().assertIsSelected()
        compose.onNodeWithContentDescription("Log").performClick()
        compose.runOnIdle {
            org.junit.Assert.assertEquals("Phone", viewport)
            org.junit.Assert.assertFalse(logs)
        }
    }
}

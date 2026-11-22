package site.lxwis.codelab

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WorkspaceTabMenuTest {
    @get:Rule val compose = createComposeRule()

    @Test fun pinAndCloseOtherActionsRemainAccessible() {
        var pinned = false
        var closedOthers = false
        compose.setContent {
            MaterialTheme {
                WorkspaceTab("main.js", active = true, pinned = pinned,
                    dirty = true, motionMillis = 0,
                    onSelect = {}, onClose = {},
                    onTogglePin = { pinned = true },
                    onCloseOthers = { closedOthers = true }, onCloseAll = {},
                    onMove = {}, onReveal = {})
            }
        }
        compose.onNodeWithContentDescription("Tab actions for main.js").performClick()
        compose.onNodeWithText("Pin tab").performClick()
        compose.runOnIdle { assertEquals(true, pinned) }
        compose.onNodeWithContentDescription("Tab actions for main.js").performClick()
        compose.onNodeWithText("Close others").performClick()
        compose.runOnIdle { assertEquals(true, closedOthers) }
    }
}

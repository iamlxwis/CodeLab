package site.lxwis.codelab

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WorkspacePanelsUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun allThreeModesRemainAccessibleAtNarrowWidth() {
        val selected = mutableStateOf("Editor")
        compose.setContent {
            MaterialTheme {
                WorkspacePanels(selected.value, Modifier.width(280.dp)) { selected.value = it }
            }
        }
        compose.onNodeWithText("Editor").assertIsSelected()
        compose.onNodeWithText("Console").assertIsDisplayed().performClick().assertIsSelected()
        compose.onNodeWithText("Split").assertIsDisplayed().performClick().assertIsSelected()
    }

    @Test fun backgroundTabHasAnAccessibleCloseButton() {
        var closed = false
        compose.setContent {
            MaterialTheme {
                WorkspaceTab("other.js", active = false, pinned = false,
                    dirty = false, motionMillis = 0,
                    onSelect = {}, onClose = { closed = true },
                    onTogglePin = {}, onCloseOthers = {}, onCloseAll = {},
                    onMove = {}, onReveal = {})
            }
        }
        compose.onNodeWithContentDescription("Close other.js").performClick()
        compose.runOnIdle { assertTrue(closed) }
    }

    @Test fun longFileNameKeepsCloseActionInsideNarrowTabViewport() {
        var closed = false
        val path = "a-very-long-source-file-name-that-must-ellipsis.js"
        compose.setContent {
            MaterialTheme { Box(Modifier.width(180.dp)) {
                WorkspaceTab(path, active = true, pinned = false, dirty = true, motionMillis = 0,
                    onSelect = {}, onClose = { closed = true }, onTogglePin = {},
                    onCloseOthers = {}, onCloseAll = {}, onMove = {}, onReveal = {}, maxWidth = 180.dp)
            } }
        }
        compose.onNodeWithContentDescription("Close $path").assertIsDisplayed().performClick()
        compose.runOnIdle { assertTrue(closed) }
    }

    @Test fun splitDividerExposesAReadableResizeTarget() {
        compose.setContent {
            MaterialTheme { PhonePanelDivider(60, 900f, {}) }
        }
        compose.onNodeWithContentDescription("Resize split panels").assertExists()
    }
}

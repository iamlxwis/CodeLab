package site.lxwis.codelab

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ExecutionChooserUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun unavailableOnlineCannotRunAndCancelDoesNotExecute() {
        val events = mutableListOf<String>()
        compose.setContent {
            MaterialTheme {
                ExecutionChooser("main.js", "Offline",
                    ExecutionOptions("Run locally.", "Internet connection required.", true, false),
                    cancel = { events += "cancel" }, install = { events += it }, run = { events += it })
            }
        }
        compose.onNodeWithText("Online").performClick()
        compose.onNodeWithText("Run").assertIsNotEnabled()
        compose.runOnIdle { assertEquals(emptyList<String>(), events) }
        compose.onNodeWithText("Cancel").performClick()
        compose.runOnIdle { assertEquals(listOf("cancel"), events) }
    }

    @Test fun installedModeRequiresExplicitRunAndMissingPackOpensManagement() {
        val events = mutableListOf<String>()
        compose.setContent {
            MaterialTheme {
                ExecutionChooser("main.py", "Offline",
                    ExecutionOptions("Python runtime module not installed", "Requires approval to upload source.",
                        false, true, "Python"),
                    cancel = { events += "cancel" }, install = { events += it }, run = { events += it })
            }
        }
        compose.onNodeWithText("Run").assertIsNotEnabled()
        compose.onNodeWithText("Manage Python").performClick()
        compose.onNodeWithText("Online").performClick()
        compose.runOnIdle { assertEquals(listOf("Python"), events) }
        compose.onNodeWithText("Run").assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(listOf("Python", "Online"), events) }
    }
}

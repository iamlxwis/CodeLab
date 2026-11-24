package site.lxwis.codelab

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class EditorChromeUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun languageIsNoninteractiveStatusAndVisibilityPreferenceWorks() {
        val showStatus = mutableStateOf(true)
        compose.setContent { MaterialTheme {
            CodeEditor("chrome-test", "main.js", "const answer = 42;", {},
                showStatusBar = showStatus.value, motionMillis = 0)
        } }
        compose.onNodeWithText("UTF-8 · JavaScript", substring = true).assertHasNoClickAction()
        compose.runOnIdle { showStatus.value = false }
        compose.onNodeWithText("UTF-8 · JavaScript", substring = true).assertDoesNotExist()
    }

    @Test fun unpinnedToolbarActionsRemainInOverflow() {
        compose.setContent { MaterialTheme {
            CodeEditor("chrome-overflow", "main.js", "const answer = 42;", {},
                workspaceToolbar = emptyList(), motionMillis = 0)
        } }
        compose.onNodeWithContentDescription("Editor actions").performClick()
        compose.onNodeWithText("Undo").assertExists()
        compose.onNodeWithText("Redo").assertExists()
    }
}

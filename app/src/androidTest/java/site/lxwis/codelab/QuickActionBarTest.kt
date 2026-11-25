package site.lxwis.codelab

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QuickActionBarTest {
    @get:Rule val compose = createComposeRule()

    @Test fun pinnedActionRemainsTappableInWorkspaceActions() {
        var selected = ""
        compose.setContent {
            MaterialTheme {
                WorkspaceActionsSheet(projectOpen = true, fileOpen = true, multipleProjects = false,
                    javascriptFile = true, focusMode = false, quickActions = listOf("files", "projectsearch"),
                    onToggleQuick = {}, onAction = { selected = it }, onDismiss = {})
            }
        }
        compose.onNodeWithText("Browse files").performClick()
        compose.runOnIdle { assertEquals("files", selected) }
    }
}

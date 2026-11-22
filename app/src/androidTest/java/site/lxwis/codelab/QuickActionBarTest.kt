package site.lxwis.codelab

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QuickActionBarTest {
    @get:Rule val compose = createComposeRule()

    @Test fun chosenQuickActionIsTappable() {
        var selected = ""
        compose.setContent {
            MaterialTheme {
                QuickActionsBar(listOf("files", "projectsearch")) { selected = it }
            }
        }
        compose.onNodeWithContentDescription("Explorer").performClick()
        compose.runOnIdle { assertEquals("files", selected) }
    }
}

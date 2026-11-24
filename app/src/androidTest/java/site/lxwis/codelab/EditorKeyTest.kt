package site.lxwis.codelab

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertHasClickAction
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class EditorKeyTest {
    @get:Rule val compose = createComposeRule()
    @Test fun arrowKeyIsAccessibleAndClickable() {
        var clicks = 0
        compose.setContent { MaterialTheme { EditorKey("←", 0, false) { clicks++ } } }
        compose.onNodeWithContentDescription("Move cursor left").assertHasClickAction().performClick()
        compose.runOnIdle { assertEquals(1, clicks) }
    }
}

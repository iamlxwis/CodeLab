package site.lxwis.codelab

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class ClaySurfaceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun surfaceKeepsItsTextVisibleToAccessibility() {
        compose.setContent {
            MaterialTheme {
                ClayPanel { Text("Local projects") }
            }
        }
        compose.onNodeWithText("Local projects").assertIsDisplayed()
    }
}

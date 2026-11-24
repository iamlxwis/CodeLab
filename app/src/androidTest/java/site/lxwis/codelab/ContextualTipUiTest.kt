package site.lxwis.codelab

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ContextualTipUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun compactTipKeepsDetailsGuideAndPersistentDismissalAccessible() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("feature-discovery", Context.MODE_PRIVATE)
        val hadEnabled = prefs.contains("enabled")
        val beforeEnabled = prefs.getBoolean("enabled", true)
        val id = "qa-compact-tip-${System.nanoTime()}"
        prefs.edit().putBoolean("enabled", true).commit()
        var opened = false
        try {
            compose.setContent { MaterialTheme {
                ContextualTip(id, "Workspace tips", "Detailed guidance") { opened = true }
            } }
            compose.onNodeWithText("Workspace tips").assertExists()
            compose.onNodeWithText("Detailed guidance").assertDoesNotExist()
            compose.onNodeWithContentDescription("Show Workspace tips details").performClick()
            compose.onNodeWithText("Detailed guidance").assertExists()
            compose.onNodeWithText("Open guide").performClick()
            compose.runOnIdle { assertTrue(opened) }
            compose.onNodeWithContentDescription("Dismiss Workspace tips tip").performClick()
            compose.onNodeWithText("Workspace tips").assertDoesNotExist()
            assertTrue(prefs.getBoolean(id, false))
        } finally {
            prefs.edit().remove(id).apply {
                if (hadEnabled) putBoolean("enabled", beforeEnabled) else remove("enabled")
            }.commit()
        }
    }
}

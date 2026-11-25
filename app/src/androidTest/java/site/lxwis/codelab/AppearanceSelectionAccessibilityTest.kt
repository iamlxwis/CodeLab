package site.lxwis.codelab

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class AppearanceSelectionAccessibilityTest {
    @get:Rule val compose = createComposeRule()

    @Test fun allThreeModesExposeSelectionAndCanBeChanged() {
        var chosen by mutableStateOf("System")
        compose.setContent {
            MaterialTheme { SettingChoices("Color mode", appAppearanceModes, chosen) { chosen = it } }
        }
        for (name in appAppearanceModes) compose.onNodeWithText(name).assertExists()
        compose.onNodeWithText("System").assertIsSelected()
        compose.onNodeWithText("Light").performClick()
        compose.onNodeWithText("Light").assertIsSelected()
        compose.onNodeWithText("Dark").performClick()
        compose.onNodeWithText("Dark").assertIsSelected()
    }
}

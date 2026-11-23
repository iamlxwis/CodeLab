package site.lxwis.codelab

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class AppearanceAccentAccessibilityTest {
    @get:Rule val compose = createComposeRule()

    @Test fun sixAccentsSpeakTheirNamesAndSelectionUpdates() {
        var chosen by mutableStateOf("Blue")
        compose.setContent {
            MaterialTheme {
                AppearanceAccentSwatches(chosen) { chosen = it }
            }
        }
        for (name in listOf("Mint", "Blue", "Violet", "Amber", "Rose", "Mono")) {
            compose.onNodeWithContentDescription("$name accent").assertExists()
        }
        compose.onNodeWithContentDescription("Blue accent").assertIsSelected()
        compose.onNodeWithContentDescription("Mint accent").performClick()
        compose.onNodeWithContentDescription("Mint accent").assertIsSelected()
    }
}

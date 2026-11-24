package site.lxwis.codelab

import android.app.Application
import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ReimaginedSettingsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun searchedMotionPageAppliesPresetsAndPersistsTransition() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val model = WorkspaceModel(app)
        val prefs = app.getSharedPreferences("appearance", Context.MODE_PRIVATE)
        val before = prefs.all.toMap()
        try {
            compose.setContent { MaterialTheme { ReimaginedSettings(model, {}, {}) } }
            compose.onNodeWithContentDescription("Search settings").performTextInput("motion")
            compose.onNodeWithText("Motion & Haptics").performClick()
            compose.onNodeWithText("Reduced").performClick()
            compose.runOnIdle { assertEquals("Reduced", model.motionPreference) }
            compose.onNodeWithText("Fluid").performClick()
            compose.onNodeWithText("Slide").performScrollTo().performClick()
            compose.runOnIdle {
                assertEquals("Fluid", model.motionPreference)
                assertEquals(60, model.springIntensity)
                assertEquals("Slide", WorkspaceModel(app).transitionEffect)
            }
            compose.onNodeWithContentDescription("Back to settings").performClick()
            compose.onNodeWithContentDescription("Search settings").assertExists()
        } finally {
            // Restore only keys changed by this test; no unrelated project data is touched.
            val edit = prefs.edit()
            for (key in listOf("motion", "motion-intensity", "motion-speed", "spring-intensity", "transition-effect")) {
                when (val value = before[key]) {
                    is String -> edit.putString(key, value)
                    is Int -> edit.putInt(key, value)
                    else -> edit.remove(key)
                }
            }
            edit.commit()
        }
    }
}

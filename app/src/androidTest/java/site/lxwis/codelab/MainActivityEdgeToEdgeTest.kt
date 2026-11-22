package site.lxwis.codelab

import android.graphics.Color
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityEdgeToEdgeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun workspaceRootAndTransparentStatusBar() {
        compose.onRoot().assertExists()
        compose.activityRule.scenario.onActivity { activity ->
            @Suppress("DEPRECATION")
            val statusBar = activity.window.statusBarColor
            assertEquals(Color.TRANSPARENT, statusBar)
        }
    }
}

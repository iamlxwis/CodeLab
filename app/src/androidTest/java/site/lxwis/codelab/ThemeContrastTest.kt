package site.lxwis.codelab

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemeContrastTest {
    @get:Rule val compose = createComposeRule()

    @Test fun lightAndDarkUseNeutralBackgroundsAndLegibleForeground() {
        var mode by mutableStateOf("Dark")
        var text = Color.Unspecified
        var background = Color.Unspecified
        var darkAppearance = false
        compose.setContent {
            CodeLabTheme(mode, false, false) { dark ->
                CodeLabForegroundSurface(Modifier.fillMaxSize()) {
                    val foreground = LocalContentColor.current
                    val canvas = MaterialTheme.colorScheme.background
                    SideEffect { text = foreground; background = canvas; darkAppearance = dark }
                    Text("CodeLab")
                }
            }
        }
        for ((name, expected) in listOf("Dark" to Color.Black, "Light" to Color.White)) {
            compose.runOnIdle { mode = name }
            compose.runOnIdle {
                assertEquals(expected, background)
                assertEquals(name == "Dark", darkAppearance)
                val bright = maxOf(text.luminance(), background.luminance())
                val dim = minOf(text.luminance(), background.luminance())
                assertTrue((bright + .05f) / (dim + .05f) >= 4.5f)
            }
        }
    }

    @Test fun editorConfigurationDoesNotRecolorAppSurfaces() {
        var background = Color.Unspecified
        var editorMode = ""
        compose.setContent {
            CodeLabTheme("Light", true, false,
                EditorAppearance(editorMode = "Dark", editorBackground = "#212224")) {
                val current = MaterialTheme.colorScheme.background
                val editor = LocalEditorAppearance.current
                SideEffect { background = current; editorMode = editor.editorMode }
            }
        }
        compose.runOnIdle {
            assertEquals(Color.White, background)
            assertEquals("Dark", editorMode)
        }
    }

    @Test fun systemModeTracksTheCurrentAndroidAppearance() {
        var seen: Pair<Boolean, Boolean>? = null
        compose.setContent {
            val expected = androidx.compose.foundation.isSystemInDarkTheme()
            CodeLabTheme("System", false, false) { actual -> SideEffect { seen = expected to actual } }
        }
        compose.runOnIdle { assertEquals(requireNotNull(seen).first, requireNotNull(seen).second) }
    }
}

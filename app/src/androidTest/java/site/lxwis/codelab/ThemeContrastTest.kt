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

    @Test fun allStylesAndModesProvideLegibleDefaultForeground() {
        var visualStyle by mutableStateOf("Minimal")
        var appearance by mutableStateOf("Dark")
        var actualText = Color.Unspecified
        var expectedText = Color.Unspecified
        var background = Color.Unspecified

        compose.setContent {
            // Nocturne previously forced a dark surface even in Light mode.
            CodeLabTheme("Nocturne", visualStyle, appearance, "Rose", false, false) { dark ->
                CodeLabBackdrop(visualStyle, dark) {
                    CodeLabForegroundSurface(Modifier.fillMaxSize()) {
                        val foreground = LocalContentColor.current
                        val onSurface = studioReadable(MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.background)
                        val canvas = MaterialTheme.colorScheme.background
                        SideEffect {
                            actualText = foreground
                            expectedText = onSurface
                            background = canvas
                        }
                        Text("Foreground regression sample")
                    }
                }
            }
        }

        for (style in CodeLabVisualStyles.all) {
            for (mode in listOf("Dark", "Light")) {
                compose.runOnIdle {
                    visualStyle = style
                    appearance = mode
                }
                compose.waitForIdle()
                compose.runOnIdle {
                    assertEquals("$style $mode should inherit onSurface", expectedText, actualText)
                    val bright = maxOf(actualText.luminance(), background.luminance())
                    val dim = minOf(actualText.luminance(), background.luminance())
                    val ratio = (bright + .05f) / (dim + .05f)
                    assertTrue("$style $mode contrast ratio: $ratio", ratio >= 4.5f)
                }
            }
        }
    }
    @Test fun customSurfaceAndAccentRemainLegibleInBothModes() {
        var mode by mutableStateOf("Dark")
        var scheme: androidx.compose.material3.ColorScheme? = null
        compose.setContent {
            CodeLabTheme("Obsidian", "Crystal", mode, "#FFFFFF", true, false,
                StudioAppearance(background = "#FFFFFF", surface = "#FAFAFA", foreground = "#FFFFFF")) {
                val current = MaterialTheme.colorScheme
                SideEffect { scheme = current }
            }
        }
        for (value in listOf("Dark", "Light")) {
            compose.runOnIdle { mode = value }
            compose.waitForIdle()
            compose.runOnIdle {
                val c = requireNotNull(scheme)
                fun contrast(a: Color, b: Color): Float {
                    val x = a.luminance(); val y = b.luminance()
                    return (maxOf(x, y) + .05f) / (minOf(x, y) + .05f)
                }
                assertTrue(contrast(c.onBackground, c.background) >= 4.5f)
                assertTrue(contrast(c.onSurface, c.surface) >= 4.5f)
                assertTrue(contrast(c.primary, c.surface) >= 4.5f)
                assertTrue(contrast(c.onPrimary, c.primary) >= 4.5f)
                assertTrue(contrast(c.onPrimaryContainer, c.primaryContainer) >= 4.5f)
            }
        }
    }

}

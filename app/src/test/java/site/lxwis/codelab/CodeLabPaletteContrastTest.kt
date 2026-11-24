package site.lxwis.codelab

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

class CodeLabPaletteContrastTest {
    private fun contrast(a: Color, b: Color): Float {
        val x = a.luminance(); val y = b.luminance()
        return (maxOf(x, y) + .05f) / (minOf(x, y) + .05f)
    }
    @Test fun allPresetsHaveReadableTextAndPrimaryButtons() {
        for (style in CodeLabVisualStyles.all) for (dark in listOf(false, true)) {
            val c = codeLabColorScheme("Obsidian", style, dark, AppearanceRecipes.defaultAccent(style), false)
            for ((name, ratio) in listOf(
                "body" to contrast(c.onSurface, c.surface),
                "secondary" to contrast(c.onSurfaceVariant, c.surface),
                "primary button" to contrast(c.onPrimary, c.primary),
                "primary container" to contrast(c.onPrimaryContainer, c.primaryContainer)
            )) assertTrue("$style dark=$dark $name: $ratio", ratio >= 4.5f)
        }
    }
}

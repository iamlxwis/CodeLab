package site.lxwis.codelab

import androidx.compose.runtime.staticCompositionLocalOf

/** A visual treatment, separate from light/dark mode and syntax colors. */
internal object CodeLabVisualStyles {
    val all = listOf("Minimal", "Glass", "Clay", "AMOLED", "Paper")
    fun accepts(style: String) = style in all
}

internal val LocalCodeLabVisualStyle = staticCompositionLocalOf { "Minimal" }

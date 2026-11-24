package site.lxwis.codelab

import androidx.compose.runtime.staticCompositionLocalOf

/** Public presets; legacy names remain readable for stored user recipes. */
internal object CodeLabVisualStyles {
    val all = listOf("Obsidian", "Titanium", "Crystal", "Midnight", "Porcelain", "Sandstone")
    private val legacy = mapOf("Minimal" to "Titanium", "Glass" to "Crystal",
        "Clay" to "Titanium", "AMOLED" to "Obsidian", "Paper" to "Sandstone")
    fun accepts(style: String) = style in all || style in legacy
    fun canonical(style: String) = legacy[style] ?: style.takeIf { it in all } ?: "Obsidian"
    fun effective(style: String, dark: Boolean): String = when (val preset = canonical(style)) {
        "Obsidian" -> if (dark) preset else "Porcelain"
        "Porcelain" -> if (dark) "Obsidian" else preset
        "Sandstone" -> if (dark) "Titanium" else preset
        else -> preset
    }
    fun defaultMode(style: String) = if (canonical(style) in listOf("Porcelain", "Sandstone")) "Light" else "Dark"
}

internal val LocalCodeLabVisualStyle = staticCompositionLocalOf { "Obsidian" }

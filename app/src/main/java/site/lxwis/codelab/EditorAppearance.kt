package site.lxwis.codelab

import android.content.SharedPreferences
import androidx.compose.runtime.staticCompositionLocalOf
import org.json.JSONObject

internal data class EditorAppearance(
    val uiFont: String = "Inter",
    val uiScale: Int = 100,
    val uiFontRevision: Long = 0,
    val editorFont: String = "JetBrains Mono",
    val editorTextSize: Int = 0,
    val editorFontRevision: Long = 0,
    val editorMode: String = "System",
    val editorBackground: String = "",
    val selectionColor: String = "",
    val comments: String = "",
    val strings: String = "",
    val numbers: String = "",
    val keywords: String = "",
    val functions: String = "",
    val types: String = "",
    val properties: String = ""
) {
    fun encode(): String = JSONObject()
        .put("uiFont", uiFont).put("uiScale", uiScale).put("uiFontRevision", uiFontRevision)
        .put("editorFont", editorFont).put("editorTextSize", editorTextSize).put("editorFontRevision", editorFontRevision)
        .put("editorMode", editorMode).put("editorBackground", editorBackground).put("selectionColor", selectionColor)
        .put("comments", comments).put("strings", strings).put("numbers", numbers).put("keywords", keywords)
        .put("functions", functions).put("types", types).put("properties", properties).toString()

    companion object {
        private val rgb = Regex("#[0-9a-fA-F]{6}")

        fun decode(raw: String?): EditorAppearance = try {
            val saved = JSONObject(raw ?: "{}")
            fun color(key: String) = saved.optString(key).takeIf(rgb::matches).orEmpty()
            EditorAppearance(
                uiFont = saved.optString("uiFont").takeIf { it in listOf("Inter", "System", "Serif", "Custom") } ?: "Inter",
                uiScale = saved.optInt("uiScale", 100).coerceIn(80, 140),
                uiFontRevision = saved.optLong("uiFontRevision"),
                editorFont = saved.optString("editorFont").takeIf { it in listOf("JetBrains Mono", "System Mono", "Custom") }
                    ?: "JetBrains Mono",
                editorTextSize = saved.optInt("editorTextSize").takeIf { it in 10..28 } ?: 0,
                editorFontRevision = saved.optLong("editorFontRevision"),
                editorMode = migrateAppearanceMode(saved.optString("editorMode")),
                editorBackground = color("editorBackground"),
                selectionColor = color("selectionColor"),
                comments = color("comments"),
                strings = color("strings"),
                numbers = color("numbers"),
                keywords = color("keywords"),
                functions = color("functions"),
                types = color("types"),
                properties = color("properties")
            )
        } catch (_: Exception) { EditorAppearance() }
    }
}

internal val LocalEditorAppearance = staticCompositionLocalOf { EditorAppearance() }
internal val LocalEditorAppearanceUpdater = staticCompositionLocalOf<((EditorAppearance) -> Unit)?> { null }

/** Older releases stored font and editor settings in the current app preset. */
internal fun legacyAppearanceSettings(preferences: SharedPreferences): String? {
    val style = preferences.getString("visual-style", null)
        ?: when (preferences.getString("preset", null)) {
            "OLED" -> "AMOLED"
            "Paper", "GitHub Light" -> "Paper"
            else -> "Obsidian"
        }
    val canonical = CodeLabVisualStyles.canonical(style)
    return (listOf(style, canonical) + CodeLabVisualStyles.legacyNames(canonical))
        .firstNotNullOfOrNull { preferences.getString("studio:$it", null) }
}

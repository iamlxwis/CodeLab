package site.lxwis.codelab

import org.json.JSONObject

/** Optional overrides are independent of preset names. Missing or invalid fields use defaults. */
internal data class StudioAppearance(
    val background: String = "", val surface: String = "", val foreground: String = "",
    val corners: Int = 20, val opacity: Int = 76, val border: Int = 1, val shadow: Int = 0,
    val uiFont: String = "Inter", val editorFont: String = "JetBrains Mono",
    val comments: String = "", val strings: String = "", val numbers: String = "", val keywords: String = "",
    val glassQuality: String = "Auto", val blur: Int = 12,
    val uiFontRevision: Long = 0L, val editorFontRevision: Long = 0L,
    val editorBackground: String = "", val consoleBackground: String = "",
    val navigationBackground: String = "", val borderColor: String = "", val selectionColor: String = "",
    val uiScale: Int = 100, val editorTextSize: Int = 0
) {
    fun encode(): String = JSONObject().put("background", background).put("surface", surface)
        .put("foreground", foreground).put("corners", corners).put("opacity", opacity)
        .put("border", border).put("shadow", shadow).put("uiFont", uiFont).put("editorFont", editorFont)
        .put("comments", comments).put("strings", strings).put("numbers", numbers).put("keywords", keywords)
        .put("glassQuality", glassQuality).put("blur", blur)
        .put("uiFontRevision", uiFontRevision).put("editorFontRevision", editorFontRevision)
        .put("editorBackground", editorBackground).put("consoleBackground", consoleBackground)
        .put("navigationBackground", navigationBackground).put("borderColor", borderColor)
        .put("selectionColor", selectionColor).put("uiScale", uiScale).put("editorTextSize", editorTextSize).toString()

    companion object {
        fun decode(raw: String?): StudioAppearance = try {
            val j = JSONObject(raw ?: "{}")
            fun hex(key: String) = j.optString(key).takeIf { it.matches(Regex("#[0-9a-fA-F]{6}")) }.orEmpty()
            StudioAppearance(hex("background"), hex("surface"), hex("foreground"),
                j.optInt("corners", 20).coerceIn(4, 32),
                j.optInt("opacity", 76).coerceIn(35, 100),
                j.optInt("border", 1).coerceIn(0, 3),
                j.optInt("shadow", 0).coerceIn(0, 12),
                j.optString("uiFont").takeIf { it in listOf("Inter", "System", "Serif", "Custom") } ?: "Inter",
                j.optString("editorFont").takeIf { it in listOf("JetBrains Mono", "System Mono", "Custom") } ?: "JetBrains Mono",
                hex("comments"), hex("strings"), hex("numbers"), hex("keywords"),
                j.optString("glassQuality").takeIf { it in listOf("Auto", "Lightweight", "High") } ?: "Auto",
                j.optInt("blur", 12).coerceIn(0,24), j.optLong("uiFontRevision", 0L), j.optLong("editorFontRevision", 0L),
                hex("editorBackground"), hex("consoleBackground"), hex("navigationBackground"), hex("borderColor"), hex("selectionColor"),
                j.optInt("uiScale", 100).coerceIn(80, 140),
                j.optInt("editorTextSize", 0).takeIf { it in 10..28 } ?: 0)
        } catch (_: Exception) { StudioAppearance() }
    }
}

internal val LocalStudioAppearanceUpdater = androidx.compose.runtime.staticCompositionLocalOf<((StudioAppearance) -> Unit)?> { null }

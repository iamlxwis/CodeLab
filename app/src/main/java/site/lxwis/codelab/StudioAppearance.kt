package site.lxwis.codelab

import org.json.JSONObject

/** Optional overrides are independent of preset names. Missing or invalid fields use defaults. */
internal data class StudioAppearance(
    val background: String = "", val surface: String = "", val foreground: String = "",
    val corners: Int = 20, val opacity: Int = 76, val border: Int = 1, val shadow: Int = 0,
    val uiFont: String = "Inter", val editorFont: String = "JetBrains Mono",
    val comments: String = "", val strings: String = "", val numbers: String = "", val keywords: String = "",
    val glassQuality: String = "Auto", val blur: Int = 12
) {
    fun encode(): String = JSONObject().put("background", background).put("surface", surface)
        .put("foreground", foreground).put("corners", corners).put("opacity", opacity)
        .put("border", border).put("shadow", shadow).put("uiFont", uiFont).put("editorFont", editorFont)
        .put("comments", comments).put("strings", strings).put("numbers", numbers).put("keywords", keywords)
        .put("glassQuality", glassQuality).put("blur", blur).toString()

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
                j.optInt("blur", 12).coerceIn(0,24))
        } catch (_: Exception) { StudioAppearance() }
    }
}

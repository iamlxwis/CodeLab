package site.lxwis.codelab

import org.json.JSONArray
import org.json.JSONObject

/** User-defined appearance recipes. Import from preferences is validated; import payload size is bounded separately. */
internal data class AppearanceRecipe(
    val id: String, val name: String, val style: String, val mode: String, val accent: String,
    val studio: StudioAppearance = StudioAppearance(),
    val motion: ThemeMotionRecipe? = null
)

/** Legacy recipes omit motion and must not change existing accessibility preferences. */
internal data class ThemeMotionRecipe(val preference: String, val intensity: Int, val speed: Int, val spring: Int) {
    fun json() = JSONObject().put("preference", preference).put("intensity", intensity)
        .put("speed", speed).put("spring", spring)
    companion object {
        fun decode(json: JSONObject?): ThemeMotionRecipe? {
            if (json == null) return null
            val preference = json.optString("preference").takeIf { it in listOf("System", "Full", "Reduced") }
                ?: return null
            return ThemeMotionRecipe(preference, json.optInt("intensity", 100).coerceIn(0, 100),
                json.optInt("speed", 100).coerceIn(50, 200), json.optInt("spring", 35).coerceIn(0, 100))
        }
    }
}

internal object AppearanceRecipes {
    private fun validStyle(value: String) = CodeLabVisualStyles.accepts(value)
    private val modes = setOf("Dark", "Light", "System")
    private val accents = setOf("Mint", "Blue", "Violet", "Amber", "Rose", "Mono")
    private val hexColor = Regex("#[0-9a-fA-F]{6}")

    fun validAccent(value: String): Boolean = value in accents || hexColor.matches(value)
    fun defaultAccent(style: String): String = if (CodeLabVisualStyles.canonical(style) in listOf("Crystal", "Obsidian", "Titanium")) "Mono" else "Blue"
    fun validName(name: String): Boolean =
        name == name.trim() && name.isNotBlank() && name.length <= 32 &&
            name.none { it.isISOControl() }

    fun decode(raw: String?): List<AppearanceRecipe> = try {
        val array = JSONArray(raw ?: "[]")
        val result = mutableListOf<AppearanceRecipe>()
        val ids = HashSet<String>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val candidate = AppearanceRecipe(obj.optString("id"), obj.optString("name"),
                obj.optString("style"), obj.optString("mode"), obj.optString("accent"),
                StudioAppearance.decode(obj.optString("studio")), ThemeMotionRecipe.decode(obj.optJSONObject("motion")))
            if (candidate.id.length in 1..64 && validName(candidate.name) &&
                validStyle(candidate.style) && candidate.mode in modes &&
                validAccent(candidate.accent) && ids.add(candidate.id)) result += candidate
        }
        result
    } catch (_: Exception) { emptyList() }

    fun encode(recipes: List<AppearanceRecipe>): String {
        val array = JSONArray()
        recipes.forEach { recipe ->
            if (validStyle(recipe.style) && recipe.mode in modes &&
                validName(recipe.name) && validAccent(recipe.accent) &&
                recipe.id.length in 1..64) {
                array.put(JSONObject().put("id", recipe.id).put("name", recipe.name)
                    .put("style", recipe.style).put("mode", recipe.mode)
                    .put("accent", recipe.accent).put("studio", recipe.studio.encode())
                    .put("motion", recipe.motion?.json() ?: JSONObject.NULL))
            }
        }
        return array.toString()
    }
}

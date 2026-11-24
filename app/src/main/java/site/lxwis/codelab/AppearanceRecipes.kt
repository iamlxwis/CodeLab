package site.lxwis.codelab

import org.json.JSONArray
import org.json.JSONObject

/** User-defined appearance recipes. Import from preferences is always validated and bounded. */
internal data class AppearanceRecipe(
    val id: String, val name: String, val style: String, val mode: String, val accent: String,
    val studio: StudioAppearance = StudioAppearance()
)

internal object AppearanceRecipes {
    private fun validStyle(value: String) = CodeLabVisualStyles.accepts(value)
    private val modes = setOf("Dark", "Light", "System")
    private val accents = setOf("Mint", "Blue", "Violet", "Amber", "Rose", "Mono")
    private val hexColor = Regex("#[0-9a-fA-F]{6}")
    const val LIMIT = 20

    fun validAccent(value: String): Boolean = value in accents || hexColor.matches(value)
    fun defaultAccent(style: String): String = if (CodeLabVisualStyles.canonical(style) in listOf("Crystal", "Obsidian", "Titanium")) "Mono" else "Blue"
    fun validName(name: String): Boolean =
        name == name.trim() && name.isNotBlank() && name.length <= 32 &&
            name.none { it.isISOControl() }

    fun decode(raw: String?): List<AppearanceRecipe> = try {
        val array = JSONArray(raw ?: "[]")
        val result = mutableListOf<AppearanceRecipe>()
        for (i in 0 until minOf(array.length(), LIMIT)) {
            val obj = array.optJSONObject(i) ?: continue
            val candidate = AppearanceRecipe(obj.optString("id"), obj.optString("name"),
                obj.optString("style"), obj.optString("mode"), obj.optString("accent"),
                StudioAppearance.decode(obj.optString("studio")))
            if (candidate.id.length in 1..64 && validName(candidate.name) &&
                validStyle(candidate.style) && candidate.mode in modes &&
                validAccent(candidate.accent) && result.none { it.id == candidate.id }) result += candidate
        }
        result
    } catch (_: Exception) { emptyList() }

    fun encode(recipes: List<AppearanceRecipe>): String {
        val array = JSONArray()
        recipes.take(LIMIT).forEach { recipe ->
            if (validStyle(recipe.style) && recipe.mode in modes &&
                validName(recipe.name) && validAccent(recipe.accent) &&
                recipe.id.length in 1..64) {
                array.put(JSONObject().put("id", recipe.id).put("name", recipe.name)
                    .put("style", recipe.style).put("mode", recipe.mode)
                    .put("accent", recipe.accent).put("studio", recipe.studio.encode()))
            }
        }
        return array.toString()
    }
}

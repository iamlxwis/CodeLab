package site.lxwis.codelab

import org.json.JSONArray
import org.json.JSONObject

/** User-defined appearance recipes. Import from preferences is always validated and bounded. */
internal data class AppearanceRecipe(
    val id: String, val name: String, val style: String, val mode: String, val accent: String
)

internal object AppearanceRecipes {
    private val styles = setOf("Minimal", "Glass", "Clay", "AMOLED", "Paper")
    private val modes = setOf("Dark", "Light", "System")
    private val accents = setOf("Mint", "Blue", "Violet", "Amber", "Rose", "Mono")
    private val hexColor = Regex("#[0-9a-fA-F]{6}")
    const val LIMIT = 20

    fun validAccent(value: String): Boolean = value in accents || hexColor.matches(value)
    fun defaultAccent(style: String): String = if (style == "Glass" || style == "AMOLED") "Mono" else "Mint"
    fun validName(name: String): Boolean =
        name == name.trim() && name.isNotBlank() && name.length <= 32 &&
            name.none { it.isISOControl() }

    fun decode(raw: String?): List<AppearanceRecipe> = try {
        val array = JSONArray(raw ?: "[]")
        val result = mutableListOf<AppearanceRecipe>()
        for (i in 0 until minOf(array.length(), LIMIT)) {
            val obj = array.optJSONObject(i) ?: continue
            val candidate = AppearanceRecipe(obj.optString("id"), obj.optString("name"),
                obj.optString("style"), obj.optString("mode"), obj.optString("accent"))
            if (candidate.id.length in 1..64 && validName(candidate.name) &&
                candidate.style in styles && candidate.mode in modes &&
                validAccent(candidate.accent) && result.none { it.id == candidate.id }) result += candidate
        }
        result
    } catch (_: Exception) { emptyList() }

    fun encode(recipes: List<AppearanceRecipe>): String {
        val array = JSONArray()
        recipes.take(LIMIT).forEach { recipe ->
            if (recipe.style in styles && recipe.mode in modes &&
                validName(recipe.name) && validAccent(recipe.accent) &&
                recipe.id.length in 1..64) {
                array.put(JSONObject().put("id", recipe.id).put("name", recipe.name)
                    .put("style", recipe.style).put("mode", recipe.mode)
                    .put("accent", recipe.accent))
            }
        }
        return array.toString()
    }
}

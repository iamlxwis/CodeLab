package site.lxwis.codelab

import org.json.JSONArray
import org.json.JSONObject

internal data class WorkspaceLayout(
    val name: String, val phoneSplit: Boolean, val phonePercent: Int, val tabletPercent: Int,
    val status: Boolean, val density: String, val actions: List<String>,
    val toolbar: List<String>? = null, val explorer: Boolean = true, val focus: Boolean? = null,
    val systemStatus: Boolean? = null, val output: String? = null, val maximized: Boolean? = null,
    val keyboardLayout: String? = null, val keyboardSymbols: String? = null,
    val accessory: Boolean? = null, val keyboardOnly: Boolean? = null
) {
    fun json() = JSONObject().put("name", name).put("split", phoneSplit)
        .put("phone", phonePercent).put("tablet", tabletPercent).put("status", status)
        .put("density", density).put("actions", JSONArray(actions))
        .put("toolbar", toolbar?.let { JSONArray(it) } ?: JSONObject.NULL).put("explorer", explorer)
        .put("focus", focus ?: JSONObject.NULL).put("systemStatus", systemStatus ?: JSONObject.NULL)
        .put("output", output ?: JSONObject.NULL).put("maximized", maximized ?: JSONObject.NULL)
        .put("keyboardLayout", keyboardLayout ?: JSONObject.NULL).put("keyboardSymbols", keyboardSymbols ?: JSONObject.NULL)
        .put("accessory", accessory ?: JSONObject.NULL).put("keyboardOnly", keyboardOnly ?: JSONObject.NULL)
    companion object {
        fun from(j: JSONObject): WorkspaceLayout? = runCatching {
            val name = j.optString("name").takeIf { it.isNotBlank() && it.length <= 64 } ?: return null
            val actions = j.optJSONArray("actions") ?: JSONArray()
            val toolbar = j.optJSONArray("toolbar")?.let { array ->
                (0 until minOf(array.length(), 64)).map { array.optString(it) }
                    .filter { it in EditorToolbarOrder.known }.distinct()
            }
            fun optionalBoolean(key: String) = if (j.has(key) && !j.isNull(key)) j.optBoolean(key) else null
            WorkspaceLayout(name, j.optBoolean("split"), j.optInt("phone", 60).coerceIn(35,80),
                j.optInt("tablet",60).coerceIn(35,80), j.optBoolean("status",true),
                j.optString("density").takeIf { it in listOf("Compact","Comfortable","Spacious") } ?: "Comfortable",
                (0 until minOf(actions.length(),6)).map { actions.optString(it) }.filter { it.matches(Regex("[a-z-]{1,40}")) }.distinct(),
                toolbar, j.optBoolean("explorer", true),
                optionalBoolean("focus"), optionalBoolean("systemStatus"),
                j.optString("output").takeIf { it in listOf("Editor", "Console", "Preview") }, optionalBoolean("maximized"),
                j.optString("keyboardLayout").takeIf { it in listOf("Standard", "Minimal", "Brackets", "Selection", "Navigation", "Custom") },
                j.optString("keyboardSymbols").takeIf { it.length <= 512 && j.has("keyboardSymbols") && !j.isNull("keyboardSymbols") },
                optionalBoolean("accessory"), optionalBoolean("keyboardOnly"))
        }.getOrNull()
        fun decode(raw: String?): List<WorkspaceLayout> = runCatching {
            val a = JSONArray(raw ?: "[]")
            (0 until minOf(a.length(), 64)).mapNotNull { a.optJSONObject(it)?.let(::from) }
        }.getOrDefault(emptyList())
        fun encode(items: List<WorkspaceLayout>) = JSONArray(items.map { it.json() }).toString()
    }
}

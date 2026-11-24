package site.lxwis.codelab

import org.json.JSONArray
import org.json.JSONObject

internal data class WorkspaceLayout(
    val name: String, val phoneSplit: Boolean, val phonePercent: Int, val tabletPercent: Int,
    val status: Boolean, val density: String, val actions: List<String>
) {
    fun json() = JSONObject().put("name", name).put("split", phoneSplit)
        .put("phone", phonePercent).put("tablet", tabletPercent).put("status", status)
        .put("density", density).put("actions", JSONArray(actions))
    companion object {
        fun from(j: JSONObject): WorkspaceLayout? = runCatching {
            val name = j.optString("name").takeIf { it.isNotBlank() && it.length <= 64 } ?: return null
            val actions = j.optJSONArray("actions") ?: JSONArray()
            WorkspaceLayout(name, j.optBoolean("split"), j.optInt("phone", 60).coerceIn(35,80),
                j.optInt("tablet",60).coerceIn(35,80), j.optBoolean("status",true),
                j.optString("density").takeIf { it in listOf("Compact","Comfortable","Spacious") } ?: "Comfortable",
                (0 until minOf(actions.length(),6)).map { actions.optString(it) }.filter { it.matches(Regex("[a-z-]{1,40}")) }.distinct())
        }.getOrNull()
        fun decode(raw: String?): List<WorkspaceLayout> = runCatching {
            val a = JSONArray(raw ?: "[]")
            (0 until minOf(a.length(), 64)).mapNotNull { a.optJSONObject(it)?.let(::from) }
        }.getOrDefault(emptyList())
        fun encode(items: List<WorkspaceLayout>) = JSONArray(items.map { it.json() }).toString()
    }
}

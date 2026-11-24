package site.lxwis.codelab

import java.nio.charset.StandardCharsets
import java.util.Base64

/** Small, app-private user snippet presets. Invalid/corrupt entries are ignored. */
internal data class StoredSnippet(val title: String, val body: String)

internal object SavedSnippetCodec {
    const val MAX_COUNT = 24
    const val MAX_BODY = 4096
    private const val MAX_TITLE = 60

    private fun valid(snippet: StoredSnippet) =
        snippet.title.isNotBlank() && snippet.title == snippet.title.trim() &&
            snippet.title.length <= MAX_TITLE &&
            snippet.title.none { it.isISOControl() } &&
            snippet.body.isNotEmpty() && snippet.body.length <= MAX_BODY

    fun add(existing: List<StoredSnippet>, item: StoredSnippet): List<StoredSnippet>? =
        if (!valid(item) || existing.size >= MAX_COUNT ||
            existing.any { it.title.equals(item.title, ignoreCase = true) }) null
        else existing + item

    fun remove(existing: List<StoredSnippet>, title: String): List<StoredSnippet> =
        existing.filterNot { it.title == title }

    fun encode(items: List<StoredSnippet>): String {
        val encoder = Base64.getUrlEncoder().withoutPadding()
        return items.take(MAX_COUNT).filter(::valid).joinToString("\n") { item ->
            encoder.encodeToString(item.title.toByteArray(StandardCharsets.UTF_8)) + ":" +
                encoder.encodeToString(item.body.toByteArray(StandardCharsets.UTF_8))
        }
    }

    fun decode(raw: String?): List<StoredSnippet> {
        if (raw.isNullOrEmpty() || raw.length > MAX_COUNT * (MAX_BODY * 2 + 200))
            return emptyList()
        val decoder = Base64.getUrlDecoder()
        val result = mutableListOf<StoredSnippet>()
        for (line in raw.lineSequence().take(MAX_COUNT * 2)) {
            val pair = line.split(':', limit = 2)
            if (pair.size != 2) continue
            val item = try {
                StoredSnippet(
                    String(decoder.decode(pair[0]), StandardCharsets.UTF_8),
                    String(decoder.decode(pair[1]), StandardCharsets.UTF_8)
                )
            } catch (_: IllegalArgumentException) { continue }
            if (valid(item) && result.none { it.title.equals(item.title, ignoreCase = true) })
                result.add(item)
            if (result.size == MAX_COUNT) break
        }
        return result
    }
}

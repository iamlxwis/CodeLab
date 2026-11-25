package site.lxwis.codelab

import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Read-only GitHub release metadata. Actual APKs are still certificate-verified by Android. */
internal object RuntimeReleaseAssets {
    private const val ENDPOINT = "https://api.github.com/repos/iamlxwis/CodeLab/releases/latest"
    private const val MAX_BODY_BYTES = 256_000
    private const val MAX_APK_BYTES = 120L * 1024L * 1024L

    /** Only an explicitly published, small, recognized APK may enable a Download button. */
    fun parseAssetNames(json: String): Set<String> = parseAssetSizes(json).keys

    private fun parseAssetSizes(json: String): Map<String, Long> {
        val assets = JSONObject(json).optJSONArray("assets") ?: return emptyMap()
        val known = OptionalRuntimeLocator.Pack.entries.map {
            "CodeLab-" + it.suffix + ".apk"
        }.toSet()
        val result = mutableMapOf<String, Long>()
        for (i in 0 until assets.length()) {
            val asset = assets.optJSONObject(i) ?: continue
            val name = asset.optString("name")
            val size = asset.optLong("size", -1)
            if (name in known && asset.optString("state") == "uploaded" &&
                size in 1..MAX_APK_BYTES) result[name] = size
        }
        return result
    }

    /** Call from Dispatchers.IO; never connect on the Android UI thread. */
    fun fetch(): Map<String, Long> {
        val connection = URL(ENDPOINT).openConnection() as HttpURLConnection
        connection.connectTimeout = 6_000
        connection.readTimeout = 6_000
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.setRequestProperty("User-Agent", "CodeLab-Android")
        return try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK)
                throw IOException("GitHub release lookup failed (HTTP " +
                    connection.responseCode + ")")
            val output = ByteArrayOutputStream()
            connection.inputStream.use { stream ->
                val block = ByteArray(4096)
                while (true) {
                    val count = stream.read(block)
                    if (count < 0) break
                    if (output.size() + count > MAX_BODY_BYTES)
                        throw IOException("Release metadata exceeds safe size")
                    output.write(block, 0, count)
                }
            }
            parseAssetSizes(output.toString("UTF-8"))
        } finally {
            connection.disconnect()
        }
    }
}

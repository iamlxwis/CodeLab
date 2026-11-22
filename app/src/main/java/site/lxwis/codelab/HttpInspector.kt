package site.lxwis.codelab

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URI
import javax.net.ssl.HttpsURLConnection

/** Bounded, on-demand HTTPS GET inspection, not a general-purpose HTTP proxy. */
internal object HttpInspector {
    data class Result(val status: Int, val contentType: String,
        val headers: String, val body: String, val truncated: Boolean)

    fun validate(raw: String): URI {
        require(raw.length in 12..2048) { "Enter a complete HTTPS URL (up to 2048 characters)." }
        val uri = URI(raw.trim())
        require(uri.scheme.equals("https", true) && !uri.host.isNullOrBlank() &&
            uri.userInfo == null && uri.fragment == null && uri.port in -1..65535) {
            "Only HTTPS URLs without embedded credentials or fragments are supported."
        }
        require(uri.host != "localhost" && uri.host != "127.0.0.1" &&
            uri.host != "::1") { "Loopback addresses are not supported." }
        return uri
    }

    fun fetch(raw: String): Result {
        val url = validate(raw).toURL()
        val connection = (url.openConnection() as HttpsURLConnection).apply {
            requestMethod = "GET"
            instanceFollowRedirects = false
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Accept", "application/json, text/plain, */*")
        }
        try {
            val status = connection.responseCode
            val contentType = connection.contentType.orEmpty()
            val safeHeaders = connection.headerFields.entries
                .filter { (key, _) -> key != null &&
                    !key.equals("set-cookie", true) && !key.equals("authorization", true) }
                .take(25)
                .joinToString("\n") { (key, value) ->
                    key + ": " + value.take(3).joinToString(", ").take(300)
                }
            val bodyStream = if (status >= 400) connection.errorStream else connection.inputStream
            val bytes = ByteArrayOutputStream()
            var truncated = false
            bodyStream?.use { stream ->
                val buffer = ByteArray(4096)
                while (true) {
                    val n = stream.read(buffer)
                    if (n < 0) break
                    val remaining = 128 * 1024 - bytes.size()
                    if (n > remaining) {
                        if (remaining > 0) bytes.write(buffer, 0, remaining)
                        truncated = true
                        break
                    }
                    bytes.write(buffer, 0, n)
                }
            }
            val original = bytes.toByteArray().toString(Charsets.UTF_8)
            val formatted = if (contentType.contains("json", ignoreCase = true)) {
                runCatching {
                    when (val obj = JSONTokener(original).nextValue()) {
                        is JSONObject -> obj.toString(2)
                        is JSONArray -> obj.toString(2)
                        else -> original
                    }
                }.getOrDefault(original)
            } else if (contentType.contains("text", ignoreCase = true) ||
                contentType.contains("xml", ignoreCase = true)) original
            else "[Non-text response: " + bytes.size() + " bytes]"
            return Result(status, contentType, safeHeaders, formatted.take(135000), truncated)
        } finally {
            connection.disconnect()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HttpInspectorSheet(dismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    var url by rememberSaveable { mutableStateOf("https://") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<HttpInspector.Result?>(null) }
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = dismiss, sheetState = sheet,
        containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.94f)
            .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Text("HTTPS Inspector", style = MaterialTheme.typography.headlineSmall)
            Text("Inspect an HTTPS GET response. No source code or credentials are sent.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(url, { url = it.take(2048) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("HTTPS URL") }, singleLine = true,
                maxLines = 1)
            Button(enabled = !loading && url.length > 8, onClick = {
                scope.launch {
                    loading = true
                    result = null
                    error = ""
                    try {
                        result = withContext(Dispatchers.IO) { HttpInspector.fetch(url) }
                    } catch (e: Exception) {
                        error = e.localizedMessage ?: "Unable to request URL."
                    } finally { loading = false }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text(if (loading) "Requesting…" else "Send GET") }
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
            result?.let { response ->
                Text("HTTP " + response.status + " · " + response.contentType,
                    style = MaterialTheme.typography.titleMedium)
                Text("Redirects are not followed · 10s timeout · 128 KiB response cap",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (response.truncated)
                    Text("Response was truncated.", color = MaterialTheme.colorScheme.error)
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Headers", style = MaterialTheme.typography.titleSmall)
                    SelectionContainer {
                        Text(response.headers, fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Body", style = MaterialTheme.typography.titleSmall)
                    SelectionContainer {
                        Text(response.body.take(135000), fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
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
import java.net.URI
import java.util.Locale
import javax.net.ssl.HttpsURLConnection

/** Bounded, on-demand HTTPS GET inspection, not a general-purpose HTTP proxy. */
internal object HttpInspector {
    data class Result(val status: Int, val contentType: String,
        val headers: String, val body: String, val truncated: Boolean)

    data class Request(
        val method: String,
        val url: String,
        val headers: String = "",
        val body: String = ""
    )

    private val methods = setOf("GET", "POST", "PUT", "DELETE")
    private val protectedHeaders = setOf(
        "host", "connection", "content-length", "transfer-encoding",
        "proxy-authorization", "proxy-connection", "upgrade"
    )

    fun validate(raw: String): URI {
        require(raw.length in 12..2048) { "Enter a complete HTTPS URL (up to 2048 characters)." }
        val uri = URI(raw.trim())
        val host = uri.host?.lowercase(Locale.ROOT)
        require(uri.scheme.equals("https", true) && !host.isNullOrBlank() &&
            uri.userInfo == null && uri.fragment == null && uri.port in -1..65535) {
            "Only HTTPS URLs without embedded credentials or fragments are supported."
        }
        // Do not permit obvious local-network targets in the API inspector.
        // TLS certificate verification and redirect blocking remain mandatory.
        require(host != "localhost" && !host!!.endsWith(".localhost") &&
            !host.endsWith(".local") && !host.endsWith(".internal") &&
            ':' !in host && !host.matches(Regex("""\d+\.\d+\.\d+\.\d+"""))) {
            "Local hosts and IP literals are not supported."
        }
        return uri
    }

    /** Header values are kept only in the current UI session, never in preferences. */
    fun parseHeaders(raw: String): List<Pair<String, String>> {
        require(raw.length <= 4096) { "Headers exceed 4 KiB." }
        val lines = raw.lines().filter { it.isNotBlank() }
        require(lines.size <= 16) { "Use at most 16 headers." }
        val names = mutableSetOf<String>()
        return lines.map { line ->
            require('\r' !in line && '\u0000' !in line) { "Invalid header characters." }
            val colon = line.indexOf(':')
            require(colon > 0) { "Headers must use Name: value." }
            val name = line.substring(0, colon).trim()
            val value = line.substring(colon + 1).trim()
            val key = name.lowercase(Locale.ROOT)
            require(name.matches(Regex("[A-Za-z][A-Za-z0-9-]{0,63}")) &&
                value.length <= 1024 && key !in protectedHeaders) {
                "Invalid or restricted request header: $name"
            }
            require(names.add(key)) { "Duplicate header: $name" }
            name to value
        }
    }

    fun fetch(raw: String): Result = request(Request("GET", raw))

    fun request(spec: Request): Result {
        val method = spec.method.uppercase(Locale.ROOT)
        require(method in methods) { "Unsupported request method." }
        require(method in setOf("POST", "PUT") || spec.body.isEmpty()) {
            "Only POST and PUT support request bodies."
        }
        val bytesToSend = spec.body.toByteArray(Charsets.UTF_8)
        require(bytesToSend.size <= 64 * 1024) { "Request body exceeds 64 KiB." }
        val headers = parseHeaders(spec.headers)
        val url = validate(spec.url).toURL()
        val connection = (url.openConnection() as HttpsURLConnection).apply {
            requestMethod = method
            instanceFollowRedirects = false
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Accept", "application/json, text/plain, */*")
            if (bytesToSend.isNotEmpty() && headers.none { it.first.equals("Content-Type", true) }) {
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
            headers.forEach { (name, value) -> setRequestProperty(name, value) }
            if (bytesToSend.isNotEmpty()) {
                doOutput = true
                setFixedLengthStreamingMode(bytesToSend.size)
            }
        }
        try {
            if (bytesToSend.isNotEmpty()) connection.outputStream.use { it.write(bytesToSend) }
            val status = connection.responseCode
            val contentType = connection.contentType.orEmpty()
            val safeHeaders = connection.headerFields.entries
                .filter { (key, _) -> key != null &&
                    !key.equals("set-cookie", true) && !key.equals("authorization", true) }
                .take(25)
                .joinToString("\n") { (key, value) ->
                    key + ": " + value.orEmpty().take(3).joinToString(", ").take(300)
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
    var method by rememberSaveable { mutableStateOf("GET") }
    // Do not persist tokens, request headers or bodies to disk or saved state.
    var requestHeaders by remember { mutableStateOf("") }
    var requestBody by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<HttpInspector.Result?>(null) }
    val acceptsBody = method == "POST" || method == "PUT"
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = dismiss, sheetState = sheet,
        containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.94f)
            .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("HTTPS Request Inspector", style = MaterialTheme.typography.headlineSmall)
            Text("Send a deliberate HTTPS request. Redirects are blocked; credentials are not saved.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("GET", "POST", "PUT", "DELETE").forEach { option ->
                    FilterChip(selected = method == option, onClick = { method = option },
                        label = { Text(option) })
                }
            }
            OutlinedTextField(url, { url = it.take(2048) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("HTTPS URL") }, singleLine = true, maxLines = 1)
            OutlinedTextField(requestHeaders, { requestHeaders = it.take(4096) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Request headers (Name: value, one per line)") },
                minLines = 2, maxLines = 4)
            if (acceptsBody) {
                OutlinedTextField(requestBody, { requestBody = it.take(65536) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Request body (UTF-8, up to 64 KiB)") },
                    minLines = 2, maxLines = 5)
            }
            Button(enabled = !loading && url.length > 8, onClick = {
                scope.launch {
                    loading = true
                    result = null
                    error = ""
                    try {
                        result = withContext(Dispatchers.IO) {
                            HttpInspector.request(HttpInspector.Request(
                                method, url, requestHeaders, if (acceptsBody) requestBody else ""))
                        }
                    } catch (e: Exception) {
                        error = e.localizedMessage ?: "Unable to request URL."
                    } finally { loading = false }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(if (loading) "Requesting…" else "Send $method")
            }
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
            result?.let { response ->
                Text("HTTP " + response.status + " · " + response.contentType,
                    style = MaterialTheme.typography.titleMedium)
                Text("No redirects · 10s timeout · 128 KiB response cap",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (response.truncated)
                    Text("Response was truncated.", color = MaterialTheme.colorScheme.error)
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Response headers", style = MaterialTheme.typography.titleSmall)
                    SelectionContainer {
                        Text(response.headers, fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Response body", style = MaterialTheme.typography.titleSmall)
                    SelectionContainer {
                        Text(response.body, fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

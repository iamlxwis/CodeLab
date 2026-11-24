package site.lxwis.codelab

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.webkit.ConsoleMessage
import android.webkit.MimeTypeMap
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets

private const val PREVIEW_HOST = "appassets.androidplatform.net"
private const val PREVIEW_ROOT = "https://appassets.androidplatform.net/project/"

/** An offline virtual HTTPS origin: project assets never leave the device. */
@Composable
fun HtmlPreview(project: String, path: String, source: String, modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = remember(project) { ProjectStore(File(context.filesDir, "workspace")) }
    val live = remember(project, path) { PreviewSource(source) }
    var debounced by remember(project, path) { mutableStateOf(source) }
    var revision by remember(project, path) { mutableIntStateOf(0) }
    var loading by remember(project, path) { mutableStateOf(true) }
    var failure by remember(project, path) { mutableStateOf<String?>(null) }
    var messages by remember(project, path) { mutableStateOf(emptyList<String>()) }
    var viewport by remember(project, path) { mutableStateOf("Auto") }
    var consoleVisible by remember(project, path) { mutableStateOf(true) }
    val handler = remember { Handler(Looper.getMainLooper()) }
    val url = PREVIEW_ROOT + Uri.encode(path, "/") + "?revision=" + revision + "-" + debounced.hashCode()

    LaunchedEffect(source, project, path) {
        delay(300)
        debounced = source
    }
    Column(modifier) {
        Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Local preview", style = MaterialTheme.typography.titleSmall)
                Text(if (loading) "Loading…" else if (failure != null) "Preview unavailable" else "Offline · " + path,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            IconButton(onClick = { revision++; failure = null; messages = emptyList(); loading = true }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh preview")
            }
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Icon(Icons.Default.Devices, null, Modifier.size(18.dp)
                .align(Alignment.CenterVertically),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            val choices = listOf("Auto", "Phone", "Tablet", "Desktop")
            SingleChoiceSegmentedButtonRow {
                choices.forEachIndexed { index, choice ->
                    val stateLabel = stringResource(if (viewport == choice) R.string.preview_selected else R.string.preview_not_selected)
                    SegmentedButton(selected = viewport == choice, onClick = { viewport = choice },
                        shape = SegmentedButtonDefaults.itemShape(index, choices.size),
                        modifier = Modifier.heightIn(min = 48.dp).semantics { stateDescription = stateLabel }) {
                        Text(choice)
                    }
                }
            }
            val logState = stringResource(if (consoleVisible) R.string.preview_log_visible else R.string.preview_log_hidden)
            OutlinedIconToggleButton(checked = consoleVisible, onCheckedChange = { consoleVisible = it },
                modifier = Modifier.size(48.dp).semantics { stateDescription = logState }) {
                BadgedBox(badge = { if (messages.isNotEmpty()) Badge { Text(messages.size.toString()) } }) {
                    Icon(Icons.Default.List, stringResource(R.string.preview_log))
                }
            }
        }
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp))
        HorizontalDivider()
        Box(Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.TopCenter) {
        val viewportWidth = when (viewport) {
            "Phone" -> 390.dp
            "Tablet" -> 768.dp
            "Desktop" -> 1100.dp
            else -> 10000.dp
        }
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    setBackgroundColor(android.graphics.Color.WHITE)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    settings.setSupportMultipleWindows(false)
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            return request.url.scheme != "https" || request.url.host != PREVIEW_HOST
                        }
                        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse {
                            val uri = request.url
                            if (uri.scheme != "https" || uri.host != PREVIEW_HOST ||
                                !uri.path.orEmpty().startsWith("/project/")) return previewResponse("text/plain", "")
                            val requested = Uri.decode(uri.encodedPath.orEmpty()).removePrefix("/project/")
                            val extension = requested.substringAfterLast('.', "").lowercase()
                            val type = when (extension) {
                                "html", "htm" -> "text/html"
                                "css" -> "text/css"
                                "js", "mjs" -> "application/javascript"
                                "svg" -> "image/svg+xml"
                                "json" -> "application/json"
                                "woff" -> "font/woff"
                                "woff2" -> "font/woff2"
                                "ttf" -> "font/ttf"
                                "otf" -> "font/otf"
                                else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "text/plain"
                            }
                            val data = if (requested == path) live.text.toByteArray(StandardCharsets.UTF_8)
                            else runCatching { store.readAsset(project, requested) }
                                .getOrElse { exception ->
                                    handler.post {
                                        messages = (messages + "Could not load " + requested +
                                            ": " + (exception.message ?: "unavailable")).takeLast(20)
                                    }
                                    byteArrayOf()
                                }
                            return previewResponse(type, data)
                        }
                        override fun onPageFinished(view: WebView, url: String) { loading = false }
                        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                            if (request.isForMainFrame) {
                                loading = false
                                failure = error.description.toString()
                            }
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                            messages = (messages + message.messageLevel().toString() + ": " +
                                message.message()).takeLast(20)
                            return true
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxHeight().widthIn(max = viewportWidth).fillMaxWidth(),
            update = { view ->
                live.text = debounced
                if (view.tag != url) {
                    view.tag = url
                    loading = true
                    failure = null
                    view.loadUrl(url)
                }
            }
        )
        }
        if (failure != null) {
            Text("Preview error: " + failure, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }
        if (messages.isNotEmpty() && consoleVisible) {
            HorizontalDivider()
            Text("WEB CONSOLE", Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                style = MaterialTheme.typography.labelSmall)
            Column(Modifier.fillMaxWidth().heightIn(max = 112.dp).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp)) {
                messages.takeLast(8).forEach { Text(it, style = MaterialTheme.typography.labelSmall) }
            }
        }
    }
}

private class PreviewSource(@Volatile var text: String)

private fun previewResponse(type: String, text: String) =
    previewResponse(type, text.toByteArray(StandardCharsets.UTF_8))

private fun previewResponse(type: String, bytes: ByteArray) =
    WebResourceResponse(type,
        if (type.startsWith("image/") || type.startsWith("font/") ||
            type.startsWith("audio/") || type.startsWith("video/")) null else "UTF-8",
        ByteArrayInputStream(bytes))

package site.lxwis.codelab

import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.MimeTypeMap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets

private const val VIRTUAL_HOST = "codelab.preview.invalid"

/** WebView rendering local project HTML/CSS/JS with no external network resources. */
@Composable
fun HtmlPreview(project: String, path: String, source: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember(project) { ProjectStore(File(context.filesDir, "workspace")) }
    var webView by remember(project, path) { mutableStateOf<WebView?>(null) }
    var logs by remember(project, path) { mutableStateOf(emptyList<String>()) }
    val paper = MaterialTheme.colorScheme.surface.toArgb()
    val base = remember(path) {
        "https://" + VIRTUAL_HOST + "/" + android.net.Uri.encode(path, "/")
    }
    Column(modifier) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Local preview", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            IconButton(onClick = {
                logs = emptyList()
                webView?.loadDataWithBaseURL(base, source, "text/html", "UTF-8", null)
            }) { Icon(Icons.Default.Refresh, contentDescription = "Refresh preview") }
        }
        HorizontalDivider()
        AndroidView(factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(paper)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.javaScriptCanOpenWindowsAutomatically = false
                settings.setSupportMultipleWindows(false)
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) =
                        request.url.scheme != "https" || request.url.host != VIRTUAL_HOST

                    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse {
                        val uri = request.url
                        if (uri.scheme != "https" || uri.host != VIRTUAL_HOST)
                            return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(byteArrayOf()))
                        val requested = uri.path.orEmpty().removePrefix("/")
                        val ext = requested.substringAfterLast('.', "").lowercase()
                        val type = when (ext) {
                            "html", "htm" -> "text/html"
                            "css" -> "text/css"
                            "js", "mjs" -> "application/javascript"
                            "svg" -> "image/svg+xml"
                            "json" -> "application/json"
                            else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "text/plain"
                        }
                        val content = runCatching { store.read(project, requested) }.getOrElse { "" }
                        return WebResourceResponse(type, "UTF-8",
                            ByteArrayInputStream(content.toByteArray(StandardCharsets.UTF_8)))
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                        val line = message.messageLevel().toString() + ": " + message.message()
                        logs = (logs + line.take(400)).takeLast(30)
                        return true
                    }
                }
                webView = this
            }
        }, modifier = Modifier.weight(1f).fillMaxWidth())
        if (logs.isNotEmpty()) {
            HorizontalDivider()
            Text("BROWSER CONSOLE", Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelSmall)
            Column(Modifier.fillMaxWidth().heightIn(max = 100.dp)
                .verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp)) {
                logs.takeLast(8).forEach { Text(it, style = MaterialTheme.typography.labelSmall) }
            }
        }
    }
    LaunchedEffect(webView, source, base) {
        delay(250)
        webView?.loadDataWithBaseURL(base, source, "text/html", "UTF-8", null)
    }
    DisposableEffect(webView) {
        val owned = webView
        onDispose { owned?.stopLoading(); owned?.destroy() }
    }
}

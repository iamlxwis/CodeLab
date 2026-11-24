package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Bundled, selectable notices stay accessible offline from Help & About. */
@Composable
internal fun LicenseNotices(close: () -> Unit) {
    val context = LocalContext.current
    val notices = remember { linkedMapOf(
        "Inter" to "Inter-OFL.txt", "JetBrains Mono" to "JetBrainsMono-OFL.txt",
        "Language icon sources" to "language-icons-SOURCE.txt",
        "Simple Icons" to "SimpleIcons-CC0.md", "Devicon" to "Devicon-MIT.txt",
        "Mozilla Rhino" to "rhino/LICENSE.txt", "Rhino notices" to "rhino/NOTICE.txt",
        "Rhino tools notices" to "rhino/NOTICE-tools.txt", "Rhino source" to "rhino/SOURCE.txt") }
    var selected by remember { mutableStateOf<String?>(null) }
    var content by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(selected) {
        content = null
        val path = notices[selected] ?: return@LaunchedEffect
        content = withContext(Dispatchers.IO) {
            runCatching { context.assets.open("licenses/$path").bufferedReader().use { it.readText() } }
                .getOrDefault("Could not open the bundled notice.")
        }
    }
    CodeLabDialog(onDismissRequest = { if (selected != null) selected = null else close() },
        properties = DialogProperties(usePlatformDefaultWidth = false)) {
        androidx.activity.compose.BackHandler(enabled = selected != null) { selected = null }
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (selected != null) selected = null else close() }) {
                        Icon(Icons.Default.ArrowBack, if (selected != null) "Back to notices" else "Close notices")
                    }
                    Text(selected ?: "Licenses & acknowledgements", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                }
                if (selected == null) Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Text("Third-party components retain their original licenses and trademarks.", Modifier.padding(vertical = 12.dp), style = MaterialTheme.typography.bodyMedium)
                    notices.keys.forEach { name ->
                        TextButton(onClick = { selected = name }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(name) }
                    }
                } else if (content == null) CircularProgressIndicator(Modifier.padding(24.dp))
                else SelectionContainer(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Text(content.orEmpty(), Modifier.padding(vertical = 12.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

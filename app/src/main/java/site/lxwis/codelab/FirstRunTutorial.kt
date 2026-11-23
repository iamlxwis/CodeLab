package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private data class TutorialPage(
    val title: String, val description: String, val details: String,
    val icon: ImageVector
)
private val pages = listOf(
    TutorialPage("Welcome to CodeLab", "Create your first project",
        "Tap New project, choose a language and select an example. Your files are saved locally.", Icons.Default.FolderOpen),
    TutorialPage("Write and run", "Code in the editor",
        "Open a file and tap Run. Online execution requires your approval. Offline Python needs a downloaded module.", Icons.Default.Code),
    TutorialPage("Make it yours", "Everything stays accessible",
        "Use Languages to manage runtimes, Appearance for themes and More to find tools. You can replay this guide in Settings.", Icons.Default.Tune)
)

/** Short, skippable tutorial: only shown automatically once, replayable in More > Settings. */
@Composable
internal fun FirstRunTutorial(onFinish: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val current = pages[page]
    Dialog(onDismissRequest = onFinish,
        properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 14.dp) {
            Column(Modifier.heightIn(max = 540.dp).verticalScroll(rememberScrollState())
                .padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("GETTING STARTED", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("${page + 1} of ${pages.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    repeat(pages.size) { index ->
                        LinearProgressIndicator(
                            progress = { if (index <= page) 1f else 0f },
                            modifier = Modifier.weight(1f).height(4.dp),
                            trackColor = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
                Surface(shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer) {
                    Icon(current.icon, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(16.dp).size(26.dp))
                }
                Text(current.title, style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold)
                Text(current.description, style = MaterialTheme.typography.titleMedium)
                Text(current.details, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onFinish) { Text("Skip") }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { if (page == pages.lastIndex) onFinish() else page++ }) {
                        Text(if (page == pages.lastIndex) "Start coding" else "Next")
                        Spacer(Modifier.width(6.dp))
                        Icon(if (page == pages.lastIndex) Icons.Default.Check
                            else Icons.Default.ArrowForward,
                            contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private data class GuideStep(
    val title: String, val subtitle: String, val instruction: String,
    val icon: ImageVector, val action: String? = null
)
private val guide = listOf(
    GuideStep("Welcome to CodeLab", "Your local workspace",
        "Projects are stored on your device. Search your library or sort by Recent, Favorites and name.",
        Icons.Default.FolderOpen),
    GuideStep("Create a project", "Start with New project",
        "Name the project, select a language and choose a starter example.",
        Icons.Default.Add, "newproject"),
    GuideStep("Choose language and template", "Editing works without a runtime",
        "Pick Beginner Example, Hello World or Blank. A starter template does not guarantee offline execution.",
        Icons.Default.Code, "newproject"),
    GuideStep("Import projects", "Bring an existing project into CodeLab",
        "Use Import for ZIP backups or public GitHub snapshots. Imports are copied to private project storage.",
        Icons.Default.Archive),
    GuideStep("Navigate the editor", "Tabs and panels",
        "Switch between open tabs. Use the file explorer, project search and editor commands to navigate.",
        Icons.Default.Description, "files"),
    GuideStep("Manage files", "Build your project structure",
        "Create and rename files or folders in the explorer. Use the tabs to return to a file.",
        Icons.Default.CreateNewFolder, "files"),
    GuideStep("Run code offline", "Run stays on your device",
        "Choose Offline in the visible selector near Run. If no module is installed, open Modules to install a compatible one.",
        Icons.Default.Computer, "languages"),
    GuideStep("Run code online", "Approval before source transfer",
        "Choose Online near Run. Source is sent to a configured provider only after you confirm the upload.",
        Icons.Default.Cloud),
    GuideStep("Language modules", "Install only what you need",
        "In Languages, inspect Python installation and other supported runtimes. Some engines are still being migrated.",
        Icons.Default.Extension, "languages"),
    GuideStep("Console and output", "Inspect results and errors",
        "Open Console after Run to read program output, execution status and error links to source.",
        Icons.Default.Terminal),
    GuideStep("Switch themes", "Five distinct styles",
        "In Appearance, compare Minimal, Glass, Clay, AMOLED and Paper using previews of actual app surfaces.",
        Icons.Default.Palette, "appearance"),
    GuideStep("Create an appearance preset", "Your own colors and saved looks",
        "Select an accent or enter #RRGGBB, type a preset name and save. Load, edit or delete presets later.",
        Icons.Default.ColorLens, "appearance"),
    GuideStep("Find tools and settings", "Use the More menu",
        "Project tools, editor preferences, languages, appearance and tutorial replay are within More.",
        Icons.Default.Settings, "actions"),
    GuideStep("Export a backup", "Protect your local files",
        "Export a project ZIP from the tools menu and keep it outside CodeLab before uninstalling the app.",
        Icons.Default.FileUpload, "exportproject")
)

/** Four essential steps by default; the remaining ten topics are optional. */
@Composable
internal fun FirstRunTutorial(
    onFinish: () -> Unit,
    onAction: (String) -> Unit = {},
    projectOpen: Boolean = false
) {
    var index by rememberSaveable { mutableIntStateOf(0) }
    var advanced by rememberSaveable { mutableStateOf(false) }
    val current = guide[index]
    val total = if (advanced) guide.size else 4
    Dialog(onDismissRequest = onFinish,
        properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Column(Modifier.heightIn(max = 590.dp).verticalScroll(rememberScrollState())
                .padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (index < 4) "GETTING STARTED" else "EXPLORE MORE",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium)
                    Text((index + 1).toString() + " of " + total,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LinearProgressIndicator(
                    progress = { (index + 1f) / total.toFloat() },
                    modifier = Modifier.fillMaxWidth())
                Surface(shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer) {
                    Icon(current.icon, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(18.dp).size(30.dp))
                }
                Text(current.title, style = MaterialTheme.typography.headlineSmall)
                Text(current.subtitle, style = MaterialTheme.typography.titleMedium)
                Text(current.instruction, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                val canTry = current.action != null && (projectOpen ||
                    current.action in setOf("newproject", "languages", "appearance", "actions"))
                if (canTry) {
                    OutlinedButton(onClick = {
                        onFinish()
                        current.action?.let(onAction)
                    }) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null,
                            modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Try it in CodeLab")
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = onFinish) {
                        Text(if (index == 3 || index == guide.lastIndex) "Finish basics" else "Skip")
                    }
                    Spacer(Modifier.weight(1f))
                    if (index > 0) TextButton(onClick = { index-- }) { Text("Back") }
                    Button(onClick = {
                        if (index == 3 && !advanced) {
                            advanced = true
                            index = 4
                        } else if (index < guide.lastIndex) index++
                        else onFinish()
                    }) {
                        Text(if (index == 3 && !advanced) "More topics"
                            else if (index == guide.lastIndex) "Done" else "Next")
                        Icon(Icons.Default.ArrowForward, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                    }
                }
                if (index == 3 && !advanced) Text(
                    "Finish now, or explore optional tools and customization.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

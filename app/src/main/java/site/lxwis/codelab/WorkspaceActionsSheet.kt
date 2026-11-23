package site.lxwis.codelab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class ActionEntry(
    val id: String, val name: String, val description: String,
    val section: String, val icon: ImageVector
)

private data class ActionSection(
    val title: String, val description: String, val icon: ImageVector
)

private val sections = listOf(
    ActionSection("Files & projects", "Create, organize and import", Icons.Default.FolderOpen),
    ActionSection("Editor & search", "Find, share and navigate", Icons.Default.Edit),
    ActionSection("More tools", "Advanced execution and optional tools", Icons.Default.Terminal),
    ActionSection("Settings", "Personalize your workspace", Icons.Default.Tune)
)

/**
 * Discoverable, two-level navigation replacing the unbounded workspace overflow menu.
 * All existing destinations are retained, but only the current category is displayed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WorkspaceActionsSheet(
    projectOpen: Boolean,
    fileOpen: Boolean,
    multipleProjects: Boolean,
    javascriptFile: Boolean,
    cloudReady: Boolean,
    focusMode: Boolean,
    quickActions: List<String>,
    onToggleQuick: (String) -> Unit,
    onAction: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var search by rememberSaveable { mutableStateOf("") }
    var section by rememberSaveable { mutableStateOf<String?>(null) }
    val all = buildList {
        fun add(id: String, name: String, description: String, section: String, icon: ImageVector) {
            this.add(ActionEntry(id, name, description, section, icon))
        }
        val files = "Files & projects"
        val editor = "Editor & search"
        val tools = "More tools"
        val settings = "Settings"
        if (!projectOpen) {
            add("newproject", "New project", "Start a local workspace", files, Icons.Default.Add)
            add("importzip", "Import project ZIP", "Restore a backed-up project", files, Icons.Default.FolderZip)
            add("folderimport", "Import device folder", "Copy an external folder into a project", files, Icons.Default.FolderOpen)
            add("githubimport", "Import GitHub source", "Download a public source snapshot", files, Icons.Default.Download)
        } else {
            add("files", "Browse files", "Open the project explorer", files, Icons.Default.FolderOpen)
            add("newfile", "New file", "Create a file in this project", files, Icons.Default.NoteAdd)
            add("newfolder", "New folder", "Organize the project", files, Icons.Default.CreateNewFolder)
            add("importfile", "Import file", "Copy a file from your device", files, Icons.Default.FileOpen)
            add("importmany", "Import multiple files", "Add several local files", files, Icons.Default.UploadFile)
            add("exportproject", "Export project ZIP", "Back up the complete project", files, Icons.Default.Archive)
            add("switchproject", "Switch project", "Open a different workspace", files, Icons.Default.SwapHoriz)
            if (fileOpen) {
                add("rename", "Rename or move file", "Change its project path", files, Icons.Default.DriveFileRenameOutline)
                add("duplicate", "Duplicate file", "Create a separate copy", files, Icons.Default.ContentCopy)
                add("exportcurrent", "Export current file", "Save this file outside CodeLab", files, Icons.Default.SaveAlt)
                add("properties", "File properties", "Path, size and encoding", files, Icons.Default.Info)
                if (multipleProjects)
                    add("copyproject", "Copy to another project", "Keep the original file", files, Icons.Default.FolderCopy)
            }
            add("projectsearch", "Find in project", "Search text across files", editor, Icons.Default.Search)
            add("projectreplace", "Replace across files", "Review and apply replacements", editor, Icons.Default.FindReplace)
            add("panels", "Panel layout", "Adjust editor and console sizes", editor, Icons.Default.ViewSidebar)
            add("commands", "Command palette", "Search every workspace action", editor, Icons.Default.ManageSearch)
            if (fileOpen) {
                add("screenshot", "Code snippet image", "Capture selected code or file lines", tools, Icons.Default.PhotoCamera)
                add("history", "File history", "Review previous snapshots", editor, Icons.Default.History)
            }
            add("focus", if (focusMode) "Leave focus mode" else "Focus mode",
                "Hide distractions while coding", editor, Icons.Default.Fullscreen)
            add("languages", "Languages", "Browse local and online availability", tools, Icons.Default.Code)
            if (cloudReady) add("cloudrun", "Run online", "Requires confirmation before upload", tools, Icons.Default.Cloud)
            if (javascriptFile) add("jsdebug", "JavaScript diagnostics", "Inspect recent execution", tools, Icons.Default.BugReport)
            add("git", "Version control", "Manage local Git history", tools, Icons.Default.AccountTree)
            add("workspacecommands", "Workspace commands", "Local file commands, not a Linux shell", tools, Icons.Default.Terminal)
            add("httpsinspector", "HTTPS inspector", "Inspect remote HTTP responses", tools, Icons.Default.Public)
            add("devtools", "Developer tools", "JSON, encoders and hashes", tools, Icons.Default.Build)
            add("performance", "Performance", "Review measured diagnostics", tools, Icons.Default.Speed)
            add("projectsettings", "Project settings", "Metadata and entrypoint", settings, Icons.Default.Settings)
        }
        add("trash", "Recently deleted", "Recover removed files and projects", "Files & projects", Icons.Default.RestoreFromTrash)
        if (!projectOpen) add("languages", "Languages", "Explore CodeLab file support",
            "More tools", Icons.Default.Code)
        add("appearance", "Appearance", "Theme, accent and motion", "Settings", Icons.Default.Palette)
        add("tutorial", "Quick start guide", "Resume or replay the 14-topic guide", "Settings", Icons.Default.School)
        add("accessibility", "Accessibility", "Contrast, text and feedback", "Settings", Icons.Default.AccessibilityNew)
    }
    val query = search.trim()
    val matches = all.filter {
        (section == null || it.section == section) &&
            (query.isBlank() || it.name.contains(query, true) || it.description.contains(query, true))
    }
    fun act(id: String) {
        onDismiss()
        onAction(id)
    }
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.88f).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (section != null && query.isBlank()) IconButton(onClick = { section = null }) {
                    Icon(Icons.Default.ArrowBack, "Back to categories")
                }
                Column(Modifier.weight(1f)) {
                    Text(section ?: "Workspace", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold)
                    Text(if (section == null) "Find an action or choose a category"
                        else "Select a tool to continue",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Close actions") }
            }
            OutlinedTextField(search, { search = it }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(14.dp),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = { Text(if (section == "Settings") "Search settings" else "Search actions") })
            if (section == null && query.isBlank()) {
                Text("QUICK ACTIONS", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                quickActions.mapNotNull { id -> all.firstOrNull { it.id == id } }
                    .chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { item ->
                            Surface(onClick = { act(item.id) }, modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                                Column(Modifier.fillMaxWidth().padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(item.icon, null, tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp))
                                    Text(item.name, style = MaterialTheme.typography.labelLarge,
                                        maxLines = 2)
                                }
                            }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                Text("Star actions below to customize your quick actions.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("BROWSE", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (section == null && query.isBlank()) {
                LazyColumn(Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)) {
                    items(sections) { group ->
                        Surface(onClick = { section = group.title },
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(14.dp)) {
                            Row(Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(group.icon, null, tint = MaterialTheme.colorScheme.primary)
                                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                                    Text(group.title, fontWeight = FontWeight.Medium)
                                    Text(group.description, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Default.ChevronRight, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)) {
                    if (matches.isEmpty()) item {
                        Text("No matching actions", Modifier.padding(18.dp))
                    }
                    items(matches, key = { it.id }) { item ->
                        Row(Modifier.fillMaxWidth().clickable { act(item.id) }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(item.icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                                Text(item.name, fontWeight = FontWeight.Medium)
                                Text(item.description, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { onToggleQuick(item.id) },
                                enabled = item.id in quickActions || quickActions.size < 6,
                                modifier = Modifier.size(48.dp)) {
                                Icon(if (item.id in quickActions) Icons.Default.Star
                                    else Icons.Default.StarBorder,
                                    if (item.id in quickActions) "Unpin ${item.name} quick action"
                                        else "Pin ${item.name} quick action")
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .45f))
                    }
                }
            }
        }
    }
}

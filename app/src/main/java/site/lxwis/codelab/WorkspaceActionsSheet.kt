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
import androidx.compose.animation.core.tween
import kotlinx.coroutines.launch

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
    focusMode: Boolean,
    quickActions: List<String>,
    onToggleQuick: (String) -> Unit,
    onMoveQuick: (String, Int) -> Unit = { _, _ -> },
    onAction: (String) -> Unit,
    onDismiss: () -> Unit,
    motionMillis: Int = 170
) {
    var search by rememberSaveable { mutableStateOf("") }
    var editQuick by rememberSaveable { mutableStateOf(false) }
    var section by rememberSaveable { mutableStateOf<String?>(null) }
    val all = buildList {
        fun add(id: String, name: String, description: String, section: String, icon: ImageVector) {
            this.add(ActionEntry(id, name, description, section, icon))
        }
        val files = "Files & projects"
        val editor = "Editor & search"
        val tools = "More tools"
        val settings = "Settings"
        add("settings", "Settings", "Appearance, workspace and accessibility", settings, Icons.Default.Settings)
        if (!projectOpen) {
            add("newproject", "New project", "Start a local workspace", files, Icons.Default.Add)
            add("importzip", "Import project ZIP", "Restore a backed-up project", files, Icons.Default.FolderZip)
            add("folderimport", "Import device folder", "Copy an external folder into a project", files, Icons.Default.FolderOpen)
            add("githubimport", "Import GitHub source", "Download a public source snapshot", files, Icons.Default.Download)
        } else {
            add("files", "Browse files", "Open the project explorer", files, Icons.Default.FolderOpen)
            add("newfile", "New file", "Create a file in this project", files, Icons.Default.NoteAdd)
            add("runfiles", "Run a file", "Choose a project file to execute", files, Icons.Default.PlayArrow)
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
                add("screenshot", "Snippet Studio", "Capture code, console output, or both", tools, Icons.Default.PhotoCamera)
                add("history", "File history", "Review previous snapshots", editor, Icons.Default.History)
            }
            add("focus", if (focusMode) "Leave focus mode" else "Focus mode",
                "Hide distractions while coding", editor, Icons.Default.Fullscreen)
            add("languages", "Languages", "Browse local and online availability", tools, Icons.Default.Code)
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.72f).padding(horizontal = 20.dp),
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
                ReimaginedSheetSizeButton(sheetState)
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Close actions") }
            }
            CodeLabSearch(search, { search = it }, if (section == "Settings") "Search settings" else "Search actions")
            if (section == null && query.isBlank()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Pinned actions", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = { editQuick = !editQuick }) { Text(if (editQuick) "Done" else "Edit") }
                }
            }
            if (section == null && query.isBlank()) {
                LazyColumn(Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)) {
                    items(quickActions.mapNotNull { id -> all.firstOrNull { it.id == id } }, key = { it.id }) { item ->
                        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(item.icon, null, Modifier.size(20.dp))
                            Text(item.name, Modifier.weight(1f).clickable { act(item.id) }.padding(8.dp),
                                style = MaterialTheme.typography.labelLarge)
                            if (editQuick) {
                            IconButton(onClick = { onMoveQuick(item.id, -1) }, enabled = quickActions.firstOrNull() != item.id) {
                                Icon(Icons.Default.ArrowUpward, "Move ${item.name} earlier")
                            }
                            IconButton(onClick = { onMoveQuick(item.id, 1) }, enabled = quickActions.lastOrNull() != item.id) {
                                Icon(Icons.Default.ArrowDownward, "Move ${item.name} later")
                            }
                            IconButton(onClick = { onToggleQuick(item.id) }) { Icon(Icons.Default.Close, "Unpin ${item.name}") }
                            } else Icon(Icons.Default.ChevronRight, null, Modifier.size(19.dp))
                        }
                    }
                    item { Text("Browse", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
                        ReimaginedEmptyState(Icons.Default.SearchOff, "No matching actions",
                            "Try a different search or clear the filter.", action = "Clear search",
                            onAction = { search = ""; section = null })
                    }
                    items(matches, key = { it.id }) { item ->
                        Row(Modifier.fillMaxWidth().animateItem(
                            fadeInSpec = if (motionMillis > 0) tween(motionMillis) else null,
                            placementSpec = if (motionMillis > 0) tween(motionMillis) else null,
                            fadeOutSpec = if (motionMillis > 0) tween(motionMillis) else null).clickable { act(item.id) }
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


/** Native partial/expanded sheet anchors, with an accessible alternative to dragging. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReimaginedSheetSizeButton(state: SheetState) {
    val scope = rememberCoroutineScope()
    if (state.hasPartiallyExpandedState && state.hasExpandedState) {
        val expanded = state.currentValue == SheetValue.Expanded
        IconButton(onClick = {
            scope.launch { if (expanded) state.partialExpand() else state.expand() }
        }) {
            Icon(if (expanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                if (expanded) "Collapse sheet" else "Expand sheet")
        }
    }
}

/** Consistent, actionable empty state using the current theme. */
@Composable
internal fun ReimaginedEmptyState(icon: ImageVector, title: String, detail: String,
    modifier: Modifier = Modifier, action: String? = null, onAction: (() -> Unit)? = null) {
    Column(modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Icon(icon, null, Modifier.padding(14.dp).size(28.dp),
                tint = MaterialTheme.colorScheme.primary)
        }
        Text(title, style = MaterialTheme.typography.titleMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (action != null && onAction != null) TextButton(onClick = onAction) { Text(action) }
    }
}

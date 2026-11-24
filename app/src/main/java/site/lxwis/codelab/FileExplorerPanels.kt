package site.lxwis.codelab

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.spring
import android.provider.Settings
import android.view.View
import android.view.WindowInsets as PlatformWindowInsets
import android.os.Build
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.ViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FileExplorerSheet(
    m: WorkspaceModel,
    initialDirectory: String = "",
    onClose: () -> Unit,
    onNew: () -> Unit,
    onOpen: (String) -> Unit,
    onRename: (String) -> Unit,
    onDelete: (String) -> Unit,
    onRenameFolder: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onNewFolder: () -> Unit
) {
    var search by rememberSaveable(m.project) { mutableStateOf("") }
    var expanded by remember(m.project, initialDirectory) {
        mutableStateOf((initialDirectory.ifBlank { m.active?.substringBeforeLast('/', "").orEmpty() }).trim('/').split('/').filter { it.isNotBlank() }
            .runningFold("") { prefix, part -> prefix + part + "/" }.drop(1).toSet())
    }
    var menuFor by remember { mutableStateOf<String?>(null) }
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val allFiles = m.files
    val rows = remember(allFiles, m.folders, expanded, search) {
        explorerRows(allFiles, m.folders, expanded, search)
    }

    ModalBottomSheet(onDismissRequest = onClose, sheetState = state,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(if (allFiles.size <= 3) .52f else .82f)
            .padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Explorer", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold)
                    Text(m.project.orEmpty() + " · " + allFiles.size + if (allFiles.size == 1) " file" else " files",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onNewFolder) { Icon(Icons.Default.CreateNewFolder, "New folder") }
                IconButton(onClick = onNew) { Icon(Icons.Default.NoteAdd, "New file") }
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Close explorer") }
            }
            CodeLabSearch(search, { search = it }, "Search files")
            LazyColumn(Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)) {
                items(rows, key = { if (it.folder) "folder:" + it.path else "file:" + it.path }) { row ->
                if (row.folder) {
                    val folder = row.path
                    Surface(onClick = { expanded = if (folder in expanded) expanded - folder else expanded + folder },
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(11.dp)) {
                        Row(Modifier.fillMaxWidth().padding(start = (8 + row.depth.coerceAtMost(6) * 14).dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (folder in expanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                                if (folder in expanded) "Collapse folder" else "Expand folder", Modifier.size(18.dp))
                            Icon(if (folder in expanded) Icons.Default.FolderOpen else Icons.Default.Folder, null, Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary)
                            Text(folder.removeSuffix("/").substringAfterLast('/'),
                                Modifier.weight(1f).padding(start = 12.dp),
                                fontWeight = FontWeight.Medium)
                            Box {
                                IconButton(onClick = { menuFor = folder }) {
                                    Icon(Icons.Default.MoreVert, "Folder actions")
                                }
                                DropdownMenu(expanded = menuFor == folder, onDismissRequest = { menuFor = null }) {
                                    DropdownMenuItem(text = { Text("Rename / move") },
                                        onClick = { menuFor = null; onRenameFolder(folder) })
                                    DropdownMenuItem(text = { Text("Move to trash") },
                                        onClick = { menuFor = null; onDeleteFolder(folder) })
                                }
                            }
                        }
                    }
                } else {
                    val file = row.path
                    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp)
                        .background(if (file == m.active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            RoundedCornerShape(10.dp)).padding(start = (8 + row.depth.coerceAtMost(6) * 14).dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        FileTypeIcon(file, Modifier.size(18.dp), selected = file == m.active)
                        Text(if (search.isBlank()) file.substringAfterLast('/') else file,
                            Modifier.weight(1f).clickable { onOpen(file) }
                                .padding(horizontal = 12.dp, vertical = 13.dp),
                            color = if (file == m.active) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace)
                        Box {
                            IconButton(onClick = { menuFor = file }) {
                                Icon(Icons.Default.MoreVert, "Actions for " + file)
                            }
                            DropdownMenu(expanded = menuFor == file, onDismissRequest = { menuFor = null }) {
                                DropdownMenuItem(text = { Text("Rename / move") },
                                    onClick = { menuFor = null; onRename(file) })
                                DropdownMenuItem(text = { Text("Move to trash", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = { menuFor = null; onDelete(file) })
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f))
                }
                }
                if (rows.isEmpty()) item {
                    Text("No matching files", Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
internal fun ProjectSearchDialog(m: WorkspaceModel, onDismiss: () -> Unit,
    onHit: (String, Int) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var matchCase by rememberSaveable { mutableStateOf(false) }
    var folder by rememberSaveable { mutableStateOf("") }
    var kind by remember { mutableStateOf<ProjectFileKind?>(null) }
    val grouped = m.searchHits.groupBy { it.path }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Search project") },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(query, { query = it; m.clearSearchHits() },
                    Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text("Search text") })
                SearchScopeFilters(m.folders, folder, kind,
                    onFolder = { folder = it; m.clearSearchHits() },
                    onKind = { kind = it; m.clearSearchHits() })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = matchCase,
                        onCheckedChange = { matchCase = it; m.clearSearchHits() })
                    Text("Match case")
                    Spacer(Modifier.weight(1f))
                    FilledTonalButton(enabled = query.isNotBlank(),
                        onClick = { m.searchProject(query, matchCase, folder, kind) }) {
                        Text("Search")
                    }
                }
                Text("${m.searchHits.size} results in ${grouped.size} files · first 200 matches",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyColumn(Modifier.heightIn(max = 340.dp)) {
                    grouped.forEach { (path, hits) ->
                        item(key = "group:$path") {
                            Text("$path · ${hits.size}",
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace)
                        }
                        items(hits, key = { "$path:${it.line}" }) { hit ->
                            Column(Modifier.fillMaxWidth().clickable {
                                onHit(hit.path, hit.line)
                            }.padding(vertical = 9.dp)) {
                                Text("Line ${hit.line}",
                                    style = MaterialTheme.typography.labelSmall)
                                Text(hit.preview, maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } })
}

@Composable
internal fun TrashDialog(m: WorkspaceModel, onDismiss: () -> Unit) {
    var confirmEmpty by remember { mutableStateOf(false) }
    if (confirmEmpty) AlertDialog(onDismissRequest = { confirmEmpty = false },
        title = { Text("Permanently delete trash?") },
        text = { Text("This removes all deleted projects and files. This cannot be undone.") },
        confirmButton = { TextButton(onClick = { m.emptyTrash(); confirmEmpty = false }) {
            Text("Delete permanently", color = MaterialTheme.colorScheme.error)
        } }, dismissButton = { TextButton(onClick = { confirmEmpty = false }) { Text("Cancel") } })
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Recently deleted") },
        text = {
            Column {
                Text("Deleted projects and files remain on this device until emptied.",
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                LazyColumn(Modifier.heightIn(max = 320.dp)) {
                    items(m.trashItems, key = { it.id }) { item ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(if (item.kind == "project") item.project else item.path,
                                    maxLines = 2, style = MaterialTheme.typography.bodyMedium)
                                Text(item.kind + " · " + item.project,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(enabled = true, onClick = { m.restoreTrash(item.id) }) {
                                Text("Restore")
                            }
                        }
                    }
                }
                if (m.trashItems.isEmpty()) Text("Trash is empty.")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
        dismissButton = {
            if (m.trashItems.isNotEmpty()) TextButton(enabled = true,
                onClick = { confirmEmpty = true }) { Text("Empty trash", color = MaterialTheme.colorScheme.error) }
        })
}

@Composable
internal fun FileHistoryDialog(m: WorkspaceModel, onDismiss: () -> Unit) {
    var restoreId by remember { mutableStateOf<String?>(null) }
    if (restoreId != null) AlertDialog(onDismissRequest = { restoreId = null },
        title = { Text("Restore this version?") },
        text = { Text("Your current file will be saved to history before restoration.") },
        confirmButton = { TextButton(onClick = {
            restoreId?.let(m::restoreHistory)
            restoreId = null
        }) { Text("Restore") } },
        dismissButton = { TextButton(onClick = { restoreId = null }) { Text("Cancel") } })
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("File history") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Local snapshots are captured during saves, at most once every 20 seconds. Up to 12 versions per file.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (m.fileHistory.isEmpty()) Text(if (m.busy) "Loading…" else "No older versions yet.")
                LazyColumn(Modifier.heightIn(max = 240.dp)) {
                    items(m.fileHistory, key = { it.id }) { version ->
                        Row(Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(java.text.DateFormat.getDateTimeInstance()
                                    .format(java.util.Date(version.savedAt)),
                                    style = MaterialTheme.typography.bodyMedium)
                                Text("${version.bytes} bytes",
                                    style = MaterialTheme.typography.labelSmall)
                            }
                            TextButton(enabled = true, onClick = {
                                m.previewHistory(version.id)
                            }) { Text("View") }
                            TextButton(enabled = true, onClick = {
                                restoreId = version.id
                            }) { Text("Restore") }
                        }
                    }
                }
                m.historyPreview?.let { old ->
                    HorizontalDivider()
                    Text("Snapshot vs current · + added, − removed",
                        style = MaterialTheme.typography.labelSmall)
                    val rows = remember(old, m.source) {
                        LineDiff.compare(old, m.source.take(5000))
                            .filter { it.kind != LineDiff.Kind.SAME }
                            .take(120)
                    }
                    Column(Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState())
                        .padding(6.dp)) {
                        if (rows.isEmpty()) Text("No changes in displayed excerpt.")
                        rows.forEach { entry ->
                            val marker = when (entry.kind) {
                                LineDiff.Kind.ADDED -> "+ "
                                LineDiff.Kind.REMOVED -> "− "
                                LineDiff.Kind.SAME -> "  "
                            }
                            Text(marker + entry.text,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = when (entry.kind) {
                                    LineDiff.Kind.ADDED -> Color(0xff48b981)
                                    LineDiff.Kind.REMOVED -> Color(0xffef7a7a)
                                    LineDiff.Kind.SAME -> MaterialTheme.colorScheme.onSurface
                                })
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } })
}

@Composable
internal fun ProjectReplaceDialog(m: WorkspaceModel, onDismiss: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var replacement by rememberSaveable { mutableStateOf("") }
    var matchCase by rememberSaveable { mutableStateOf(false) }
    var regex by rememberSaveable { mutableStateOf(false) }
    var folder by rememberSaveable { mutableStateOf("") }
    var kind by remember { mutableStateOf<ProjectFileKind?>(null) }
    var confirm by remember { mutableStateOf(false) }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false },
        title = { Text("Replace in multiple files?") },
        text = { Text("Replace " + (m.replaceSummary?.matches ?: 0) + " matches in " +
            (m.replaceSummary?.files ?: 0) + " files? Previous versions can be restored from file history.") },
        confirmButton = { TextButton(onClick = {
            m.replaceAcrossFiles(query, replacement, matchCase, regex, folder, kind)
            confirm = false
            onDismiss()
        }) { Text("Replace all") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel") } })
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("Replace across project") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(query, {
                    query = it
                    m.clearReplaceSummary()
                }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true, label = { Text("Find") })
                OutlinedTextField(replacement, {
                    replacement = it
                    m.clearReplaceSummary()
                }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true, label = { Text("Replace with") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(matchCase, onCheckedChange = {
                        matchCase = it
                        m.clearReplaceSummary()
                    })
                    Text("Aa", style = MaterialTheme.typography.labelMedium)
                    Checkbox(regex, onCheckedChange = {
                        regex = it
                        m.clearReplaceSummary()
                    })
                    Text("Regex", style = MaterialTheme.typography.labelMedium)
                }
                SearchScopeFilters(m.folders, folder, kind,
                    onFolder = { folder = it; m.clearReplaceSummary() },
                    onKind = { kind = it; m.clearReplaceSummary() })
                Button(enabled = query.isNotBlank(),
                    onClick = { m.previewReplaceAcrossFiles(query, replacement, matchCase,
                        regex, folder, kind) },
                    modifier = Modifier.fillMaxWidth()) { Text("Preview changes") }
                m.replaceSummary?.let { summary ->
                    Text("${summary.matches} matches in ${summary.files} files",
                        fontWeight = FontWeight.SemiBold)
                    ReplaceMatchPreview(m.replaceHits)
                    if (summary.matches > m.replaceHits.size)
                        Text("Showing first ${m.replaceHits.size} individual matches",
                            style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            TextButton(enabled = (m.replaceSummary?.matches ?: 0) > 0,
                onClick = { confirm = true }) { Text("Apply previewed changes") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } })
}

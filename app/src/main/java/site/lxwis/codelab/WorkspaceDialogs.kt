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

@Composable
internal fun NameDialog(title: String, initial: String, label: String, example: String, cancel: () -> Unit, submit: (String) -> Unit) {
    var name by rememberSaveable(title, initial) { mutableStateOf(initial) }
    AlertDialog(onDismissRequest = cancel, title = { Text(title) },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text(label) }, placeholder = { Text(example) }) },
        confirmButton = { TextButton(onClick = { submit(name.trim()) }, enabled = name.isNotBlank()) { Text(if (title.startsWith("New")) "Create" else if (title.startsWith("Import")) "Import" else "Rename") } },
        dismissButton = { TextButton(onClick = cancel) { Text("Cancel") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateProjectDialog(cancel: () -> Unit, submit: (String, String) -> Unit) {
    val keyboard = LocalSoftwareKeyboardController.current
    var name by rememberSaveable { mutableStateOf("") }
    var template by rememberSaveable { mutableStateOf("JavaScript") }
    var browsing by rememberSaveable { mutableStateOf(false) }
    var search by rememberSaveable { mutableStateOf("") }
    var offlineOnly by rememberSaveable { mutableStateOf(false) }
    val languages = LanguageCatalog.entries.filter {
        (!offlineOnly || it.local != null) &&
            (search.isBlank() || it.name.contains(search.trim(), true) ||
                it.extension.contains(search.trim(), true))
    }
    ModalBottomSheet(onDismissRequest = cancel,
        modifier = Modifier.imePadding(),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.86f)
            .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (browsing) {
                    IconButton(onClick = { browsing = false; keyboard?.hide() }) {
                        Icon(Icons.Default.ArrowBack, "Back to project details")
                    }
                    Text("Choose language", modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineSmall)
                } else {
                    Text("New project", modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineSmall)
                    Button(onClick = { keyboard?.hide(); submit(name.trim(), template) },
                        enabled = name.isNotBlank(), shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) {
                        Text("Create project")
                    }
                }
            }
            if (!browsing) {
            Text("Choose a language, then create your workspace.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(name, { name = it.take(100) }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
                label = { Text("Project name") }, placeholder = { Text("e.g. Playground") })
            Text("POPULAR", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                listOf(
                    listOf("JavaScript", "Java", "Python"),
                    listOf("Lua", "SQLite", "Web")
                ).forEach { row ->
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        row.forEach { choice ->
                            FilterChip(
                                selected = template == choice,
                                onClick = { template = choice; browsing = false },
                                modifier = Modifier.weight(1f),
                                label = { Text(choice, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis) })
                        }
                    }
                }
            }
            Text("STARTER TEMPLATES", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("To-do app", "API fetch", "Algorithms", "SQL playground", "Lua game loop")
                    .chunked(2).forEach { choices ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            choices.forEach { choice ->
                                FilterChip(selected = template == choice,
                                    onClick = { template = choice; browsing = false },
                                    modifier = Modifier.weight(1f),
                                    label = { Text(choice, maxLines = 1,
                                        overflow = TextOverflow.Ellipsis) })
                            }
                            if (choices.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
            }
            Surface(onClick = { browsing = !browsing; keyboard?.hide() },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Row(Modifier.fillMaxWidth().padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(template, fontWeight = FontWeight.SemiBold)
                        Text("Browse ${LanguageCatalog.entries.size} languages & file types",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ExpandMore, contentDescription = "Browse languages")
                }
            }
            }
            if (browsing) {
                Text("Search all languages and file types. Editing is offline; execution depends on the runtime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(search, { search = it }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true, label = { Text("Search language or extension") },
                    leadingIcon = { Icon(Icons.Default.Search, null) })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(selected = offlineOnly, onClick = { offlineOnly = !offlineOnly },
                        label = { Text("Offline-capable") })
                    Spacer(Modifier.width(10.dp))
                    Text("${languages.size} results", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LazyColumn(Modifier.weight(1f)) {
                    items(languages, key = { it.name }) { language ->
                        Row(Modifier.fillMaxWidth().clickable {
                            template = language.name; browsing = false; keyboard?.hide()
                        }.semantics { contentDescription = "Select " + language.name }
                            .padding(vertical = 11.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(language.name)
                                Text(language.local ?: "Edit offline · online run not guaranteed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("." + language.extension, fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.labelSmall)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f))
                    }
                }
            } else {
                val info = if (template == "Web") "Offline HTML/CSS/JS preview"
                    else LanguageCatalog.entries.firstOrNull { it.name == template }?.local
                        ?.let { "$it · included with CodeLab" }
                        ?: "Edit offline. Running this language may require an online compiler."
                Text(info, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FileExplorerSheet(
    m: WorkspaceModel,
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
    var directory by rememberSaveable(m.project) { mutableStateOf("") }
    var menuFor by remember { mutableStateOf<String?>(null) }
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val allFiles = m.files
    val matching = if (search.isBlank()) allFiles.filter { it.startsWith(directory) }
        else allFiles.filter { it.contains(search, ignoreCase = true) }
    val folders = if (search.isNotBlank()) emptyList() else (matching.mapNotNull {
        val next = it.removePrefix(directory).substringBefore('/', "")
        if (next.isEmpty()) null else directory + next + "/"
    } + m.folders.filter { it.startsWith(directory) }.mapNotNull {
        val next = it.removePrefix(directory).substringBefore('/', "")
        if (next.isEmpty()) null else directory + next + "/"
    }).distinct().sorted()
    val visibleFiles = if (search.isBlank()) matching.filter {
        !it.removePrefix(directory).contains('/')
    } else matching

    ModalBottomSheet(onDismissRequest = onClose, sheetState = state,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.85f)
            .padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Explorer", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold)
                    Text(m.project.orEmpty() + " · " + allFiles.size + " files",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onNewFolder, enabled = true) { Text("Folder") }
                FilledTonalButton(onClick = onNew, shape = RoundedCornerShape(11.dp)) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("File")
                }
            }
            OutlinedTextField(search, { search = it }, Modifier.fillMaxWidth(),
                singleLine = true, label = { Text("Search files") },
                shape = RoundedCornerShape(12.dp))
            if (directory.isNotEmpty() && search.isBlank()) {
                TextButton(onClick = {
                    directory = directory.removeSuffix("/").substringBeforeLast('/', "")
                        .let { if (it.isEmpty()) "" else it + "/" }
                }) { Icon(Icons.Default.ArrowBack, null, Modifier.size(18.dp)); Text("Parent folder") }
            }
            LazyColumn(Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)) {
                items(folders, key = { "folder:" + it }) { folder ->
                    Surface(onClick = { directory = folder },
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(11.dp)) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FolderOpen, null, Modifier.size(18.dp),
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
                }
                items(visibleFiles, key = { "file:" + it }) { file ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FolderOpen, null, Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (search.isBlank()) file.removePrefix(directory) else file,
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
                                DropdownMenuItem(text = { Text("Delete") },
                                    onClick = { menuFor = null; onDelete(file) })
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f))
                }
                if (folders.isEmpty() && visibleFiles.isEmpty()) item {
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
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Search project") },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(),
                    singleLine = true, label = { Text("Find text in all files") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = matchCase, onCheckedChange = { matchCase = it })
                    Text("Match case")
                    Spacer(Modifier.weight(1f))
                    FilledTonalButton(enabled = query.isNotBlank(),
                        onClick = { m.searchProject(query, matchCase) }) { Text("Search") }
                }
                Text("${m.searchHits.size} results · first 200 matches",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyColumn(Modifier.heightIn(max = 340.dp)) {
                    items(m.searchHits) { hit ->
                        Column(Modifier.fillMaxWidth().clickable { onHit(hit.path, hit.line) }
                            .padding(vertical = 9.dp)) {
                            Text("${hit.path}:${hit.line}", fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium)
                            Text(hit.preview, maxLines = 2, overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
    var confirm by remember { mutableStateOf(false) }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false },
        title = { Text("Replace in multiple files?") },
        text = { Text("Replace " + (m.replaceSummary?.matches ?: 0) + " matches in " +
            (m.replaceSummary?.files ?: 0) + " files? Previous versions can be restored from file history.") },
        confirmButton = { TextButton(onClick = {
            m.replaceAcrossFiles(query, replacement, matchCase, regex)
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
                Button(enabled = query.isNotBlank(),
                    onClick = { m.previewReplaceAcrossFiles(query, replacement, matchCase, regex) },
                    modifier = Modifier.fillMaxWidth()) { Text("Preview changes") }
                m.replaceSummary?.let { summary ->
                    Text("${summary.matches} matches in ${summary.files} files",
                        fontWeight = FontWeight.SemiBold)
                    summary.paths.take(12).forEach { path ->
                        Text(path, fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (summary.paths.size > 12)
                        Text("… and ${summary.paths.size - 12} more files")
                }
            }
        },
        confirmButton = {
            TextButton(enabled = (m.replaceSummary?.matches ?: 0) > 0,
                onClick = { confirm = true }) { Text("Apply previewed changes") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } })
}

@Composable
internal fun ProjectSwitcher(m: WorkspaceModel, dismiss: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    AlertDialog(onDismissRequest = dismiss,
        title = { Text("Switch workspace") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search projects") }, singleLine = true)
                val visible = m.projects.filter { it.contains(query, ignoreCase = true) }
                    .sortedWith(compareBy<String> { it !in m.favorites }
                        .thenBy { m.projects.indexOf(it) })
                LazyColumn(Modifier.heightIn(max = 330.dp)) {
                    items(visible, key = { it }) { name ->
                        Row(Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically) {
                            if (name in m.favorites)
                                Icon(Icons.Default.Star, null, Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary)
                            TextButton(enabled = true, modifier = Modifier.weight(1f),
                                onClick = { m.openProject(name); dismiss() }) {
                                Text(name, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = dismiss) { Text("Close") } })
}

@Composable
internal fun GitDialog(m: WorkspaceModel, dismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("git-identity", 0) }
    var message by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf(prefs.getString("author", "") ?: "") }
    var email by rememberSaveable { mutableStateOf(prefs.getString("email", "") ?: "") }
    var branch by rememberSaveable { mutableStateOf("") }
    val state = m.gitState
    AlertDialog(onDismissRequest = dismiss,
        title = { Text("Git · local history") },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (m.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (state == null) Text("Loading repository status…")
                else if (!state.initialized) {
                    Text("No repository in this project.")
                    Button(enabled = true, onClick = m::gitInit) { Text("Initialize Git") }
                } else {
                    Text("Branch · " + state.branch, fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.titleSmall)
                    Row(Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(enabled = true, onClick = m::gitRefresh) { Text("Refresh") }
                        state.branches.forEach { name ->
                            FilterChip(selected = name == state.branch, enabled = true,
                                onClick = { m.gitCheckout(name) }, label = { Text(name) })
                        }
                    }
                    OutlinedTextField(branch, { branch = it }, modifier = Modifier.fillMaxWidth(),
                        label = { Text("New branch") }, singleLine = true)
                    TextButton(enabled = branch.isNotBlank(), onClick = {
                        m.gitBranch(branch)
                        branch = ""
                    }) { Text("Create and switch branch") }
                    HorizontalDivider()
                    Text("Working tree · " + state.changes.size + " entries",
                        style = MaterialTheme.typography.titleSmall)
                    state.changes.take(20).forEach { change ->
                        Text(change.kind + " · " + change.path, fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall, maxLines = 2)
                    }
                    if (state.changes.size > 20)
                        Text("Additional entries not shown.", style = MaterialTheme.typography.labelSmall)
                    HorizontalDivider()
                    Text("Commit all changes", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(message, { message = it.take(500) },
                        modifier = Modifier.fillMaxWidth(), label = { Text("Commit message") },
                        maxLines = 3)
                    OutlinedTextField(author, { author = it.take(100) },
                        modifier = Modifier.fillMaxWidth(), label = { Text("Author name") },
                        singleLine = true)
                    OutlinedTextField(email, { email = it.take(200) },
                        modifier = Modifier.fillMaxWidth(), label = { Text("Author email") },
                        singleLine = true)
                    Button(enabled = message.isNotBlank()
                        && author.isNotBlank() && email.contains('@') && state.changes.isNotEmpty(),
                        onClick = {
                            prefs.edit().putString("author", author).putString("email", email).apply()
                            m.gitCommit(message, author, email)
                            message = ""
                        }, modifier = Modifier.fillMaxWidth()) { Text("Commit locally") }
                    HorizontalDivider()
                    Text("Recent commits", style = MaterialTheme.typography.titleSmall)
                    if (state.commits.isEmpty()) Text("No commits yet.")
                    state.commits.forEach { commit ->
                        Text(commit.shortId + " · " + commit.message + " — " + commit.author,
                            style = MaterialTheme.typography.bodySmall, maxLines = 2)
                    }
                    Text("Git operations work offline. Remote pushes and GitHub authentication are not configured.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = { TextButton(onClick = dismiss) { Text("Done") } })
}

@Composable
internal fun CommandPaletteDialog(m: WorkspaceModel, dismiss: () -> Unit,
    execute: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val actions = listOf(
        "newproject" to "Create project",
        "newfile" to "Create file",
        "newfolder" to "Create folder",
        "files" to "Browse files",
        "projectsearch" to "Search project",
        "projectreplace" to "Replace across project",
        "screenshot" to "Code Screenshot Studio",
        "history" to "File history",
        "git" to "Git version control",
        "devtools" to "Developer tools",
        "languages" to "Language manager",
        "appearance" to "Appearance",
        "accessibility" to "Accessibility",
        "performance" to "Performance diagnostics",
        "panels" to "Panel layout"
    )
    AlertDialog(onDismissRequest = dismiss,
        title = { Text("Command palette") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(query, { query = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    placeholder = { Text("Search actions") })
                LazyColumn(Modifier.heightIn(max = 340.dp)) {
                    items(actions.filter { it.second.contains(query, ignoreCase = true) }, key = { it.first }) { item ->
                        val requiresFile = item.first in setOf("screenshot", "history")
                        val requiresProject = item.first in setOf("newfile", "newfolder", "files",
                            "projectsearch", "projectreplace", "screenshot", "history", "git", "panels")
                        TextButton(enabled = (!requiresProject || m.project != null) &&
                            (!requiresFile || m.active != null),
                            modifier = Modifier.fillMaxWidth(), onClick = { execute(item.first) }) {
                            Text(item.second, modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start)
                        }
                    }
                }
                Text("Tip: Ctrl+Shift+P with a hardware keyboard",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { TextButton(onClick = dismiss) { Text("Close") } })
}

@Composable
internal fun GitHubImportDialog(m: WorkspaceModel, dismiss: () -> Unit) {
    var slug by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(onDismissRequest = dismiss,
        title = { Text("Import public GitHub project") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(slug, { slug = it.take(190) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("owner/repository or GitHub URL") },
                    placeholder = { Text("owner/example") }, singleLine = true)
                OutlinedTextField(name, { name = it.take(100) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Local project name") }, singleLine = true)
                Text("Requires internet. Imports a source snapshot from a public GitHub repository, not commit history. " +
                    "Project size and file safety limits apply. No token or account is needed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(enabled = slug.isNotBlank() && name.isNotBlank(),
                onClick = {
                    m.importPublicGitHub(slug, name)
                    dismiss()
                }) { Text("Import") }
        },
        dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } })
}

@Composable
internal fun ProjectSettingsDialog(m: WorkspaceModel, dismiss: () -> Unit) {
    var description by remember(m.project) { mutableStateOf(m.projectMetadata.description) }
    var entrypoint by remember(m.project) { mutableStateOf(m.projectMetadata.entrypoint) }
    var filesExpanded by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = dismiss,
        title = { Text("Project settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Template · " + m.projectMetadata.template,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(description, { description = it.take(500) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2, maxLines = 4,
                    label = { Text("Project description") })
                Text("Preferred entry file", style = MaterialTheme.typography.labelMedium)
                Box {
                    OutlinedButton(onClick = { filesExpanded = true }, enabled = m.files.isNotEmpty()) {
                        Text(if (entrypoint.isEmpty()) "Not configured" else entrypoint,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    DropdownMenu(expanded = filesExpanded,
                        onDismissRequest = { filesExpanded = false }) {
                        DropdownMenuItem(text = { Text("No preference") }, onClick = {
                            entrypoint = ""
                            filesExpanded = false
                        })
                        m.files.take(80).forEach { path ->
                            DropdownMenuItem(text = { Text(path, maxLines = 2) },
                                onClick = { entrypoint = path; filesExpanded = false })
                        }
                    }
                }
                Text("This selects a preferred entry file; Run continues to execute the active editor tab.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Settings are kept inside the project and included in ZIP backups.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (entrypoint.isNotEmpty()) TextButton(enabled = true,
                    onClick = { m.select(entrypoint); dismiss() }) {
                    Text("Open entry file")
                }
            }
        },
        confirmButton = {
            TextButton(enabled = true, onClick = {
                m.configureProject(description, entrypoint)
                dismiss()
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } })
}

@Composable
internal fun JavaScriptDebugDialog(m: WorkspaceModel, dismiss: () -> Unit) {
    var breakpointText by rememberSaveable { mutableStateOf("") }
    val parts = breakpointText.split(',', ' ', ';', '\n')
        .filter { it.isNotBlank() }
    val lines = parts.mapNotNull(String::toIntOrNull)
    val valid = parts.size == lines.size && lines.all { it in 1..100000 } && lines.size <= 80
    AlertDialog(onDismissRequest = dismiss, title = { Text("JavaScript debugger") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Execution pauses at the first statement. Use Step or Continue in the console.",
                    style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(breakpointText, { breakpointText = it.take(600) },
                    label = { Text("Optional breakpoint line numbers") },
                    placeholder = { Text("4, 8, 15") },
                    modifier = Modifier.fillMaxWidth(), maxLines = 3)
                Text("Debugger: Mozilla Rhino, offline. Applies to the active .js file. " +
                    "JavaScript may continue until the timeout limit while paused.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!valid) Text("Enter up to 80 positive line numbers.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error)
            }
        }, confirmButton = {
            TextButton(enabled = valid && !m.runner.running,
                onClick = {
                    m.debugCurrent(lines)
                    dismiss()
                }) { Text("Start debugging") }
        }, dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } })
}

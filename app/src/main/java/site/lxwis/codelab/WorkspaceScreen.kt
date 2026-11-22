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
internal fun Studio(m: WorkspaceModel, modifier: Modifier) {
    var dialog by rememberSaveable { mutableStateOf("") }
    var selected by rememberSaveable { mutableStateOf("") }
    var incoming by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedSnippet by remember { mutableStateOf("") }
    var selectedSnippetLine by remember { mutableIntStateOf(1) }
    var focusMode by rememberSaveable { mutableStateOf(false) }
    var explorerPinned by rememberSaveable { mutableStateOf(true) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptics = LocalHapticFeedback.current
    val systemMotionEnabled = remember(context) {
        Settings.Global.getFloat(context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }
    val motionEnabled = when (m.motionPreference) {
        "Full" -> true
        "Reduced" -> false
        else -> systemMotionEnabled
    }
    val motionMillis = if (motionEnabled) (150 + m.motionIntensity / 2).coerceIn(150, 200) else 0
    val activity = context as? ComponentActivity
    DisposableEffect(activity, focusMode) {
        val window = activity?.window
        val previousFlags = window?.decorView?.systemUiVisibility ?: 0
        if (focusMode) {
            if (Build.VERSION.SDK_INT >= 30) {
                window?.insetsController?.hide(
                    PlatformWindowInsets.Type.statusBars() or PlatformWindowInsets.Type.navigationBars())
                window?.insetsController?.systemBarsBehavior =
                    android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                window?.decorView?.systemUiVisibility = previousFlags or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            }
        }
        onDispose {
            if (Build.VERSION.SDK_INT >= 30) {
                window?.insetsController?.show(
                    PlatformWindowInsets.Type.statusBars() or PlatformWindowInsets.Type.navigationBars())
            } else window?.decorView?.systemUiVisibility = previousFlags
        }
    }
    var importedFile by rememberSaveable { mutableStateOf<String?>(null) }
    val exportFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) {
        it?.let(m::exportFile)
    }
    val importMany = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) m.importFiles(uris)
    }
    val importFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) {
            importedFile = it.toString()
            dialog = "importfile"
        }
    }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { it?.let(m::export) }
    var folderImportName by rememberSaveable { mutableStateOf("") }
    val importTree = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null && folderImportName.isNotBlank()) {
            m.importExternalFolder(uri, folderImportName)
            folderImportName = ""
        }
    }
    val importZip = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) { incoming = it.toString(); dialog = "import" }
    }
    val p = m.project
    LaunchedEffect(m.active) {
        selectedSnippet = ""
        selectedSnippetLine = 1
    }
    fun openAction(action: String) {
        when (action) {
            "importfile" -> importFile.launch(arrayOf("*/*"))
            "importmany" -> importMany.launch(arrayOf("*/*"))
            "importzip" -> importZip.launch(arrayOf("*/*"))
            "exportcurrent" -> m.active?.let { exportFile.launch(it.substringAfterLast('/')) }
            "exportproject" -> p?.let { export.launch(it + ".zip") }
            "rename", "duplicate" -> m.active?.let { selected = it; dialog = action }
            "projectreplace" -> { m.clearReplaceSummary(); dialog = action }
            "history" -> { m.refreshHistory(); dialog = action }
            "git" -> { m.gitRefresh(); dialog = action }
            "cloudrun" -> m.requestCloudRun()
            "focus" -> {
                focusMode = !focusMode
                m.console = false
                m.preview = false
            }
            else -> dialog = action
        }
    }
    BackHandler(enabled = p != null && dialog.isEmpty()) {
        if (focusMode) focusMode = false
        else if (m.preview) m.preview = false else if (m.console) m.console = false else m.home()
    }

    Column(modifier.fillMaxSize()) {
        if (!focusMode || p == null) WorkspaceTopBar(m, p) { dialog = it }
        if (p == null) {
            ProjectLibrary(m, motionMillis, { dialog = "newproject" },
                { importZip.launch(arrayOf("*/*")) }, { dialog = "githubimport" },
                { selected = it; dialog = "renameproject" },
                { selected = it; dialog = "deleteproject" },
                { selected = it; dialog = "duplicateproject" })
        } else {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            if (!focusMode) Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { dialog = "files" }) {
                    Icon(Icons.Default.FolderOpen, "Browse files")
                }
                if (m.files.isNotEmpty()) {
                    IconButton(onClick = { explorerPinned = !explorerPinned },
                        modifier = Modifier.size(38.dp)) {
                        Icon(if (explorerPinned) Icons.Default.VerticalSplit else Icons.Default.ViewSidebar,
                            "Toggle tablet explorer", Modifier.size(19.dp))
                    }
                }
                LazyRow(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    items(m.tabs, key = { it }) { path ->
                        val active = m.active == path
                        val tabColor by animateColorAsState(
                            if (active) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
                            animationSpec = tween(motionMillis), label = "tab-color")
                        Row(Modifier.padding(end = 2.dp)
                            .background(tabColor,
                                RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp)), verticalAlignment = Alignment.CenterVertically) {
                            Text(path.substringAfterLast('/'), Modifier.clickable {
                                UiLatencyProbe.nextFrame(context, "Switch tab")
                                if (m.touchFeedback) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                m.select(path)
                            }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                            if (active) IconButton(onClick = { m.closeTab(path) }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Close, "Close $path", Modifier.size(16.dp)) }
                        }
                    }
                }
                IconButton(onClick = { dialog = "newfile" }) { Icon(Icons.Default.Add, "Add file") }
            }
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                val wide = maxWidth >= 820.dp
                val showSidebar = wide && explorerPinned && !focusMode
                Row(Modifier.fillMaxSize()) {
                    AnimatedVisibility(visible = showSidebar,
                        enter = expandHorizontally(animationSpec = tween(motionMillis)) +
                            fadeIn(animationSpec = tween(motionMillis)),
                        exit = shrinkHorizontally(animationSpec = tween(motionMillis)) +
                            fadeOut(animationSpec = tween(motionMillis))) {
                        Row {
                        Surface(Modifier.width(224.dp).fillMaxHeight(),
                            color = MaterialTheme.colorScheme.surfaceContainer) {
                            Column(Modifier.fillMaxSize().padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountTree, null, Modifier.size(17.dp),
                                        tint = MaterialTheme.colorScheme.primary)
                                    Text("EXPLORER", Modifier.weight(1f).padding(start = 9.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { dialog = "files" }, Modifier.size(36.dp)) {
                                        Icon(Icons.Default.OpenInFull, "Open file explorer", Modifier.size(17.dp))
                                    }
                                }
                                HorizontalDivider()
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    items(m.files, key = { it }) { file ->
                                        val active = file == m.active
                                        Surface(onClick = {
                                            UiLatencyProbe.nextFrame(context, "Explorer select")
                                            if (m.touchFeedback) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            m.select(file)
                                        },
                                            shape = RoundedCornerShape(9.dp),
                                            color = if (active) MaterialTheme.colorScheme.primaryContainer
                                                else Color.Transparent) {
                                            Row(Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 11.dp),
                                                verticalAlignment = Alignment.CenterVertically) {
                                                FileTypeIcon(file, Modifier.size(16.dp), selected = active)
                                                Text(file, Modifier.padding(start = 8.dp),
                                                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                                                    fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                                                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                    if ((!m.console && !m.preview) || wide) Box(Modifier.weight(
                        if (wide && (m.console || m.preview)) m.panelSplitPercent / 100f else 1f
                    ).fillMaxHeight()) {
                        val file = m.active
                        if (file != null) key(p, file) {
                            CodeEditor(fileKey = "$p/$file", filename = file, text = m.source,
                                onTextChange = m::edit, jumpLine = m.pendingLine,
                                onJumpHandled = m::consumeJump, onSave = m::flush, onRun = m::run,
                                onShare = { dialog = "screenshot" },
                                onPreviousTab = { m.moveActiveTab(-1) },
                                onNextTab = { m.moveActiveTab(1) },
                                onOpenCommands = { dialog = "commands" },
                                onSelectionChange = { snippet, line ->
                                    selectedSnippet = snippet
                                    selectedSnippetLine = line
                                },
                                modifier = Modifier.fillMaxSize())
                        } else TextButton(onClick = { dialog = "files" }, modifier = Modifier.align(Alignment.Center)) { Text("Open a file") }
                    }
                    if (m.console) RunConsole(m.runner,
                        Modifier.weight(if (wide) (100 - m.panelSplitPercent) / 100f else 1f)
                            .fillMaxHeight(), m.active)
                    else if (m.preview && m.active != null) HtmlPreview(p, m.active!!, m.source,
                        Modifier.weight(if (wide) (100 - m.panelSplitPercent) / 100f else 1f)
                            .fillMaxHeight())
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            if (focusMode) TextButton(onClick = { focusMode = false }, modifier = Modifier.padding(start = 8.dp)) {
                Icon(Icons.Default.FullscreenExit, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Exit Focus Mode")
            }
            if (!focusMode) Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                PanelButton("Editor", !m.console && !m.preview) { m.console = false; m.preview = false }
                PanelButton("Console", m.console) { m.console = true; m.preview = false }
                if (m.active?.endsWith(".html",true) == true || m.active?.endsWith(".htm",true) == true)
                    PanelButton("Preview", m.preview) { m.console = false; m.preview = true }
                Text(when {
                    m.active?.endsWith(".js", true) == true -> "JavaScript"
                    m.active?.endsWith(".sql", true) == true -> "SQLite"
                    m.active?.endsWith(".lua", true) == true -> "Lua"
                    m.active?.endsWith(".java", true) == true -> "Java · beta"
                    m.active?.endsWith(".html", true) == true -> "HTML"
                    m.active?.let(m::cloudAvailable) == true -> "Cloud available"
                    else -> "Text · cloud setup"
                }, Modifier.padding(start = 18.dp), style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        AnimatedVisibility(m.busy,
            enter = fadeIn(tween(motionMillis)),
            exit = fadeOut(tween(motionMillis))) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                Text("Updating local workspace…", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    when (dialog) {
        "actions" -> WorkspaceActionsSheet(
            projectOpen = p != null,
            fileOpen = m.active != null,
            multipleProjects = m.projects.size > 1,
            javascriptFile = m.active?.endsWith(".js", true) == true,
            cloudReady = m.active?.let(m::cloudAvailable) == true,
            focusMode = focusMode,
            onAction = ::openAction,
            onDismiss = { dialog = "" })
        "languages" -> LanguageManagerSheet(m) { dialog = "" }
        "switchproject" -> ProjectSwitcher(m) { dialog = "" }
        "screenshot" -> CodeScreenshotStudio(
            m.active.orEmpty(), m.source, selectedSnippet, selectedSnippetLine
        ) { dialog = "" }
        "workspacecommands" -> WorkspaceCommandSheet(m) { dialog = "" }
        "jsdebug" -> JavaScriptDebugDialog(m) { dialog = "" }
        "projectsettings" -> ProjectSettingsDialog(m) { dialog = "" }
        "git" -> GitDialog(m) { dialog = "" }
        "history" -> FileHistoryDialog(m) { dialog = "" }
        "projectreplace" -> ProjectReplaceDialog(m) { dialog = "" }
        "projectsearch" -> ProjectSearchDialog(m, onDismiss = { dialog = "" }) { path, line ->
            dialog = ""
            m.select(path, goToLine = line)
        }
        "trash" -> TrashDialog(m) { dialog = "" }
        "properties" -> AlertDialog(onDismissRequest = { dialog = "" },
            title = { Text("File properties") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Path: " + m.active.orEmpty(), fontFamily = FontFamily.Monospace)
                Text("Characters: " + m.source.length)
                Text("Lines: " + (m.source.count { it == '\n' } + 1))
                Text("UTF-8 bytes: " + m.source.toByteArray(Charsets.UTF_8).size)
                Text("Encoding: UTF-8")
                Text("Project: " + m.project.orEmpty())
            } }, confirmButton = { TextButton(onClick = { dialog = "" }) { Text("Close") } })

        "newproject" -> CreateProjectDialog(cancel = { dialog = "" }) { name, template ->
            m.openProject(name, true, template)
            dialog = ""
        }
        "newfile", "newfolder", "rename", "renamefolder", "renameproject", "duplicateproject", "import", "importfile", "duplicate" -> {
            val title = when (dialog) { "newproject" -> "New project"; "newfile" -> "New file"; "newfolder" -> "New folder"; "renamefolder" -> "Rename folder"; "duplicateproject" -> "Duplicate project"; "rename" -> "Rename file"; "renameproject" -> "Rename project"; "importfile" -> "Import file"; "duplicate" -> "Duplicate file"; else -> "Import project" }
            val file = dialog == "newfile" || dialog == "newfolder" || dialog == "renamefolder" || dialog == "rename" || dialog == "importfile" || dialog == "duplicate"
            NameDialog(title, if (dialog.startsWith("rename")) selected else if (dialog == "duplicateproject") selected + "-copy" else if (dialog == "duplicate") selected.substringBeforeLast(".") + "-copy" + (if (selected.contains(".")) "." + selected.substringAfterLast(".") else "") else "", if (file) "File path" else "Project name",
                if (file) "e.g. src/helpers.js" else "e.g. Playground", { dialog = "" }) { name ->
                when (dialog) { "renameproject" -> m.manageProject(selected, name); "newproject" -> m.openProject(name, true); "newfile" -> m.select(name, true); "newfolder" -> m.createFolder(name); "renamefolder" -> m.changeFolder(selected, name); "duplicateproject" -> m.duplicateProject(selected, name); "rename" -> m.changeFile(selected, name); "import" -> incoming?.let { m.importArchive(name, Uri.parse(it)) }; "importfile" -> importedFile?.let { m.importFile(Uri.parse(it), name) }; "duplicate" -> m.duplicateFile(selected, name) }
                dialog = ""
            }
        }
        "copyproject" -> AlertDialog(onDismissRequest = { dialog = "" },
            title = { Text("Copy to project") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Copy " + m.active.orEmpty() + " to:")
                m.projects.filter { it != m.project }.forEach { destination ->
                    TextButton(onClick = {
                        m.active?.let { m.copyFileToProject(it, destination, it) }
                        dialog = ""
                    }) { Text(destination) }
                }
            } }, confirmButton = { TextButton(onClick = { dialog = "" }) { Text("Cancel") } })
        "files" -> FileExplorerSheet(m, onClose = { dialog = "" }, onNew = { dialog = "newfile" },
            onOpen = { m.select(it); dialog = "" }, onRename = { selected = it; dialog = "rename" },
            onDelete = { selected = it; dialog = "delete" },
            onRenameFolder = { selected = it; dialog = "renamefolder" },
            onDeleteFolder = { selected = it; dialog = "deletefolder" },
            onNewFolder = { dialog = "newfolder" })
        "deletefolder" -> AlertDialog(onDismissRequest = { dialog = "" },
            title = { Text("Delete folder?") },
            text = { Text("Move " + selected + " and its contents to Recently deleted?") },
            confirmButton = { TextButton(onClick = { m.changeFolder(selected, null); dialog = "" }) {
                Text("Move to trash", color = MaterialTheme.colorScheme.error)
            } },
            dismissButton = { TextButton(onClick = { dialog = "" }) { Text("Cancel") } })
        "delete" -> AlertDialog(onDismissRequest = { dialog = "" }, title = { Text("Delete file?") },
            text = { Text(selected) }, confirmButton = { TextButton(onClick = { m.changeFile(selected, null); dialog = "" }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { dialog = "" }) { Text("Cancel") } })
        "deleteproject" -> AlertDialog(onDismissRequest = { dialog = "" }, title = { Text("Delete project?") },
            text = { Text("Delete $selected and all its files from this device?") },
            confirmButton = { TextButton(onClick = { m.manageProject(selected, null); dialog = "" }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { dialog = "" }) { Text("Cancel") } })
        "folderimport" -> NameDialog("Import folder as project", "", "Project name",
            "MyExternalProject", { dialog = "" }) { name ->
                folderImportName = name
                dialog = ""
                importTree.launch(null)
            }
        "githubimport" -> GitHubImportDialog(m) { dialog = "" }
        "httpsinspector" -> HttpInspectorSheet { dialog = "" }
        "devtools" -> DeveloperToolbox { dialog = "" }
        "commands" -> CommandPaletteDialog(m, dismiss = { dialog = "" },
            execute = { action ->
                dialog = action
                when (action) {
                    "git" -> m.gitRefresh()
                    "history" -> m.refreshHistory()
                }
            })
        "accessibility" -> AlertDialog(onDismissRequest = { dialog = "" },
            title = { Text("Accessibility") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("High contrast", Modifier.weight(1f))
                        Switch(checked = m.highContrast, onCheckedChange = m::chooseHighContrast)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Larger interface text", Modifier.weight(1f))
                        Switch(checked = m.largeInterfaceText,
                            onCheckedChange = m::chooseLargeInterfaceText)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Run-button haptic feedback", Modifier.weight(1f))
                        Switch(checked = m.touchFeedback, onCheckedChange = m::chooseTouchFeedback)
                    }
                    Text("System font scaling and reduced-motion settings remain supported. These options apply immediately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }, confirmButton = { TextButton(onClick = { dialog = "" }) { Text("Done") } })
        "performance" -> AlertDialog(onDismissRequest = { dialog = "" },
            title = { Text("Performance diagnostics") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Last workspace I/O: " + m.lastOperationName)
                    Text(m.lastOperationMillis.toString() + " ms", fontFamily = FontFamily.Monospace)
                    Text("Last local save: " + m.lastSaveMillis + " ms",
                        fontFamily = FontFamily.Monospace)
                    Text("Editor buffer: " + m.source.toByteArray(Charsets.UTF_8).size + " bytes")
                    Text("Project files: " + m.files.size)
                    Text("Numbers are captured from actual local operations on this device. Not synthetic benchmarks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }, confirmButton = { TextButton(onClick = { dialog = "" }) { Text("Done") } })
        "panels" -> AlertDialog(onDismissRequest = { dialog = "" },
            title = { Text("Workspace panels") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Editor width · ${m.panelSplitPercent}%")
                    Slider(value = m.panelSplitPercent.toFloat(),
                        onValueChange = { m.setPanelSplit(it.toInt()) },
                        valueRange = 35f..80f)
                    Text("When the console or preview is open on a tablet, the editor and output share the available width.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = explorerPinned,
                            onCheckedChange = { explorerPinned = it })
                        Text("Show explorer sidebar on larger screens")
                    }
                }
            }, confirmButton = { TextButton(onClick = { dialog = "" }) { Text("Done") } })
        "appearance" -> AlertDialog(onDismissRequest = { dialog = "" },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = { Text("Appearance") },
            text = { Column(Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("THEME", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf("Dark", "Light", "System").forEach { mode ->
                        FilterChip(selected = m.appearance == mode, onClick = { m.setMode(mode) },
                            label = { Text(mode) })
                    }
                }
                Text("PRESET", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf("Obsidian", "Nocturne", "Paper", "Dracula", "One Dark", "Monokai", "GitHub Light", "OLED").forEach { preset ->
                        FilterChip(selected = m.themePreset == preset, onClick = { m.choosePreset(preset) },
                            label = { Text(preset) })
                    }
                }
                Text("ACCENT", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
                Row(Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Mint", "Blue", "Violet", "Amber").forEach { accent ->
                        FilterChip(selected = m.accent == accent, onClick = { m.chooseAccent(accent) },
                            label = { Text(accent) })
                    }
                }
                Text("ANIMATIONS", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
                Row(Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf("System", "Full", "Reduced").forEach { mode ->
                        FilterChip(selected = m.motionPreference == mode,
                            onClick = { m.chooseMotion(mode) }, label = { Text(mode) })
                    }
                }
                Text("Motion intensity · " + m.motionIntensity + "%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(value = m.motionIntensity.toFloat(),
                    onValueChange = { m.chooseMotionIntensity(it.toInt()) },
                    valueRange = 0f..100f)
                Text("INTERFACE DENSITY", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
                Row(Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf("Compact", "Comfortable", "Spacious").forEach { density ->
                        FilterChip(selected = m.interfaceDensity == density,
                            onClick = { m.chooseDensity(density) },
                            label = { Text(density) })
                    }
                }
                Text("More editor preferences are available from the editor toolbar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } },
            confirmButton = { TextButton(onClick = { dialog = "" }) { Text("Done") } })
    }
    m.cloudPending?.let { filename ->
        val language = m.cloudLanguages[CloudJudge0.extension(filename)]
        if (language != null) {
            OnlineRunConfirmation(filename, language.name,
                cancel = m::cancelCloudRun, start = m::startCloudRun)
        }
    }
    m.error?.let { issue ->
        AlertDialog(onDismissRequest = { m.error = null }, title = { Text("Workspace error") }, text = { Text(issue) },
            confirmButton = { TextButton(onClick = { m.error = null }) { Text("Close") } })
    }
}

@Composable
private fun PanelButton(text: String, active: Boolean, click: () -> Unit) {
    val target = if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val background by animateColorAsState(target, animationSpec = tween(170), label = "panel-selection")
    Surface(onClick = click, color = background, shape = RoundedCornerShape(10.dp)) {
        Text(text, Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium)
    }
}

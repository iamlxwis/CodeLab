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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import android.provider.Settings
import android.view.View
import android.view.WindowInsets as PlatformWindowInsets
import android.os.Build
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
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
    var requestedLanguage by rememberSaveable { mutableStateOf<String?>(null) }
    var selected by rememberSaveable { mutableStateOf("") }
    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(m.undoDeletion?.id) {
        val deleted = m.undoDeletion ?: return@LaunchedEffect
        val result = snackbarHost.showSnackbar(deleted.label,
            actionLabel = "Undo",
            withDismissAction = true,
            duration = SnackbarDuration.Long)
        if (result == SnackbarResult.ActionPerformed) m.undoDelete(deleted.id)
        else m.dismissUndo(deleted.id)
    }
    var incoming by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedSnippet by remember { mutableStateOf("") }
    var selectedSnippetLine by remember { mutableIntStateOf(1) }
    var focusMode by rememberSaveable { mutableStateOf(false) }
    var explorerPinned by rememberSaveable { mutableStateOf(true) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val tutorialPrefs = remember(context) {
        context.getSharedPreferences("first-run-guide", android.content.Context.MODE_PRIVATE)
    }
    var showTutorial by rememberSaveable { mutableStateOf(!tutorialPrefs.getBoolean("complete", false)) }
    var tutorialDetour by rememberSaveable { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val motion = rememberCodeLabMotion(m.motionPreference, m.motionIntensity, m.motionSpeed, m.springIntensity)
    val motionMillis = motion.millis(170)
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
            "tutorial" -> { dialog = ""; showTutorial = true }
            "run-active" -> m.run()
            "save" -> m.flush()
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

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
        if (!focusMode || p == null) WorkspaceTopBar(m, p) { dialog = it }
        if (p != null && !focusMode) RunModeStrip(m,
            onLanguages = {
                val ext = m.active?.substringAfterLast('.', "")?.lowercase()
                requestedLanguage = if (ext == "py") "Python"
                    else OptionalRuntimeLocator.Pack.entries.firstOrNull { it.extension == ext }?.label
                dialog = "languages"
            },
            onProjectSettings = { dialog = "projectsettings" })
        if (p == null) {
            Box(Modifier.weight(1f)) { ProjectLibrary(m, motionMillis, { dialog = "newproject" },
                { importZip.launch(arrayOf("*/*")) }, { dialog = "githubimport" },
                { selected = it; dialog = "renameproject" },
                { selected = it; dialog = "deleteproject" },
                { selected = it; dialog = "duplicateproject" }, importFolder = { dialog = "folderimport" }) }
            HomeNavigation { openAction(it) }
        } else {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            if (!focusMode) Row(verticalAlignment = Alignment.CenterVertically) {
                val threshold = with(LocalDensity.current) { 32.dp.toPx() }
                var explorerDrag by remember { mutableFloatStateOf(0f) }
                IconButton(onClick = { dialog = "files" }, modifier = Modifier.pointerInput(threshold) {
                    detectHorizontalDragGestures(onDragStart = { explorerDrag = 0f },
                        onDragEnd = { if (explorerDrag >= threshold) dialog = "files" },
                        onHorizontalDrag = { change, dx -> change.consume(); explorerDrag += dx })
                }) {
                    Icon(Icons.Default.FolderOpen, "Browse files or swipe right")
                }
                if (m.files.isNotEmpty() && LocalConfiguration.current.screenWidthDp >= 820) {
                    IconButton(onClick = { explorerPinned = !explorerPinned },
                        modifier = Modifier.size(38.dp)) {
                        Icon(if (explorerPinned) Icons.Default.VerticalSplit else Icons.Default.ViewSidebar,
                            "Toggle tablet explorer", Modifier.size(19.dp))
                    }
                }
                val tabScroll = rememberLazyListState()
                LaunchedEffect(m.active, m.tabs) {
                    val index = m.tabs.indexOf(m.active)
                    if (index >= 0 && tabScroll.layoutInfo.visibleItemsInfo.none { it.index == index }) {
                        if (motionMillis > 0) tabScroll.animateScrollToItem(index) else tabScroll.scrollToItem(index)
                    }
                }
                LazyRow(Modifier.weight(1f), state = tabScroll,
                    verticalAlignment = Alignment.CenterVertically) {
                    items(m.tabs, key = { it }) { path ->
                        WorkspaceTab(path = path, active = m.active == path,
                            pinned = path in m.pinnedTabs,
                            dirty = m.isTabDirty(path),
                            motionMillis = motionMillis,
                            onSelect = {
                                UiLatencyProbe.nextFrame(context, "Switch tab")
                                if (m.touchFeedback) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                m.select(path)
                            },
                            onClose = { m.closeTab(path) },
                            onTogglePin = { m.togglePinnedTab(path) },
                            onCloseOthers = { m.closeOtherTabs(path) },
                            onCloseAll = { m.closeOtherTabs(null) },
                            onMove = { m.reorderTab(path, it) },
                            onReveal = { selected = path; dialog = "filereveal" })
                    }
                }
                if (m.tabs.size > 1) OpenTabsMenu(m.tabs, m.active, m::select)
                IconButton(onClick = { dialog = "newfile" }) { Icon(Icons.Default.Add, "Add file") }
            }
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                val wide = maxWidth >= 820.dp
                val phoneSplit = !wide && m.phoneSplitEnabled &&
                    (m.console || m.preview) && !focusMode
                val editorPane: @Composable (Modifier) -> Unit = { pane ->
                    WorkspaceEditorPane(m, p, pane,
                        onFiles = { dialog = "files" },
                        onScreenshot = { dialog = "screenshot" },
                        onCommands = { dialog = "commands" },
                        onSelection = { text, line ->
                            selectedSnippet = text
                            selectedSnippetLine = line
                        })
                }
                val outputPane: @Composable (Modifier) -> Unit = { pane ->
                    if (m.console) RunConsole(m.runner, pane, m.active, m.files,
                        onToggleMaximize = if (wide) null else { { m.choosePhoneSplitEnabled(!m.phoneSplitEnabled) } }) { path, line ->
                        m.select(path, goToLine = line)
                    }
                    else if (m.preview && m.active != null)
                        HtmlPreview(p, m.active!!, m.source, pane)
                }
                if (phoneSplit) {
                    val heightPx = with(LocalDensity.current) { maxHeight.toPx() }
                    Column(Modifier.fillMaxSize()) {
                        editorPane(Modifier.weight(m.phoneSplitPercent.toFloat()).fillMaxWidth())
                        PhonePanelDivider(m.phoneSplitPercent, heightPx, m::choosePhoneSplitPercent)
                        outputPane(Modifier.weight((100 - m.phoneSplitPercent).toFloat()).fillMaxWidth())
                    }
                } else {
                    val showSidebar = wide && explorerPinned && !focusMode
                    Row(Modifier.fillMaxSize()) {
                    AnimatedVisibility(visible = showSidebar,
                        // Translate the fixed-width sidebar; do not continuously
                        // resize the native editor and its text layout.
                        enter = slideInHorizontally(animationSpec = tween(motionMillis)) { -it / 4 } +
                            fadeIn(animationSpec = tween(motionMillis)),
                        exit = slideOutHorizontally(animationSpec = tween(motionMillis)) { -it / 4 } +
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
                        if ((!m.console && !m.preview) || wide)
                            editorPane(Modifier.weight(
                                if (wide && (m.console || m.preview)) m.panelSplitPercent / 100f else 1f
                            ).fillMaxHeight())
                        if (m.console || m.preview)
                            outputPane(Modifier.weight(
                                if (wide) (100 - m.panelSplitPercent) / 100f else 1f
                            ).fillMaxHeight())
                    }
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
                PanelButton("Editor", !m.console && !m.preview, motionMillis) { m.console = false; m.preview = false }
                PanelButton("Console", m.console && (!m.phoneSplitEnabled || LocalConfiguration.current.screenWidthDp >= 820), motionMillis) {
                    m.choosePhoneSplitEnabled(false); m.console = true; m.preview = false
                }
                if (LocalConfiguration.current.screenWidthDp < 820) {
                    PhoneSplitToggle(m.phoneSplitEnabled && (m.console || m.preview)) {
                        val next = !(m.phoneSplitEnabled && (m.console || m.preview))
                        m.choosePhoneSplitEnabled(next)
                        if (next && !m.console && !m.preview) m.console = true
                        if (!next) { m.console = false; m.preview = false }
                    }
                }
                if (m.active?.endsWith(".html",true) == true || m.active?.endsWith(".htm",true) == true)
                    PanelButton("Preview", m.preview, motionMillis) { m.console = false; m.preview = true }
                val languageLabel = when {
                    m.active?.endsWith(".js", true) == true -> "JavaScript"
                    m.active?.endsWith(".sql", true) == true -> "SQLite"
                    m.active?.endsWith(".lua", true) == true -> "Lua"
                    m.active?.endsWith(".java", true) == true -> "Java · beta"
                    m.active?.endsWith(".html", true) == true -> "HTML"
                    m.active?.let(m::cloudAvailable) == true -> "Cloud available"
                    else -> "Text · cloud setup"
                }
                if (m.showEditorStatus) AnimatedContent(targetState = languageLabel,
                    transitionSpec = {
                        fadeIn(tween(motionMillis)) togetherWith fadeOut(tween(motionMillis))
                    }, label = "language-label") { value ->
                    Text(value, Modifier.padding(start = 18.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                QuickActionsBar(m.quickEditorActions, onAction = ::openAction)
            }
        }
        AnimatedVisibility(tutorialDetour && dialog.isEmpty() && !showTutorial,
            enter = fadeIn(tween(motionMillis)), exit = fadeOut(tween(motionMillis))) {
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("Quick start", Modifier.weight(1f).padding(start = 8.dp), style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { tutorialDetour = false; showTutorial = true }) { Text("Continue guide") }
                }
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
        SnackbarHost(hostState = snackbarHost,
            modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp)) { data ->
            Snackbar(snackbarData = data, shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface,
                actionColor = MaterialTheme.colorScheme.primary,
                dismissActionContentColor = MaterialTheme.colorScheme.onSurfaceVariant)
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
            quickActions = if (p != null) m.quickEditorActions else m.quickHomeActions,
            onToggleQuick = { m.toggleQuickAction(it, p != null) },
            onMoveQuick = { id, offset -> m.moveQuickAction(id, offset, p != null) },
            onAction = ::openAction,
            onDismiss = { dialog = "" })
        "languages" -> LanguageManagerSheet(m, initialLanguage = requestedLanguage) {
            dialog = ""
            requestedLanguage = null
        }
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

        "newproject" -> GuidedProjectSheet(cancel = { dialog = "" }) { name, template, starter, execution ->
            m.chooseExecutionMode(execution)
            m.openProject(name, true, template, starter)
            dialog = ""
        }
        "import" -> incoming?.let { source -> ImportReviewDialog(Uri.parse(source), true, m.projects, { dialog = "" }) { name ->
            m.importArchive(name, Uri.parse(source)); dialog = ""
        } }
        "importfile" -> importedFile?.let { source -> ImportReviewDialog(Uri.parse(source), false, m.files, { dialog = "" }) { name ->
            m.importFile(Uri.parse(source), name); dialog = ""
        } }
        "newfile", "newfolder", "rename", "renamefolder", "renameproject", "duplicateproject", "duplicate" -> {
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
        "files", "filereveal" -> FileExplorerSheet(m,
            initialDirectory = if (dialog == "filereveal") {
                selected.substringBeforeLast('/', "").let { if (it.isBlank()) "" else "$it/" }
            } else "",
            onClose = { dialog = "" }, onNew = { dialog = "newfile" },
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
        "delete" -> AlertDialog(onDismissRequest = { dialog = "" }, title = { Text("Move file to trash?") },
            text = { Text(selected) }, confirmButton = { TextButton(onClick = { m.changeFile(selected, null); dialog = "" }) { Text("Move to trash", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { dialog = "" }) { Text("Cancel") } })
        "deleteproject" -> AlertDialog(onDismissRequest = { dialog = "" }, title = { Text("Move project to trash?") },
            text = { Text("$selected and its files can be recovered from Recently deleted.") },
            confirmButton = { TextButton(onClick = { m.manageProject(selected, null); dialog = "" }) { Text("Move to trash", color = MaterialTheme.colorScheme.error) } },
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
                    Text("Interface density")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Compact", "Comfortable", "Spacious").forEach { density ->
                            FilterChip(selected = m.interfaceDensity == density, onClick = { m.chooseDensity(density) },
                                label = { Text(density, maxLines = 1) })
                        }
                    }
                    Text("Tablet editor width · ${m.panelSplitPercent}%")
                    Slider(value = m.panelSplitPercent.toFloat(),
                        onValueChange = { m.setPanelSplit(it.toInt()) },
                        valueRange = 35f..80f)
                    Text("Phone editor height · ${m.phoneSplitPercent}%")
                    Slider(value = m.phoneSplitPercent.toFloat(),
                        onValueChange = { m.choosePhoneSplitPercent(it.toInt()) },
                        valueRange = 35f..80f)
                    Text("In Split mode, the editor stays above the console or preview. Drag the divider or use this slider to adjust height.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = explorerPinned,
                            onCheckedChange = { explorerPinned = it })
                        Text("Show explorer sidebar on larger screens")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = m.showEditorStatus, onCheckedChange = m::chooseEditorStatus)
                        Text("Show editor status bar")
                    }
                    TextButton(onClick = { dialog = "layoutpresets" }) { Text("Save or restore layouts") }
                }
            }, confirmButton = { TextButton(onClick = { dialog = "" }) { Text("Done") } })
        "appearance" -> AppearanceGallery(m) { dialog = "" }
        "settings" -> ReimaginedSettings(m, onAction = ::openAction, close = { dialog = "" })
        "layoutpresets" -> WorkspaceLayoutPresets(m) { dialog = "" }
    }
    if (showTutorial) FirstRunTutorial(
        onFinish = {
            // A process killed after Skip must not discard asynchronous preference writes.
            val saved = tutorialPrefs.edit().putBoolean("complete", true)
                .remove("resume-index").commit()
            if (saved) { showTutorial = false; tutorialDetour = false }
            else m.error = "Could not save tutorial completion. Retry."
        },
        onTryAction = { action, resumeAt ->
            // Save the next topic durably before visiting a live CodeLab control.
            val saved = tutorialPrefs.edit().putBoolean("complete", false)
                .putInt("resume-index", resumeAt).commit()
            if (saved) {
                showTutorial = false
                tutorialDetour = true
                openAction(action)
            } else m.error = "Could not save tutorial progress. Retry."
        },
        projectOpen = p != null,
        resumeFrom = tutorialPrefs.getInt("resume-index", 0),
        motion = motion
    )
    m.cloudPending?.let { filename ->
        val language = m.cloudLanguages[CloudJudge0.extension(filename)]
        if (language != null) {
            OnlineRunConfirmation(filename, language.name,
                cancel = m::cancelCloudRun, start = m::startCloudRun)
        }
    }
    if (m.gitPullInProgress) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Updating Git branch") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    Text("Applying fast-forward changes. Editing is temporarily paused.")
                }
            },
            confirmButton = {}
        )
    }
    m.error?.let { issue ->
        AlertDialog(onDismissRequest = { m.error = null }, title = { Text("Workspace error") }, text = { Text(issue) },
            confirmButton = { TextButton(onClick = { m.error = null }) { Text("Close") } },
            dismissButton = {
                val missingLanguage = when {
                    issue.contains("Python", ignoreCase = true) &&
                        issue.contains("module", ignoreCase = true) -> "Python"
                    else -> OptionalRuntimeLocator.Pack.entries.firstOrNull { pack ->
                        issue.contains(pack.label, ignoreCase = true) &&
                            (issue.contains("module", ignoreCase = true) ||
                                issue.contains("runtime", ignoreCase = true))
                    }?.label
                }
                if (missingLanguage != null) {
                    TextButton(onClick = {
                        m.error = null
                        requestedLanguage = missingLanguage
                        dialog = "languages"
                    }) { Text("Manage " + missingLanguage) }
                }
            })
    }
}

@Composable
private fun PanelButton(text: String, active: Boolean, motionMillis: Int, click: () -> Unit) {
    val target = if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val background by animateColorAsState(target, animationSpec = tween(motionMillis), label = "panel-selection")
    Surface(onClick = click, color = background, shape = RoundedCornerShape(10.dp)) {
        Text(text, Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium)
    }
}

/** Direct destinations replace hidden home overflow navigation. */
@Composable
private fun HomeNavigation(onAction: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val navigation = studioColor(LocalStudioAppearance.current.navigationBackground, colors.surfaceContainerHigh)
    Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(28.dp), color = navigation,
        contentColor = studioReadable(colors.onSurface, navigation),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f))) {
        Row(Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf(Triple("projects", "Projects", Icons.Default.Folder),
                Triple("languages", "Languages", Icons.Default.Code),
                Triple("actions", "Tools", Icons.Default.Build),
                Triple("settings", "Settings", Icons.Default.Settings)).forEach { (id, label, icon) ->
                Column(Modifier.weight(1f).heightIn(min = 54.dp).background(
                    if (id == "projects") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    RoundedCornerShape(24.dp)).clickable { if (id != "projects") onAction(id) }.padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(icon, null, Modifier.size(20.dp), tint = if (id == "projects")
                        MaterialTheme.colorScheme.onPrimaryContainer else studioReadable(colors.onSurfaceVariant, navigation))
                    Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                }
            }
        }
    }
}

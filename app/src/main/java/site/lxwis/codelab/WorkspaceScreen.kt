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
import kotlinx.coroutines.launch

@Composable
internal fun Studio(m: WorkspaceModel, modifier: Modifier) {
    var dialog by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf(MainDestination.Home) }
    var returnToDownloads by rememberSaveable { mutableStateOf(false) }
    var requestedLanguage by rememberSaveable { mutableStateOf<String?>(null) }
    var template by rememberSaveable { mutableStateOf("JavaScript") }
    var selected by rememberSaveable { mutableStateOf("") }
    val snackbarHost = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    LaunchedEffect(m.transferNotice) {
        val notice = m.transferNotice ?: return@LaunchedEffect
        snackbarHost.showSnackbar(notice)
        m.clearTransferNotice()
    }
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
    var captureReader by remember { mutableStateOf<(() -> EditorCapture?)?>(null) }
    var snippet by remember { mutableStateOf<SnippetSnapshot?>(null) }
    var resultFirst by remember { mutableStateOf(false) }
    val focusMode = m.focusMode && m.project != null
    val explorerPinned = m.explorerPinned
    val wideOutputExpanded = m.outputMaximized
    val context = androidx.compose.ui.platform.LocalContext.current
    val tutorialPrefs = remember(context) {
        context.getSharedPreferences("first-run-guide", android.content.Context.MODE_PRIVATE)
    }
    var showTutorial by rememberSaveable { mutableStateOf(!tutorialPrefs.getBoolean("complete", false)) }
    var tutorialDetour by rememberSaveable { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val motion = rememberCodeLabMotion(m.motionPreference, m.motionIntensity, m.motionSpeed, m.springIntensity, m.transitionEffect)
    val motionMillis = motion.millis(170)
    // Reclaim chrome for every text input, including runtime stdin and WebView forms.
    val keyboardEditing = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val configuration = LocalConfiguration.current
    val wideWorkspace = WorkspaceSizeClass.of(configuration.screenWidthDp) != WorkspaceSizeClass.Compact
    val shortWorkspace = configuration.screenHeightDp < 480
    val outputOpen = m.console || m.preview
    val splitOpen = outputOpen && if (wideWorkspace) !wideOutputExpanded else m.phoneSplitEnabled
    val currentPanel = if (!outputOpen) "Editor" else if (splitOpen) "Split" else "Console"

    fun choosePanel(pane: String) {
        when (pane) {
            "Editor" -> { m.console = false; m.preview = false }
            "Console" -> {
                m.chooseOutputMaximized(true)
                m.choosePhoneSplitEnabled(false)
                m.console = true
                m.preview = false
            }
            "Split" -> {
                m.chooseOutputMaximized(false)
                if (!wideWorkspace) m.choosePhoneSplitEnabled(true)
                if (!m.preview) m.console = true
            }
        }
    }
    val activity = context as? ComponentActivity
    DisposableEffect(activity, focusMode, m.systemStatusVisible) {
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
        if (!focusMode && !m.systemStatusVisible) {
            if (Build.VERSION.SDK_INT >= 30) window?.insetsController?.hide(PlatformWindowInsets.Type.statusBars())
            else window?.decorView?.systemUiVisibility = previousFlags or View.SYSTEM_UI_FLAG_FULLSCREEN
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
    var folderImportUri by rememberSaveable { mutableStateOf<String?>(null) }
    val importTree = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) { folderImportUri = uri.toString(); dialog = "folderreview" }
    }
    val importZip = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) { incoming = it.toString(); dialog = "import" }
    }
    val p = m.project
    LaunchedEffect(m.isOnline) { if (m.isOnline) m.updater.check() }
    LaunchedEffect(m.project, m.console, m.preview, m.outputMaximized, m.busy) { m.rememberWorkspacePanels() }
    fun openSnippet(result: Boolean = false) {
        val project = m.project ?: return
        val run = m.snippetExecution()
        val file = m.active ?: run?.file ?: return
        snippet = SnippetSnapshot(project, file, m.source, captureReader?.invoke(), run)
        resultFirst = result
        dialog = "screenshot"
    }
    fun openAction(action: String) {
        when (action) {
            "newproject" -> { template = "JavaScript"; dialog = action }
            "importfile" -> importFile.launch(arrayOf("*/*"))
            "importmany" -> importMany.launch(arrayOf("*/*"))
            "importzip" -> importZip.launch(arrayOf("*/*"))
            "folderimport" -> importTree.launch(null)
            "exportcurrent" -> if (m.active != null) dialog = "exportfilereview"
            "exportproject" -> if (p != null) dialog = "exportprojectreview"
            "rename", "duplicate" -> m.active?.let { selected = it; dialog = action }
            "projectreplace" -> { m.clearReplaceSummary(); dialog = action }
            "history" -> { m.refreshHistory(); dialog = action }
            "git" -> { m.gitRefresh(); dialog = action }
            "cloudrun" -> m.requestCloudRun()
            "tutorial" -> { dialog = ""; showTutorial = true }
            "run-active" -> m.run()
            "save" -> m.flush()
            "screenshot" -> openSnippet()
            "focus" -> {
                m.chooseFocusMode(!focusMode)
                m.console = false
                m.preview = false
            }
            else -> dialog = action
        }
    }
    BackHandler(enabled = dialog.isEmpty() && (p != null || destination != MainDestination.Home)) {
        if (p == null) destination = MainDestination.Home
        else if (focusMode) m.chooseFocusMode(false)
        else if (m.preview) m.preview = false else if (m.console) m.console = false else m.home(keepExecution = true)
    }

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
        if (p != null && !focusMode) WorkspaceTopBar(m, p, ::openAction, showFile = outputOpen && !splitOpen)
        if (p == null) {
            MainDestinations(m, destination, { destination = it }, ::openAction, projects = { bottom ->
                ProjectLibrary(m, motionMillis, { openAction("newproject") },
                    { importZip.launch(arrayOf("*/*")) }, { dialog = "githubimport" },
                    { selected = it; dialog = "renameproject" },
                    { selected = it; dialog = "deleteproject" },
                    { selected = it; dialog = "duplicateproject" }, importFolder = { openAction("folderimport") },
                    bottomPadding = bottom)
            }, modifier = Modifier.weight(1f),
                backdropVisible = dialog.isEmpty() && !showTutorial && !keyboardEditing,
                workspaceCapsuleVisible = !keyboardEditing)
        } else {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            if (!focusMode && (!outputOpen || splitOpen)) Row(verticalAlignment = Alignment.CenterVertically) {
                val threshold = with(LocalDensity.current) { 32.dp.toPx() }
                var explorerDrag by remember { mutableFloatStateOf(0f) }
                IconButton(onClick = { dialog = "files" }, modifier = Modifier.pointerInput(threshold) {
                    detectHorizontalDragGestures(onDragStart = { explorerDrag = 0f },
                        onDragEnd = { if (explorerDrag >= threshold) dialog = "files"; explorerDrag = 0f },
                        onDragCancel = { explorerDrag = 0f },
                        onHorizontalDrag = { change, dx -> change.consume(); explorerDrag += dx })
                }) {
                    Icon(Icons.Default.FolderOpen, "Browse files or swipe right")
                }
                if (m.files.isNotEmpty() && WorkspaceSizeClass.of(LocalConfiguration.current.screenWidthDp) == WorkspaceSizeClass.Expanded) {
                    IconButton(onClick = { m.chooseExplorerPinned(!explorerPinned) },
                        modifier = Modifier.size(38.dp)) {
                        Icon(if (explorerPinned) Icons.Default.VerticalSplit else Icons.Default.ViewSidebar,
                            "Toggle tablet explorer", Modifier.size(19.dp))
                    }
                }
                val tabScroll = rememberLazyListState()
                BoxWithConstraints(Modifier.weight(1f)) {
                val tabWidth = maxWidth
                LaunchedEffect(m.active, m.tabs, tabWidth) {
                    val index = m.tabs.indexOf(m.active)
                    if (index >= 0 && tabWidth > 0.dp) {
                        // Request on the current width. Adding a tab also introduces the switcher,
                        // so the old viewport can report a new tab visible before it shrinks.
                        tabScroll.requestScrollToItem(index)
                    }
                }
                LazyRow(Modifier.fillMaxWidth(), state = tabScroll,
                    verticalAlignment = Alignment.CenterVertically) {
                    items(m.tabs, key = { it }) { path ->
                        Box(Modifier.animateItem(
                            fadeInSpec = if (motionMillis > 0) tween(motionMillis) else null,
                            placementSpec = if (motionMillis > 0) tween(motionMillis) else null,
                            fadeOutSpec = if (motionMillis > 0) tween(motionMillis) else null)) {
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
                            onReveal = { selected = path; dialog = "filereveal" }, maxWidth = tabWidth)
                        }
                    }
                }
                }
                if (m.tabs.size > 1) OpenTabsMenu(m.tabs, m.active, m::select)
                if (!keyboardEditing) IconButton(onClick = { dialog = "newfile" }) {
                    Icon(Icons.Default.Add, "Add file")
                }
            }
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                val widthClass = WorkspaceSizeClass.of(maxWidth.value.toInt())
                val wide = widthClass != WorkspaceSizeClass.Compact
                val phoneSplit = !wide && m.phoneSplitEnabled &&
                    (m.console || m.preview) && !focusMode
                val editorPane: @Composable (Modifier) -> Unit = { pane ->
                    WorkspaceEditorPane(m, p, pane,
                        onFiles = { dialog = "files" },
                        onScreenshot = { openSnippet() },
                        onCommands = { dialog = "commands" },
                        onCaptureReader = { captureReader = it })
                }
                val outputPane: @Composable (Modifier) -> Unit = { pane ->
                    if (m.console) RunConsole(m.runner, pane, m.active, m.files,
                        onToggleMaximize = { if (wide) m.chooseOutputMaximized(!wideOutputExpanded)
                            else { m.chooseOutputMaximized(m.phoneSplitEnabled); m.choosePhoneSplitEnabled(!m.phoneSplitEnabled) } },
                        onNotice = { message ->
                            snackbarScope.launch { snackbarHost.showSnackbar(message) }
                        }, motionMillis = motionMillis, onRerun = m::requestRerun,
                        onSnippet = { openSnippet(result = true) }) { path, line ->
                        m.select(path, goToLine = line)
                    }
                    else if (m.preview && m.active != null)
                        HtmlPreview(p, m.active!!, m.source, pane, m.touchFeedback)
                }
                if (phoneSplit) {
                    val heightPx = with(LocalDensity.current) { (maxHeight - 48.dp).coerceAtLeast(1.dp).toPx() }
                    Column(Modifier.fillMaxSize()) {
                        editorPane(Modifier.weight(m.phoneSplitPercent.toFloat()).fillMaxWidth())
                        PhonePanelDivider(m.phoneSplitPercent, heightPx, m::choosePhoneSplitPercent, m.touchFeedback)
                        outputPane(Modifier.weight((100 - m.phoneSplitPercent).toFloat()).fillMaxWidth())
                    }
                } else {
                    val showSidebar = widthClass == WorkspaceSizeClass.Expanded && explorerPinned && !focusMode && !(wideOutputExpanded && (m.console || m.preview))
                    val wideSplit = wide && !wideOutputExpanded && (m.console || m.preview)
                    val panelWidth = (maxWidth - (if (showSidebar) 225.dp else 0.dp) - 48.dp).coerceAtLeast(1.dp)
                    val panelWidthPx = with(LocalDensity.current) { panelWidth.toPx() }
                    val splitRange = WorkspaceSizeClass.editorPercentRange(panelWidth.value)
                    val maxEditor = splitRange.last
                    val widePercent = m.panelSplitPercent.coerceIn(splitRange)
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
                        if ((!m.console && !m.preview) || wideSplit)
                            editorPane(Modifier.weight(
                                if (wideSplit) widePercent.toFloat() else 1f
                            ).fillMaxHeight())
                        if (wideSplit) WidePanelDivider(widePercent, panelWidthPx, maxEditor, m::setPanelSplit, splitRange.first, m.touchFeedback)
                        if (m.console || m.preview)
                            outputPane(Modifier.weight(
                                if (wideSplit) (100 - widePercent).toFloat() else 1f
                            ).fillMaxHeight())
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            if (focusMode) TextButton(onClick = { m.chooseFocusMode(false) }, modifier = Modifier.padding(start = 8.dp)) {
                Icon(Icons.Default.FullscreenExit, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Exit Focus Mode")
            }
            if (!focusMode && !keyboardEditing && !shortWorkspace) {
                WorkspacePanels(currentPanel, Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    ::choosePanel)
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
                Text(m.transferLabel ?: "Updating local workspace…", style = MaterialTheme.typography.labelSmall,
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
        "search", "snippets", "templates" -> UniversalSearch(m, when (dialog) {
            "snippets" -> SearchCategory.Snippets
            "templates" -> SearchCategory.Templates
            else -> SearchCategory.All
        }, onAction = { dialog = ""; openAction(it) }, onTemplate = { template = it; dialog = "newproject" },
            close = { dialog = "" })
        "downloads", "updates" -> DownloadCenter(m, updatesOnly = dialog == "updates",
            onRuntime = { language ->
                returnToDownloads = true
                requestedLanguage = language
                dialog = "languages"
            }, close = { dialog = "" })
        "utilities" -> UtilitiesScreen(::openAction) { dialog = "" }
        "activity" -> ActivityScreen(m.developerActivity) { dialog = "" }
        "actions" -> WorkspaceActionsSheet(
            projectOpen = p != null,
            fileOpen = m.active != null,
            multipleProjects = m.projects.size > 1,
            javascriptFile = m.active?.endsWith(".js", true) == true,
            focusMode = focusMode,
            quickActions = if (p != null) m.quickEditorActions else m.quickHomeActions,
            onToggleQuick = { m.toggleQuickAction(it, p != null) },
            onMoveQuick = { id, offset -> m.moveQuickAction(id, offset, p != null) },
            onAction = ::openAction,
            motionMillis = motionMillis,
            onDismiss = { dialog = "" })
        "library-import" -> LibraryImportSheet(onDismiss = { dialog = "" },
            onZip = { dialog = ""; openAction("importzip") },
            onGitHub = { dialog = "githubimport" },
            onFolder = { dialog = ""; openAction("folderimport") })
        "languages", "catalog" -> LanguageManagerScreen(m, initialLanguage = requestedLanguage,
            showCatalog = dialog == "catalog", detailOnly = returnToDownloads, onDownloads = { dialog = "downloads" }) {
            dialog = if (returnToDownloads) "downloads" else ""
            returnToDownloads = false
            requestedLanguage = null
        }
        "switchproject" -> ProjectSwitcher(m) { dialog = "" }
        "screenshot" -> snippet?.let {
            SnippetStudio(it, appearanceIsDark(m.appearance, isSystemInDarkTheme()), resultFirst) {
                dialog = ""
                snippet = null
            }
        }
        "runfiles" -> RunFilePicker(m.files, m.files.filter(m::cloudAvailable).map(::languageExtension).toSet(),
            m::runTarget, setOf("py", "ts").filter { m.isLocalExecutable("file.$it") }.toSet()) { dialog = "" }
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
        "properties" -> CodeLabAlertDialog(onDismissRequest = { dialog = "" },
            title = { Text("File properties") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Path: " + m.active.orEmpty(), fontFamily = FontFamily.Monospace)
                Text("Characters: " + m.source.length)
                Text("Lines: " + (m.source.count { it == '\n' } + 1))
                Text("UTF-8 bytes: " + m.source.toByteArray(Charsets.UTF_8).size)
                Text("Encoding: UTF-8")
                Text("Project: " + m.project.orEmpty())
            } }, confirmButton = { TextButton(onClick = { dialog = "" }) { Text("Close") } })

        "newproject" -> GuidedProjectSheet(cancel = { dialog = "" }, initialLanguage = template) { name, template, starter ->
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
        "copyproject" -> CodeLabAlertDialog(onDismissRequest = { dialog = "" },
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
        "deletefolder" -> CodeLabAlertDialog(onDismissRequest = { dialog = "" },
            title = { Text("Delete folder?") },
            text = { Text("Move " + selected + " and its contents to Recently deleted?") },
            confirmButton = { TextButton(onClick = { m.changeFolder(selected, null); dialog = "" }) {
                Text("Move to trash", color = MaterialTheme.colorScheme.error)
            } },
            dismissButton = { TextButton(onClick = { dialog = "" }) { Text("Cancel") } })
        "delete" -> CodeLabAlertDialog(onDismissRequest = { dialog = "" }, title = { Text("Move file to trash?") },
            text = { Text(selected) }, confirmButton = { TextButton(onClick = { m.changeFile(selected, null); dialog = "" }) { Text("Move to trash", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { dialog = "" }) { Text("Cancel") } })
        "deleteproject" -> CodeLabAlertDialog(onDismissRequest = { dialog = "" }, title = { Text("Move project to trash?") },
            text = { Text("$selected and its files can be recovered from Recently deleted.") },
            confirmButton = { TextButton(onClick = { m.manageProject(selected, null); dialog = "" }) { Text("Move to trash", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { dialog = "" }) { Text("Cancel") } })
        "folderreview" -> folderImportUri?.let { source ->
            ImportReviewDialog(Uri.parse(source), true, m.projects, { dialog = ""; folderImportUri = null }, folder = true) { name ->
                m.importExternalFolder(Uri.parse(source), name)
                dialog = ""; folderImportUri = null
            }
        }
        "exportfilereview" -> m.active?.let { path ->
            ExportReviewDialog(path.substringAfterLast('/'), listOf(path), false, { dialog = "" }) {
                dialog = ""; exportFile.launch(path.substringAfterLast('/'))
            }
        }
        "exportprojectreview" -> p?.let { project ->
            ExportReviewDialog(project + ".zip", m.files, true, { dialog = "" }) {
                dialog = ""; export.launch(project + ".zip")
            }
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
        "accessibility" -> CodeLabAlertDialog(onDismissRequest = { dialog = "" },
            title = { Text("Accessibility") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("High contrast", Modifier.weight(1f))
                        Switch(checked = m.highContrast, onCheckedChange = m::chooseHighContrast,
                            modifier = Modifier.semantics { contentDescription = "High contrast" })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Larger interface text", Modifier.weight(1f))
                        Switch(checked = m.largeInterfaceText,
                            onCheckedChange = m::chooseLargeInterfaceText,
                            modifier = Modifier.semantics { contentDescription = "Larger interface text" })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Touch feedback", Modifier.weight(1f))
                        Switch(checked = m.touchFeedback, onCheckedChange = m::chooseTouchFeedback,
                            modifier = Modifier.semantics { contentDescription = "Touch feedback" })
                    }
                    Text("System font scaling and reduced-motion settings remain supported. These options apply immediately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }, confirmButton = { TextButton(onClick = { dialog = "" }) { Text("Done") } })
        "performance" -> PerformancePanel(m) { dialog = "" }
        "panels" -> CodeLabAlertDialog(onDismissRequest = { dialog = "" },
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
                            onCheckedChange = m::chooseExplorerPinned)
                        Text("Show explorer sidebar on larger screens")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = m.showEditorStatus, onCheckedChange = m::chooseEditorStatus)
                        Text("Show editor status bar")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Show Android status bar", Modifier.weight(1f))
                        Switch(m.systemStatusVisible, m::chooseSystemStatusVisible,
                            modifier = Modifier.semantics { contentDescription = "Show Android status bar" })
                    }
                    TextButton(onClick = { dialog = "layoutpresets" }) { Text("Save or restore layouts") }
                }
            }, confirmButton = { TextButton(onClick = { dialog = "" }) { Text("Done") } })
        "workspaceview" -> CodeLabAlertDialog(onDismissRequest = { dialog = "" },
            title = { Text("Workspace view") },
            text = { WorkspacePanels(currentPanel, Modifier.fillMaxWidth()) { pane ->
                choosePanel(pane)
                dialog = ""
            } },
            confirmButton = { TextButton(onClick = { dialog = "" }) { Text("Cancel") } })
        "appearance" -> ReimaginedSettings(m, ::openAction, { dialog = "" }, initialPage = "appearance")
        "settings" -> ReimaginedSettings(m, onAction = ::openAction, close = { dialog = "" })
        "licenses" -> LicenseNotices { dialog = "" }
        "layoutpresets" -> WorkspaceLayoutPresets(m) { dialog = "" }
        else -> if (dialog.startsWith("settings/")) key(dialog) {
            ReimaginedSettings(m, ::openAction, { dialog = "" }, initialPage = dialog.substringAfter('/'))
        }
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
        motion = motion,
        transitionEffect = m.transitionEffect,
        runtimeBusy = m.runner.running
    )
    m.executionRequest?.let { request ->
        ExecutionChooser(request.file, request.suggested, m.executionOptions(request),
            cancel = m::cancelExecution,
            install = { language ->
                m.cancelExecution()
                requestedLanguage = language
                dialog = "languages"
            },
            run = { mode -> m.confirmExecution(request, mode) })
    }
    m.cloudPending?.let { filename ->
        val language = m.cloudLanguages[CloudJudge0.extension(filename)]
        if (language != null) {
            OnlineRunConfirmation(filename, language.name,
                cancel = m::cancelCloudRun, start = m::startCloudRun)
        }
    }
    if (m.gitPullInProgress) {
        CodeLabAlertDialog(
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
        CodeLabAlertDialog(onDismissRequest = { m.error = null }, title = { Text("Workspace error") }, text = { Text(issue) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WorkspacePanels(current: String, modifier: Modifier = Modifier, choose: (String) -> Unit) {
    val panels = listOf("Editor", "Console", "Split")
    SingleChoiceSegmentedButtonRow(modifier) {
        panels.forEachIndexed { index, pane ->
            SegmentedButton(current == pane, { choose(pane) },
                SegmentedButtonDefaults.itemShape(index, panels.size), icon = {}) { Text(pane) }
        }
    }
}

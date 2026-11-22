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
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.lifecycle.ViewModelProvider

private val night = darkColorScheme(
    primary = Color(0xff83e5ca), onPrimary = Color(0xff083329),
    primaryContainer = Color(0xff1d3c35), onPrimaryContainer = Color(0xffadf4df),
    surface = Color(0xff101517), background = Color(0xff101517),
    surfaceVariant = Color(0xff20292d), onSurfaceVariant = Color(0xffacbbbd),
    onSurface = Color(0xffe6eeed), outlineVariant = Color(0xff364145),
    surfaceContainer = Color(0xff191f22), surfaceContainerHigh = Color(0xff222a2d),
    surfaceContainerHighest = Color(0xff2b3436), surfaceBright = Color(0xff263033)
)
private val day = lightColorScheme(
    primary = Color(0xff006b57), onPrimary = Color.White,
    primaryContainer = Color(0xffc2efdf), onPrimaryContainer = Color(0xff103c32),
    surface = Color(0xfff5f8f7), background = Color(0xfff5f8f7),
    surfaceVariant = Color(0xffe2eae7), onSurfaceVariant = Color(0xff495852),
    onSurface = Color(0xff152322), outlineVariant = Color(0xffbdcbc5),
    surfaceContainer = Color(0xffebf1ef), surfaceContainerHigh = Color(0xffe3ece9),
    surfaceContainerHighest = Color(0xffdce6e3)
)

class MainActivity : ComponentActivity() {
    private lateinit var model: WorkspaceModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProvider(this)[WorkspaceModel::class.java]
        setContent {
            val dark = if (model.themePreset == "Paper") false else
                model.appearance == "Dark" || (model.appearance == "System" && isSystemInDarkTheme())
            val base = when (model.themePreset) {
                "Nocturne" -> night.copy(
                    background = Color(0xff0c1020), surface = Color(0xff0c1020),
                    surfaceContainer = Color(0xff151a2d), surfaceContainerHigh = Color(0xff20263b),
                    surfaceVariant = Color(0xff222942), outlineVariant = Color(0xff333b55))
                "Paper" -> day
                else -> if (dark) night else day
            }
            val accent = when (model.accent) {
                "Blue" -> if (dark) Color(0xff8ab7fa) else Color(0xff245eaf)
                "Violet" -> if (dark) Color(0xffc4b0ff) else Color(0xff6747b7)
                "Amber" -> if (dark) Color(0xffffc978) else Color(0xff936000)
                else -> base.primary
            }
            val accentContainer = accent.copy(alpha = .15f).compositeOver(base.surface)
            val scheme = base.copy(primary = accent,
                onPrimary = if (dark) Color(0xff101517) else Color.White,
                primaryContainer = accentContainer, onPrimaryContainer = accent,
                secondary = accent, onSecondary = if (dark) Color(0xff101517) else Color.White,
                secondaryContainer = accentContainer, onSecondaryContainer = accent)
            SideEffect {
                @Suppress("DEPRECATION")
                window.statusBarColor = scheme.surface.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = scheme.surface.toArgb()
                if (android.os.Build.VERSION.SDK_INT >= 30) {
                    val mask = android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    window.insetsController?.setSystemBarsAppearance(if (dark) 0 else mask, mask)
                }
            }
            val typography = Typography(
                headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                titleLarge = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                titleMedium = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                bodyMedium = MaterialTheme.typography.bodyMedium,
                labelSmall = MaterialTheme.typography.labelSmall.copy(letterSpacing = .3.sp)
            )
            MaterialTheme(colorScheme = scheme, typography = typography, shapes = Shapes(
                extraSmall = RoundedCornerShape(7.dp),
                small = RoundedCornerShape(11.dp),
                medium = RoundedCornerShape(15.dp),
                large = RoundedCornerShape(20.dp),
                extraLarge = RoundedCornerShape(28.dp)
            )) {
                Surface(Modifier.fillMaxSize()) {
                    Studio(model, Modifier.windowInsetsPadding(WindowInsets.safeDrawing).imePadding())
                }
            }
        }
    }
    override fun onStop() {
        model.flush()
        super.onStop()
    }
}

@Composable
private fun Studio(m: WorkspaceModel, modifier: Modifier) {
    var dialog by rememberSaveable { mutableStateOf("") }
    var selected by rememberSaveable { mutableStateOf("") }
    var incoming by rememberSaveable { mutableStateOf<String?>(null) }
    var menu by remember { mutableStateOf(false) }
    var focusMode by rememberSaveable { mutableStateOf(false) }
    var explorerPinned by rememberSaveable { mutableStateOf(true) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val systemMotionEnabled = remember(context) {
        Settings.Global.getFloat(context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }
    val motionEnabled = when (m.motionPreference) {
        "Full" -> true
        "Reduced" -> false
        else -> systemMotionEnabled
    }
    val motionMillis = if (motionEnabled) m.motionIntensity * 2 else 0
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
    val importZip = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) { incoming = it.toString(); dialog = "import" }
    }
    val p = m.project
    BackHandler(enabled = p != null && dialog.isEmpty()) {
        if (focusMode) focusMode = false
        else if (m.preview) m.preview = false else if (m.console) m.console = false else m.home()
    }

    Column(modifier.fillMaxSize()) {
        if (!focusMode || p == null) Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (p == null) Mark(Modifier.padding(horizontal = 8.dp).size(34.dp))
            else IconButton(onClick = { m.home() }, enabled = !m.busy) { Icon(Icons.Default.ArrowBack, "Project library") }
            Column(Modifier.weight(1f).padding(start = 4.dp)) {
                Text(if (p == null) "CodeLab" else p, fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (p != null) Text(if (m.dirty) "Saving changes…" else "Local workspace", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (p != null && (m.runner.running || m.active != null)) {
                Button(
                    onClick = { if (m.runner.running) m.runner.stop() else m.run() },
                    enabled = !m.busy,
                    shape = RoundedCornerShape(11.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    AnimatedContent(targetState = m.runner.running, label = "execution-state") { running ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Icon(if (running) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(if (running) "Stop" else if (m.active?.endsWith(".html", true) == true) "Preview" else "Run",
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "Workspace options") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    if (p != null) {
                        DropdownMenuItem(text = { Text("Panel layout…") },
                            onClick = { menu = false; dialog = "panels" })
                        DropdownMenuItem(text = { Text("New file") }, onClick = { menu = false; dialog = "newfile" })
                        DropdownMenuItem(text = { Text("Import file…") }, onClick = { menu = false; importFile.launch(arrayOf("*/*")) })
                        DropdownMenuItem(text = { Text("Replace across files…") },
                            onClick = {
                                menu = false
                                m.clearReplaceSummary()
                                dialog = "projectreplace"
                            })
                        DropdownMenuItem(text = { Text("Search across files…") },
                            onClick = { menu = false; dialog = "projectsearch" })
                        DropdownMenuItem(text = { Text("New folder…") },
                            onClick = { menu = false; dialog = "newfolder" })
                        DropdownMenuItem(text = { Text("Copy file to another project…") },
                            enabled = m.active != null && m.projects.size > 1,
                            onClick = { menu = false; dialog = "copyproject" })
                        DropdownMenuItem(text = { Text("Import multiple files…") },
                            onClick = { menu = false; importMany.launch(arrayOf("*/*")) })
                        if (m.active != null) {
                            DropdownMenuItem(text = { Text("Rename current file…") }, onClick = {
                                menu = false; selected = m.active!!; dialog = "rename"
                            })
                            DropdownMenuItem(text = { Text("Duplicate current file…") }, onClick = {
                                menu = false; selected = m.active!!; dialog = "duplicate"
                            })
                            DropdownMenuItem(text = { Text("Export current file…") }, onClick = {
                                menu = false; exportFile.launch(m.active!!.substringAfterLast('/'))
                            })
                        }
                        DropdownMenuItem(text = { Text("File history…") }, enabled = m.active != null,
                            onClick = { menu = false; dialog = "history"; m.refreshHistory() })
                        DropdownMenuItem(text = { Text("File properties") },
                            enabled = m.active != null, onClick = { menu = false; dialog = "properties" })
                        DropdownMenuItem(text = { Text("Export project ZIP") }, onClick = { menu = false; export.launch("$p.zip") })
                        if (m.active != null && m.cloudAvailable(m.active!!)) {
                            DropdownMenuItem(text = { Text("Run via cloud…") },
                                onClick = { menu = false; m.requestCloudRun() })
                        }
                    } else DropdownMenuItem(text = { Text("Import project ZIP") }, onClick = { menu = false; importZip.launch(arrayOf("*/*")) })
                    DropdownMenuItem(text = { Text("Recently deleted…") },
                        onClick = { menu = false; dialog = "trash" })
                    if (p != null) DropdownMenuItem(text = { Text(if (focusMode) "Exit Focus Mode" else "Focus Mode") },
                        onClick = { menu = false; focusMode = !focusMode; m.console = false; m.preview = false })
                    DropdownMenuItem(text = { Text("Languages & Cloud") },
                        onClick = { menu = false; dialog = "languages" })
                    DropdownMenuItem(text = { Text("Appearance") }, onClick = { menu = false; dialog = "appearance" })
                }
            }
        }
        if (p == null) {
            ProjectLibrary(m, motionMillis, { dialog = "newproject" }, { importZip.launch(arrayOf("*/*")) },
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
                            Text(path.substringAfterLast('/'), Modifier.clickable { if (!m.busy) m.select(path) }
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
                                        Surface(onClick = { if (!m.busy) m.select(file) },
                                            shape = RoundedCornerShape(9.dp),
                                            color = if (active) MaterialTheme.colorScheme.primaryContainer
                                                else Color.Transparent) {
                                            Row(Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 11.dp),
                                                verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Description, null, Modifier.size(16.dp),
                                                    tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
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
                                onJumpHandled = m::consumeJump, onSave = m::flush,
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
        "languages" -> LanguageManagerSheet(m) { dialog = "" }
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
                    listOf("Obsidian", "Nocturne", "Paper").forEach { preset ->
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
            OnlineRunConfirmation(filename, m.cloudEndpoint, language.name,
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

@Composable
private fun Mark(modifier: Modifier) {
    val ink = MaterialTheme.colorScheme.onPrimaryContainer
    Canvas(modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(9.dp)).padding(7.dp)) {
        val w = size.width
        val h = size.height
        fun stroke(a: Float,b: Float,c: Float,d: Float) = drawLine(ink, Offset(w*a,h*b), Offset(w*c,h*d), w*.095f, StrokeCap.Round)
        stroke(.28f,.29f,.07f,.5f); stroke(.07f,.5f,.28f,.71f)
        stroke(.59f,.15f,.42f,.85f)
        stroke(.73f,.29f,.94f,.5f); stroke(.94f,.5f,.73f,.71f)
    }
}

@Composable
private fun DashboardMetric(value: String, label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Surface(modifier, color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Icon(icon, null, Modifier.size(19.dp), tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProjectLibrary(m: WorkspaceModel, motionMillis: Int, new: () -> Unit,
    importZip: () -> Unit,
    rename: (String) -> Unit, delete: (String) -> Unit, duplicate: (String) -> Unit) {
    var options by remember { mutableStateOf<String?>(null) }
    var projectSearch by rememberSaveable { mutableStateOf("") }
    var projectSort by rememberSaveable { mutableStateOf("Recent") }
    val spacing = when (m.interfaceDensity) {
        "Compact" -> 8.dp
        "Spacious" -> 18.dp
        else -> 12.dp
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(spacing)) {
        item {
            Text("WORKSPACE", fontSize = 11.sp, letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("Projects", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Local development, organized.", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardMetric("${m.projects.size}", "Projects", Icons.Default.Folder,
                    Modifier.weight(1f))
                DashboardMetric(if (m.isOnline) "Online" else "Offline", "Connection",
                    Icons.Default.Wifi, Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = new, enabled = !m.busy, shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text("New project")
                }
                OutlinedButton(onClick = importZip, enabled = !m.busy, shape = RoundedCornerShape(10.dp)) { Text("Import ZIP") }
            }
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(value = projectSearch, onValueChange = { projectSearch = it },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = { Text("Search projects") })
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Recent", "A–Z", "Z–A").forEach { option ->
                    FilterChip(selected = projectSort == option, onClick = { projectSort = option },
                        label = { Text(option) })
                }
            }
            Spacer(Modifier.height(16.dp))
            val visibleCount = m.projects.count { it.contains(projectSearch.trim(), ignoreCase = true) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("ON THIS DEVICE", fontSize = 11.sp, letterSpacing = 1.6.sp,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$visibleCount / ${m.projects.size}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (m.projects.isEmpty() && !m.busy) item {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .65f), shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("No projects yet", fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    Text("Create a project or import a project ZIP.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (projectSearch.isNotBlank() && m.projects.none { it.contains(projectSearch.trim(), ignoreCase = true) }) item {
            Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SearchOff, null, Modifier.size(26.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("No matching projects", fontWeight = FontWeight.Medium)
                    TextButton(onClick = { projectSearch = "" }) { Text("Clear search") }
                }
            }
        }
        items(m.projects.filter { it.contains(projectSearch.trim(), ignoreCase = true) }.let { visible ->
            when (projectSort) {
                "A–Z" -> visible.sortedWith(String.CASE_INSENSITIVE_ORDER)
                "Z–A" -> visible.sortedWith(String.CASE_INSENSITIVE_ORDER.reversed())
                else -> visible
            }
        }, key = { it }) { name ->
            Surface(onClick = { m.openProject(name) }, enabled = !m.busy,
                modifier = Modifier.animateContentSize(animationSpec = tween(motionMillis)),
                shape = RoundedCornerShape(16.dp), tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(11.dp),
                        color = MaterialTheme.colorScheme.primaryContainer) {
                        Icon(Icons.Default.Folder, null, Modifier.padding(10.dp).size(22.dp),
                            tint = MaterialTheme.colorScheme.primary)
                    }
                    Text(name, Modifier.weight(1f).padding(start = 14.dp), maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                    Box {
                        IconButton(onClick = { options = name }) { Icon(Icons.Default.MoreVert, "Manage $name") }
                        DropdownMenu(expanded = options == name, onDismissRequest = { options = null }) {
                            DropdownMenuItem(text = { Text("Duplicate") }, onClick = { options = null; duplicate(name) })
                            DropdownMenuItem(text = { Text("Rename") }, onClick = { options = null; rename(name) })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = { options = null; delete(name) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NameDialog(title: String, initial: String, label: String, example: String, cancel: () -> Unit, submit: (String) -> Unit) {
    var name by rememberSaveable(title, initial) { mutableStateOf(initial) }
    AlertDialog(onDismissRequest = cancel, title = { Text(title) },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text(label) }, placeholder = { Text(example) }) },
        confirmButton = { TextButton(onClick = { submit(name.trim()) }, enabled = name.isNotBlank()) { Text(if (title.startsWith("New")) "Create" else if (title.startsWith("Import")) "Import" else "Rename") } },
        dismissButton = { TextButton(onClick = cancel) { Text("Cancel") } })
}

@Composable
private fun CreateProjectDialog(cancel: () -> Unit, submit: (String, String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var template by rememberSaveable { mutableStateOf("JavaScript") }
    var otherTemplates by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = cancel,
        title = { Text("New project") },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, singleLine = true,
                    label = { Text("Project name") }, placeholder = { Text("e.g. Playground") })
                Spacer(Modifier.height(14.dp))
                Text("STARTER", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    listOf(listOf("JavaScript", "Java", "Lua"), listOf("SQLite", "Web")).forEach { row ->
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            row.forEach { option ->
                                FilterChip(selected = template == option,
                                    onClick = { template = option },
                                    label = { Text(option) })
                            }
                        }
                    }
                }
                Box {
                    TextButton(onClick = { otherTemplates = true }) {
                        Text(if (template in listOf("JavaScript", "Java", "Lua", "SQLite", "Web"))
                            "More languages…" else template + " · change language")
                    }
                    DropdownMenu(expanded = otherTemplates, onDismissRequest = { otherTemplates = false }) {
                        listOf("Python", "C", "C++", "C#", "TypeScript", "Kotlin", "Rust",
                            "Go", "PHP", "Ruby", "Swift", "Bash", "Dart", "Scala", "Perl",
                            "Haskell", "R").forEach { language ->
                            DropdownMenuItem(text = { Text(language) }, onClick = {
                                template = language
                                otherTemplates = false
                            })
                        }
                    }
                }
                if (template !in listOf("JavaScript", "Java", "Lua", "SQLite", "Web")) {
                    Text("Editing works offline. Running " + template +
                        " requires a verified online compiler.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = { TextButton(onClick = { submit(name.trim(), template) },
            enabled = name.isNotBlank()) { Text("Create") } },
        dismissButton = { TextButton(onClick = cancel) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileExplorerSheet(
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
                TextButton(onClick = onNewFolder, enabled = !m.busy) { Text("Folder") }
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
private fun ProjectSearchDialog(m: WorkspaceModel, onDismiss: () -> Unit,
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
                    FilledTonalButton(enabled = query.isNotBlank() && !m.busy,
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
private fun TrashDialog(m: WorkspaceModel, onDismiss: () -> Unit) {
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
                            TextButton(enabled = !m.busy, onClick = { m.restoreTrash(item.id) }) {
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
            if (m.trashItems.isNotEmpty()) TextButton(enabled = !m.busy,
                onClick = { confirmEmpty = true }) { Text("Empty trash", color = MaterialTheme.colorScheme.error) }
        })
}

@Composable
private fun FileHistoryDialog(m: WorkspaceModel, onDismiss: () -> Unit) {
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
                            TextButton(enabled = !m.busy, onClick = {
                                m.previewHistory(version.id)
                            }) { Text("View") }
                            TextButton(enabled = !m.busy, onClick = {
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
private fun ProjectReplaceDialog(m: WorkspaceModel, onDismiss: () -> Unit) {
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
                Button(enabled = query.isNotBlank() && !m.busy,
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
            TextButton(enabled = (m.replaceSummary?.matches ?: 0) > 0 && !m.busy,
                onClick = { confirm = true }) { Text("Apply previewed changes") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } })
}

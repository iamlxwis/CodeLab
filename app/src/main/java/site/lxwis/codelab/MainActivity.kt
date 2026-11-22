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
            val dark = model.appearance == "Dark" || (model.appearance == "System" && isSystemInDarkTheme())
            val base = if (dark) night else day
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
            MaterialTheme(colorScheme = scheme) {
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
    var importedFile by rememberSaveable { mutableStateOf<String?>(null) }
    val exportFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) {
        it?.let(m::exportFile)
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
        if (m.preview) m.preview = false else if (m.console) m.console = false else m.home()
    }

    Column(modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
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
                        DropdownMenuItem(text = { Text("New file") }, onClick = { menu = false; dialog = "newfile" })
                        DropdownMenuItem(text = { Text("Import file…") }, onClick = { menu = false; importFile.launch(arrayOf("*/*")) })
                        if (m.active != null) {
                            DropdownMenuItem(text = { Text("Rename current file…") }, onClick = {
                                menu = false; selected = m.active!!; dialog = "rename"
                            })
                            DropdownMenuItem(text = { Text("Export current file…") }, onClick = {
                                menu = false; exportFile.launch(m.active!!.substringAfterLast('/'))
                            })
                        }
                        DropdownMenuItem(text = { Text("Export project ZIP") }, onClick = { menu = false; export.launch("$p.zip") })
                        if (m.active != null && m.cloudAvailable(m.active!!)) {
                            DropdownMenuItem(text = { Text("Run via cloud…") },
                                onClick = { menu = false; m.requestCloudRun() })
                        }
                    } else DropdownMenuItem(text = { Text("Import project ZIP") }, onClick = { menu = false; importZip.launch(arrayOf("*/*")) })
                    DropdownMenuItem(text = { Text("Languages & Cloud") },
                        onClick = { menu = false; dialog = "languages" })
                    DropdownMenuItem(text = { Text("Appearance") }, onClick = { menu = false; dialog = "appearance" })
                }
            }
        }
        if (p == null) {
            ProjectLibrary(m, { dialog = "newproject" }, { importZip.launch(arrayOf("*/*")) },
                { selected = it; dialog = "renameproject" }, { selected = it; dialog = "deleteproject" })
        } else {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { dialog = "files" }) {
                    Icon(Icons.Default.FolderOpen, "Browse files")
                }
                LazyRow(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    items(m.tabs, key = { it }) { path ->
                        val active = m.active == path
                        Row(Modifier.padding(end = 2.dp)
                            .background(if (active) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
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
                Row(Modifier.fillMaxSize()) {
                    if ((!m.console && !m.preview) || wide) Box(Modifier.weight(1f).fillMaxHeight()) {
                        val file = m.active
                        if (file != null) key(p, file) {
                            CodeEditor(fileKey = "$p/$file", filename = file, text = m.source, onTextChange = m::edit, modifier = Modifier.fillMaxSize())
                        } else TextButton(onClick = { dialog = "files" }, modifier = Modifier.align(Alignment.Center)) { Text("Open a file") }
                    }
                    if (m.console) RunConsole(m.runner, Modifier.weight(if (wide) 0.7f else 1f).fillMaxHeight(), m.active)
                    else if (m.preview && m.active != null) HtmlPreview(p, m.active!!, m.source,
                        Modifier.weight(if (wide) 0.8f else 1f).fillMaxHeight())
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
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
        AnimatedVisibility(m.busy, enter = fadeIn(tween(130)), exit = fadeOut(tween(130))) {
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
        "newproject" -> CreateProjectDialog(cancel = { dialog = "" }) { name, template ->
            m.openProject(name, true, template)
            dialog = ""
        }
        "newfile", "rename", "renameproject", "import", "importfile" -> {
            val title = when (dialog) { "newproject" -> "New project"; "newfile" -> "New file"; "rename" -> "Rename file"; "renameproject" -> "Rename project"; "importfile" -> "Import file"; else -> "Import project" }
            val file = dialog == "newfile" || dialog == "rename" || dialog == "importfile"
            NameDialog(title, if (dialog.startsWith("rename")) selected else "", if (file) "File path" else "Project name",
                if (file) "e.g. src/helpers.js" else "e.g. Playground", { dialog = "" }) { name ->
                when (dialog) { "renameproject" -> m.manageProject(selected, name); "newproject" -> m.openProject(name, true); "newfile" -> m.select(name, true); "rename" -> m.changeFile(selected, name); "import" -> incoming?.let { m.importArchive(name, Uri.parse(it)) }; "importfile" -> importedFile?.let { m.importFile(Uri.parse(it), name) } }
                dialog = ""
            }
        }
        "files" -> FileExplorerSheet(m, onClose = { dialog = "" }, onNew = { dialog = "newfile" },
            onOpen = { m.select(it); dialog = "" }, onRename = { selected = it; dialog = "rename" },
            onDelete = { selected = it; dialog = "delete" })
        "delete" -> AlertDialog(onDismissRequest = { dialog = "" }, title = { Text("Delete file?") },
            text = { Text(selected) }, confirmButton = { TextButton(onClick = { m.changeFile(selected, null); dialog = "" }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { dialog = "" }) { Text("Cancel") } })
        "deleteproject" -> AlertDialog(onDismissRequest = { dialog = "" }, title = { Text("Delete project?") },
            text = { Text("Delete $selected and all its files from this device?") },
            confirmButton = { TextButton(onClick = { m.manageProject(selected, null); dialog = "" }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { dialog = "" }) { Text("Cancel") } })
        "appearance" -> AlertDialog(onDismissRequest = { dialog = "" },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = { Text("Appearance") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("THEME", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf("Dark", "Light", "System").forEach { mode ->
                        FilterChip(selected = m.appearance == mode, onClick = { m.setMode(mode) },
                            label = { Text(mode) })
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
private fun ProjectLibrary(m: WorkspaceModel, new: () -> Unit, importZip: () -> Unit, rename: (String) -> Unit, delete: (String) -> Unit) {
    var options by remember { mutableStateOf<String?>(null) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Projects", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = new, enabled = !m.busy, shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text("New project")
                }
                OutlinedButton(onClick = importZip, enabled = !m.busy, shape = RoundedCornerShape(10.dp)) { Text("Import ZIP") }
            }
            Spacer(Modifier.height(24.dp))
            Text("ON THIS DEVICE", fontSize = 11.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        items(m.projects, key = { it }) { name ->
            Surface(onClick = { m.openProject(name) }, enabled = !m.busy, shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .65f)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Mark(Modifier.size(34.dp))
                    Text(name, Modifier.weight(1f).padding(start = 14.dp), maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                    Box {
                        IconButton(onClick = { options = name }) { Icon(Icons.Default.MoreVert, "Manage $name") }
                        DropdownMenu(expanded = options == name, onDismissRequest = { options = null }) {
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
    onDelete: (String) -> Unit
) {
    var search by rememberSaveable(m.project) { mutableStateOf("") }
    var directory by rememberSaveable(m.project) { mutableStateOf("") }
    var menuFor by remember { mutableStateOf<String?>(null) }
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val allFiles = m.files
    val matching = if (search.isBlank()) allFiles.filter { it.startsWith(directory) }
        else allFiles.filter { it.contains(search, ignoreCase = true) }
    val folders = if (search.isNotBlank()) emptyList() else matching.mapNotNull {
        val next = it.removePrefix(directory).substringBefore('/', "")
        if (next.isEmpty()) null else directory + next + "/"
    }.distinct().sorted()
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
                            Text("Open", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
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

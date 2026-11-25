package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

internal data class SettingDestination(val name: String, val detail: String, val id: String, val icon: ImageVector)

internal val settingsDestinations = listOf(
    SettingDestination("Appearance", "Light and dark, fonts and glass", "appearance", Icons.Default.Palette),
    SettingDestination("Performance & Interaction", "Motion, haptics and automatic optimization", "motion", Icons.Default.Speed),
    SettingDestination("Editor", "Fonts, text size, indentation and syntax", "editor", Icons.Default.Code),
    SettingDestination("Workspace", "Keyboard, gestures, panels and layouts", "keyboard", Icons.Default.Keyboard),
    SettingDestination("Execution", "Language support and offline runtimes", "languages", Icons.Default.Extension),
    SettingDestination("Downloads & Storage", "Downloads, backups and recovery", "storage", Icons.Default.FolderOpen),
    SettingDestination("Privacy & Security", "Source uploads, credentials and local data", "privacy", Icons.Default.Security),
    SettingDestination("Help & About", "Tutorials, updates, licenses and accessibility", "help", Icons.Default.HelpOutline)
)

@Composable
internal fun ReimaginedSettings(m: WorkspaceModel, onAction: (String) -> Unit, close: () -> Unit, initialPage: String? = null) {
    var query by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var page by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(initialPage) }
    val title = settingsDestinations.firstOrNull { it.id == page }?.name ?: "Settings"
    fun goBack() { if (page != null && initialPage == null) page = null else close() }
    CodeLabDialog(onDismissRequest = ::goBack,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        androidx.activity.compose.BackHandler(enabled = page != null, onBack = ::goBack)
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(onClick = ::goBack,
                        modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)) {
                        Icon(Icons.Default.ArrowBack, if (page != null && initialPage == null) "Back to settings" else "Close settings")
                    }
                    Text(title, Modifier.weight(1f), style = if (page == null) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium)
                }
                if (page == null) {
                    Spacer(Modifier.height(12.dp))
                    CodeLabSearch(query, { query = it }, "Search settings")
                }
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (page == null) {
                        val matches = settingsDestinations.filter { it.name.contains(query, true) || it.detail.contains(query, true) }
                        if (matches.isEmpty()) item { Text("No matching settings", style = MaterialTheme.typography.bodyMedium) }
                        items(matches, key = { it.id }) { row ->
                            Box(Modifier.animateItem(
                                fadeInSpec = if (workspaceMotion().millis(140) > 0) androidx.compose.animation.core.tween(workspaceMotion().millis(140)) else null,
                                placementSpec = if (workspaceMotion().millis(140) > 0) androidx.compose.animation.core.tween(workspaceMotion().millis(140)) else null,
                                fadeOutSpec = if (workspaceMotion().millis(140) > 0) androidx.compose.animation.core.tween(workspaceMotion().millis(140)) else null)) {
                            SettingsLink(row.name, row.detail, row.icon) {
                                when (row.id) {
                                    "languages" -> onAction(row.id)
                                    else -> page = row.id
                                }
                            }
                            }
                        }
                    } else item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            when (page) {
                                "appearance" -> AppearanceSettings(m)
                                "motion" -> {
                                    SettingToggle("Reduce animations", m.motionPreference == "Reduced") {
                                        m.chooseMotionPreset(if (it) "Reduced" else "Standard")
                                    }
                                    SettingToggle("Haptic feedback", m.touchFeedback, m::chooseTouchFeedback)
                                    SettingToggle("Automatic optimization", m.automaticOptimization, m::chooseAutomaticOptimization)
                                    var advanced by rememberSaveable { mutableStateOf(false) }
                                    SettingsLink("Advanced motion", "Adjust animation speed and response", Icons.Default.Animation) {
                                        advanced = !advanced
                                    }
                                    if (advanced) MotionSettingsControls(m)
                                }
                                "editor" -> {
                                    EditorAppearanceControls(m)
                                    EditorPreferenceControls(false)
                                    SettingToggle("Focus mode", m.focusMode, m::chooseFocusMode)
                                }
                                "keyboard" -> {
                                    EditorPreferenceControls(true)
                                    SettingsLink("Workspace layout", "Density, panel sizes and status bar", Icons.Default.ViewSidebar) { onAction("panels") }
                                    SettingsLink("Saved layouts", "Save and restore workspace configurations", Icons.Default.Dashboard) { onAction("layoutpresets") }
                                    Text("Gestures", style = MaterialTheme.typography.titleMedium)
                                    Text("Swipe the editor navigation strip to change files. Swipe the Explorer button to reveal files. Long-press a tab handle, then drag to reorder. Pinch with two fingers to change editor text size.")
                                    Text("Drag the console divider to resize it; double-tap its header to maximize. Swipe project rows for quick actions. Long-press files and projects for contextual actions.")
                                    Text("Use tab menus, text-size controls, Explorer and panel sliders as alternatives. Gestures stay inside workspace controls to preserve Android navigation and text selection.", style = MaterialTheme.typography.bodySmall)
                                }
                                "storage" -> {
                                    SettingsLink("Downloads", "Manage app updates and offline packs", Icons.Default.Download) { onAction("downloads") }
                                    Text("Projects are stored in CodeLab's private app storage. Export a backup before uninstalling or clearing app data.")
                                    SettingsLink("Import projects", "Review a ZIP archive before copying it", Icons.Default.Archive) { onAction("importzip") }
                                    SettingsLink("Import device folder", "Copy files while preserving the original folder", Icons.Default.FolderOpen) { onAction("folderimport") }
                                    if (m.project != null) SettingsLink("Export project", "Review files and choose a backup destination", Icons.Default.FileUpload) { onAction("exportproject") }
                                    SettingsLink("Recently deleted", "Restore deleted projects and files", Icons.Default.RestoreFromTrash) { onAction("trash") }
                                    SettingsLink("Saved layouts", "Named workspace configurations", Icons.Default.Dashboard) { onAction("layoutpresets") }
                                }
                                "privacy" -> {
                                    SettingsLink("Developer activity", "Configure activity recording and clear history", Icons.Default.QueryStats) { onAction("activity") }
                                    Text("Source code", style = MaterialTheme.typography.titleMedium)
                                    Text("Offline execution stays on your device. Online execution requires confirmation before uploading source. Changing execution mode does not upload code.")
                                    Text("Credentials", style = MaterialTheme.typography.titleMedium)
                                    Text("Optional online and GitHub tokens are encrypted with Android Keystore. Configure or remove online credentials in Languages; GitHub credentials are managed in the project's Git panel.")
                                    SettingsLink("Online execution settings", "Manage the endpoint and saved credential", Icons.Default.Cloud) { onAction("languages") }
                                    if (m.project != null) SettingsLink("GitHub sync settings", "Review the remote and encrypted token", Icons.Default.AccountTree) { onAction("git") }
                                    Text("Runtime packs", style = MaterialTheme.typography.titleMedium)
                                    Text("Optional runtime packs use Android package installation and signature verification. Downloads never grant permission to upload projects.")
                                }
                                "help" -> {
                                    FeatureDiscoverySettings()
                                    Text("CodeLab — Reimagined", style = MaterialTheme.typography.titleLarge)
                                    val context = androidx.compose.ui.platform.LocalContext.current
                                    val version = remember(context) { context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty() }
                                    Text("Version " + version, style = MaterialTheme.typography.bodySmall)
                                    SettingsLink("Software update", "Check official GitHub releases", Icons.Default.SystemUpdate) { onAction("updates") }
                                    SettingsLink("What's new", "Release notes and update history", Icons.Default.NewReleases) { onAction("updates") }
                                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                                    SettingsLink("GitHub", "Source code and issue tracker", Icons.Default.OpenInNew) {
                                        uriHandler.openUri("https://github.com/iamlxwis/CodeLab")
                                    }
                                    SettingsLink("Quick start guide", "Replay practical lessons and try real features", Icons.Default.School) { onAction("tutorial") }
                                    SettingsLink("Accessibility", "Contrast, interface text and touch feedback", Icons.Default.AccessibilityNew) { onAction("accessibility") }
                                    SettingsLink("Performance diagnostics", "Actual local save and workspace timings", Icons.Default.Speed) { onAction("performance") }
                                    SettingsLink("Licenses & acknowledgements", "Bundled font, icon and runtime notices", Icons.Default.Description) { onAction("licenses") }
                                    Text("Inter and JetBrains Mono use the SIL Open Font License. Third-party runtime notices are included with the app.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsLink(title: String, detail: String, icon: ImageVector, click: () -> Unit) {
    Surface(onClick = click, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null)
        }
    }
}

@Composable
internal fun MotionSettingsControls(m: WorkspaceModel) {
    Text("Animation preset", style = MaterialTheme.typography.titleMedium)
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("Reduced", "Standard", "Fluid").forEach { preset ->
            val selected = when (m.motionPreference) { "System" -> "Standard"; "Full" -> "Fluid"; else -> m.motionPreference }
            FilterChip(selected == preset, { m.chooseMotionPreset(preset) }, label = { Text(preset) })
        }
    }
    Text("Speed · ${m.motionSpeed}%")
    Slider(m.motionSpeed.toFloat(), { m.chooseMotionSpeed(it.toInt()) }, valueRange = 50f..200f,
        modifier = Modifier.semantics { contentDescription = "Animation speed" })
    Text("Intensity · ${m.motionIntensity}%")
    Slider(m.motionIntensity.toFloat(), { m.chooseMotionIntensity(it.toInt()) }, valueRange = 0f..100f,
        modifier = Modifier.semantics { contentDescription = "Animation intensity" })
    Text("Spring response · ${m.springIntensity}%")
    Slider(m.springIntensity.toFloat(), { m.chooseSpringIntensity(it.toInt()) }, valueRange = 0f..100f,
        modifier = Modifier.semantics { contentDescription = "Spring intensity" })
    Text("Transition effect")
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("None", "Fade", "Slide").forEach { effect ->
            FilterChip(m.transitionEffect == effect, { m.chooseTransitionEffect(effect) }, label = { Text(effect) })
        }
    }
    Text("Android's Remove animations setting always takes precedence. Touch feedback respects the system haptics setting.", style = MaterialTheme.typography.bodySmall)
}

@Composable
internal fun SettingToggle(label: String, checked: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked, change, modifier = Modifier.semantics { contentDescription = label })
    }
}

/** Same preferences as the live editor; changes also update already mounted editor views. */
@Composable
private fun EditorPreferenceControls(keyboard: Boolean) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("editor", android.content.Context.MODE_PRIVATE) }
    var revision by remember { mutableIntStateOf(0) }
    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> revision++ }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    key(revision) {
        if (keyboard) {
            SettingToggle("Coding accessory bar", prefs.getBoolean("symbol-toolbar", true)) { prefs.edit().putBoolean("symbol-toolbar", it).apply() }
            SettingToggle("Show accessory only with keyboard", prefs.getBoolean("keyboard-only-symbols", false)) { prefs.edit().putBoolean("keyboard-only-symbols", it).apply() }
            SettingToggle("Hide accessory with hardware keyboard", prefs.getBoolean("auto-hide-hardware-toolbar", false)) { prefs.edit().putBoolean("auto-hide-hardware-toolbar", it).apply() }
            Text("Accessory layout")
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Standard", "Minimal", "Brackets", "Selection", "Navigation").forEach { layout ->
                    FilterChip(prefs.getString("shortcut-layout", "Standard") == layout,
                        { prefs.edit().putString("shortcut-layout", layout).apply() }, label = { Text(layout) })
                }
            }
            Text("Customize individual symbols from the editor's Mobile shortcut layout menu. Hardware shortcuts include Ctrl+S to save, Ctrl+F to find, and Ctrl+Z to undo.", style = MaterialTheme.typography.bodySmall)
        } else {
            val size = prefs.getInt("font-size", 15).coerceIn(10, 28)
            Text("Editor text · ${size}sp")
            Slider(size.toFloat(), { prefs.edit().putInt("font-size", it.toInt()).apply() }, valueRange = 10f..28f,
                modifier = Modifier.semantics { contentDescription = "Editor font size" })
            listOf("word-wrap" to "Wrap long lines", "line-numbers" to "Line numbers", "active-line" to "Highlight active line",
                "minimap" to "Minimap", "diagnostic-markers" to "Structural hints").forEach { (id, label) ->
                SettingToggle(label, prefs.getBoolean(id, id != "minimap")) { prefs.edit().putBoolean(id, it).apply() }
            }
            SettingToggle("Indent with tabs", prefs.getBoolean("indent-tabs", false)) { prefs.edit().putBoolean("indent-tabs", it).apply() }
            Text("Indent width")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(2, 4, 8).forEach { width ->
                    FilterChip(prefs.getInt("indent-width", 4) == width,
                        { prefs.edit().putInt("indent-width", width).apply() }, label = { Text(width.toString()) })
                }
            }
        }
    }
}

@Composable
internal fun WorkspaceLayoutPresets(m: WorkspaceModel, close: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    CodeLabToolDialog(onDismissRequest = close, title = { Text("Workspace layouts") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name, { name = it.take(64) }, label = { Text("Layout name") }, singleLine = true)
            Button(enabled = name.isNotBlank(), onClick = {
                notice = if (m.saveNamedLayout(name)) "Layout saved" else "Could not save layout"
                if (notice == "Layout saved") name = ""
            }) { Text("Save current layout") }
            if (notice.isNotEmpty()) Text(notice)
            LazyColumn(Modifier.heightIn(max = 240.dp)) {
                items(m.savedWorkspaceLayouts, key = { it.name }) { layout ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(layout.name, style = MaterialTheme.typography.titleSmall)
                            Text("${layout.density} · ${layout.phonePercent}% editor · ${layout.toolbar?.size ?: 0} pinned tools",
                                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { m.restoreNamedLayout(layout.name); notice = "Layout restored" }) { Text("Restore") }
                        IconButton(onClick = { m.deleteNamedLayout(layout.name) }) { Icon(Icons.Default.DeleteOutline, "Delete layout ${layout.name}") }
                    }
                }
            }
        }
    }, confirmButton = { TextButton(onClick = close) { Text("Done") } })
}

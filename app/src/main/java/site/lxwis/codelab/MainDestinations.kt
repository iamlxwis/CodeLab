package site.lxwis.codelab

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal enum class MainDestination(val label: String, val active: ImageVector, val inactive: ImageVector) {
    Home("Home", Icons.Filled.Home, Icons.Outlined.Home),
    Projects("Projects", Icons.Filled.Folder, Icons.Outlined.Folder),
    Explore("Explore", Icons.Filled.Explore, Icons.Outlined.Explore)
}

internal data class WorkspaceLocation(val project: String, val file: String)

private data class ExploreCategory(
    val title: String,
    val description: String,
    val action: String,
    val icon: ImageVector,
    val color: Color
)

private val exploreCategories = listOf(
    ExploreCategory("Languages", "Editor and execution support", "catalog", Icons.Default.Code, Color(0xff455fa2)),
    ExploreCategory("Offline runtimes", "Install and manage signed packs", "languages", Icons.Default.Extension, Color(0xff397261)),
    ExploreCategory("Templates", "Start from a runnable example", "templates", Icons.Default.Layers, Color(0xff805b9e)),
    ExploreCategory("Snippets", "Find saved code and examples", "snippets", Icons.Default.ContentCut, Color(0xff916447)),
    ExploreCategory("Downloads", "App updates and runtime packs", "downloads", Icons.Default.Download, Color(0xff4e6786)),
    ExploreCategory("Utilities", "Developer tools and diagnostics", "utilities", Icons.Default.Build, Color(0xff77662e))
)

@Composable
internal fun MainDestinations(
    model: WorkspaceModel,
    destination: MainDestination,
    onDestination: (MainDestination) -> Unit,
    onAction: (String) -> Unit,
    projects: @Composable (Dp) -> Unit,
    modifier: Modifier = Modifier,
    backdropVisible: Boolean = true
) {
    val rendering = if (backdropVisible) rememberGlassRendering(model) else GlassRendering.Solid
    val backdrop = if (rendering >= GlassRendering.Blur) rememberGraphicsLayer() else null
    var origin by remember { mutableStateOf(Offset.Zero) }
    val workspace = model.recentWorkspace
    val runtimeStatus = model.parkedWorkspace?.takeIf { it == workspace }?.let {
        listOfNotNull(model.runner.execution?.mode, model.runner.status).joinToString(" · ")
    }
    val haptics = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    Box(modifier.fillMaxWidth().background(colors.background)) {
        Box(Modifier.fillMaxSize()
            .onGloballyPositioned { origin = it.positionInRoot() }
            .then(if (backdrop != null) Modifier.recordGlassBackdrop(backdrop, colors.background) else Modifier)) {
            val bottomPadding = if (workspace != null) 150.dp else 84.dp
            when (destination) {
                MainDestination.Home -> HomeDashboard(model, workspace, bottomPadding, onAction)
                MainDestination.Projects -> projects(bottomPadding)
                MainDestination.Explore -> ExploreScreen(bottomPadding, onAction)
            }
        }
        IconButton(onClick = { onAction("settings") },
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 16.dp)
                .size(48.dp).background(colors.surfaceContainer, CircleShape)) {
            Icon(Icons.Default.Settings, "Settings", Modifier.size(21.dp))
        }
        Column(Modifier.align(Alignment.BottomCenter).widthIn(max = 520.dp).fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (workspace != null) {
                LiquidGlassSurface(Modifier.fillMaxWidth(), RoundedCornerShape(50), backdrop, origin,
                    rendering, model.progressiveBlur) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { model.openWorkspaceLocation(workspace) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)) {
                            FileTypeIcon(workspace.file, Modifier.size(22.dp))
                            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                Text(workspace.project, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelLarge)
                                val subtitle = when {
                                    model.isWorkspaceDirty(workspace) -> workspace.file.substringAfterLast('/') + " · Saving"
                                    runtimeStatus != null -> workspace.file.substringAfterLast('/') + " · " + runtimeStatus
                                    else -> workspace.file.substringAfterLast('/')
                                }
                                Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ArrowOutward, "Resume workspace", Modifier.size(18.dp))
                        }
                        if (model.runner.running || isRunnableWorkspaceFile(model, workspace.file)) {
                            IconButton(onClick = {
                                if (model.runner.running) model.runner.stop()
                                else model.openWorkspaceLocation(workspace, run = true)
                            }) {
                                val running = model.runner.running
                                Icon(if (running) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    if (running) "Stop" else "Run recent file", Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }
            FloatingNavigation(destination, {
                if (model.touchFeedback && it != destination) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onDestination(it)
            }, { onAction("search") }, backdrop, origin, rendering, model.progressiveBlur)
        }
    }
}

@Composable
private fun FloatingNavigation(
    current: MainDestination,
    onDestination: (MainDestination) -> Unit,
    search: () -> Unit,
    backdrop: GraphicsLayer?,
    origin: Offset,
    rendering: GlassRendering,
    progressive: Boolean
) {
    val colors = MaterialTheme.colorScheme
    val motion = workspaceMotion()
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        LiquidGlassSurface(Modifier.weight(1f), CircleShape, backdrop, origin, rendering, progressive) {
            BoxWithConstraints(Modifier.fillMaxWidth().padding(4.dp)) {
                val tabWidth = maxWidth / MainDestination.entries.size
                val selection by animateDpAsState(tabWidth * current.ordinal,
                    animationSpec = if (motion.millis(180) > 0) spring(dampingRatio = .82f, stiffness = 510f) else snap(),
                    label = "navigation selection")
                Box(Modifier.matchParentSize()) {
                    Box(Modifier.offset(x = selection).width(tabWidth).fillMaxHeight()
                        .background(colors.primaryContainer, CircleShape))
                }
                Row(Modifier.fillMaxWidth()) {
                    MainDestination.entries.forEach { item ->
                        val selected = item == current
                        Column(Modifier.weight(1f).heightIn(min = 50.dp).clip(CircleShape)
                            .selectable(selected, role = Role.Tab, onClick = { onDestination(item) })
                            .padding(vertical = 5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center) {
                            Icon(if (selected) item.active else item.inactive, null, Modifier.size(21.dp),
                                tint = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant)
                            Text(item.label, style = MaterialTheme.typography.labelSmall, maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
        LiquidGlassSurface(Modifier.size(58.dp), CircleShape, backdrop, origin, rendering, progressive) {
            IconButton(search, Modifier.fillMaxSize()) { Icon(Icons.Default.Search, "Search") }
        }
    }
}

@Composable
private fun HomeDashboard(
    model: WorkspaceModel,
    workspace: WorkspaceLocation?,
    bottomPadding: Dp,
    onAction: (String) -> Unit
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = pagePadding(bottomPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { DestinationTitle("Home") }
        if (model.projects.isEmpty() && !model.busy) item {
            ReimaginedEmptyState(Icons.Default.Code, "Start coding",
                "Create a project to open your first workspace.",
                action = "New project", onAction = { onAction("newproject") })
        }
        val recentFiles = model.recentWorkspaceFiles.filter { it.project in model.projects && it != workspace }.take(4)
        if (recentFiles.isNotEmpty()) {
            item { Text("Recent files", style = MaterialTheme.typography.titleLarge) }
            items(recentFiles, key = { it.project + "/" + it.file }) { location ->
                DestinationRow(location.file.substringAfterLast('/'), location.project,
                    leading = { FileTypeIcon(location.file, Modifier.size(24.dp)) }) { model.openWorkspaceLocation(location) }
            }
        }
        val activity = model.developerActivity
        val report = activity.report
        item {
            DestinationRow("Developer activity", "Coding time and run history",
                leading = { Icon(Icons.Default.QueryStats, null) }) { onAction("activity") }
        }
        if (activity.enabled && (report.files + report.projects + report.runs > 0)) {
            item { ActivitySummary(report) }
            if (report.executions.isNotEmpty()) {
                item { Text("Recent executions", style = MaterialTheme.typography.titleMedium) }
                items(report.executions.take(3)) { ActivityRunRow(it) }
            }
        }
        item {
            DestinationRow("Software update", model.updater.available?.let { "Update available · build ${it.versionCode}" }
                ?: "Check for updates and read release notes",
                leading = { Icon(Icons.Default.SystemUpdate, null) }) { onAction("updates") }
        }
        val pins = model.quickHomeActions.mapNotNull { id -> workspaceCommandCatalog.firstOrNull { it.id == id } }
            .filterNot { it.id in setOf("newproject", "importzip") || it.requiresProject }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Pinned actions", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { onAction("actions") }) { Text("Edit shortcuts") }
            }
        }
        items(pins, key = { it.id }) { action -> DestinationRow(action.label) { onAction(action.id) } }
    }
}

@Composable
private fun ExploreScreen(bottomPadding: Dp, onAction: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = pagePadding(bottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { DestinationTitle("Explore") }
        items(exploreCategories.chunked(2), key = { it.first().action }) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { category ->
                    Surface({ onAction(category.action) }, Modifier.weight(1f).heightIn(min = 136.dp),
                        shape = CodeLabCorners.card, color = MaterialTheme.colorScheme.surfaceContainer) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(42.dp).background(category.color.copy(alpha = .18f), CodeLabCorners.iconTile),
                                contentAlignment = Alignment.Center) {
                                val ink = contrastAccent(category.color, MaterialTheme.colorScheme.surfaceContainer)
                                Icon(category.icon, null, Modifier.size(23.dp), tint = ink)
                            }
                            Text(category.title, style = MaterialTheme.typography.titleMedium)
                            Text(category.description, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun UtilitiesScreen(onAction: (String) -> Unit, close: () -> Unit) {
    DestinationPage("Utilities", close) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                DestinationRow("Text & data tools", "Format JSON, convert encodings, and hash text",
                    leading = { Icon(Icons.Default.Build, null) }) { onAction("devtools") }
            }
            item {
                DestinationRow("HTTP inspector", "Inspect a response and its headers",
                    leading = { Icon(Icons.Default.Http, null) }) { onAction("httpsinspector") }
            }
            item {
                DestinationRow("Performance diagnostics", "Inspect local editor and project timings",
                    leading = { Icon(Icons.Default.Speed, null) }) { onAction("performance") }
            }
            item {
                DestinationRow("Command palette", "Find and run CodeLab actions",
                    leading = { Icon(Icons.Default.Terminal, null) }) { onAction("commands") }
            }
        }
    }
}

internal fun pagePadding(bottom: Dp) =
    PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = bottom)

@Composable
internal fun DestinationTitle(title: String) {
    Text(title, Modifier.padding(end = 52.dp), style = MaterialTheme.typography.headlineLarge)
}

@Composable
internal fun DestinationRow(
    title: String,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(Modifier.fillMaxWidth().heightIn(min = 58.dp).padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) { leading() }
                Spacer(Modifier.width(10.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun ProjectLanguageIcon(template: String?, modifier: Modifier = Modifier) {
    val extension = if (template == "Web") "html" else template?.let(LanguageCatalog::extensionFor)
    if (extension == null) Icon(Icons.Default.Folder, null, modifier)
    else FileTypeIcon("file." + extension, modifier)
}

private fun isRunnableWorkspaceFile(model: WorkspaceModel, path: String): Boolean {
    val extension = languageExtension(path)
    if (extension in setOf("html", "htm")) return true
    return model.cloudAvailable(path) || extension in setOf("py", "ts") || OfflineRuntimeRegistry.manifest(extension) != null
}

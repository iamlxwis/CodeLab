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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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
internal fun WorkspaceTopBar(m: WorkspaceModel, project: String?, onAction: (String) -> Unit) {
    val haptics = LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val motion = rememberCodeLabMotion(m.motionPreference, m.motionIntensity, m.motionSpeed, m.springIntensity, m.transitionEffect)
    val duration = motion.millis(170)
    val scale by animateFloatAsState(if (pressed) .97f else 1f,
        animationSpec = motion.pressSpec(), label = "run-press")
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        if (project == null) Mark(Modifier.padding(horizontal = 8.dp).size(34.dp))
        else IconButton(onClick = { UiLatencyProbe.nextFrame(context, "Home"); m.home() }) {
            Icon(Icons.Default.ArrowBack, "Project library")
        }
        val swipeThreshold = with(LocalDensity.current) { 44.dp.toPx() }
        var titleDrag by remember(project) { mutableFloatStateOf(0f) }
        Column(Modifier.weight(1f).padding(start = 4.dp)
            .pointerInput(project, swipeThreshold) {
                if (project != null) detectHorizontalDragGestures(
                    onDragStart = { titleDrag = 0f },
                    onDragEnd = { if (kotlin.math.abs(titleDrag) >= swipeThreshold)
                        m.moveActiveTab(if (titleDrag > 0) -1 else 1) },
                    onHorizontalDrag = { change, dx -> change.consume(); titleDrag += dx })
            }.clickable(enabled = project != null) { onAction("switchproject") }) {
            AnimatedContent(
                targetState = project ?: "CodeLab",
                transitionSpec = { fadeIn(tween(duration)) togetherWith fadeOut(tween(duration)) },
                label = "project-title"
            ) { title ->
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (project != null) Text(if (m.dirty) "Saving changes…" else "Local workspace",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (project != null && (m.runner.running || m.active != null)) {
            Button(
                onClick = {
                    UiLatencyProbe.nextFrame(context, "Run")
                    if (m.touchFeedback) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (m.runner.running) m.runner.stop() else m.runPreferred()
                },
                interactionSource = interaction,
                modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale },
                shape = RoundedCornerShape(11.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                AnimatedContent(targetState = m.runner.running,
                    transitionSpec = { fadeIn(tween(duration)) togetherWith fadeOut(tween(duration)) },
                    label = "execution-state") { running ->
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(if (running) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(if (running) "Stop" else if (m.active?.endsWith(".html", true) == true)
                            "Preview" else "Run", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        if (project != null) {
            IconButton(onClick = {
                UiLatencyProbe.nextFrame(context, "Save")
                if (m.touchFeedback) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                m.flush()
            }) { Icon(Icons.Default.Save, "Save file") }
            IconButton(onClick = { onAction("projectsearch") }) {
                Icon(Icons.Default.Search, "Search project")
            }
        }
        IconButton(onClick = { onAction("actions") }) {
            Icon(Icons.Default.MoreVert, "More tools and settings")
        }
    }
}


@Composable
private fun Mark(modifier: Modifier) {
    Surface(modifier, color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(10.dp)) {
        Icon(androidx.compose.ui.res.painterResource(R.drawable.ic_codelab_splash_mark),
            contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.fillMaxSize().padding(2.dp))
    }
}

@Composable
internal fun ProjectLibrary(m: WorkspaceModel, motionMillis: Int, new: () -> Unit,
    importZip: () -> Unit, importGitHub: () -> Unit,
    rename: (String) -> Unit, delete: (String) -> Unit, duplicate: (String) -> Unit,
    importFolder: (() -> Unit)? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var options by remember { mutableStateOf<String?>(null) }
    var importOptions by rememberSaveable { mutableStateOf(false) }
    var projectSearch by rememberSaveable { mutableStateOf("") }
    var projectSort by rememberSaveable { mutableStateOf("Recent") }
    var favoritesOnly by rememberSaveable { mutableStateOf(false) }
    var sortOpen by remember { mutableStateOf(false) }
    LaunchedEffect(m.projects, m.project) { m.refreshProjectCardDetails() }
    val spacing = CodeLabSpacing.forDensity(m.interfaceDensity)
    val visibleProjects = orderedProjects(
        m.projects.filter { it.contains(projectSearch.trim(), ignoreCase = true) && (!favoritesOnly || it in m.favorites) },
        m.favorites, projectSort)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(
        horizontal = CodeLabSpacing.screen, vertical = CodeLabSpacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing)) {
        item {
             Text("Projects", style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold)
             Spacer(Modifier.height(CodeLabSpacing.large))
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CodeLabSpacing.small)) {
                Button(onClick = new, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("New project", maxLines = 1)
                }
                OutlinedButton(onClick = { importOptions = true }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.FileDownload, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Import", maxLines = 1)
                }
            }
            Spacer(Modifier.height(12.dp))
            CodeLabSearch(projectSearch, { projectSearch = it }, "Find a project")
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = favoritesOnly, onClick = { favoritesOnly = !favoritesOnly },
                    label = { Text("Favorites") },
                    leadingIcon = { Icon(if (favoritesOnly) Icons.Default.Star else Icons.Default.StarBorder, null, Modifier.size(17.dp)) })
                Spacer(Modifier.weight(1f))
                Box {
                    TextButton(onClick = { sortOpen = true }) {
                        Icon(Icons.Default.Sort, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(projectSort)
                        Icon(Icons.Default.ArrowDropDown, null, Modifier.size(18.dp))
                    }
                    CodeLabDropdownMenu(expanded = sortOpen, onDismissRequest = { sortOpen = false }) {
                        listOf("Recent", "A–Z", "Z–A").forEach { choice ->
                            DropdownMenuItem(text = { Text(choice) }, onClick = { projectSort = choice; sortOpen = false },
                                trailingIcon = if (projectSort == choice) ({ Icon(Icons.Default.Check, null) }) else null)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            val visibleCount = visibleProjects.size
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$visibleCount " + if (visibleCount == 1) "project" else "projects", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    IconButton(onClick = { m.chooseProjectGrid(!m.projectGrid) }) {
                        Icon(if (m.projectGrid) Icons.Default.ViewList else Icons.Default.GridView,
                            if (m.projectGrid) "List view" else "Grid view")
                    }
                }
            }
        }
        if (m.busy && m.projects.isEmpty()) item {
            WorkspaceSkeleton(3)
        }
        if (m.projects.isEmpty() && !m.busy) item {
            ClayPanel {
                ReimaginedEmptyState(Icons.Default.FolderOpen, "No projects yet",
                    "Start with a template or import work you already have.",
                    action = "New project", onAction = new)
            }
        }
        if (visibleProjects.isEmpty() && m.projects.isNotEmpty()) item {
            val noFavorites = favoritesOnly && m.projects.none { it in m.favorites }
            ClayPanel {
                Column(Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(if (noFavorites) Icons.Default.StarBorder else Icons.Default.SearchOff,
                        null, Modifier.size(26.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(if (noFavorites) "No favorites yet" else "No matching projects",
                        fontWeight = FontWeight.Medium)
                    TextButton(onClick = { projectSort = "Recent"; projectSearch = ""; favoritesOnly = false }) {
                        Text("Show all projects")
                    }
                }
            }
        }
        items(visibleProjects.chunked(if (m.projectGrid) 2 else 1), key = { it.first() }) { row ->
            Row(Modifier.fillMaxWidth().animateItem(
                fadeInSpec = if (motionMillis > 0) tween(motionMillis) else null,
                placementSpec = if (motionMillis > 0) tween(motionMillis) else null,
                fadeOutSpec = if (motionMillis > 0) tween(motionMillis) else null),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { name ->
            val pressInteraction = remember(name) { MutableInteractionSource() }
            val isPressed by pressInteraction.collectIsPressedAsState()
            val pressScale by animateFloatAsState(
                if (isPressed) .97f else 1f,
                animationSpec = CodeLabMotion(motionMillis / 170f, m.springIntensity).pressSpec(),
                label = "project-press")
            val threshold = with(LocalDensity.current) { 56.dp.toPx() }
            var drag by remember(name) { mutableFloatStateOf(0f) }
            ClayActionCard(
                onClick = { UiLatencyProbe.nextFrame(context, "Open project"); m.openProject(name) },
                interactionSource = pressInteraction,
                modifier = Modifier.weight(1f).graphicsLayer { scaleX = pressScale; scaleY = pressScale }
                    .pointerInput(name, threshold) {
                        detectHorizontalDragGestures(onDragStart = { drag = 0f },
                            onDragEnd = { if (kotlin.math.abs(drag) > threshold) options = name; drag = 0f },
                            onDragCancel = { drag = 0f },
                            onHorizontalDrag = { change, dx -> change.consume(); drag += dx })
                    }
            ) {
                Row(Modifier.fillMaxWidth().padding(if (m.projectGrid) 4.dp else 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (!m.projectGrid) Surface(shape = CodeLabCorners.iconTile,
                        tonalElevation = CodeLabElevation.icon,
                        color = MaterialTheme.colorScheme.primaryContainer) {
                        Icon(Icons.Default.Folder, null, Modifier.padding(10.dp).size(22.dp),
                            tint = MaterialTheme.colorScheme.primary)
                    }
                    if (name in m.favorites) {
                        Icon(Icons.Default.Star, "Favorite project",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = if (m.projectGrid) 2.dp else 10.dp).size(18.dp))
                    }
                    Column(Modifier.weight(1f).padding(start = if (m.projectGrid) 6.dp else 14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(name, maxLines = 2, overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium)
                        val summary = m.projectCardDetails[name]
                        Text(if (summary != null) summary.template + " · " +
                            summary.fileCount + (if (summary.fileCount == 1) " file" else " files")
                            else "Local project",
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (summary?.description?.isNotBlank() == true) Text(
                            summary.description,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box {
                        IconButton(onClick = { options = name }) { Icon(Icons.Default.MoreVert, "Manage $name") }
                        CodeLabDropdownMenu(expanded = options == name, onDismissRequest = { options = null }) {
                            DropdownMenuItem(
                                text = { Text(if (name in m.favorites) "Remove from favorites" else "Add to favorites") },
                                onClick = { options = null; m.toggleFavoriteProject(name) })
                            DropdownMenuItem(text = { Text("Duplicate") }, leadingIcon = { Icon(Icons.Default.ContentCopy, null) }, onClick = { options = null; duplicate(name) })
                            DropdownMenuItem(text = { Text("Rename") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { options = null; rename(name) })
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text("Move to trash", color = MaterialTheme.colorScheme.error) }, leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) }, onClick = { options = null; delete(name) })
                        }
                    }
                }
            }
            }
            if (m.projectGrid && row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
    if (importOptions) LibraryImportSheet(
        onDismiss = { importOptions = false },
        onZip = { importOptions = false; importZip() },
        onGitHub = { importOptions = false; importGitHub() },
        onFolder = importFolder?.let { action -> { importOptions = false; action() } }
    )
}

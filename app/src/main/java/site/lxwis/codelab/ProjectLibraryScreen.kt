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
internal fun WorkspaceTopBar(m: WorkspaceModel, project: String?, onAction: (String) -> Unit) {
    val haptics = LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val duration = rememberCodeLabMotion(m.motionPreference, m.motionIntensity).millis(170)
    val scale by animateFloatAsState(if (pressed) .97f else 1f,
        animationSpec = tween(duration, easing = FastOutSlowInEasing), label = "run-press")
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        if (project == null) Mark(Modifier.padding(horizontal = 8.dp).size(34.dp))
        else IconButton(onClick = { UiLatencyProbe.nextFrame(context, "Home"); m.home() }) {
            Icon(Icons.Default.ArrowBack, "Project library")
        }
        Column(Modifier.weight(1f).padding(start = 4.dp)
            .clickable(enabled = project != null) { onAction("switchproject") }) {
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
internal fun ProjectLibrary(m: WorkspaceModel, motionMillis: Int, new: () -> Unit,
    importZip: () -> Unit, importGitHub: () -> Unit,
    rename: (String) -> Unit, delete: (String) -> Unit, duplicate: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var options by remember { mutableStateOf<String?>(null) }
    var importOptions by rememberSaveable { mutableStateOf(false) }
    var projectSearch by rememberSaveable { mutableStateOf("") }
    var projectSort by rememberSaveable { mutableStateOf("Recent") }
    LaunchedEffect(m.projects, m.project) { m.refreshProjectCardDetails() }
    val spacing = CodeLabSpacing.forDensity(m.interfaceDensity)
    val visibleProjects = orderedProjects(
        m.projects.filter { it.contains(projectSearch.trim(), ignoreCase = true) },
        m.favorites, projectSort)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(
        horizontal = CodeLabSpacing.screen, vertical = CodeLabSpacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing)) {
        item {
            Text("YOUR WORKSPACE", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(7.dp))
            Text("Projects", style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold)
            Text("Open a project or start something new.", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(value = projectSearch, onValueChange = { projectSearch = it },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(18.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (projectSearch.isNotEmpty()) IconButton(onClick = { projectSearch = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear project search")
                    }
                },
                placeholder = { Text("Find a project") })
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Recent", "Favorites", "A–Z", "Z–A")) { option ->
                    FilterChip(selected = projectSort == option, onClick = { projectSort = option },
                        label = { Text(option) },
                        leadingIcon = if (projectSort == option)
                            ({ Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) })
                            else null)
                }
            }
            Spacer(Modifier.height(12.dp))
            val visibleCount = visibleProjects.size
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("ON THIS DEVICE", fontSize = 11.sp, letterSpacing = 1.6.sp,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$visibleCount / ${m.projects.size}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (m.projects.isEmpty()) item {
            ClayPanel {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("No projects yet", fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    Text("Choose New project to start with a template, or import work you already have.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = new) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("New project") }
                }
            }
        }
        if (visibleProjects.isEmpty() && m.projects.isNotEmpty()) item {
            val noFavorites = projectSort == "Favorites" && m.projects.none { it in m.favorites }
            ClayPanel {
                Column(Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(if (noFavorites) Icons.Default.StarBorder else Icons.Default.SearchOff,
                        null, Modifier.size(26.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(if (noFavorites) "No favorites yet" else "No matching projects",
                        fontWeight = FontWeight.Medium)
                    TextButton(onClick = { projectSort = "Recent"; projectSearch = "" }) {
                        Text("Show all projects")
                    }
                }
            }
        }
        items(visibleProjects, key = { it }) { name ->
            val pressInteraction = remember(name) { MutableInteractionSource() }
            val isPressed by pressInteraction.collectIsPressedAsState()
            val pressScale by animateFloatAsState(
                if (isPressed) .97f else 1f,
                animationSpec = tween(motionMillis, easing = FastOutSlowInEasing),
                label = "project-press")
            ClayActionCard(
                onClick = { UiLatencyProbe.nextFrame(context, "Open project"); m.openProject(name) },
                interactionSource = pressInteraction,
                modifier = Modifier.graphicsLayer { scaleX = pressScale; scaleY = pressScale }
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CodeLabCorners.iconTile,
                        tonalElevation = CodeLabElevation.icon,
                        color = MaterialTheme.colorScheme.primaryContainer) {
                        Icon(Icons.Default.Folder, null, Modifier.padding(10.dp).size(22.dp),
                            tint = MaterialTheme.colorScheme.primary)
                    }
                    if (name in m.favorites) {
                        Icon(Icons.Default.Star, "Favorite project",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 10.dp).size(18.dp))
                    }
                    Column(Modifier.weight(1f).padding(start = 14.dp),
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
                        DropdownMenu(expanded = options == name, onDismissRequest = { options = null }) {
                            DropdownMenuItem(
                                text = { Text(if (name in m.favorites) "Remove from favorites" else "Add to favorites") },
                                onClick = { options = null; m.toggleFavoriteProject(name) })
                            DropdownMenuItem(text = { Text("Duplicate") }, onClick = { options = null; duplicate(name) })
                            DropdownMenuItem(text = { Text("Rename") }, onClick = { options = null; rename(name) })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = { options = null; delete(name) })
                        }
                    }
                }
            }
        }
    }
    if (importOptions) LibraryImportSheet(
        onDismiss = { importOptions = false },
        onZip = { importOptions = false; importZip() },
        onGitHub = { importOptions = false; importGitHub() }
    )
}

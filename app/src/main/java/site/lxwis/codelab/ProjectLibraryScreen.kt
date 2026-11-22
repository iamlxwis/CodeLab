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
    val systemMotion = remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }
    val duration = if (m.motionPreference == "Reduced" ||
        (m.motionPreference == "System" && !systemMotion)) 0 else 170
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
            Text(project ?: "CodeLab", fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (project != null) Text(if (m.dirty) "Saving changes…" else "Local workspace",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (project != null && (m.runner.running || m.active != null)) {
            Button(
                onClick = {
                    UiLatencyProbe.nextFrame(context, "Run")
                    if (m.touchFeedback) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (m.runner.running) m.runner.stop() else m.run()
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
internal fun ProjectLibrary(m: WorkspaceModel, motionMillis: Int, new: () -> Unit,
    importZip: () -> Unit, importGitHub: () -> Unit,
    rename: (String) -> Unit, delete: (String) -> Unit, duplicate: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
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
                Button(onClick = new, enabled = true, shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text("New project")
                }
                OutlinedButton(onClick = importZip, enabled = true,
                    shape = RoundedCornerShape(10.dp)) { Text("Import ZIP") }
                TextButton(onClick = importGitHub, enabled = true) { Text("GitHub") }
            }
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(value = projectSearch, onValueChange = { projectSearch = it },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = { Text("Search projects") })
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Recent", "Favorites", "A–Z", "Z–A").forEach { option ->
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
        if (m.projects.isEmpty()) item {
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
                "Favorites" -> visible.filter { it in m.favorites }
                "A–Z" -> visible.sortedWith(String.CASE_INSENSITIVE_ORDER)
                "Z–A" -> visible.sortedWith(String.CASE_INSENSITIVE_ORDER.reversed())
                else -> visible.sortedWith(compareBy<String> { it !in m.favorites }
                    .thenBy { m.projects.indexOf(it) })
            }
        }, key = { it }) { name ->
            val pressInteraction = remember(name) { MutableInteractionSource() }
            val isPressed by pressInteraction.collectIsPressedAsState()
            val pressScale by animateFloatAsState(
                if (isPressed) .97f else 1f,
                animationSpec = tween(motionMillis, easing = FastOutSlowInEasing),
                label = "project-press")
            Surface(onClick = { UiLatencyProbe.nextFrame(context, "Open project"); m.openProject(name) },
                enabled = true,
                interactionSource = pressInteraction,
                modifier = Modifier.graphicsLayer { scaleX = pressScale; scaleY = pressScale }
                    .animateContentSize(animationSpec = tween(motionMillis)),
                shape = RoundedCornerShape(16.dp), tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(11.dp),
                        color = MaterialTheme.colorScheme.primaryContainer) {
                        Icon(Icons.Default.Folder, null, Modifier.padding(10.dp).size(22.dp),
                            tint = MaterialTheme.colorScheme.primary)
                    }
                    if (name in m.favorites) {
                        Icon(Icons.Default.Star, "Favorite project",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 10.dp).size(18.dp))
                    }
                    Text(name, Modifier.weight(1f).padding(start = 14.dp), maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
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
}

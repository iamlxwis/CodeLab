package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** A visible execution selection directly beneath the editor's Run control. */
@Composable
internal fun RunModeStrip(
    m: WorkspaceModel, onLanguages: () -> Unit, onProjectSettings: () -> Unit
) {
    val active = m.active
    val localReady = active?.let(m::isLocalExecutable) ?: false
    val cloudReady = active?.let(m::cloudAvailable) ?: false
    val preview = active?.endsWith(".html", true) == true ||
        active?.endsWith(".htm", true) == true
    val missingModule = m.executionMode == "Offline" &&
        active != null && !localReady && !preview
    val status = when {
        m.runner.running -> "Executing " + m.executionMode.lowercase()
        active == null -> "Open a file to run"
        preview -> "Offline HTML preview"
        m.executionMode == "Offline" && localReady -> "Ready: local runtime"
        missingModule -> "Offline runtime not available for this file"
        m.executionMode == "Online" && cloudReady -> "Online: source upload needs confirmation"
        m.executionMode == "Online" && !m.isOnline -> "Internet connection required"
        else -> "No online compiler for this file"
    }
    var modesOpen by remember { mutableStateOf(false) }
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box {
                TextButton(onClick = { modesOpen = true }, enabled = !m.runner.running) {
                    Icon(if (m.executionMode == "Offline") Icons.Default.Computer else Icons.Default.Cloud,
                        null, Modifier.size(16.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(m.executionMode)
                    Icon(Icons.Default.ArrowDropDown, null, Modifier.size(16.dp))
                }
                DropdownMenu(expanded = modesOpen, onDismissRequest = { modesOpen = false }) {
                    listOf("Offline", "Online").forEach { mode ->
                        DropdownMenuItem(text = { Text(mode) }, onClick = {
                            m.chooseExecutionMode(mode); modesOpen = false
                        }, leadingIcon = { Icon(if (mode == "Offline") Icons.Default.Computer else Icons.Default.Cloud, null) })
                    }
                }
            }
            if (m.runner.running) CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
            Text(status, Modifier.weight(1f), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (missingModule || (m.executionMode == "Online" && !cloudReady)) {
                TextButton(onClick = onLanguages) { Text(if (missingModule) "Packs" else "Setup") }
            }
            RunTargetMenu(m, onProjectSettings)
        }
    }
}

/** Explicit run source: clicking the primary Run action respects the entrypoint. */
@Composable
internal fun RunTargetMenu(m: WorkspaceModel, onProjectSettings: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(44.dp)) {
            Icon(Icons.Default.MoreHoriz, "Run file and entry options")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            m.active?.let { path ->
                DropdownMenuItem(text = { Text("Run active · ${path.substringAfterLast('/')}") },
                    onClick = { expanded = false; m.runTarget(path) })
            }
            m.projectMetadata.entrypoint.takeIf { it in m.files && it != m.active }?.let { path ->
                DropdownMenuItem(text = { Text("Run entry · ${path.substringAfterLast('/')}") },
                    onClick = { expanded = false; m.runTarget(path) })
            }
            DropdownMenuItem(text = { Text("Choose another file…") },
                onClick = { expanded = false; showPicker = true })
            DropdownMenuItem(text = { Text("Set preferred entry file…") },
                onClick = { expanded = false; onProjectSettings() })
        }
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    if (showPicker) RunFilePicker(m.files, m.cloudLanguages.keys, m::runTarget,
        installedModules = if (PythonModuleLocator.installed(context)) setOf("py") else emptySet()
    ) { showPicker = false }
}

@Composable
internal fun RunFilePicker(
    files: List<String>,
    cloudExtensions: Set<String> = emptySet(),
    onRun: (String) -> Unit,
    installedModules: Set<String> = emptySet(),
    onDismiss: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var runnableOnly by rememberSaveable { mutableStateOf(true) }
    val matches = files.map { it to RunCapability.forPath(it, cloudExtensions, installedModules) }
        .filter { (path, target) ->
            path.contains(query, ignoreCase = true) &&
                (!runnableOnly || target.mode != RunCapability.Mode.UNAVAILABLE)
        }.sortedWith(compareBy({ it.second.mode.ordinal }, { it.first.lowercase() }))
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("Choose a run target") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(),
                    label = { Text("Search project files") }, singleLine = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(selected = runnableOnly,
                        onClick = { runnableOnly = !runnableOnly },
                        label = { Text("Runnable only") })
                    Spacer(Modifier.width(10.dp))
                    Text(matches.size.toString() + " files",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (matches.isEmpty()) Text(
                    "No matching runnable files. Disable the filter to see all files.",
                    style = MaterialTheme.typography.bodySmall)
                LazyColumn(Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(matches, key = { it.first }) { (path, target) ->
                        val available = target.mode != RunCapability.Mode.UNAVAILABLE
                        Surface(onClick = { onDismiss(); onRun(path) },
                            enabled = available,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = MaterialTheme.shapes.medium) {
                            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                Text(path, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(target.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (available) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } })
}

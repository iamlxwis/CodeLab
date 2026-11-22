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
internal fun NameDialog(title: String, initial: String, label: String, example: String, cancel: () -> Unit, submit: (String) -> Unit) {
    var name by rememberSaveable(title, initial) { mutableStateOf(initial) }
    AlertDialog(onDismissRequest = cancel, title = { Text(title) },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text(label) }, placeholder = { Text(example) }) },
        confirmButton = { TextButton(onClick = { submit(name.trim()) }, enabled = name.isNotBlank()) { Text(if (title.startsWith("New")) "Create" else if (title.startsWith("Import")) "Import" else "Rename") } },
        dismissButton = { TextButton(onClick = cancel) { Text("Cancel") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateProjectDialog(cancel: () -> Unit, submit: (String, String) -> Unit) {
    val keyboard = LocalSoftwareKeyboardController.current
    var name by rememberSaveable { mutableStateOf("") }
    var template by rememberSaveable { mutableStateOf("JavaScript") }
    var browsing by rememberSaveable { mutableStateOf(false) }
    var search by rememberSaveable { mutableStateOf("") }
    var offlineOnly by rememberSaveable { mutableStateOf(false) }
    val languages = LanguageCatalog.entries.filter {
        (!offlineOnly || it.local != null) &&
            (search.isBlank() || it.name.contains(search.trim(), true) ||
                it.extension.contains(search.trim(), true))
    }
    ModalBottomSheet(onDismissRequest = cancel,
        modifier = Modifier.imePadding(),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.86f)
            .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (browsing) {
                    IconButton(onClick = { browsing = false; keyboard?.hide() }) {
                        Icon(Icons.Default.ArrowBack, "Back to project details")
                    }
                    Text("Choose language", modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineSmall)
                } else {
                    Text("New project", modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineSmall)
                    Button(onClick = { keyboard?.hide(); submit(name.trim(), template) },
                        enabled = name.isNotBlank(), shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) {
                        Text("Create project")
                    }
                }
            }
            if (!browsing) {
            Text("Choose a language, then create your workspace.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(name, { name = it.take(100) }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
                label = { Text("Project name") }, placeholder = { Text("e.g. Playground") })
            Text("POPULAR", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                listOf(
                    listOf("JavaScript", "Java", "Python"),
                    listOf("Lua", "SQLite", "Web")
                ).forEach { row ->
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        row.forEach { choice ->
                            FilterChip(
                                selected = template == choice,
                                onClick = { template = choice; browsing = false },
                                modifier = Modifier.weight(1f),
                                label = { Text(choice, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis) })
                        }
                    }
                }
            }
            Text("STARTER TEMPLATES", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("To-do app", "API fetch", "Algorithms", "SQL playground", "Lua game loop")
                    .chunked(2).forEach { choices ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            choices.forEach { choice ->
                                FilterChip(selected = template == choice,
                                    onClick = { template = choice; browsing = false },
                                    modifier = Modifier.weight(1f),
                                    label = { Text(choice, maxLines = 1,
                                        overflow = TextOverflow.Ellipsis) })
                            }
                            if (choices.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
            }
            Surface(onClick = { browsing = !browsing; keyboard?.hide() },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Row(Modifier.fillMaxWidth().padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(template, fontWeight = FontWeight.SemiBold)
                        Text("Browse ${LanguageCatalog.entries.size} languages & file types",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ExpandMore, contentDescription = "Browse languages")
                }
            }
            }
            if (browsing) {
                Text("Search all languages and file types. Editing is offline; execution depends on the runtime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(search, { search = it }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true, label = { Text("Search language or extension") },
                    leadingIcon = { Icon(Icons.Default.Search, null) })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(selected = offlineOnly, onClick = { offlineOnly = !offlineOnly },
                        label = { Text("Offline-capable") })
                    Spacer(Modifier.width(10.dp))
                    Text("${languages.size} results", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LazyColumn(Modifier.weight(1f)) {
                    items(languages, key = { it.name }) { language ->
                        Row(Modifier.fillMaxWidth().clickable {
                            template = language.name; browsing = false; keyboard?.hide()
                        }.semantics { contentDescription = "Select " + language.name }
                            .padding(vertical = 11.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(language.name)
                                Text(language.local ?: "Edit offline · online run not guaranteed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("." + language.extension, fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.labelSmall)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f))
                    }
                }
            } else {
                val info = if (template == "Web") "Offline HTML/CSS/JS preview"
                    else LanguageCatalog.entries.firstOrNull { it.name == template }?.local
                        ?.let { "$it · included with CodeLab" }
                        ?: "Edit offline. Running this language may require an online compiler."
                Text(info, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)

@Composable
internal fun ProjectSwitcher(m: WorkspaceModel, dismiss: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    AlertDialog(onDismissRequest = dismiss,
        title = { Text("Switch workspace") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search projects") }, singleLine = true)
                val visible = m.projects.filter { it.contains(query, ignoreCase = true) }
                    .sortedWith(compareBy<String> { it !in m.favorites }
                        .thenBy { m.projects.indexOf(it) })
                LazyColumn(Modifier.heightIn(max = 330.dp)) {
                    items(visible, key = { it }) { name ->
                        Row(Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically) {
                            if (name in m.favorites)
                                Icon(Icons.Default.Star, null, Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary)
                            TextButton(enabled = true, modifier = Modifier.weight(1f),
                                onClick = { m.openProject(name); dismiss() }) {
                                Text(name, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = dismiss) { Text("Close") } })
}


@Composable
internal fun ProjectSettingsDialog(m: WorkspaceModel, dismiss: () -> Unit) {
    var description by remember(m.project) { mutableStateOf(m.projectMetadata.description) }
    var entrypoint by remember(m.project) { mutableStateOf(m.projectMetadata.entrypoint) }
    var filesExpanded by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = dismiss,
        title = { Text("Project settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Template · " + m.projectMetadata.template,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(description, { description = it.take(500) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2, maxLines = 4,
                    label = { Text("Project description") })
                Text("Preferred entry file", style = MaterialTheme.typography.labelMedium)
                Box {
                    OutlinedButton(onClick = { filesExpanded = true }, enabled = m.files.isNotEmpty()) {
                        Text(if (entrypoint.isEmpty()) "Not configured" else entrypoint,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    DropdownMenu(expanded = filesExpanded,
                        onDismissRequest = { filesExpanded = false }) {
                        DropdownMenuItem(text = { Text("No preference") }, onClick = {
                            entrypoint = ""
                            filesExpanded = false
                        })
                        m.files.take(80).forEach { path ->
                            DropdownMenuItem(text = { Text(path, maxLines = 2) },
                                onClick = { entrypoint = path; filesExpanded = false })
                        }
                    }
                }
                Text("Run uses this entry file when available. Use the Run arrow to run another file.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Settings are kept inside the project and included in ZIP backups.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (entrypoint.isNotEmpty()) TextButton(enabled = true,
                    onClick = { m.select(entrypoint); dismiss() }) {
                    Text("Open entry file")
                }
            }
        },
        confirmButton = {
            TextButton(enabled = true, onClick = {
                m.configureProject(description, entrypoint)
                dismiss()
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } })
}

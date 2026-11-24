package site.lxwis.codelab

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val quickLanguages = listOf(
    "JavaScript", "Python", "TypeScript", "Java", "C++", "Lua", "SQLite", "Web"
)
private val projectDemos = listOf(
    "To-do app", "API fetch", "Algorithms", "SQL playground", "Lua game loop"
)

/**
 * Progressive project creation: name -> language -> starting point -> preview.
 * The create action stays visible; language browsing never discards the project name.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GuidedProjectSheet(cancel: () -> Unit, submit: (String, String, String, String) -> Unit) {
    val keyboard = LocalSoftwareKeyboardController.current
    var name by rememberSaveable { mutableStateOf("") }
    var language by rememberSaveable { mutableStateOf("JavaScript") }
    var starter by rememberSaveable { mutableStateOf(StarterExamples.BEGINNER) }
    var browsing by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var offlineOnly by rememberSaveable { mutableStateOf(false) }
    var demosOpen by rememberSaveable { mutableStateOf(false) }
    var guided by rememberSaveable { mutableStateOf(false) }
    var execution by rememberSaveable { mutableStateOf("Offline") }
    val isDemo = language in projectDemos
    val catalog = LanguageCatalog.entries.firstOrNull { it.name == language }
    val context = androidx.compose.ui.platform.LocalContext.current
    val runStatus = when {
        isDemo -> "Ready-made example project"
        language == "Python" -> PythonModuleLocator.check(context).description
        else -> RuntimePackCatalog.capability(language).let { capability ->
            if (capability.state == RuntimePackCatalog.State.BUNDLED && language !in setOf("Java", "HTML", "CSS", "Web")) "Offline execution available" else capability.label
        }
    }
    val snippet = remember(language, starter) {
        when {
            isDemo -> "Includes example project files ready to explore."
            language == "Web" -> StarterExamples.example("HTML", starter)
            catalog != null -> StarterExamples.example(language, starter)
            else -> ""
        }
    }
    ModalBottomSheet(
        onDismissRequest = cancel,
        modifier = Modifier.imePadding(),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(.91f).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (browsing) {
                    IconButton(onClick = { browsing = false; keyboard?.hide() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to project setup")
                    }
                    Text("Choose language", Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineSmall)
                } else {
                    Column(Modifier.weight(1f)) {
                        Text("New project", style = MaterialTheme.typography.headlineSmall)
                        Text("Choose a language and starting point",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = cancel) {
                        Icon(Icons.Default.Close, contentDescription = "Close project setup")
                    }
                }
            }
            if (browsing) {
                OutlinedTextField(query, { query = it },
                    Modifier.fillMaxWidth(), singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text("Search languages and file types") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(offlineOnly, onClick = { offlineOnly = !offlineOnly },
                        label = { Text("Included offline") })
                    Spacer(Modifier.width(12.dp))
                    Text("Editing is available for every listed language",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val results = LanguageCatalog.entries.filter {
                    (!offlineOnly || it.local != null) &&
                        (it.name.contains(query, true) || it.extension.contains(query, true))
                }
                LazyColumn(Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Web is a multi-file project template, not a language-catalog entry.
                    // It must be discoverable by search as well as through the quick tile.
                    if ("Web".contains(query, ignoreCase = true)) {
                        item(key = "web-project") {
                            Surface(
                                onClick = { language = "Web"; browsing = false; keyboard?.hide() },
                                color = if (language == "Web") MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text("Web", style = MaterialTheme.typography.titleMedium)
                                        Text("Offline HTML, CSS and JavaScript preview",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text("index.html", fontFamily = FontFamily.Monospace,
                                        style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                    items(results, key = { it.name }) { item ->
                        Surface(
                            onClick = { language = item.name; browsing = false; keyboard?.hide() },
                            color = if (language == item.name) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.titleMedium)
                                    Text(if (item.local != null) "Offline capable" else "Edit offline · no bundled runtime",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("." + item.extension, fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (guided) "Guided setup" else "Quick setup", Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = { guided = !guided }) { Text(if (guided) "Quick setup" else "Guide me") }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Project name", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedTextField(name, { name = it.take(100) },
                                Modifier.fillMaxWidth(), singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                placeholder = { Text("My first project") })
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Language", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                items(quickLanguages) { choice ->
                                    val selected = language == choice
                                    Surface(onClick = { language = choice },
                                        color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceContainerLow,
                                        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                            else null,
                                        shape = RoundedCornerShape(16.dp)) {
                                        Column(Modifier.width(100.dp).padding(9.dp),
                                            verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                            FileTypeIcon("file." + (LanguageCatalog.extensionFor(choice) ?: "html"), Modifier.size(23.dp))
                                            Text(choice, maxLines = 1,
                                                style = MaterialTheme.typography.titleSmall)
                                        }
                                    }
                                }
                            }
                            Surface(onClick = { browsing = true; keyboard?.hide() },
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shape = RoundedCornerShape(14.dp)) {
                                Row(Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(language, style = MaterialTheme.typography.titleMedium)
                                        Text(runStatus, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text("Change", color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelLarge)
                                    Icon(Icons.Default.ChevronRight, "Browse all languages",
                                        Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }
                    item {
                        Column {
                            Text("Execution", style = MaterialTheme.typography.labelMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Offline", "Online").forEach { mode ->
                                    FilterChip(selected = execution == mode, onClick = { execution = mode },
                                        label = { Text(mode) })
                                }
                            }
                            Text(if (execution == "Offline") "Runs locally when the required pack is available."
                                else "Internet and explicit source-upload approval required.",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (guided && !isDemo) item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Template", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            listOf(
                                Triple(StarterExamples.BEGINNER, "Beginner example", "Learn from a short sample"),
                                Triple(StarterExamples.HELLO, "Hello World", "Minimal starting program"),
                                Triple(StarterExamples.BLANK, "Blank file", "Write everything yourself")
                            ).forEach { (mode, title, detail) ->
                                Surface(onClick = { starter = mode },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (starter == mode) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerLow) {
                                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = starter == mode,
                                            onClick = { starter = mode })
                                        Column(Modifier.padding(start = 8.dp)) {
                                            Text(title, style = MaterialTheme.typography.titleSmall)
                                            Text(detail, style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (guided) item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Preview", Modifier.weight(1f),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(if (isDemo) "Project" else StarterExamples.fileName(
                                    language, catalog?.extension ?: "html"),
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.labelSmall)
                            }
                            Surface(color = MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = RoundedCornerShape(16.dp)) {
                                Text(if (snippet.isBlank()) "An empty file will be created."
                                    else snippet.take(900),
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    maxLines = 9, overflow = TextOverflow.Ellipsis,
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    item {
                        TextButton(onClick = { demosOpen = !demosOpen }) {
                            Icon(if (demosOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Ready-made project examples")
                        }
                        if (demosOpen) Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            projectDemos.forEach { demo ->
                                Surface(onClick = { language = demo },
                                    color = if (language == demo) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = RoundedCornerShape(12.dp)) {
                                    Row(Modifier.fillMaxWidth().padding(13.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Text(demo, Modifier.weight(1f))
                                        if (language == demo)
                                            Icon(Icons.Default.Check, contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
                Button(onClick = { keyboard?.hide(); submit(name.trim(), language, starter, execution) },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(15.dp)) {
                    Text("Create project")
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

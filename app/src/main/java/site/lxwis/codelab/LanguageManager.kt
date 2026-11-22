package site.lxwis.codelab

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

private data class SupportedLanguage(val name: String, val extension: String, val local: String? = null)

private val catalog = listOf(
    SupportedLanguage("JavaScript", "js", "Offline · Rhino"),
    SupportedLanguage("Lua", "lua", "Offline · LuaJ"),
    SupportedLanguage("Java", "java", "Offline beta · BeanShell"),
    SupportedLanguage("SQLite", "sql", "Offline · in-memory"),
    SupportedLanguage("HTML", "html", "Offline preview"),
    SupportedLanguage("CSS", "css", "Offline preview"),
    SupportedLanguage("Python", "py"),
    SupportedLanguage("C", "c"),
    SupportedLanguage("C++", "cpp"),
    SupportedLanguage("C#", "cs"),
    SupportedLanguage("TypeScript", "ts"),
    SupportedLanguage("Kotlin", "kt"),
    SupportedLanguage("Rust", "rs"),
    SupportedLanguage("Go", "go"),
    SupportedLanguage("PHP", "php"),
    SupportedLanguage("Ruby", "rb"),
    SupportedLanguage("Swift", "swift"),
    SupportedLanguage("Bash", "sh"),
    SupportedLanguage("Dart", "dart"),
    SupportedLanguage("Scala", "scala"),
    SupportedLanguage("Perl", "pl"),
    SupportedLanguage("Haskell", "hs"),
    SupportedLanguage("R", "r"),
    SupportedLanguage("Elixir", "ex"),
    SupportedLanguage("Erlang", "erl"),
    SupportedLanguage("OCaml", "ml"),
    SupportedLanguage("Fortran", "f90"),
    SupportedLanguage("COBOL", "cob"),
    SupportedLanguage("Pascal", "pas"),
    SupportedLanguage("Assembly", "asm"),
    SupportedLanguage("Objective-C", "m"),
    SupportedLanguage("Groovy", "groovy"),
    SupportedLanguage("F#", "fs"),
    SupportedLanguage("Visual Basic .NET", "vb"),
    SupportedLanguage("Prolog", "plg")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageManagerSheet(model: WorkspaceModel, close: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("All") }
    var inspected by remember { mutableStateOf<SupportedLanguage?>(null) }
    val detected = remember { OfflineRuntimeRegistry.engines()
        .associateBy({ it.extension }, { OfflineRuntimeRegistry.locallyAvailable(it.extension) }) }
    val filteredLanguages = catalog.filter { language ->
        (language.name.contains(search, ignoreCase = true) ||
            language.extension.contains(search, ignoreCase = true)) &&
        when (filter) {
            "Offline" -> language.local != null
            "Cloud ready" -> model.isOnline && model.cloudLanguages.containsKey(language.extension)
            else -> true
        }
    }

    inspected?.let { language ->
        val engine = OfflineRuntimeRegistry.manifest(language.extension)
        AlertDialog(onDismissRequest = { inspected = null },
            title = { Text(language.name + " runtime") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (engine == null) "No embedded runtime" else if (detected[language.extension] == true)
                        "Installed in this APK" else "Runtime unavailable",
                        style = MaterialTheme.typography.titleMedium)
                    Text(engine?.engine?.plus(" · ")?.plus(engine.version)
                        ?: "Editing supported, execution requires a compatible online compiler.")
                    Text(engine?.capabilities ?: "No offline compiler or interpreter bundled.",
                        style = MaterialTheme.typography.bodySmall)
                    Text(engine?.limitations ?: "The Android app does not bundle an offline toolchain for this language.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Offline runtimes are packaged with CodeLab; no runtime download is required.",
                        style = MaterialTheme.typography.labelSmall)
                }
            }, confirmButton = { TextButton(onClick = { inspected = null }) { Text("Close") } })
    }
    ModalBottomSheet(onDismissRequest = close, sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.92f).padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Languages", style = MaterialTheme.typography.headlineSmall)
                    Text("Local execution first · online when available",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                }
                Icon(if (model.isOnline) Icons.Default.Cloud else Icons.Default.WifiOff,
                    contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            OutlinedTextField(search, { search = it },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) },
                label = { Text("Find a language") })
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Offline", "Cloud ready").forEach { option ->
                    FilterChip(selected = filter == option, onClick = { filter = option },
                        label = { Text(option) })
                }
            }
            Text("${filteredLanguages.size} languages matching",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider()
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp),
                contentPadding = PaddingValues(bottom = 28.dp)) {
                item {
                    Text("COMPILER CONNECTION", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(if (!model.isOnline) "Device offline — local runtimes remain available"
                        else if (model.cloudChecking) "Checking compiler language availability…"
                        else if (model.cloudError != null) "Compiler not ready: " + model.cloudError
                        else "Online compiler verified · " + model.cloudLanguages.size + " language families",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (model.cloudError != null && model.isOnline)
                            MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    if (model.cloudChecking) {
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { model.refreshCloudLanguages() },
                        modifier = Modifier.fillMaxWidth(), enabled = !model.cloudChecking && model.isOnline,
                        shape = RoundedCornerShape(12.dp)) {
                        Text(if (model.cloudChecking) "Checking…" else "Check free compiler availability")
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("No account or API key needed in CodeLab. The public compiler may be busy, " +
                        "unavailable, or restrict free requests. Local languages work without internet. " +
                        "Online runs require your confirmation before any source code is sent.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text("LANGUAGE AVAILABILITY", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                }
                if (filteredLanguages.isEmpty()) item {
                    Text("No languages match this filter.",
                        Modifier.padding(vertical = 24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(filteredLanguages, key = { it.extension }) { language ->
                    val cloud = model.cloudLanguages[language.extension]
                    val engine = OfflineRuntimeRegistry.manifest(language.extension)
                    val localInstalled = engine != null && detected[language.extension] == true
                    Row(Modifier.fillMaxWidth().heightIn(min = 50.dp)
                        .clickable { inspected = language },
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(19.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(language.name, style = MaterialTheme.typography.bodyMedium)
                            Text(when {
                                localInstalled && cloud != null -> "Offline · " + engine!!.engine + " " + engine.version + " · Cloud ready"
                                localInstalled -> "Offline · " + engine!!.engine + " " + engine.version
                                engine != null -> "Offline runtime unavailable"
                                language.local != null && cloud != null -> language.local + " · Online ready"
                                language.local != null -> language.local
                                cloud != null && model.isOnline -> "Online ready · " + cloud.name
                                !model.isOnline -> "Online only · no connection"
                                else -> "Online execution not verified"
                            }, style = MaterialTheme.typography.labelSmall,
                                maxLines = 2,
                                color = if (cloud != null || localInstalled)
                                    MaterialTheme.colorScheme.primary else
                                    MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("." + language.extension, fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f))
                }
                item {
                    Text("Other file types can still be edited. Runtime availability is verified " +
                        "against the configured compiler's /languages endpoint; editor support " +
                        "does not mean code can be executed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun OnlineRunConfirmation(
    filename: String, endpoint: String, language: String,
    cancel: () -> Unit, start: (String) -> Unit
) {
    var stdin by rememberSaveable(filename) { mutableStateOf("") }
    AlertDialog(onDismissRequest = cancel,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        icon = { Icon(Icons.Default.Cloud, contentDescription = null) },
        title = { Text("Run online?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(filename + " · " + language, style = MaterialTheme.typography.bodyMedium)
                Text("Your current source code and the input below will be sent to:",
                    style = MaterialTheme.typography.bodySmall)
                Text(endpoint, color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium)
                Text("Do not upload passwords, private keys, or other secrets.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(stdin, { stdin = it.take(4096) },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Standard input (optional)") },
                    placeholder = { Text("Text supplied before execution") },
                    minLines = 2, maxLines = 4)
                Text("Online input is provided before the run, not interactively.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = { start(stdin) }) { Text("Send and run") }
        },
        dismissButton = { TextButton(onClick = cancel) { Text("Cancel") } }
    )
}

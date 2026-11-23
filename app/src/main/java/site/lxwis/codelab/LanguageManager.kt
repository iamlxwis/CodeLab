package site.lxwis.codelab

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
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

private val catalog = LanguageCatalog.entries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageManagerSheet(model: WorkspaceModel, initialLanguage: String? = null, close: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("All") }
    var inspected by remember(initialLanguage) {
        mutableStateOf(catalog.firstOrNull { it.name == initialLanguage })
    }
    val context = LocalContext.current
    val module = remember(context) { PythonModuleDownload(context.applicationContext) }
    var downloadId by remember { mutableLongStateOf(module.lastJob()) }
    var transfer by remember { mutableStateOf(module.inspect(downloadId)) }
    var moduleMessage by remember { mutableStateOf("") }
    var recheck by remember { mutableIntStateOf(0) }
    val lifecycle = LocalLifecycleOwner.current
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                recheck++
                downloadId = module.lastJob()
                transfer = module.inspect(downloadId)
            }
        }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { lifecycle.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(downloadId) {
        if (downloadId > 0L) {
            do {
                transfer = withContext(Dispatchers.IO) { module.inspect(downloadId) }
                if (transfer.stage != PythonModuleDownload.Stage.DOWNLOADING &&
                    transfer.stage != PythonModuleDownload.Stage.PAUSED) break
                delay(650L)
            } while (true)
        }
    }
    val python = remember(recheck) { PythonModuleLocator.check(context) }
    val detected = remember(python.state) {
        OfflineRuntimeRegistry.engines()
            .associateBy({ it.extension }, { OfflineRuntimeRegistry.locallyAvailable(it.extension) }) +
            ("py" to python.ready)
    }
    val filteredLanguages = catalog.filter { language ->
        (language.name.contains(search, ignoreCase = true) ||
            language.extension.contains(search, ignoreCase = true)) &&
        when (filter) {
            "Offline" -> detected[language.extension] == true
            "Cloud ready" -> model.isOnline && model.cloudLanguages.containsKey(language.extension)
            else -> true
        }
    }

    ModalBottomSheet(onDismissRequest = close, sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.92f).padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Languages & file types", style = MaterialTheme.typography.headlineSmall)
                    Text("Edit broadly · run only when a runtime is available",
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
                    Text("EXECUTION AVAILABILITY", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(if (!model.isOnline) "Device offline — local runtimes remain available"
                        else if (model.cloudChecking) "Checking compiler language availability…"
                        else if (model.cloudError != null) "Online execution is not available right now"
                        else "Online execution available for " + model.cloudLanguages.size + " language families",
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
                        Text(if (model.cloudChecking) "Checking…" else "Check online availability")
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Online execution uses an external service. Availability varies by language. " +
                        "Your code is sent only after you approve an online run.",
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
                    val localInstalled = detected[language.extension] == true
                    Row(Modifier.fillMaxWidth().heightIn(min = 50.dp)
                        .clickable { inspected = language },
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(19.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(language.name, style = MaterialTheme.typography.bodyMedium)
                            Text(when {
                                language.name == "Python" -> python.description +
                                    (if (cloud != null) " · Cloud ready" else "")
                                localInstalled && cloud != null -> "Offline · " + engine!!.engine + " " + engine.version + " · Cloud ready"
                                localInstalled -> "Offline · " + engine!!.engine + " " + engine.version
                                engine != null -> "Offline runtime unavailable"
                                language.local != null && cloud != null -> language.local + " · Online ready"
                                language.local != null -> language.local
                                cloud != null && model.isOnline -> "Online execution ready"
                                !model.isOnline -> "Online only · no connection"
                                else -> RuntimePackCatalog.capability(language.name).label
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
                    Text("Language listings indicate editing support, not offline execution. " +
                        "Run availability is checked separately for your connection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
    inspected?.let { language ->
        val engine = OfflineRuntimeRegistry.manifest(language.extension)
        AlertDialog(onDismissRequest = { inspected = null },
            title = { Text(language.name + " runtime") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (language.name == "Python") python.description
                        else if (engine == null) RuntimePackCatalog.capability(language.name).label else if (detected[language.extension] == true)
                        "Installed in this APK" else "Runtime unavailable",
                        style = MaterialTheme.typography.titleMedium)
                    Text(if (language.name == "Python") "CPython 3.13 · optional companion APK"
                        else engine?.engine?.plus(" · ")?.plus(engine.version)
                            ?: "Editing supported, execution requires a compatible online compiler.")
                    Text(engine?.capabilities ?: RuntimePackCatalog.capability(language.name).detail,
                        style = MaterialTheme.typography.bodySmall)
                    Text(engine?.limitations ?: "The Android app does not bundle an offline toolchain for this language.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (language.name == "Python")
                        "Download and manage Python here. Android confirms the final installation."
                        else "Other engines remain bundled during modular migration.",
                        style = MaterialTheme.typography.labelSmall)
                    if (language.name == "Python") {
                        if (transfer.stage == PythonModuleDownload.Stage.DOWNLOADING ||
                            transfer.stage == PythonModuleDownload.Stage.PAUSED) {
                            if (transfer.total > 0L) {
                                LinearProgressIndicator(progress = { transfer.progress },
                                    modifier = Modifier.fillMaxWidth())
                                Text("${transfer.received / 1024 / 1024} of ${transfer.total / 1024 / 1024} MB",
                                    style = MaterialTheme.typography.labelSmall)
                            } else LinearProgressIndicator(Modifier.fillMaxWidth())
                            if (transfer.message.isNotEmpty()) Text(transfer.message,
                                style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = {
                                module.cancel()
                                downloadId = -1L
                                transfer = PythonModuleDownload.Transfer(PythonModuleDownload.Stage.IDLE)
                            }) { Text("Cancel download") }
                        } else {
                            if (transfer.stage == PythonModuleDownload.Stage.FAILED) {
                                Text(transfer.message, color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            if (transfer.stage == PythonModuleDownload.Stage.DOWNLOADED) {
                                val verification = remember(downloadId, transfer.stage) { module.verify() }
                                Text(verification ?: "Download complete · signature verified",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (verification == null) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error)
                                if (verification == null) Button(onClick = {
                                    moduleMessage = module.install()
                                }) { Text("Install module") }
                            }
                            Button(onClick = {
                                moduleMessage = ""
                                try {
                                    downloadId = module.begin()
                                    transfer = PythonModuleDownload.Transfer(
                                        PythonModuleDownload.Stage.DOWNLOADING)
                                } catch (e: Exception) {
                                    moduleMessage = "Cannot start download: " + e.javaClass.simpleName
                                }
                            }) {
                                Text(if (python.ready) "Check for updates" else
                                    if (transfer.stage == PythonModuleDownload.Stage.DOWNLOADED)
                                        "Download again" else "Download Python")
                            }
                            // Also allow removal of incompatible or older-signed packs.
                            if (python.state != PythonModuleLocator.State.NOT_INSTALLED)
                                OutlinedButton(onClick = {
                                    moduleMessage = module.uninstall()
                                }) { Text("Remove Python module") }
                        }
                        if (moduleMessage.isNotEmpty()) Text(moduleMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = {
                            recheck++
                            transfer = module.inspect()
                        }) { Text("Refresh module status") }
                    }
                }
            }, confirmButton = { TextButton(onClick = { inspected = null }) { Text("Close") } })
    }

}

@Composable
fun OnlineRunConfirmation(
    filename: String, language: String,
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
                Text("Your current source code and the input below will be sent to an external " +
                    "execution service. The service may process or retain that code.",
                    style = MaterialTheme.typography.bodySmall)
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

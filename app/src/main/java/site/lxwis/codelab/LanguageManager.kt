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
import androidx.compose.material.icons.filled.Close
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
    var filter by rememberSaveable { mutableStateOf("Installed") }
    var removePythonConfirm by remember { mutableStateOf(false) }
    var optionalInspect by remember { mutableStateOf<OptionalRuntimeLocator.Pack?>(null) }
    var clearCacheConfirm by remember { mutableStateOf(false) }
    var wifiOnly by remember { mutableStateOf(false) }
    var inspected by remember(initialLanguage) {
        mutableStateOf(catalog.firstOrNull { it.name == initialLanguage })
    }
    val context = LocalContext.current
    val module = remember(context) { PythonModuleDownload(context.applicationContext) }
    LaunchedEffect(module) { wifiOnly = module.wifiOnly() }
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
    val offlineEngineCount = OfflineRuntimeRegistry.engines().count {
        it.extension in setOf("js", "lua", "java", "sql") &&
            OfflineRuntimeRegistry.locallyAvailable(it.extension)
    } + if (python.ready) 1 else 0
    val cachedInstallerBytes = remember(transfer, recheck) { module.cachedBytes() }
    val optionalPacks = remember(context) { OptionalRuntimeLocator.Pack.entries.associateWith {
        OptionalRuntimeDownload(context.applicationContext, it)
    } }
    val optionalTransfers = remember(recheck) {
        optionalPacks.mapValues { it.value.inspect() }
    }
    val optionalStatuses = remember(recheck) {
        optionalPacks.keys.associateWith { OptionalRuntimeLocator.check(context, it) }
    }
    val pythonApkBytes = remember(recheck) { module.installedApkBytes() }
    val pythonVersionCode = remember(recheck) { module.installedVersionCode() }
    val filteredLanguages = catalog.filter { language ->
        (language.name.contains(search, ignoreCase = true) ||
            language.extension.contains(search, ignoreCase = true)) &&
        when (filter) {
            "Installed" -> detected[language.extension] == true
            "Downloads" -> (language.name == "Python" && (python.state !=
                PythonModuleLocator.State.NOT_INSTALLED || cachedInstallerBytes > 0L ||
                transfer.stage != PythonModuleDownload.Stage.IDLE)) ||
                (optionalPacks.keys.any { it.label == language.name &&
                    (optionalStatuses[it]?.state != PythonModuleLocator.State.NOT_INSTALLED ||
                     optionalPacks[it]?.cachedBytes() ?: 0L > 0L ||
                     optionalTransfers[it]?.stage != OptionalRuntimeDownload.Stage.IDLE) })
            "Downloadable" -> RuntimePackCatalog.isDownloadable(language.name)
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
                    Text("Language Manager", style = MaterialTheme.typography.headlineSmall)
                    Text("Manage offline engines, downloads and execution",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                }
                Icon(if (model.isOnline) Icons.Default.Cloud else Icons.Default.WifiOff,
                    contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                IconButton(onClick = close) {
                    Icon(Icons.Default.Close, contentDescription = "Close languages")
                }
            }
            OutlinedTextField(search, { search = it; if (it.isNotBlank()) filter = "All" },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) },
                label = { Text("Find a language") })
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Installed", "Downloads", "Downloadable", "All", "Cloud ready").forEach { option ->
                    FilterChip(selected = filter == option, onClick = { filter = option },
                        label = { Text(option) })
                }
            }
            Text("${filteredLanguages.size} matching · $offlineEngineCount executable engines · " +
                "Installer cache: ${cachedInstallerBytes / 1024 / 1024} MB",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider()
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp),
                contentPadding = PaddingValues(bottom = 28.dp)) {
                // Separate transfer management from results so installed engines are
                // immediately visible, even on small Android screens.
                if (search.isBlank() && filter == "Downloads") item {
                    Text("DOWNLOAD MANAGER", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("Python: " + python.description,
                        style = MaterialTheme.typography.bodySmall)
                    Text("Python package APK: ${pythonApkBytes / 1024 / 1024} MB" +
                        (pythonVersionCode?.let { " · version code $it" } ?: "") +
                        " · Installer cache: ${cachedInstallerBytes / 1024 / 1024} MB",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Wi-Fi only downloads", style = MaterialTheme.typography.bodyMedium)
                            Text("Applies to the next Python download",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = wifiOnly, onCheckedChange = {
                            wifiOnly = it
                            module.setWifiOnly(it)
                        })
                    }
                    Text(when (transfer.stage) {
                        PythonModuleDownload.Stage.DOWNLOADING, PythonModuleDownload.Stage.PAUSED ->
                            "Python download: " + (if (transfer.total > 0L)
                                "${(transfer.progress * 100).toInt()}%" else "in progress") +
                                (if (transfer.stage == PythonModuleDownload.Stage.PAUSED)
                                    " · paused by Android" else "")
                        PythonModuleDownload.Stage.DOWNLOADED -> "Python installer downloaded · ready for signature check"
                        PythonModuleDownload.Stage.FAILED -> "Python download failed · retry from Manage Python"
                        else -> "No active module download"
                    }, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (transfer.stage == PythonModuleDownload.Stage.DOWNLOADING ||
                        transfer.stage == PythonModuleDownload.Stage.PAUSED)
                        LinearProgressIndicator(progress = { transfer.progress },
                            modifier = Modifier.fillMaxWidth())
                    Text("JavaScript and Lua remain built into CodeLab as safe fallbacks. " +
                        "Optional modules are verified before use. Unpublished compiler packs cannot be downloaded.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            inspected = catalog.firstOrNull { it.name == "Python" }
                        }) { Text("Manage Python") }
                        if (cachedInstallerBytes > 0L) OutlinedButton(onClick = {
                            clearCacheConfirm = true
                        }) { Text("Clear cache") }
                    }
                    OptionalRuntimeLocator.Pack.entries.forEach { pack ->
                        val transfer = optionalTransfers[pack]
                        val status = optionalStatuses[pack]
                        Text(pack.label + ": " + (status?.description ?: "Unknown") +
                            " · installer " + ((optionalPacks[pack]?.cachedBytes() ?: 0L) / 1024 / 1024) + " MB",
                            style = MaterialTheme.typography.bodySmall)
                        Text("Download: " + (transfer?.stage?.name ?: "IDLE") +
                            (if (transfer != null && transfer.total > 0)
                                " · " + (transfer.progress * 100).toInt() + "%" else ""),
                            style = MaterialTheme.typography.labelSmall)
                        OutlinedButton(onClick = { optionalInspect = pack }) {
                            Text("Manage " + pack.label)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
                if (search.isBlank() && filter == "Cloud ready") item {
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
                }
                if (filteredLanguages.isEmpty()) item {
                    Text(if (filter == "Downloads" && search.isBlank())
                        "No downloaded optional modules. Open Downloadable to install Python."
                        else "No languages match this filter.",
                        Modifier.padding(vertical = 24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(filteredLanguages, key = { it.extension }) { language ->
                    val cloud = model.cloudLanguages[language.extension]
                    val engine = OfflineRuntimeRegistry.manifest(language.extension)
                    val localInstalled = detected[language.extension] == true
                    Row(Modifier.fillMaxWidth().heightIn(min = 50.dp)
                        .clickable {
                            val optional = OptionalRuntimeLocator.Pack.entries.firstOrNull {
                                it.label == language.name
                            }
                            if (optional != null) optionalInspect = optional
                            else inspected = language
                        },
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(19.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(language.name, style = MaterialTheme.typography.bodyMedium)
                            Text(when {
                                language.name == "Python" ->
                                    (if (transfer.stage == PythonModuleDownload.Stage.DOWNLOADING ||
                                         transfer.stage == PythonModuleDownload.Stage.PAUSED)
                                        "Downloading Python · ${(transfer.progress * 100).toInt()}%"
                                     else python.description) +
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
    optionalInspect?.let { pack ->
        OptionalRuntimeManagerDialog(
            pack = pack,
            manager = requireNotNull(optionalPacks[pack]),
            status = requireNotNull(optionalStatuses[pack]),
            onDismiss = { optionalInspect = null; recheck++ },
            onRefresh = { recheck++ }
        )
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
                        "Install, update, reinstall or remove the signed Python companion. " +
                            "Android confirms package installation and removal."
                        else if (engine != null) "This engine is built into CodeLab, " +
                            "so it cannot be uninstalled separately."
                        else "No verified downloadable runtime is published for this language.",
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
                                val verification = remember(downloadId, transfer.stage, recheck) { module.verify() }
                                val versionDecision = remember(downloadId, transfer.stage, recheck) {
                                    module.downloadedVersionDecision()
                                }
                                val verifiedDescription = when (versionDecision) {
                                    PythonModuleVersionPolicy.Decision.UPDATE ->
                                        "Newer signed Python runtime verified"
                                    PythonModuleVersionPolicy.Decision.REINSTALL ->
                                        "Current Python version downloaded; reinstall is optional"
                                    PythonModuleVersionPolicy.Decision.NEW_INSTALL ->
                                        "Python download complete · signature verified"
                                    else -> "Python download verified"
                                }
                                Text(verification ?: verifiedDescription,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (verification == null) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error)
                                if (verification == null) Button(onClick = {
                                    moduleMessage = module.install()
                                }) { Text(when (versionDecision) {
                                    PythonModuleVersionPolicy.Decision.UPDATE -> "Update Python"
                                    PythonModuleVersionPolicy.Decision.REINSTALL -> "Reinstall Python"
                                    else -> "Install module"
                                }) }
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
                                Text(if (transfer.stage == PythonModuleDownload.Stage.FAILED)
                                    "Retry Python download" else if (python.ready)
                                    "Download latest Python" else
                                    if (transfer.stage == PythonModuleDownload.Stage.DOWNLOADED)
                                        "Download again" else "Download Python")
                            }
                            // Also allow removal of incompatible or older-signed packs.
                            if (python.state != PythonModuleLocator.State.NOT_INSTALLED)
                                OutlinedButton(onClick = {
                                    removePythonConfirm = true
                                }) { Text("Remove Python module") }
                        }
                        if (moduleMessage.isNotEmpty()) Text(moduleMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = {
                            recheck++
                            downloadId = module.lastJob()
                            transfer = module.inspect()
                        }) { Text("Refresh module status") }
                    }
                }
            }, confirmButton = { TextButton(onClick = { inspected = null }) { Text("Close") } })
    }

    if (removePythonConfirm) AlertDialog(
        onDismissRequest = { removePythonConfirm = false },
        title = { Text("Remove Python runtime?") },
        text = { Text("Android will ask to uninstall the optional Python companion. " +
            "Your CodeLab projects and source files will not be deleted. " +
            "Python offline execution requires reinstalling the module.") },
        confirmButton = { Button(onClick = {
            removePythonConfirm = false
            moduleMessage = module.uninstall()
        }) { Text("Open uninstaller") } },
        dismissButton = { TextButton(onClick = { removePythonConfirm = false }) {
            Text("Cancel")
        } })
    if (clearCacheConfirm) AlertDialog(
        onDismissRequest = { clearCacheConfirm = false },
        title = { Text("Clear downloaded installer?") },
        text = { Text("Remove only the cached Python APK and cancel an active download. " +
            "The installed Python runtime and CodeLab projects stay untouched.") },
        confirmButton = { Button(onClick = {
            val cleared = module.clearCachedInstaller()
            downloadId = -1L
            transfer = PythonModuleDownload.Transfer(PythonModuleDownload.Stage.IDLE)
            moduleMessage = if (cleared) "Installer cache cleared" else "Unable to clear installer cache"
            clearCacheConfirm = false
            recheck++
        }) { Text("Clear installer") } },
        dismissButton = { TextButton(onClick = { clearCacheConfirm = false }) {
            Text("Cancel")
        } })

}


@Composable
internal fun OptionalRuntimeManagerDialog(
    pack: OptionalRuntimeLocator.Pack,
    manager: OptionalRuntimeDownload,
    status: PythonModuleLocator.Status,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {
    var transfer by remember(pack) { mutableStateOf(manager.inspect()) }
    var message by remember(pack) { mutableStateOf("") }
    var confirmRemove by remember(pack) { mutableStateOf(false) }
    var confirmCache by remember(pack) { mutableStateOf(false) }
    var wifiOnly by remember(pack) { mutableStateOf(manager.wifiOnly()) }
    LaunchedEffect(pack, transfer.stage) {
        if (transfer.stage == OptionalRuntimeDownload.Stage.DOWNLOADING ||
            transfer.stage == OptionalRuntimeDownload.Stage.PAUSED) {
            while (true) {
                delay(800L)
                transfer = withContext(Dispatchers.IO) { manager.inspect() }
                if (transfer.stage != OptionalRuntimeDownload.Stage.DOWNLOADING &&
                    transfer.stage != OptionalRuntimeDownload.Stage.PAUSED) break
            }
        }
    }
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(pack.label + " offline runtime") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(status.description)
                Text("Bundled " + pack.label + " remains available if this optional module is removed.",
                    style = MaterialTheme.typography.bodySmall)
                Text("Installed APK: " + manager.installedApkBytes() / 1024 / 1024 +
                    " MB · cache: " + manager.cachedBytes() / 1024 / 1024 + " MB" +
                    (manager.installedVersionCode()?.let { " · version code " + it } ?: ""),
                    style = MaterialTheme.typography.labelSmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Wi-Fi only downloads", modifier = Modifier.weight(1f))
                    Switch(checked = wifiOnly, onCheckedChange = {
                        wifiOnly = it; manager.setWifiOnly(it)
                    })
                }
                Text("Download: " + transfer.stage.name +
                    if (transfer.total > 0L) " · " + (transfer.progress * 100).toInt() + "%" else "")
                if (transfer.stage == OptionalRuntimeDownload.Stage.DOWNLOADING ||
                    transfer.stage == OptionalRuntimeDownload.Stage.PAUSED) {
                    LinearProgressIndicator(progress = { transfer.progress },
                        modifier = Modifier.fillMaxWidth())
                    OutlinedButton(onClick = {
                        manager.cancel()
                        transfer = manager.inspect()
                    }) { Text("Cancel download") }
                } else {
                    if (transfer.stage == OptionalRuntimeDownload.Stage.DOWNLOADED) {
                        val error = manager.verify()
                        Text(error ?: "APK signature and package verified",
                            color = if (error == null) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error)
                        if (error == null) Button(onClick = {
                            message = manager.install()
                        }) { Text(if (status.ready) "Update or reinstall" else "Install module") }
                    }
                    Button(onClick = {
                        message = runCatching { manager.begin(); "Download started" }
                            .getOrElse { "Download failed: " + it.javaClass.simpleName }
                        transfer = manager.inspect()
                    }) { Text(if (transfer.stage == OptionalRuntimeDownload.Stage.FAILED)
                        "Retry download" else "Download " + pack.label) }
                }
                if (status.state != PythonModuleLocator.State.NOT_INSTALLED) {
                    OutlinedButton(onClick = { confirmRemove = true }) {
                        Text("Remove " + pack.label + " module")
                    }
                }
                if (manager.cachedBytes() > 0) {
                    TextButton(onClick = { confirmCache = true }) { Text("Clear installer cache") }
                }
                if (message.isNotEmpty()) Text(message, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = {
                    transfer = manager.inspect()
                    onRefresh()
                }) { Text("Refresh") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
    if (confirmRemove) AlertDialog(onDismissRequest = { confirmRemove = false },
        title = { Text("Remove " + pack.label + " module?") },
        text = { Text("Android will ask to remove the optional runtime. Bundled execution " +
            "and CodeLab projects remain unchanged.") },
        confirmButton = { Button(onClick = {
            message = manager.uninstall(); confirmRemove = false
        }) { Text("Open Android uninstaller") } },
        dismissButton = { TextButton(onClick = { confirmRemove = false }) { Text("Cancel") } })
    if (confirmCache) AlertDialog(onDismissRequest = { confirmCache = false },
        title = { Text("Clear installer cache?") },
        text = { Text("Only the downloaded APK is removed, never projects or installed engines.") },
        confirmButton = { Button(onClick = {
            message = if (manager.clearCachedInstaller()) "Cache cleared" else "Cache clear failed"
            transfer = manager.inspect()
            confirmCache = false
        }) { Text("Clear cache") } },
        dismissButton = { TextButton(onClick = { confirmCache = false }) { Text("Cancel") } })
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

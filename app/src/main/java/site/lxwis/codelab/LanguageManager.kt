package site.lxwis.codelab

import android.text.format.Formatter
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

private val catalog = LanguageCatalog.entries

@Composable
internal fun LanguageManagerScreen(model: WorkspaceModel, initialLanguage: String? = null,
    showCatalog: Boolean = false, detailOnly: Boolean = false, onDownloads: () -> Unit, close: () -> Unit
) {
    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(if (showCatalog) "All" else "Available") }
    var showOnlineStatus by rememberSaveable { mutableStateOf(false) }
    val languageListState = rememberLazyListState()
    // Filtered rows have different lengths; always reveal the new section heading.
    LaunchedEffect(filter, search) { languageListState.scrollToItem(0) }
    var removePythonConfirm by remember { mutableStateOf(false) }
    var optionalInspect by remember(initialLanguage) {
        mutableStateOf(OptionalRuntimeLocator.Pack.entries.firstOrNull { it.label == initialLanguage })
    }
    var inspected by remember(initialLanguage) {
        mutableStateOf(catalog.firstOrNull { candidate ->
            candidate.name == initialLanguage &&
                OptionalRuntimeLocator.Pack.entries.none { it.label == candidate.name }
        })
    }
    val context = LocalContext.current
    val module = remember(context) { PythonModuleDownload(context.applicationContext) }
    var downloadId by remember { mutableLongStateOf(module.lastJob()) }
    var transfer by remember { mutableStateOf(module.inspect(downloadId)) }
    var moduleMessage by remember { mutableStateOf("") }
    var recheck by remember { mutableIntStateOf(0) }
    var publishedAssets by remember { mutableStateOf<Map<String, Long>?>(null) }
    var publishedError by remember { mutableStateOf<String?>(null) }
    var releaseRefresh by remember { mutableIntStateOf(0) }
    LaunchedEffect(releaseRefresh) {
        publishedAssets = null
        publishedError = null
        runCatching { withContext(Dispatchers.IO) { RuntimeReleaseAssets.fetch() } }
            .onSuccess { publishedAssets = it }
            .onFailure {
                publishedError = "Cannot check current GitHub release; check your connection."
            }
    }
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
    val detected = remember(python.state, recheck) {
        OfflineRuntimeRegistry.engines()
            .associateBy({ it.extension }, { OfflineRuntimeRegistry.locallyAvailable(it.extension) }) +
            ("py" to python.ready) +
            ("ts" to OptionalRuntimeLocator.check(context, OptionalRuntimeLocator.Pack.TYPESCRIPT).ready)
    }
    val optionalPacks = remember(context) { OptionalRuntimeLocator.Pack.entries.associateWith {
        OptionalRuntimeDownload(context.applicationContext, it)
    } }
    var optionalTransfers by remember(optionalPacks) {
        mutableStateOf(optionalPacks.mapValues { it.value.inspect() })
    }
    LaunchedEffect(optionalPacks, recheck) {
        do {
            optionalTransfers = withContext(Dispatchers.IO) {
                optionalPacks.mapValues { it.value.inspect() }
            }
            if (optionalTransfers.values.none { it.stage == OptionalRuntimeDownload.Stage.DOWNLOADING ||
                    it.stage == OptionalRuntimeDownload.Stage.PAUSED }) break
            delay(800L)
        } while (true)
    }
    val optionalStatuses = remember(recheck) {
        optionalPacks.keys.associateWith { OptionalRuntimeLocator.check(context, it) }
    }
    val filteredLanguages = catalog.filter { language ->
        (language.name.contains(search, ignoreCase = true) ||
            language.extension.contains(search, ignoreCase = true)) &&
            when (filter) {
                "Installed" -> detected[language.extension] == true
                "Available" -> RuntimePackCatalog.isDownloadable(language.name)
                else -> true
            }
    }.let { languages ->
        if (filter == "Available") languages.sortedBy {
            LanguageManagerPresentation.priority(it.name)
        } else languages
    }

    if (optionalInspect == null && inspected == null) DestinationPage(
        if (showCatalog) "Languages" else "Offline runtimes", close
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CodeLabSearch(search, { search = it; if (it.isNotBlank()) filter = "All" }, "Search languages")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Available", "Installed", "All").forEach { option ->
                    FilterChip(selected = filter == option, onClick = { filter = option },
                        label = { Text(option) })
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    filteredLanguages.size.toString() + " languages",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { showOnlineStatus = !showOnlineStatus }) {
                    Icon(if (model.isOnline) Icons.Default.Cloud else Icons.Default.WifiOff,
                        contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Online")
                }
                TextButton(onClick = onDownloads) {
                    Icon(Icons.Default.Settings, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Downloads")
                }
            }
            if (showOnlineStatus) {
                Surface(shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                    Column(Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Online execution", style = MaterialTheme.typography.titleSmall)
                        Text(when {
                            !model.isOnline -> "No connection. Offline engines remain available."
                            model.cloudChecking -> "Checking online language availability…"
                            model.cloudError != null -> "Online execution unavailable. Try again."
                            else -> model.cloudLanguages.size.toString() +
                                " online language families currently reported"
                        }, style = MaterialTheme.typography.bodySmall)
                        if (model.cloudChecking) LinearProgressIndicator(Modifier.fillMaxWidth())
                        OutlinedButton(onClick = { model.refreshCloudLanguages() },
                            enabled = model.isOnline && !model.cloudChecking) {
                            Text("Refresh online availability")
                        }
                        Text("Your source is uploaded only after you approve an online run.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            LazyColumn(modifier = Modifier.weight(1f), state = languageListState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)) {
                if (filteredLanguages.isEmpty()) item {
                    Text("No languages match your search or filter.",
                        modifier = Modifier.padding(vertical = 24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(filteredLanguages, key = { it.extension }) { language ->
                    val pack = OptionalRuntimeLocator.Pack.entries.firstOrNull {
                        it.label == language.name
                    }
                    val isPython = language.name == "Python"
                    val downloadable = RuntimePackCatalog.isDownloadable(language.name)
                    val moduleReady = if (isPython) python.ready
                        else pack?.let { optionalStatuses[it]?.ready == true } == true
                    val moduleProblem = if (isPython)
                        python.state != PythonModuleLocator.State.NOT_INSTALLED && !python.ready
                        else pack?.let {
                            optionalStatuses[it]?.state != PythonModuleLocator.State.NOT_INSTALLED &&
                                optionalStatuses[it]?.ready != true
                        } == true
                    val activeDownload = if (isPython)
                        transfer.stage == PythonModuleDownload.Stage.DOWNLOADING ||
                            transfer.stage == PythonModuleDownload.Stage.PAUSED
                        else pack?.let {
                            optionalTransfers[it]?.stage == OptionalRuntimeDownload.Stage.DOWNLOADING ||
                                optionalTransfers[it]?.stage == OptionalRuntimeDownload.Stage.PAUSED
                        } == true
                    val status = LanguageManagerPresentation.describe(
                        downloadable = downloadable,
                        moduleReady = moduleReady,
                        bundledReady = detected[language.extension] == true && !moduleReady,
                        onlineReady = model.isOnline &&
                            model.cloudLanguages.containsKey(language.extension),
                        preview = language.name == "HTML" || language.name == "CSS",
                        activeDownload = activeDownload,
                        moduleProblem = moduleProblem
                    )
                    Surface(
                        onClick = {
                            if (pack != null) optionalInspect = pack
                            else inspected = language
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 66.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            FileTypeIcon("file." + language.extension, Modifier.size(28.dp))
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(language.name, style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(status.detail, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(status.label, style = MaterialTheme.typography.labelMedium,
                                color = if (status.emphasis) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1)
                            Icon(Icons.Default.ChevronRight, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp))
                        }
                    }
                }
                item {
                    Text("Languages listed under All support editing. Offline execution requires a " +
                        "built-in or verified installed engine; online runs require your approval.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
    optionalInspect?.let { pack ->
        OptionalRuntimeDetails(
            pack = pack,
            manager = requireNotNull(optionalPacks[pack]),
            status = requireNotNull(optionalStatuses[pack]),
            published = publishedAssets?.containsKey("CodeLab-" + pack.suffix + ".apk"),
            downloadBytes = publishedAssets?.get("CodeLab-" + pack.suffix + ".apk"),
            publishedError = publishedError,
            onCheckRelease = { releaseRefresh++ },
            onDismiss = { if (detailOnly) close() else { optionalInspect = null; recheck++ } },
            onRefresh = { recheck++ }
        )
    }
    inspected?.let { language ->
        val engine = OfflineRuntimeRegistry.manifest(language.extension)
        DestinationPage(language.name + " runtime", close = { if (detailOnly) close() else inspected = null },
            trailing = { FileTypeIcon("file." + language.extension, Modifier.size(32.dp)) }) {
                Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (language.name == "Python") python.description
                        else if (engine == null) RuntimePackCatalog.capability(language.name).label else if (detected[language.extension] == true)
                        "Installed in this APK" else "Runtime unavailable",
                        style = MaterialTheme.typography.titleMedium)
                    Text(if (language.name == "Python") "CPython 3.13 · optional companion APK"
                        else engine?.let { "Offline runtime · " + it.version }
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
                        Text("Installed APK · " + Formatter.formatShortFileSize(context, module.installedApkBytes()) +
                            (module.installedVersionCode()?.let { " · build $it" } ?: ""),
                            style = MaterialTheme.typography.bodySmall)
                        publishedAssets?.get("CodeLab-Python.apk")?.let { bytes ->
                            Text("Download · " + Formatter.formatShortFileSize(context, bytes),
                                style = MaterialTheme.typography.bodySmall)
                        }
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
                                val checks by produceState<Pair<String?, PythonModuleVersionPolicy.Decision?>?>(null, downloadId, recheck) {
                                    value = withContext(Dispatchers.IO) { module.verify() to module.downloadedVersionDecision() }
                                }
                                val verification = checks?.first
                                val versionDecision = checks?.second
                                val verifiedDescription = when (versionDecision) {
                                    PythonModuleVersionPolicy.Decision.UPDATE ->
                                        "Newer signed Python runtime verified"
                                    PythonModuleVersionPolicy.Decision.REINSTALL ->
                                        "Current Python version downloaded; reinstall is optional"
                                    PythonModuleVersionPolicy.Decision.NEW_INSTALL ->
                                        "Python download complete · signature verified"
                                    else -> "Python download verified"
                                }
                                Text(verification ?: if (checks == null) "Verifying installer…" else verifiedDescription,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (verification == null) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error)
                                if (checks != null && verification == null) Button(onClick = {
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
        }
    }

    if (removePythonConfirm) CodeLabAlertDialog(
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

}


@Composable
internal fun OptionalRuntimeDetails(
    pack: OptionalRuntimeLocator.Pack,
    manager: OptionalRuntimeDownload,
    status: PythonModuleLocator.Status,
    published: Boolean?,
    downloadBytes: Long?,
    publishedError: String?,
    onCheckRelease: () -> Unit,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
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
    DestinationPage(pack.label + " offline runtime", onDismiss,
        trailing = { FileTypeIcon("file." + pack.extension, Modifier.size(32.dp)) }) {
            Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(status.description)
                Text(when (published) {
                    true -> "Published in the official CodeLab release"
                    false -> "Module APK is not yet published in the latest release"
                    null -> publishedError ?: "Checking published APK availability..."
                }, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (pack == OptionalRuntimeLocator.Pack.TYPESCRIPT)
                    "Single-file TypeScript transpilation via bundled official compiler. " +
                        "No static type checking, module resolution, Node.js or browser APIs. " +
                        "This engine needs its optional module installed."
                    else "Bundled " + pack.label + " remains available if this optional module is removed.",
                    style = MaterialTheme.typography.bodySmall)
                Text("Installed APK · " + Formatter.formatShortFileSize(context, manager.installedApkBytes()) +
                    (manager.installedVersionCode()?.let { " · build $it" } ?: ""),
                    style = MaterialTheme.typography.bodySmall)
                Text("Installer cache · " + Formatter.formatShortFileSize(context, manager.cachedBytes()),
                    style = MaterialTheme.typography.bodySmall)
                downloadBytes?.let { Text("Download · " + Formatter.formatShortFileSize(context, it),
                    style = MaterialTheme.typography.bodySmall) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Wi-Fi only downloads", modifier = Modifier.weight(1f))
                    Switch(checked = wifiOnly, onCheckedChange = {
                        wifiOnly = it; manager.setWifiOnly(it)
                    })
                }
                Text(when (transfer.stage) {
                    OptionalRuntimeDownload.Stage.IDLE -> "Ready to download"
                    OptionalRuntimeDownload.Stage.DOWNLOADING -> "Downloading"
                    OptionalRuntimeDownload.Stage.PAUSED -> transfer.message.ifBlank { "Download paused" }
                    OptionalRuntimeDownload.Stage.DOWNLOADED -> "Installer downloaded"
                    OptionalRuntimeDownload.Stage.FAILED -> transfer.message.ifBlank { "Download failed" }
                } + if (transfer.total > 0L) " · " + (transfer.progress * 100).toInt() + "%" else "")
                if (transfer.stage == OptionalRuntimeDownload.Stage.DOWNLOADING ||
                    transfer.stage == OptionalRuntimeDownload.Stage.PAUSED) {
                    LinearProgressIndicator(progress = { transfer.progress },
                        modifier = Modifier.fillMaxWidth())
                    OutlinedButton(onClick = {
                        manager.cancel()
                        transfer = manager.inspect()
                        onRefresh()
                    }) { Text("Cancel download") }
                } else {
                    if (transfer.stage == OptionalRuntimeDownload.Stage.DOWNLOADED) {
                        val verification by produceState<Pair<Boolean, String?>>(false to null, transfer) {
                            value = true to withContext(Dispatchers.IO) { manager.verify() }
                        }
                        val (verified, error) = verification
                        Text(error ?: if (verified) "APK signature and package verified" else "Verifying installer…",
                            color = if (error == null) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error)
                        if (verified && error == null) Button(onClick = {
                            message = manager.install()
                        }) { Text(if (status.ready) "Update or reinstall" else "Install module") }
                    }
                    Button(onClick = {
                        message = runCatching { manager.begin(); onRefresh(); "Download started" }
                            .getOrElse { "Download failed: " + it.javaClass.simpleName }
                        transfer = manager.inspect()
                    }, enabled = published == true) { Text(if (transfer.stage == OptionalRuntimeDownload.Stage.FAILED)
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
                TextButton(onClick = onCheckRelease) { Text("Check published availability") }
                TextButton(onClick = {
                    transfer = manager.inspect()
                    onRefresh()
                }) { Text("Refresh") }
            }
    }
    if (confirmRemove) CodeLabAlertDialog(onDismissRequest = { confirmRemove = false },
        title = { Text("Remove " + pack.label + " module?") },
        text = { Text("Android will ask to remove the optional runtime. " +
            (if (pack == OptionalRuntimeLocator.Pack.TYPESCRIPT) "TypeScript offline execution will stop; " else "Bundled execution will remain available; ") +
            "CodeLab projects remain unchanged.") },
        confirmButton = { Button(onClick = {
            message = manager.uninstall(); confirmRemove = false
        }) { Text("Open Android uninstaller") } },
        dismissButton = { TextButton(onClick = { confirmRemove = false }) { Text("Cancel") } })
    if (confirmCache) CodeLabAlertDialog(onDismissRequest = { confirmCache = false },
        title = { Text("Clear installer cache?") },
        text = { Text("Only the downloaded APK is removed, never projects or installed engines.") },
        confirmButton = { Button(onClick = {
            message = if (manager.clearCachedInstaller()) "Cache cleared" else "Cache clear failed"
            transfer = manager.inspect()
            onRefresh()
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
    CodeLabAlertDialog(onDismissRequest = cancel,
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

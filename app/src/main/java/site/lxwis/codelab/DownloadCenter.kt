package site.lxwis.codelab

import android.content.Context
import android.os.SystemClock
import android.text.format.Formatter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class TransferRow(
    val name: String,
    val extension: String,
    val message: String,
    val received: Long = 0,
    val total: Long = 0,
    val speed: Long = 0,
    val active: Boolean = false,
    val failed: Boolean = false,
    val completed: Boolean = false,
    val installed: Boolean = false,
    val cached: Long = 0
)

@Composable
internal fun DownloadCenter(
    model: WorkspaceModel,
    updatesOnly: Boolean,
    onRuntime: (String) -> Unit,
    close: () -> Unit
) {
    val context = LocalContext.current
    val updater = model.updater
    val python = remember(context) { PythonModuleDownload(context.applicationContext) }
    val packs = remember(context) {
        OptionalRuntimeLocator.Pack.entries.associateWith { OptionalRuntimeDownload(context.applicationContext, it) }
    }
    val scope = rememberCoroutineScope()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var change by remember { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var wifiOnly by remember { mutableStateOf(updater.wifiOnly() && python.wifiOnly() && packs.values.all { it.wifiOnly() }) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var transfer by remember { mutableStateOf(AppUpdater.Transfer()) }
    var runtimes by remember { mutableStateOf(emptyList<TransferRow>()) }
    var appSpeed by remember { mutableLongStateOf(0) }
    var verification by remember { mutableStateOf<String?>(null) }
    var verified by remember { mutableStateOf(false) }
    val available = updater.available

    fun changeDownload(action: () -> Unit) {
        if (busy) return
        busy = true
        feedback = null
        scope.launch {
            try {
                withContext(Dispatchers.IO) { action() }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                feedback = failure.localizedMessage ?: "Could not change the download."
            } finally {
                busy = false
                change++
            }
        }
    }

    LaunchedEffect(updatesOnly) { if (updatesOnly) updater.check() }
    LaunchedEffect(lifecycle, change, updatesOnly) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            var previousAt = 0L
            var previousApp = AppUpdater.Transfer()
            var previousRows = emptyList<TransferRow>()
            do {
                val next = withContext(Dispatchers.IO) {
                    updater.inspect() to if (updatesOnly) emptyList() else runtimeRows(context, python, packs)
                }
                val now = SystemClock.elapsedRealtime()
                val elapsed = now - previousAt
                val measuring = previousAt != 0L && elapsed > 0
                val current = next.first
                appSpeed = if (measuring && current.stage == AppUpdater.Stage.Downloading &&
                    previousApp.stage == AppUpdater.Stage.Downloading && current.received >= previousApp.received)
                    (current.received - previousApp.received) * 1000 / elapsed else 0
                transfer = current
                runtimes = next.second.map { row ->
                    val old = previousRows.firstOrNull { it.name == row.name }
                    if (measuring && row.active && old?.active == true && row.received >= old.received) {
                        row.copy(speed = (row.received - old.received) * 1000 / elapsed)
                    } else row
                }
                previousApp = current
                previousRows = next.second
                previousAt = now
                if (!current.active && next.second.none(TransferRow::active)) break
                delay(1200)
            } while (true)
        }
    }
    LaunchedEffect(transfer.stage, change) {
        verified = false
        verification = null
        if (transfer.stage == AppUpdater.Stage.Downloaded) {
            verification = withContext(Dispatchers.IO) { updater.verify() }
            verified = verification == null
        }
    }
    DestinationPage(if (updatesOnly) "Software update" else "Downloads", close) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!updatesOnly) item {
                SettingToggle("Wi-Fi only", wifiOnly) { value ->
                    wifiOnly = value
                    updater.setWifiOnly(value)
                    python.setWifiOnly(value)
                    packs.values.forEach { it.setWifiOnly(value) }
                }
                Text("Applies to new downloads.", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                Surface(shape = CodeLabCorners.card, color = MaterialTheme.colorScheme.surfaceContainer) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("CodeLab", style = MaterialTheme.typography.titleLarge)
                        Text("Installed ${updater.installedVersionName} · build ${updater.installedVersion}",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        when {
                            updater.checking -> LinearProgressIndicator(Modifier.fillMaxWidth())
                            available != null -> Text("Update available · build ${available.versionCode}")
                            updater.latest != null -> Text("You have the latest available build.")
                        }
                        updater.checkError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        updater.compatibilityIssue?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        if (transfer.stage != AppUpdater.Stage.Idle) {
                            val message = when {
                                transfer.stage == AppUpdater.Stage.Downloaded && verified -> "APK verified · ready to install"
                                transfer.stage == AppUpdater.Stage.Downloaded -> verification ?: "Verifying APK…"
                                else -> transfer.message
                            }
                            DownloadProgress(context, message, transfer.received, transfer.total, appSpeed, transfer.active)
                        }
                        feedback?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        when {
                            transfer.active -> TextButton(onClick = { changeDownload(updater::clear) }, enabled = !busy) {
                                Text("Cancel download")
                            }
                            verified -> Button(onClick = {
                                busy = true
                                scope.launch {
                                    try {
                                        val problem = withContext(Dispatchers.IO) { updater.verify() }
                                        if (problem != null) feedback = problem
                                        else {
                                            val intent = updater.installerIntent()
                                            context.startActivity(intent)
                                            if (intent.action == android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES) {
                                                feedback = "Allow installation from CodeLab, then return and choose Install update."
                                            }
                                        }
                                    } catch (_: android.content.ActivityNotFoundException) {
                                        feedback = "Android package installer is unavailable."
                                    } finally {
                                        busy = false
                                    }
                                }
                            }, enabled = !busy) { Text("Install update") }
                            available != null && updater.compatibilityIssue == null ->
                                Button(onClick = { changeDownload { updater.begin(available) } }, enabled = !busy) {
                                val action = if (transfer.stage == AppUpdater.Stage.Failed || verification != null) "Retry" else "Download"
                                Text("$action · " + Formatter.formatShortFileSize(context, available.apkBytes))
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { scope.launch { updater.check(force = true) } }, enabled = !updater.checking) {
                                Text("Check for updates")
                            }
                            if (!transfer.active && transfer.stage != AppUpdater.Stage.Idle) {
                                TextButton(onClick = { changeDownload(updater::clear) }, enabled = !busy) { Text("Clear APK") }
                            }
                        }
                    }
                }
            }
            val notes = updater.latest?.notes.orEmpty()
            if (updatesOnly && notes.isNotBlank()) {
                item {
                    Text("What's new", style = MaterialTheme.typography.titleLarge)
                    updater.latest?.let { Text(it.title, style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                items(notes.lines().filter(String::isNotBlank).take(140)) { line -> ReleaseNote(line) }
            }
            if (!updatesOnly) {
                item { Text("Offline runtime packs", style = MaterialTheme.typography.titleLarge) }
                items(runtimes, key = { it.extension }) { row ->
                    Surface(shape = CodeLabCorners.iconTile, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
                            val storage = if (row.cached > 0) " · " + Formatter.formatShortFileSize(context, row.cached) + " cached" else ""
                            DestinationRow(row.name, (if (row.installed) "Installed" else "Optional runtime") + storage,
                                leading = { FileTypeIcon("runtime.${row.extension}", Modifier.size(24.dp)) }) {
                                onRuntime(row.name)
                            }
                            if (row.active || row.failed || row.completed) {
                                DownloadProgress(context, row.message, row.received, row.total, row.speed, row.active)
                            }
                            if (row.active || row.failed) {
                                TextButton(onClick = {
                                    changeDownload {
                                        if (row.extension == "py") {
                                            if (row.active) python.cancel() else python.begin()
                                        } else packs.entries.firstOrNull { it.key.extension == row.extension }?.value?.let { pack ->
                                            if (row.active) pack.cancel() else pack.begin()
                                        }
                                    }
                                }, enabled = !busy) { Text(if (row.active) "Cancel download" else "Retry download") }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun runtimeRows(context: Context, python: PythonModuleDownload,
    packs: Map<OptionalRuntimeLocator.Pack, OptionalRuntimeDownload>
): List<TransferRow> = buildList {
    val transfer = python.inspect()
    val state = transfer.stage
    add(TransferRow("Python", "py", transfer.message.ifBlank {
        when (state) {
            PythonModuleDownload.Stage.IDLE -> ""
            PythonModuleDownload.Stage.DOWNLOADING -> "Downloading"
            PythonModuleDownload.Stage.PAUSED -> "Paused"
            PythonModuleDownload.Stage.DOWNLOADED -> "Downloaded · open to verify and install"
            PythonModuleDownload.Stage.FAILED -> "Download failed"
        }
    }, transfer.received, transfer.total,
        active = state in setOf(PythonModuleDownload.Stage.DOWNLOADING, PythonModuleDownload.Stage.PAUSED),
        failed = state == PythonModuleDownload.Stage.FAILED, completed = state == PythonModuleDownload.Stage.DOWNLOADED,
        installed = PythonModuleLocator.check(context).ready, cached = python.cachedBytes()))
    packs.forEach { (pack, manager) ->
        val item = manager.inspect()
        val stage = item.stage
        add(TransferRow(pack.label, pack.extension, item.message.ifBlank {
            when (stage) {
                OptionalRuntimeDownload.Stage.IDLE -> ""
                OptionalRuntimeDownload.Stage.DOWNLOADING -> "Downloading"
                OptionalRuntimeDownload.Stage.PAUSED -> "Paused"
                OptionalRuntimeDownload.Stage.DOWNLOADED -> "Downloaded · open to verify and install"
                OptionalRuntimeDownload.Stage.FAILED -> "Download failed"
            }
        }, item.received, item.total,
            active = stage in setOf(OptionalRuntimeDownload.Stage.DOWNLOADING, OptionalRuntimeDownload.Stage.PAUSED),
            failed = stage == OptionalRuntimeDownload.Stage.FAILED, completed = stage == OptionalRuntimeDownload.Stage.DOWNLOADED,
            installed = OptionalRuntimeLocator.check(context, pack).ready, cached = manager.cachedBytes()))
    }
}

@Composable
private fun DownloadProgress(context: Context, message: String, received: Long, total: Long, speed: Long, active: Boolean) {
    if (active) {
        if (total > 0) LinearProgressIndicator(progress = { (received.toDouble() / total).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth())
        else LinearProgressIndicator(Modifier.fillMaxWidth())
    }
    val size = if (total > 0) " · " + Formatter.formatShortFileSize(context, received.coerceAtLeast(0)) +
        " / " + Formatter.formatShortFileSize(context, total) else ""
    val throughput = if (speed > 0) " · " + Formatter.formatShortFileSize(context, speed) + "/s" else ""
    Text(message + size + throughput, modifier = Modifier.padding(vertical = 4.dp),
        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ReleaseNote(line: String) {
    val heading = line.startsWith("#")
    val body = line.trimStart('#', ' ').replace(Regex("^[-*] "), "• ")
    val pattern = Regex("\\*\\*([^*]+)\\*\\*")
    val note = buildAnnotatedString {
        var cursor = 0
        for (match in pattern.findAll(body)) {
            append(body.substring(cursor, match.range.first))
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(match.groupValues[1]) }
            cursor = match.range.last + 1
        }
        append(body.substring(cursor))
    }
    Text(note, style = if (heading) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium)
}

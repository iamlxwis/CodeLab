package site.lxwis.codelab

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SnippetStudio(snapshot: SnippetSnapshot, darkApp: Boolean, resultFirst: Boolean, close: () -> Unit) {
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()
    val capture = snapshot.capture
    var content by rememberSaveable { mutableStateOf(if (resultFirst) SnippetContent.Result else SnippetContent.Code) }
    var theme by rememberSaveable { mutableStateOf(if (darkApp) SnippetTheme.Dark else SnippetTheme.Light) }
    var scope by rememberSaveable { mutableStateOf(when {
        capture != null && capture.selectionEnd > capture.selectionStart -> SnippetScope.Selected
        capture != null -> SnippetScope.Visible
        else -> SnippetScope.Full
    }) }
    var numbers by rememberSaveable { mutableStateOf(true) }
    var metadata by rememberSaveable { mutableStateOf(true) }
    var page by rememberSaveable { mutableIntStateOf(0) }
    var plan by remember { mutableStateOf<SnippetRenderer.Plan?>(null) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableStateOf<String?>(null) }
    var exportJob by remember { mutableStateOf<Job?>(null) }
    var pendingSave by remember { mutableStateOf<SnippetRenderer.Plan?>(null) }
    val stem = remember(snapshot.file) {
        snapshot.file.substringAfterLast('/').replace(Regex("[^A-Za-z0-9._-]"), "_").take(72)
            .ifBlank { "CodeLab" } + "-snippet"
    }
    LaunchedEffect(snapshot, content, scope, theme, numbers, metadata) {
        plan = null
        preview = null
        page = 0
        try {
            plan = withContext(Dispatchers.Default) {
                SnippetRenderer.plan(snapshot, content, scope, theme, numbers, metadata)
            }
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            message = "Unable to render the snippet."
        }
    }
    LaunchedEffect(plan, page) {
        preview = null
        val rendered = plan ?: return@LaunchedEffect
        if (page in rendered.pages.indices) {
            preview = withContext(Dispatchers.Default) { SnippetRenderer.render(rendered, page) }
        }
    }
    fun export(image: SnippetRenderer.Plan, destination: Uri? = null, tree: Boolean = false) {
        if (exportJob?.isActive == true) return
        message = null
        progress = "Preparing PNG"
        exportJob = coroutine.launch {
            try {
                val shared = withContext(Dispatchers.IO) {
                    exportSnippetPages(context, image, stem, destination, tree) { index ->
                        withContext(Dispatchers.Main) { progress = "PNG ${index + 1} of ${image.pages.size}" }
                    }
                }
                if (shared.isNotEmpty()) context.startActivity(Intent.createChooser(snippetShareIntent(shared), "Share snippet"))
                else message = if (image.pages.size == 1) "PNG saved" else "${image.pages.size} PNGs saved"
            } catch (cancel: CancellationException) {
                message = "Export cancelled"
                throw cancel
            } catch (_: Exception) {
                message = "Unable to export the snippet."
            } finally {
                progress = null
                exportJob = null
            }
        }
    }
    val saveFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
        val image = pendingSave
        pendingSave = null
        if (uri != null && image != null) export(image, destination = uri)
    }
    val saveFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        val image = pendingSave
        pendingSave = null
        if (uri != null && image != null) export(image, destination = uri, tree = true)
    }
    ModalBottomSheet(onDismissRequest = close, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.95f).padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Snippet Studio", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = close) { Text("Close") }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SnippetChoices(SnippetContent.entries, content, SnippetContent::label) { content = it }
                SnippetChoices(SnippetTheme.entries, theme, SnippetTheme::name) { theme = it }
                if (content != SnippetContent.Result) {
                    val scopes = SnippetScope.entries.filter { value ->
                        when (value) {
                            SnippetScope.Selected -> capture != null && capture.selectionEnd > capture.selectionStart
                            SnippetScope.Visible -> capture != null
                            SnippetScope.Full -> true
                        }
                    }
                    if (scopes.size > 1) SnippetChoices(scopes, scope, SnippetScope::label) { scope = it }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (content != SnippetContent.Result) FilterChip(numbers, { numbers = !numbers }, { Text("Line numbers") })
                    FilterChip(metadata, { metadata = !metadata }, { Text("Metadata") })
                }
                if (content != SnippetContent.Code && snapshot.staleRun) {
                    Text("The result is from ${snapshot.run?.file} at a different revision or location.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val pages = plan?.pages?.size ?: 0
                if (pages > 1) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { page-- }, enabled = page > 0) { Text("Previous") }
                    Text("Page ${page + 1} of $pages", style = MaterialTheme.typography.labelMedium)
                    TextButton(onClick = { page++ }, enabled = page + 1 < pages) { Text("Next") }
                }
                preview?.let { image ->
                    Image(image.asImageBitmap(), contentDescription = "Snippet PNG preview, page ${page + 1}",
                        modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth)
                } ?: if (message == null) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                } else Unit
            }
            if (progress != null || message != null) Text(progress ?: message.orEmpty(),
                Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall)
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (progress != null) OutlinedButton({ exportJob?.cancel() }, Modifier.weight(1f)) { Text("Cancel export") }
                else {
                    OutlinedButton({
                        val image = plan ?: return@OutlinedButton
                        pendingSave = image
                        if (image.pages.size == 1) saveFile.launch("$stem.png") else saveFolder.launch(null)
                    }, Modifier.weight(1f), enabled = plan != null) { Text("Save PNG") }
                    Button({ plan?.let { export(it) } }, Modifier.weight(1f), enabled = plan != null) { Text("Share PNG") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SnippetChoices(values: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        values.forEachIndexed { index, value ->
            SegmentedButton(selected == value, { onSelect(value) },
                SegmentedButtonDefaults.itemShape(index, values.size), icon = {}) {
                Text(label(value))
            }
        }
    }
}

private suspend fun exportSnippetPages(
    context: Context, image: SnippetRenderer.Plan, stem: String, destination: Uri?, tree: Boolean,
    onPage: suspend (Int) -> Unit
): ArrayList<Uri> {
    val resolver = context.contentResolver
    val shared = arrayListOf<Uri>()
    val created = mutableListOf<DocumentFile>()
    val folder = if (tree) checkNotNull(DocumentFile.fromTreeUri(context, checkNotNull(destination))) else null
    val cache = if (destination == null) {
        val root = File(context.cacheDir, "snippet-exports").apply { mkdirs() }
        root.listFiles()?.filter { it.lastModified() < System.currentTimeMillis() - 86_400_000 }
            ?.forEach { it.deleteRecursively() }
        java.nio.file.Files.createTempDirectory(root.toPath(), "pages-").toFile()
    } else null
    try {
        for (page in image.pages.indices) {
            currentCoroutineContext().ensureActive()
            onPage(page)
            val name = if (image.pages.size == 1) "$stem.png"
                else "$stem-${(page + 1).toString().padStart(image.pages.size.toString().length, '0')}.png"
            val bitmap = SnippetRenderer.render(image, page)
            try {
                fun write(stream: OutputStream) {
                    stream.use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
                }
                if (cache != null) {
                    val file = File(cache, name)
                    write(file.outputStream())
                    shared += FileProvider.getUriForFile(context, "${context.packageName}.runtimefiles", file)
                } else {
                    val uri = if (folder != null) {
                        checkNotNull(folder.createFile("image/png", name)).also { created += it }.uri
                    } else checkNotNull(destination)
                    write(checkNotNull(resolver.openOutputStream(uri, "wt")))
                }
            } finally {
                bitmap.recycle()
            }
        }
        return shared
    } catch (failure: Exception) {
        cache?.deleteRecursively()
        created.forEach { runCatching { it.delete() } }
        throw failure
    }
}

private fun snippetShareIntent(uris: ArrayList<Uri>): Intent {
    val clip = ClipData.newRawUri("CodeLab snippet", uris.first())
    uris.drop(1).forEach { clip.addItem(ClipData.Item(it)) }
    return Intent(if (uris.size == 1) Intent.ACTION_SEND else Intent.ACTION_SEND_MULTIPLE).apply {
        type = "image/png"
        clipData = clip
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (uris.size == 1) putExtra(Intent.EXTRA_STREAM, uris.first())
        else putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
    }
}

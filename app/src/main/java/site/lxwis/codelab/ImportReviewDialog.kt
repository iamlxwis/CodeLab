package site.lxwis.codelab

import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class ImportMetadata(val name: String, val bytes: Long?)

@Composable
internal fun ImportReviewDialog(uri: Uri, project: Boolean, existing: List<String>, close: () -> Unit,
    folder: Boolean = false, submit: (String) -> Unit) {
    val context = LocalContext.current
    val metadata by produceState<ImportMetadata?>(null, uri, folder) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                if (folder) ImportMetadata(DocumentFile.fromTreeUri(context, uri)?.name.orEmpty(), null)
                else context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                    null, null, null)?.use { cursor ->
                    if (!cursor.moveToFirst()) null else ImportMetadata(
                        cursor.getString(0).orEmpty(), if (cursor.isNull(1)) null else cursor.getLong(1))
                }
            }.getOrNull() ?: ImportMetadata("", null)
        }
    }
    var name by rememberSaveable(uri.toString()) { mutableStateOf("") }
    var touched by rememberSaveable(uri.toString()) { mutableStateOf(false) }
    var previewRequested by rememberSaveable(uri.toString()) { mutableStateOf(false) }
    val preview by produceState<DocumentPreview?>(null, uri, previewRequested) {
        if (previewRequested && !folder) {
            value = withContext(Dispatchers.IO) {
                runCatching {
                    val stream = checkNotNull(context.contentResolver.openInputStream(uri))
                    if (project) DocumentPreviews.archive(stream) else DocumentPreviews.text(stream)
                }.getOrElse { DocumentPreview(message = "Could not preview the selected document.") }
            }
        } else value = null
    }
    LaunchedEffect(metadata) {
        if (!touched && name.isBlank()) name = metadata?.name.orEmpty().let {
            if (project && !folder) it.substringBeforeLast('.', it) else it
        }.take(100)
    }
    val clean = name.trim()
    val conflict = clean in existing
    val validName = if (project) ExternalFolderImporter.validSegment(clean)
        else clean.length <= 512 && clean.split('/').let { it.size <= 16 && it.all(ExternalFolderImporter::validSegment) }
    val valid = validName && !conflict
    val title = when { folder -> "Review folder import"; project -> "Review project import"; else -> "Review file import" }
    AlertDialog(onDismissRequest = close, title = { Text(title) },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (folder || project) Icon(if (folder) Icons.Default.FolderOpen else Icons.Default.FolderZip, null, Modifier.size(28.dp))
                        else FileTypeIcon(metadata?.name.orEmpty(), Modifier.size(28.dp))
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(metadata?.name?.takeIf { it.isNotBlank() } ?: if (folder) "Selected folder" else "Selected document",
                                style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(if (folder) "Device folder" else metadata?.bytes?.takeIf { it >= 0 }?.let {
                                android.text.format.Formatter.formatShortFileSize(context, it)
                            } ?: "Size not provided", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                if (!folder && (project || !ProjectStore.isBinaryAsset(metadata?.name.orEmpty()))) {
                    TextButton(onClick = { previewRequested = !previewRequested }) {
                        Text(if (previewRequested) "Hide contents" else "Preview contents")
                    }
                    if (previewRequested) {
                        val result = preview
                        if (result == null) LinearProgressIndicator(Modifier.fillMaxWidth())
                        else Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.medium) {
                            Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                result.entries.forEach { entry ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        FileTypeIcon(entry, Modifier.size(17.dp))
                                        Text(entry, Modifier.padding(start = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                result.text?.let { Text(it.ifEmpty { "Empty file" }, fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodySmall) }
                                result.message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                if (result.limited) Text("Partial preview", style = MaterialTheme.typography.labelSmall)
                                Text("Import checks the complete document; preview does not validate it.",
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                OutlinedTextField(name, { name = it.take(if (project) 100 else 512); touched = true }, Modifier.fillMaxWidth(),
                    singleLine = true, label = { Text(if (project) "Project name" else "File path") },
                    isError = conflict || (touched && !validName),
                    supportingText = {
                        if (conflict) Text("That name already exists. Choose another.")
                        else if (touched && !validName) Text("Enter a valid " + if (project) "project name." else "file path.")
                    })
                Text("The selected item is copied into CodeLab. Original files remain unchanged.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }, confirmButton = { TextButton(enabled = valid, onClick = { submit(clean) }) { Text("Import") } },
        dismissButton = { TextButton(onClick = close) { Text("Cancel") } })
}

@Composable
internal fun ExportReviewDialog(name: String, paths: List<String>, project: Boolean, close: () -> Unit, proceed: () -> Unit) {
    AlertDialog(onDismissRequest = close, title = { Text(if (project) "Export project" else "Export file") },
        text = {
            Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.large) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (project) Icon(Icons.Default.FolderZip, null, Modifier.size(28.dp))
                        else FileTypeIcon(name, Modifier.size(28.dp))
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(name, style = MaterialTheme.typography.titleSmall)
                            Text(if (project) "${paths.size} files · ZIP archive" else "Copy to device",
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                if (project) {
                    paths.take(6).forEach { path ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FileTypeIcon(path, Modifier.size(18.dp))
                            Text(path, Modifier.padding(start = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (paths.size > 6) Text("and ${paths.size - 6} more files", style = MaterialTheme.typography.labelSmall)
                }
                Text("Current edits are saved before export. Choose where to save your copy.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }, confirmButton = { TextButton(onClick = proceed) { Text("Choose location") } },
        dismissButton = { TextButton(onClick = close) { Text("Cancel") } })
}

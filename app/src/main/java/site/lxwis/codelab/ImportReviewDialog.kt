package site.lxwis.codelab

import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class ImportMetadata(val name: String, val bytes: Long?)

@Composable
internal fun ImportReviewDialog(uri: Uri, project: Boolean, existing: List<String>, close: () -> Unit,
    submit: (String) -> Unit) {
    val context = LocalContext.current
    val metadata by produceState<ImportMetadata?>(null, uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                    null, null, null)?.use { cursor ->
                    if (!cursor.moveToFirst()) null else ImportMetadata(
                        cursor.getString(0).orEmpty(), if (cursor.isNull(1)) null else cursor.getLong(1))
                }
            }.getOrNull() ?: ImportMetadata("", null)
        }
    }
    var name by rememberSaveable(uri.toString()) { mutableStateOf("") }
    var touched by rememberSaveable(uri.toString()) { mutableStateOf(false) }
    LaunchedEffect(metadata) {
        if (!touched && name.isBlank()) name = metadata?.name.orEmpty().let {
            if (project) it.substringBeforeLast('.', it) else it
        }.take(100)
    }
    val conflict = name.trim() in existing
    val valid = name.isNotBlank() && !conflict
    AlertDialog(onDismissRequest = close, title = { Text(if (project) "Review project import" else "Review file import") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (project) Icons.Default.FolderZip else Icons.Default.Description, null, Modifier.size(28.dp))
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(metadata?.name?.takeIf { it.isNotBlank() } ?: "Selected document", style = MaterialTheme.typography.titleSmall)
                            Text(metadata?.bytes?.takeIf { it >= 0 }?.let {
                                android.text.format.Formatter.formatShortFileSize(context, it)
                            } ?: "Size not provided", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                OutlinedTextField(name, { name = it.take(100); touched = true }, Modifier.fillMaxWidth(),
                    singleLine = true, label = { Text(if (project) "Project name" else "File path") }, isError = conflict,
                    supportingText = { if (conflict) Text("That name already exists. Choose another.") })
                Text("The selected item is copied into CodeLab. Original files remain unchanged.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }, confirmButton = { TextButton(enabled = valid, onClick = { submit(name.trim()) }) { Text("Import") } },
        dismissButton = { TextButton(onClick = close) { Text("Cancel") } })
}

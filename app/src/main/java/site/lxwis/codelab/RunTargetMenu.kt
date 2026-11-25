package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun RunFilePicker(
    files: List<String>,
    cloudExtensions: Set<String> = emptySet(),
    onRun: (String) -> Unit,
    installedModules: Set<String> = emptySet(),
    onDismiss: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var runnableOnly by rememberSaveable { mutableStateOf(true) }
    val matches = files.map { it to RunCapability.forPath(it, cloudExtensions, installedModules) }
        .filter { (path, target) ->
            path.contains(query, ignoreCase = true) &&
                (!runnableOnly || target.mode != RunCapability.Mode.UNAVAILABLE)
        }.sortedWith(compareBy({ it.second.mode.ordinal }, { it.first.lowercase() }))
    CodeLabAlertDialog(onDismissRequest = onDismiss,
        title = { Text("Choose a run target") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(),
                    label = { Text("Search project files") }, singleLine = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(selected = runnableOnly,
                        onClick = { runnableOnly = !runnableOnly },
                        label = { Text("Runnable only") })
                    Spacer(Modifier.width(10.dp))
                    Text(matches.size.toString() + " files",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (matches.isEmpty()) Text(
                    "No matching runnable files. Disable the filter to see all files.",
                    style = MaterialTheme.typography.bodySmall)
                LazyColumn(Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(matches, key = { it.first }) { (path, target) ->
                        val available = target.mode != RunCapability.Mode.UNAVAILABLE
                        Surface(onClick = { onDismiss(); onRun(path) },
                            enabled = available,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = MaterialTheme.shapes.medium) {
                            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                Text(path, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(target.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (available) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } })
}

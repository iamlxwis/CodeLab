package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Explicit run source: clicking the primary Run action respects the entrypoint. */
@Composable
internal fun RunTargetMenu(m: WorkspaceModel, onProjectSettings: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(40.dp)
        ) { Icon(Icons.Default.ArrowDropDown, "Choose run target") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            m.active?.let { path ->
                DropdownMenuItem(text = { Text("Run active · ${path.substringAfterLast('/')}") },
                    onClick = { expanded = false; m.runTarget(path) })
            }
            m.projectMetadata.entrypoint.takeIf { it in m.files }?.let { path ->
                DropdownMenuItem(text = { Text("Run entry · ${path.substringAfterLast('/')}") },
                    onClick = { expanded = false; m.runTarget(path) })
            }
            DropdownMenuItem(text = { Text("Choose another file…") },
                onClick = { expanded = false; showPicker = true })
            DropdownMenuItem(text = { Text("Set preferred entry file…") },
                onClick = { expanded = false; onProjectSettings() })
        }
    }
    if (showPicker) RunFilePicker(m.files, m::runTarget) { showPicker = false }
}

@Composable
internal fun RunFilePicker(
    files: List<String>,
    onRun: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("Run a file") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(),
                    label = { Text("Find file") }, singleLine = true)
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    items(files.filter { it.contains(query, ignoreCase = true) }, key = { it }) { path ->
                        TextButton(onClick = { onDismiss(); onRun(path) },
                            modifier = Modifier.fillMaxWidth()) {
                            Text(path, modifier = Modifier.fillMaxWidth(),
                                maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } })
}

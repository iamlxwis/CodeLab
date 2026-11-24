package site.lxwis.codelab

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign

@Composable
internal fun CommandPaletteDialog(m: WorkspaceModel, dismiss: () -> Unit,
    execute: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val choices = rankWorkspaceCommands(query, workspaceCommandCatalog, m.recentCommandIds)
    CodeLabAlertDialog(onDismissRequest = dismiss, title = { Text("Command palette") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(query, { query = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    placeholder = { Text("Search commands") })
                if (query.isBlank() && m.recentCommandIds.isNotEmpty())
                    Text("Recent commands first",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyColumn(Modifier.heightIn(max = 340.dp)) {
                    items(choices, key = { it.id }) { command ->
                        TextButton(
                            enabled = (!command.requiresProject || m.project != null) &&
                                (!command.requiresFile || m.active != null),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { m.rememberCommand(command.id); execute(command.id) }
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(command.label, textAlign = TextAlign.Start)
                                command.shortcut?.let {
                                    Text(it, style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                Text("Ctrl+Shift+P opens this palette with a hardware keyboard",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }, confirmButton = { TextButton(onClick = dismiss) { Text("Close") } })
}

@Composable
internal fun JavaScriptDebugDialog(m: WorkspaceModel, dismiss: () -> Unit) {
    var breakpointText by rememberSaveable { mutableStateOf("") }
    val parts = breakpointText.split(',', ' ', ';', '\n')
        .filter { it.isNotBlank() }
    val lines = parts.mapNotNull(String::toIntOrNull)
    val valid = parts.size == lines.size && lines.all { it in 1..100000 } && lines.size <= 80
    CodeLabAlertDialog(onDismissRequest = dismiss, title = { Text("JavaScript debugger") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Execution pauses at the first statement. Use Step or Continue in the console.",
                    style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(breakpointText, { breakpointText = it.take(600) },
                    label = { Text("Optional breakpoint line numbers") },
                    placeholder = { Text("4, 8, 15") },
                    modifier = Modifier.fillMaxWidth(), maxLines = 3)
                Text("Debugger: Mozilla Rhino, offline. Applies to the active .js file. " +
                    "JavaScript may continue until the timeout limit while paused.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!valid) Text("Enter up to 80 positive line numbers.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error)
            }
        }, confirmButton = {
            TextButton(enabled = valid && !m.runner.running,
                onClick = {
                    m.debugCurrent(lines)
                    dismiss()
                }) { Text("Start debugging") }
        }, dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } })
}

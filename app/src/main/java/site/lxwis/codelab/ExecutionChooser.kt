package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

internal data class ExecutionRequest(
    val project: String,
    val file: String,
    val revision: Long,
    val breakpoints: List<Int>? = null,
    val suggested: String = "Offline"
)

internal data class ExecutionOptions(
    val offline: String,
    val online: String,
    val canRunOffline: Boolean,
    val canRunOnline: Boolean,
    val installLanguage: String? = null
)

@Composable
internal fun ExecutionChooser(
    file: String,
    suggested: String,
    options: ExecutionOptions,
    cancel: () -> Unit,
    install: (String) -> Unit,
    run: (String) -> Unit
) {
    var mode by rememberSaveable(file) { mutableStateOf(suggested) }
    val enabled = if (mode == "Online") options.canRunOnline else options.canRunOffline
    CodeLabAlertDialog(onDismissRequest = cancel,
        title = { Text("Run file") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FileTypeIcon(file, Modifier.size(20.dp))
                    Text(file, style = MaterialTheme.typography.bodyMedium)
                }
                listOf("Offline", "Online").forEach { option ->
                    val online = option == "Online"
                    Surface(shape = MaterialTheme.shapes.medium,
                        color = if (mode == option) MaterialTheme.colorScheme.surfaceContainerHighest
                            else MaterialTheme.colorScheme.surfaceContainerLow) {
                        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp)
                            .selectable(selected = mode == option, role = Role.RadioButton, onClick = { mode = option })
                            .padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(if (online) Icons.Default.Cloud else Icons.Default.Computer, null)
                            Column(Modifier.weight(1f)) {
                                Text(option, style = MaterialTheme.typography.titleSmall)
                                Text(if (online) options.online else options.offline,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            RadioButton(selected = mode == option, onClick = null)
                        }
                    }
                }
                options.installLanguage?.takeIf { mode == "Offline" }?.let { language ->
                    TextButton(onClick = { install(language) }) { Text("Manage $language") }
                }
            }
        },
        confirmButton = { Button(onClick = { run(mode) }, enabled = enabled) { Text("Run") } },
        dismissButton = { TextButton(onClick = cancel) { Text("Cancel") } })
}

package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceCommandSheet(m: WorkspaceModel, dismiss: () -> Unit) {
    var input by rememberSaveable { mutableStateOf("") }
    var history by remember { mutableStateOf(emptyList<String>()) }
    val scroll = rememberScrollState()
    fun submit() {
        val line = input.trim()
        if (line.isEmpty() || m.busy) return
        history = (listOf(line) + history).take(25)
        m.executeWorkspaceCommand(line)
        input = ""
    }
    LaunchedEffect(m.terminalOutput.length) {
        scroll.animateScrollTo(scroll.maxValue)
    }
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = dismiss, sheetState = sheet,
        containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.92f)
            .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Workspace commands", style = MaterialTheme.typography.headlineSmall)
            Text("Built-in offline commands for local project files. No Linux shell or PTY.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Project · " + m.project.orEmpty(), Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium)
                TextButton(onClick = m::clearWorkspaceTerminal) { Text("Clear") }
                if (history.isNotEmpty()) TextButton(onClick = { input = history.first() }) {
                    Text("Previous")
                }
            }
            Surface(Modifier.weight(1f).fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small) {
                SelectionContainer {
                    Text(m.terminalOutput, Modifier.fillMaxSize()
                        .verticalScroll(scroll).padding(12.dp),
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(input, { input = it.take(512) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Command") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submit() }))
                Button(enabled = input.isNotBlank() && !m.busy,
                    onClick = { submit() }) { Text("Run") }
            }
            Spacer(Modifier.height(7.dp))
        }
    }
}

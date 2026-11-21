package site.lxwis.codelab

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*

@Composable
fun RunConsole(session: RunSession, modifier: Modifier = Modifier) {
    val clipboard = LocalClipboardManager.current
    val scroll = rememberScrollState()
    var input by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(scroll.maxValue) { scroll.scrollTo(scroll.maxValue) }
    LaunchedEffect(session.waiting) { if (!session.waiting) input = "" }
    Surface(modifier, color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Console", style = MaterialTheme.typography.titleSmall)
                    Text(session.status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                if (session.running) TextButton(onClick = session::stop) { Text("Stop") }
                TextButton(onClick = { clipboard.setText(AnnotatedString(session.transcript)) }, enabled = session.transcript.isNotEmpty()) { Text("Copy") }
                TextButton(onClick = session::clear, enabled = session.transcript.isNotEmpty()) { Text("Clear") }
            }
            SelectionContainer(Modifier.weight(1f).fillMaxWidth().verticalScroll(scroll)) {
                Text(session.transcript, fontFamily = FontFamily.Monospace, fontSize = 13.sp, lineHeight = 18.sp)
            }
            if (session.waiting) Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = input, onValueChange = { input = it.take(4096) }, singleLine = true,
                    modifier = Modifier.weight(1f), placeholder = { Text("Program input") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { session.submit(input) }))
                TextButton(onClick = { session.submit(input) }) { Text("Send") }
            }
        }
    }
}

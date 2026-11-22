package site.lxwis.codelab

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*

@Composable
fun RunConsole(session: RunSession, modifier: Modifier = Modifier, fileName: String? = null) {
    var tableMode by rememberSaveable(fileName) { mutableStateOf(true) }
    val sqlResult = remember(session.transcript, fileName, session.running) {
        if (!session.running && fileName?.endsWith(".sql", ignoreCase = true) == true)
            SqlResultParser.parse(session.transcript) else null
    }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var showActions by remember { mutableStateOf(false) }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) {
            val saved = runCatching {
                checkNotNull(context.contentResolver.openOutputStream(uri, "wt")).bufferedWriter().use { it.write(session.transcript) }
            }.isSuccess
            Toast.makeText(context, if (saved) "Output saved" else "Could not save output", Toast.LENGTH_SHORT).show()
        }
    }
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
                Box {
                    IconButton(onClick = { showActions = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Console actions")
                    }
                    DropdownMenu(expanded = showActions, onDismissRequest = { showActions = false }) {
                        val hasOutput = session.transcript.isNotEmpty()
                        DropdownMenuItem(text = { Text("Copy output") }, enabled = hasOutput, onClick = {
                            clipboard.setText(AnnotatedString(session.transcript))
                            showActions = false
                        })
                        DropdownMenuItem(text = { Text("Save output…") }, enabled = hasOutput, onClick = {
                            showActions = false
                            export.launch("console.txt")
                        })
                        DropdownMenuItem(text = { Text("Clear output") }, enabled = hasOutput, onClick = {
                            session.clear()
                            showActions = false
                        })
                    }
                }
            }
            if (sqlResult != null) {
                Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = tableMode, onClick = { tableMode = true },
                        label = { Text("Results") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer))
                    FilterChip(selected = !tableMode, onClick = { tableMode = false },
                        label = { Text("Output") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer))
                    Spacer(Modifier.weight(1f))
                    Text("${sqlResult.rows.size} rows", Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (sqlResult != null && tableMode) SqlDataGrid(sqlResult, Modifier.weight(1f).fillMaxWidth())
            else SelectionContainer(Modifier.weight(1f).fillMaxWidth().verticalScroll(scroll)) {
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


@Composable
private fun SqlDataGrid(table: SqlTable, modifier: Modifier = Modifier) {
    val horizontal = rememberScrollState()
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    Column(modifier.horizontalScroll(horizontal)) {
        Row {
            table.columns.forEach { column ->
                Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .65f),
                    modifier = Modifier.widthIn(min = 110.dp, max = 200.dp).width(146.dp)) {
                    Text(column, Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelMedium,
                        maxLines = 1)
                }
            }
        }
        HorizontalDivider(color = borderColor)
        LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
            itemsIndexed(table.rows) { index, row ->
                Row {
                    row.forEach { cell ->
                        Text(cell, Modifier.width(146.dp).padding(horizontal = 12.dp, vertical = 11.dp),
                            fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                            maxLines = 3, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                HorizontalDivider(color = borderColor.copy(alpha = .55f))
            }
        }
    }
}

package site.lxwis.codelab

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.History
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
fun RunConsole(session: RunSession, modifier: Modifier = Modifier, fileName: String? = null,
    knownFiles: List<String> = emptyList(),
    onOpenError: (String, Int) -> Unit = { _, _ -> }) {
    var tableMode by rememberSaveable(fileName) { mutableStateOf(true) }
    val sqlResult = remember(session.transcript, fileName, session.running) {
        if (!session.running && fileName?.endsWith(".sql", ignoreCase = true) == true)
            SqlResultParser.parse(session.transcript) else null
    }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var showActions by remember { mutableStateOf(false) }
    var showExecutionSettings by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var selectedRun by remember { mutableStateOf<RunSession.RunRecord?>(null) }
    var showFilter by rememberSaveable { mutableStateOf(false) }
    var filterOutput by rememberSaveable { mutableStateOf("") }
    val consolePreferences = remember { context.getSharedPreferences("console-settings", android.content.Context.MODE_PRIVATE) }
    var consoleFontSize by remember {
        mutableIntStateOf(consolePreferences.getInt("font-size", 13))
    }
    val errorLinks = remember(session.transcript, fileName, knownFiles) {
        parseErrorLocations(session.transcript, fileName, knownFiles)
    }
    val displayedOutput = remember(session.transcript, filterOutput, showFilter) {
        if (showFilter && filterOutput.isNotBlank())
            session.transcript.lineSequence().filter {
                it.contains(filterOutput, ignoreCase = true)
            }.joinToString("\n")
        else session.transcript
    }
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
    var sentInputs by remember { mutableStateOf(emptyList<String>()) }
    fun submitInput() {
        if (!session.waiting) return
        if (input.isNotBlank()) sentInputs = (listOf(input) + sentInputs).take(20)
        session.submit(input)
    }
    LaunchedEffect(scroll.maxValue) { scroll.scrollTo(scroll.maxValue) }
    LaunchedEffect(session.waiting) { if (!session.waiting) input = "" }
    if (showExecutionSettings) AlertDialog(
        onDismissRequest = { showExecutionSettings = false },
        title = { Text("Local execution limits") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Timeout", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf(10, 30, 60).forEach { seconds ->
                        FilterChip(selected = session.timeLimitSeconds == seconds,
                            onClick = {
                                session.chooseExecutionLimits(seconds, session.outputLimitCharacters)
                            }, label = { Text("${seconds}s") })
                    }
                }
                Text("Maximum output", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf(16000, 32000, 64000).forEach { characters ->
                        FilterChip(selected = session.outputLimitCharacters == characters,
                            onClick = {
                                session.chooseExecutionLimits(session.timeLimitSeconds, characters)
                            }, label = { Text("${characters / 1000}K") })
                    }
                }
                Text("Limits apply to the next offline run. A long-running program is stopped automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }, confirmButton = {
            TextButton(onClick = { showExecutionSettings = false }) { Text("Done") }
        })
    if (showHistory) AlertDialog(
        onDismissRequest = { showHistory = false },
        title = { Text("Execution history") },
        text = {
            Column {
                if (session.history.isEmpty()) Text("No completed runs yet.")
                LazyColumn(Modifier.heightIn(max = 280.dp)) {
                    items(session.history.size) { index ->
                        val item = session.history[index]
                        Column(Modifier.fillMaxWidth().clickable { selectedRun = item }
                            .padding(vertical = 10.dp)) {
                            Text(item.file, fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodyMedium)
                            Text(item.result, style = MaterialTheme.typography.bodySmall)
                            Text(java.text.DateFormat.getDateTimeInstance()
                                .format(java.util.Date(item.time)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
                selectedRun?.let { record ->
                    Text("Saved output · last 3,000 characters",
                        style = MaterialTheme.typography.labelSmall)
                    SelectionContainer {
                        Text(record.excerpt, fontFamily = FontFamily.Monospace,
                            maxLines = 12, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }, confirmButton = {
            TextButton(onClick = { selectedRun = null; showHistory = false }) { Text("Close") }
        })
    Surface(modifier, color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Console", style = MaterialTheme.typography.titleSmall)
                    Text(session.status, style = MaterialTheme.typography.labelSmall,
                        color = if (session.running) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (session.paused) {
                    TextButton(onClick = { session.debugAction(false) }) { Text("Continue") }
                    TextButton(onClick = { session.debugAction(true) }) { Text("Step") }
                }
                if (session.running) TextButton(onClick = session::stop) { Text("Stop") }
                else if (session.canRerun) TextButton(onClick = session::rerun) { Text("Rerun") }
                FilterChip(selected = showFilter, onClick = { showFilter = !showFilter },
                    label = { Text("Filter") })
                Box {
                    IconButton(onClick = { showActions = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Console actions")
                    }
                    DropdownMenu(expanded = showActions, onDismissRequest = { showActions = false }) {
                        val hasOutput = session.transcript.isNotEmpty()
                        DropdownMenuItem(text = { Text("Execution history…") },
                            onClick = { showActions = false; showHistory = true })
                        DropdownMenuItem(text = { Text("Execution limits…") },
                            enabled = !session.running,
                            onClick = { showActions = false; showExecutionSettings = true })
                        HorizontalDivider()
                        DropdownMenuItem(text = { Text("Copy output") }, enabled = hasOutput, onClick = {
                            clipboard.setText(AnnotatedString(session.transcript))
                            showActions = false
                        })
                        DropdownMenuItem(text = { Text("Save output…") }, enabled = hasOutput, onClick = {
                            showActions = false
                            export.launch("console.txt")
                        })
                        DropdownMenuItem(text = { Text("Smaller text") },
                            enabled = consoleFontSize > 10, onClick = {
                                consoleFontSize = (consoleFontSize - 1).coerceAtLeast(10)
                                consolePreferences.edit().putInt("font-size", consoleFontSize).apply()
                                showActions = false
                            })
                        DropdownMenuItem(text = { Text("Larger text") },
                            enabled = consoleFontSize < 22, onClick = {
                                consoleFontSize = (consoleFontSize + 1).coerceAtMost(22)
                                consolePreferences.edit().putInt("font-size", consoleFontSize).apply()
                                showActions = false
                            })
                        HorizontalDivider()
                        DropdownMenuItem(text = { Text("Clear output") }, enabled = hasOutput, onClick = {
                            session.clear()
                            showActions = false
                        })
                    }
                }
            }
            if (showFilter) {
                OutlinedTextField(filterOutput, { filterOutput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Filter console lines") },
                    singleLine = true)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            if (session.paused) {
                Column(Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("JavaScript variables · line " + session.pausedLine,
                        style = MaterialTheme.typography.titleSmall)
                    SelectionContainer {
                        Text(session.debugVariables.ifBlank { "No local primitive variables available." },
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.heightIn(max = 140.dp).verticalScroll(rememberScrollState()))
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
            RunErrorLinks(errorLinks, onOpenError)
            if (sqlResult != null && tableMode) SqlDataGrid(sqlResult, Modifier.weight(1f).fillMaxWidth())
            else SelectionContainer(Modifier.weight(1f).fillMaxWidth().verticalScroll(scroll)) {
                val textToShow = displayedOutput.ifEmpty {
                    if (showFilter && filterOutput.isNotEmpty()) "No matching lines" else ""
                }
                val defaultInk = MaterialTheme.colorScheme.onSurface
                val formatted = remember(textToShow, defaultInk) {
                    ConsoleAnsi.format(textToShow, defaultInk)
                }
                Text(formatted, fontFamily = FontFamily.Monospace, fontSize = consoleFontSize.sp,
                    lineHeight = (consoleFontSize + 5).sp)
            }
            if (session.waiting) Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = input, onValueChange = { input = it.take(4096) }, singleLine = true,
                    modifier = Modifier.weight(1f), placeholder = { Text("Program input") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submitInput() }))
                if (sentInputs.isNotEmpty()) IconButton(onClick = { input = sentInputs.first() }) {
                    Icon(Icons.Default.History, "Previous standard input")
                }
                TextButton(onClick = { submitInput() }) { Text("Send") }
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

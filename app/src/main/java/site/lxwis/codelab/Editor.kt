package site.lxwis.codelab

import android.content.Context
import android.text.Editable
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun CodeEditor(
    fileKey: String,
    filename: String,
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    jumpLine: Int? = null,
    onJumpHandled: () -> Unit = {},
    onSave: () -> Unit = {},
    onRun: () -> Unit = {},
    onShare: () -> Unit = {},
    onPreviousTab: () -> Unit = {},
    onNextTab: () -> Unit = {},
    onOpenCommands: () -> Unit = {},
    onSelectionChange: (String, Int) -> Unit = { _, _ -> }
) = key(fileKey) {
    val context = LocalContext.current
    val hardwareKeyboard = LocalConfiguration.current.keyboard != Configuration.KEYBOARD_NOKEYS
    val preferences = remember { context.getSharedPreferences("editor", Context.MODE_PRIVATE) }
    var fontSize by remember { mutableStateOf(preferences.getInt("font-size", 15)) }
    var wordWrap by remember { mutableStateOf(preferences.getBoolean("word-wrap", true)) }
    var symbols by remember { mutableStateOf(preferences.getBoolean("symbol-toolbar", true)) }
    var shortcutLayout by remember { mutableStateOf(preferences.getString("shortcut-layout", "Standard") ?: "Standard") }
    var shortcutSettings by remember { mutableStateOf(false) }
    var customizeShortcuts by remember { mutableStateOf(false) }
    val availableShortcuts = listOf("Tab", "Esc", "←", "→", "↑", "↓", "{", "}",
        "(", ")", "[", "]", "=", ";", ":", "'", "\"", "/", "<", ">")
    var customShortcuts by remember {
        mutableStateOf(preferences.getString("custom-shortcuts", "Tab { } ( ) ;")
            ?.split(" ")?.filter { it in availableShortcuts }?.distinct()
            ?: listOf("Tab", "{", "}", "(", ")", ";"))
    }
    var draftShortcuts by remember { mutableStateOf(customShortcuts) }
    var hideToolbarOnHardware by remember {
        mutableStateOf(preferences.getBoolean("auto-hide-hardware-toolbar", false))
    }
    var syntaxPalette by remember { mutableStateOf(preferences.getString("syntax-palette", "Classic") ?: "Classic") }
    var indentWidth by remember { mutableStateOf(preferences.getInt("indent-width", 4)) }
    var useTabs by remember { mutableStateOf(preferences.getBoolean("indent-tabs", false)) }
    var completionMenu by remember { mutableStateOf(false) }
    var minimap by remember { mutableStateOf(preferences.getBoolean("minimap", false)) }
    var diagnosticMarkers by remember {
        mutableStateOf(preferences.getBoolean("diagnostic-markers", true))
    }
    var issuesMenu by remember { mutableStateOf(false) }
    var readOnly by remember { mutableStateOf(false) }
    var view by remember { mutableStateOf<SourceView?>(null) }
    val cursorStatus = remember { EditorCursorStatus() }
    var find by remember { mutableStateOf(false) }
    var advancedTools by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var replacement by remember { mutableStateOf("") }
    var replaceMode by remember { mutableStateOf(false) }
    var matchCase by remember { mutableStateOf(preferences.getBoolean("search-match-case", false)) }
    var regexMode by remember { mutableStateOf(preferences.getBoolean("search-regex", false)) }
    var searchError by remember { mutableStateOf("") }
    fun searchRegex(): Regex? = try {
        Regex(if (regexMode) search else Regex.escape(search),
            if (matchCase) emptySet() else setOf(RegexOption.IGNORE_CASE))
    } catch (e: IllegalArgumentException) {
        searchError = "Invalid regular expression"
        null
    }
    var lineDialog by remember { mutableStateOf(false) }
    var lineInput by remember { mutableStateOf("") }
    var lineError by remember { mutableStateOf(false) }
    var settings by remember { mutableStateOf(false) }
    var toolsMenu by remember { mutableStateOf(false) }
    var snippetsMenu by remember { mutableStateOf(false) }
    var snippetQuery by remember { mutableStateOf("") }
    var snippetSaveDialog by remember { mutableStateOf(false) }
    var snippetTitle by remember { mutableStateOf("") }
    var snippetSelection by remember { mutableStateOf("") }
    var snippetError by remember { mutableStateOf("") }
    var savedSnippets by remember {
        mutableStateOf(SavedSnippetCodec.decode(preferences.getString("saved-snippets-v1", "")))
    }
    fun persistSnippets(items: List<StoredSnippet>) {
        savedSnippets = items
        preferences.edit().putString("saved-snippets-v1", SavedSnippetCodec.encode(items)).apply()
    }
    var outlineMenu by remember { mutableStateOf(false) }
    var referenceMenu by remember { mutableStateOf(false) }
    var renameDialog by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf("") }
    var renameOriginal by remember { mutableStateOf("") }
    var renameSnapshot by remember { mutableStateOf("") }
    var renameError by remember { mutableStateOf("") }
    var foldPreview by remember { mutableStateOf(false) }
    var foldSnapshot by remember { mutableStateOf("") }
    var conflictDialog by remember { mutableStateOf(false) }
    var conflictError by remember { mutableStateOf("") }
    var conflictSource by remember { mutableStateOf("") }
    var conflictHunks by remember { mutableStateOf<List<MergeConflictMarkers.Hunk>>(emptyList()) }
    var conflictChoices by remember { mutableStateOf<List<MergeConflictMarkers.Choice>>(emptyList()) }
    var bookmarksMenu by remember { mutableStateOf(false) }
    var showLineNumbers by remember { mutableStateOf(preferences.getBoolean("line-numbers", true)) }
    var showActiveLine by remember { mutableStateOf(preferences.getBoolean("active-line", true)) }
    var result by remember { mutableStateOf("") }
    if (snippetSaveDialog) AlertDialog(
        onDismissRequest = { snippetSaveDialog = false },
        title = { Text("Save selected code") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(snippetTitle, { snippetTitle = it.take(60); snippetError = "" },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Snippet name") },
                    singleLine = true)
                Text("${snippetSelection.length} characters selected · saved on this device",
                    style = MaterialTheme.typography.bodySmall)
                if (snippetError.isNotBlank()) Text(snippetError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(enabled = snippetTitle.isNotBlank() && snippetSelection.isNotEmpty(),
                onClick = {
                    val next = SavedSnippetCodec.add(savedSnippets,
                        StoredSnippet(snippetTitle.trim(), snippetSelection))
                    if (next == null) snippetError =
                        "Use a unique name; up to 24 snippets of 4,096 characters are supported."
                    else {
                        persistSnippets(next)
                        snippetSaveDialog = false
                        snippetTitle = ""
                    }
                }) { Text("Save snippet") }
        },
        dismissButton = {
            TextButton(onClick = { snippetSaveDialog = false }) { Text("Cancel") }
        }
    )
    if (customizeShortcuts) AlertDialog(onDismissRequest = { customizeShortcuts = false },
        title = { Text("Customize mobile shortcuts") },
        text = {
            Column(Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState())) {
                availableShortcuts.forEach { symbol ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = symbol in draftShortcuts, onCheckedChange = { enabled ->
                            draftShortcuts = if (enabled) (draftShortcuts + symbol).distinct()
                                else draftShortcuts - symbol
                        })
                        Text(symbol, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = draftShortcuts.isNotEmpty(), onClick = {
                customShortcuts = availableShortcuts.filter { it in draftShortcuts }
                preferences.edit().putString("custom-shortcuts", customShortcuts.joinToString(" ")).apply()
                shortcutLayout = "Custom"
                preferences.edit().putString("shortcut-layout", "Custom").apply()
                symbols = true
                preferences.edit().putBoolean("symbol-toolbar", true).apply()
                customizeShortcuts = false
            }) { Text("Save shortcuts") }
        },
        dismissButton = { TextButton(onClick = { customizeShortcuts = false }) { Text("Cancel") } })
    val changes by rememberUpdatedState(onTextChange)
    if (renameDialog) AlertDialog(onDismissRequest = { renameDialog = false },
        title = { Text("Rename identifier in this file") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Textual rename only; scopes, imports and external files are not analyzed.",
                    style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(renameValue, { renameValue = it; renameError = "" },
                    label = { Text("New identifier") }, singleLine = true)
                val editorSource = view?.text?.toString().orEmpty()
                if (renameOriginal.isNotEmpty()) {
                    val count = CodeAnalysis.renameCount(renameSnapshot, renameOriginal,
                        filename.substringAfterLast('.', ""))
                    Text("$count code occurrence(s) in this file will be updated. Strings and comments are excluded.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    CodeAnalysis.renamePreview(renameSnapshot, renameOriginal,
                        filename.substringAfterLast('.', "")).forEach { reference ->
                        Text("Line ${reference.line} · ${reference.preview}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1)
                    }
                    if (count > 8) Text("And ${count - 8} more occurrences…",
                        style = MaterialTheme.typography.labelSmall)
                }
                if (renameError.isNotBlank())
                    Text(renameError, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = {
            val editor = view
            if (editor != null) {
                if (editor.text.toString() != renameSnapshot) {
                    renameError = "File changed since preview. Close and reopen Rename."
                } else if (renameOriginal.isEmpty()) {
                    renameError = "No identifier selected."
                } else try {
                    editor.replaceFromUser(CodeAnalysis.lexicalRename(
                        renameSnapshot, renameOriginal, renameValue,
                        filename.substringAfterLast('.', "")))
                    renameDialog = false
                } catch (ex: IllegalArgumentException) {
                    renameError = ex.message.orEmpty()
                }
            }
        }) { Text("Rename") } },
        dismissButton = { TextButton(onClick = { renameDialog = false }) { Text("Cancel") } })

    if (foldPreview) CodeFoldPreview(foldSnapshot,
        language = filename.substringAfterLast('.', ""),
        onJump = { line -> foldPreview = false; view?.goToLogicalLine(line) },
        onClose = { foldPreview = false })
    if (conflictDialog) AlertDialog(
        onDismissRequest = { conflictDialog = false },
        title = { Text("Resolve conflict markers") },
        text = {
            if (conflictError.isNotBlank()) Text(conflictError)
            else if (conflictHunks.isEmpty()) Text("No two-way Git conflict markers in this file.")
            else Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Choose the content to keep for each conflict. The file is changed only when you apply.",
                    style = MaterialTheme.typography.bodySmall)
                conflictHunks.forEachIndexed { index, hunk ->
                    HorizontalDivider()
                    Text("Conflict " + (index + 1) + " · line " + hunk.line,
                        style = MaterialTheme.typography.titleSmall)
                    Text(hunk.oursLabel + ": " + hunk.ours.trim().take(160),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace)
                    Text(hunk.theirsLabel + ": " + hunk.theirs.trim().take(160),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MergeConflictMarkers.Choice.entries.forEach { choice ->
                            FilterChip(selected = conflictChoices.getOrNull(index) == choice,
                                onClick = {
                                    conflictChoices = conflictChoices.toMutableList().also {
                                        it[index] = choice
                                    }
                                }, label = { Text(choice.name.lowercase().replaceFirstChar {
                                    it.uppercase()
                                }) })
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (conflictError.isBlank() && conflictHunks.isNotEmpty()) {
                TextButton(enabled = !readOnly, onClick = {
                    val editor = view
                    if (editor?.text?.toString() != conflictSource) {
                        conflictError = "File changed while the dialog was open. Reopen the resolver."
                    } else {
                        editor.replaceFromUser(MergeConflictMarkers.resolve(
                            conflictSource, conflictChoices))
                        conflictDialog = false
                    }
                }) { Text("Apply resolution") }
            }
        },
        dismissButton = {
            TextButton(onClick = { conflictDialog = false }) {
                Text(if (conflictError.isNotBlank() || conflictHunks.isEmpty()) "Close" else "Cancel")
            }
        })
    LaunchedEffect(jumpLine, view) {
        if (jumpLine != null && view != null) {
            view?.goToLogicalLine(jumpLine)
            onJumpHandled()
        }
    }
    val palette = MaterialTheme.colorScheme
    val studioStyle = LocalStudioAppearance.current
    val foreground = palette.onSurface.toArgb()
    val background = palette.surface.toArgb()
    val secondary = palette.onSurfaceVariant.toArgb()
    val accent = palette.primary.toArgb()
    val dark = android.graphics.Color.luminance(background) < .35f
    fun matchCount(): Int {
        if (search.isEmpty()) return 0
        val pattern = try {
            Regex(if (regexMode) search else Regex.escape(search),
                if (matchCase) emptySet() else setOf(RegexOption.IGNORE_CASE))
        } catch (_: IllegalArgumentException) { return 0 }
        return pattern.findAll(view?.text?.toString() ?: text).count()
    }
    fun previousMatch() {
        val editor = view ?: return
        if (search.isEmpty()) return
        val pattern = searchRegex() ?: return
        val source = editor.text.toString()
        val before = editor.selectionStart.coerceAtLeast(0)
        val match = pattern.findAll(source).lastOrNull { it.range.first < before }
            ?: pattern.findAll(source).lastOrNull()
        if (match != null) {
            editor.requestFocus()
            editor.setSelection(match.range.first, match.range.last + 1)
            result = ""
            searchError = ""
        } else result = "No matches"
    }
    fun selectMatch() {
        val editor = view ?: return
        if (search.isEmpty()) return
        val source = editor.text.toString()
        val pattern = searchRegex() ?: return
        val start = editor.selectionEnd.coerceAtLeast(0)
        val match = pattern.find(source, start) ?: pattern.find(source)
        if (match != null) {
            editor.requestFocus()
            editor.setSelection(match.range.first, match.range.last + 1)
            result = ""
            searchError = ""
        } else result = "No matches"
    }
    if (lineDialog) AlertDialog(
        onDismissRequest = { lineDialog = false },
        title = { Text("Go to line") },
        text = {
            OutlinedTextField(lineInput, { lineInput = it; lineError = false },
                singleLine = true, isError = lineError, label = { Text("Line number") },
                supportingText = { if (lineError) Text("Enter a line in the current file") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        },
        confirmButton = {
            TextButton(onClick = {
                val line = lineInput.toIntOrNull()
                val editor = view
                val layout = editor?.layout
                if (editor != null && layout != null && line != null && line in 1..layout.lineCount) {
                    editor.requestFocus()
                    editor.setSelection(layout.getLineStart(line - 1))
                    lineDialog = false
                } else lineError = true
            }) { Text("Go") }
        },
        dismissButton = { TextButton(onClick = { lineDialog = false }) { Text("Cancel") } }
    )
    // Preserve a usable source viewport when the keyboard or landscape reduces height.
    // Essential editor tools remain in the first row; optional shortcuts collapse.
    BoxWithConstraints(modifier) {
    val compactEditor = maxHeight < 280.dp
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().height(48.dp).horizontalScroll(rememberScrollState())
                .padding(horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            EditorIcon(Icons.Default.Undo, "Undo") {
                view?.onTextContextMenuItem(android.R.id.undo)
            }
            EditorIcon(Icons.Default.Redo, "Redo") {
                view?.onTextContextMenuItem(android.R.id.redo)
            }
            EditorIcon(Icons.Default.FormatAlignLeft, "Format code") {
                if (!readOnly) view?.let { activeEditor ->
                    val formatted = CodeFormatter.format(filename, activeEditor.text.toString())
                    if (formatted == null) result = "Format supports JS, JSON, HTML, CSS and SQL."
                    else if (formatted != activeEditor.text.toString()) {
                        val caret = activeEditor.selectionStart.coerceIn(0, formatted.length)
                        activeEditor.text.replace(0, activeEditor.length(), formatted)
                        activeEditor.setSelection(caret)
                        result = "Formatted"
                    }
                }
            }
            EditorIcon(Icons.Default.Share, "Share code as an image") { onShare() }
            EditorIcon(Icons.Default.Search, "Find and replace", selected = find) { find = !find }
            EditorIcon(Icons.Default.WrapText, "Toggle line wrapping", selected = wordWrap) {
                wordWrap = !wordWrap
                preferences.edit().putBoolean("word-wrap", wordWrap).apply()
            }
            EditorIcon(if (advancedTools) Icons.Default.ExpandLess else Icons.Default.Tune,
                if (advancedTools) "Hide advanced editor tools" else "Show advanced editor tools",
                selected = advancedTools) { advancedTools = !advancedTools }
        }
        AnimatedVisibility(visible = advancedTools) {
            Row(Modifier.fillMaxWidth().height(48.dp).horizontalScroll(rememberScrollState())
                .padding(horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Box {
                EditorIcon(Icons.Default.List, "File symbol outline") { outlineMenu = true }
                DropdownMenu(expanded = outlineMenu, onDismissRequest = { outlineMenu = false }) {
                    val outline by produceState(initialValue = emptyList<CodeAnalysis.Symbol>(), text, outlineMenu) {
                        if (outlineMenu) {
                            delay(100)
                            value = withContext(Dispatchers.Default) { CodeAnalysis.symbols(text).take(400) }
                        }
                    }
                    if (outline.isEmpty()) DropdownMenuItem(
                        text = { Text("No symbols detected") }, enabled = false, onClick = {})
                    outline.forEach { (line, name) ->
                        DropdownMenuItem(text = { Text("$line · $name", maxLines = 1) },
                            onClick = {
                                view?.goToLogicalLine(line)
                                outlineMenu = false
                            })
                    }
                }
            }
            Box {
                EditorIcon(Icons.Default.Bookmark, "Code bookmarks") { bookmarksMenu = true }
                DropdownMenu(expanded = bookmarksMenu, onDismissRequest = { bookmarksMenu = false }) {
                    DropdownMenuItem(text = { Text("Bookmark current line") },
                        onClick = { view?.toggleBookmark(); bookmarksMenu = false })
                    view?.bookmarkedLines()?.forEach { line ->
                        DropdownMenuItem(text = { Text("Line $line") },
                            onClick = { view?.goToLogicalLine(line); bookmarksMenu = false })
                    }
                }
            }
            EditorIcon(Icons.Default.NearMe, "Go to same-file definition") {
                val editor = view
                if (editor != null) {
                    val source = editor.text.toString()
                    val identifier = CodeAnalysis.identifierAt(source, editor.selectionStart)
                    identifier?.let { CodeAnalysis.definition(source, it) }
                        ?.let { editor.goToLogicalLine(it.line) }
                }
            }
            Box {
                EditorIcon(Icons.Default.ManageSearch, "Find identifier in current file") {
                    referenceMenu = true
                }
                DropdownMenu(expanded = referenceMenu,
                    onDismissRequest = { referenceMenu = false }) {
                    val editor = view
                    val source = editor?.text?.toString().orEmpty()
                    val name = editor?.let { CodeAnalysis.identifierAt(source, it.selectionStart) }
                    val refs = name?.let { CodeAnalysis.nearbyReferences(source, it,
                        editor?.selectionStart ?: 0,
                        language = filename.substringAfterLast('.', "")) }.orEmpty()
                    if (refs.isEmpty()) DropdownMenuItem(text = { Text("Place cursor on an identifier") },
                        enabled = false, onClick = {})
                    refs.forEach { ref ->
                        DropdownMenuItem(text = { Text("Line " + ref.line + " · " + ref.preview,
                            maxLines = 2) }, onClick = {
                            editor?.goToLogicalLine(ref.line)
                            referenceMenu = false
                        })
                    }
                }
            }
            EditorIcon(Icons.Default.DriveFileRenameOutline,
                "Rename identifier in current file") {
                val editor = view
                val source = editor?.text?.toString().orEmpty()
                renameOriginal = editor?.let {
                    CodeAnalysis.identifierAt(source, it.selectionStart)
                }.orEmpty()
                renameSnapshot = source
                renameValue = renameOriginal
                renameError = ""
                renameDialog = renameValue.isNotBlank() && !readOnly
            }
            EditorIcon(Icons.Default.UnfoldLess, "Fold code preview") {
                foldSnapshot = view?.text?.toString() ?: text
                foldPreview = true
            }
            EditorIcon(Icons.Default.CompareArrows, "Resolve Git conflict markers") {
                val source = view?.text?.toString().orEmpty()
                conflictSource = source
                conflictError = ""
                conflictHunks = try { MergeConflictMarkers.parse(source) }
                    catch (e: IllegalArgumentException) {
                        conflictError = e.message ?: "Unable to parse conflict markers."
                        emptyList()
                    }
                conflictChoices = List(conflictHunks.size) { MergeConflictMarkers.Choice.OURS }
                conflictDialog = true
            }
            EditorIcon(Icons.Default.DataObject, "Jump to matching bracket") {
                view?.jumpToMatchingBracket()
            }
            Box {
                EditorIcon(Icons.Default.AutoFixHigh, "Complete word from this file") {
                    completionMenu = true
                }
                DropdownMenu(expanded = completionMenu, onDismissRequest = { completionMenu = false }) {
                    val available = view?.let {
                        CodeAnalysis.wordCompletions(it.text.toString(), it.selectionStart,
                            filename.substringAfterLast('.', ""))
                    }.orEmpty()
                    if (available.isEmpty()) DropdownMenuItem(text = { Text("No matching words in this file") },
                        enabled = false, onClick = {})
                    available.forEach { suggestion ->
                        DropdownMenuItem(text = { Text(suggestion, fontFamily = FontFamily.Monospace) },
                            onClick = {
                                if (!readOnly) view?.completeCurrentWord(suggestion)
                                completionMenu = false
                            })
                    }
                }
            }
            Box {
                EditorIcon(Icons.Default.BugReport, "Structural bracket hints") { issuesMenu = true }
                DropdownMenu(expanded = issuesMenu, onDismissRequest = { issuesMenu = false }) {
                    val issues = CodeAnalysis.bracketIssues(view?.text?.toString() ?: text,
                        filename.substringAfterLast('.', ""))
                    if (issues.isEmpty()) DropdownMenuItem(text = { Text("No bracket problems detected") },
                        enabled = false, onClick = {})
                    val currentLine = view?.let {
                        it.text.subSequence(0, it.selectionStart.coerceIn(0, it.length()))
                            .count { character -> character == '\n' } + 1
                    } ?: 1
                    val orderedIssues = issues.sortedWith(compareBy(
                        { if (it.line >= currentLine) 0 else 1 },
                        { it.line }
                    ))
                    orderedIssues.forEach { issue ->
                        DropdownMenuItem(text = { Text("Line ${issue.line}: ${issue.description}") },
                            onClick = { view?.goToLogicalLine(issue.line); issuesMenu = false })
                    }
                }
            }
            EditorIcon(Icons.Default.FormatListNumbered, "Go to line") {
                lineInput = ""; lineError = false; lineDialog = true
            }
            EditorIcon(Icons.Default.WrapText, "Toggle word wrap", selected = wordWrap) {
                wordWrap = !wordWrap
                preferences.edit().putBoolean("word-wrap", wordWrap).apply()
            }
            Box {
                EditorIcon(Icons.Default.Keyboard, "Mobile shortcut layout") { shortcutSettings = true }
                DropdownMenu(expanded = shortcutSettings, onDismissRequest = { shortcutSettings = false }) {
                    DropdownMenuItem(
                        text = { Text(if (hideToolbarOnHardware) "Show toolbar with hardware keyboard" else "Hide toolbar with hardware keyboard") },
                        onClick = {
                            hideToolbarOnHardware = !hideToolbarOnHardware
                            preferences.edit().putBoolean("auto-hide-hardware-toolbar", hideToolbarOnHardware).apply()
                            shortcutSettings = false
                        })
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text("Customize toolbar…") }, onClick = {
                        shortcutSettings = false
                        draftShortcuts = customShortcuts
                        customizeShortcuts = true
                    })
                    HorizontalDivider()
                    listOf("Standard", "Brackets", "Navigation", "Selection", "Minimal", "Custom").forEach { layout ->
                        DropdownMenuItem(text = { Text(layout) },
                            leadingIcon = {
                                if (shortcutLayout == layout) Icon(Icons.Default.Check, null)
                            }, onClick = {
                                shortcutLayout = layout
                                preferences.edit().putString("shortcut-layout", layout).apply()
                                symbols = true
                                preferences.edit().putBoolean("symbol-toolbar", true).apply()
                                shortcutSettings = false
                            })
                    }
                }
            }
            EditorIcon(Icons.Default.Code, "Show code symbols", selected = symbols) {
                symbols = !symbols
                preferences.edit().putBoolean("symbol-toolbar", symbols).apply()
            }
            Box {
                EditorIcon(Icons.Default.TextFields, "Font size: ${fontSize}sp") { settings = true }
                DropdownMenu(expanded = settings, onDismissRequest = { settings = false }) {
                    DropdownMenuItem(text = { Text(if (showLineNumbers) "Hide line numbers" else "Show line numbers") },
                        onClick = { showLineNumbers = !showLineNumbers
                            preferences.edit().putBoolean("line-numbers", showLineNumbers).apply()
                            settings = false })
                    DropdownMenuItem(text = { Text(if (minimap) "Hide minimap" else "Show minimap") },
                        onClick = {
                            minimap = !minimap
                            preferences.edit().putBoolean("minimap", minimap).apply()
                            settings = false
                        })
                    DropdownMenuItem(text = { Text(if (diagnosticMarkers) "Hide structural hints" else "Show structural hints") },
                        onClick = {
                            diagnosticMarkers = !diagnosticMarkers
                            preferences.edit().putBoolean("diagnostic-markers", diagnosticMarkers).apply()
                            settings = false
                        })
                    DropdownMenuItem(text = { Text(if (showActiveLine) "Hide active line" else "Highlight active line") },
                        onClick = { showActiveLine = !showActiveLine
                            preferences.edit().putBoolean("active-line", showActiveLine).apply()
                            settings = false })
                    HorizontalDivider()
                    listOf("Classic", "Neon", "Monochrome").forEach { palette ->
                        DropdownMenuItem(text = { Text("Syntax colors · " + palette) },
                            onClick = {
                                syntaxPalette = palette
                                preferences.edit().putString("syntax-palette", palette).apply()
                                settings = false
                            })
                    }
                    HorizontalDivider()
                    listOf(2, 4, 8).forEach { width ->
                        DropdownMenuItem(text = { Text("Indent width · $width") },
                            onClick = {
                                indentWidth = width
                                preferences.edit().putInt("indent-width", width).apply()
                                settings = false
                            })
                    }
                    DropdownMenuItem(text = { Text(if (useTabs) "Use spaces for indentation" else "Use tabs for indentation") },
                        onClick = {
                            useTabs = !useTabs
                            preferences.edit().putBoolean("indent-tabs", useTabs).apply()
                            settings = false
                        })
                    HorizontalDivider()
                    listOf(12, 14, 15, 16, 18, 20, 24).forEach {
                        DropdownMenuItem(text = { Text("Font size · ${it}sp") }, onClick = {
                            fontSize = it
                            preferences.edit().putInt("font-size", it).apply()
                            settings = false
                        })
                    }
                }
            }
            EditorIcon(if (readOnly) Icons.Default.Lock else Icons.Default.LockOpen,
                if (readOnly) "Disable read-only mode" else "Enable read-only mode", selected = readOnly) {
                readOnly = !readOnly
                view?.isEnabled = !readOnly
            }
            Box {
                EditorIcon(Icons.Default.Build, "Code transformation tools") { toolsMenu = true }
                DropdownMenu(expanded = toolsMenu, onDismissRequest = { toolsMenu = false }) {
                    listOf("Uppercase", "Lowercase", "Title case", "Trim selection",
                        "Trim trailing spaces", "Tabs to spaces", "Spaces to tabs",
                        "Sort lines A–Z", "Sort lines Z–A", "Reverse lines",
                        "Remove duplicate lines", "Remove blank lines", "Join lines",
                        "Split commas", "Indent 2 spaces", "Indent 4 spaces",
                        "Outdent 2 spaces", "Toggle comments", "Move lines up",
                        "Move lines down", "Normalize LF newlines", "Convert to CRLF").forEachIndexed { index, label ->
                        DropdownMenuItem(text = { Text(label) }, enabled = !readOnly,
                            onClick = { view?.applyCodeTool(index); toolsMenu = false })
                    }
                }
            }
            Box {
                EditorIcon(Icons.Default.AutoAwesome, "Insert code snippets") { snippetsMenu = true }
                DropdownMenu(expanded = snippetsMenu, onDismissRequest = { snippetsMenu = false }) {
                    OutlinedTextField(snippetQuery, { snippetQuery = it.take(80) },
                        modifier = Modifier.width(290.dp).padding(horizontal = 9.dp),
                        label = { Text("Search snippets") }, singleLine = true)
                    DropdownMenuItem(text = { Text("Save selected code…") },
                        onClick = {
                            val selected = view?.let { editor ->
                                val start = editor.selectionStart
                                val end = editor.selectionEnd
                                if (start >= 0 && end > start)
                                    editor.text?.subSequence(start, end)?.toString().orEmpty()
                                else ""
                            }.orEmpty()
                            if (selected.isEmpty() || selected.length > SavedSnippetCodec.MAX_BODY)
                                snippetError = "Select 1 to 4,096 characters in the editor first."
                            else {
                                snippetSelection = selected
                                snippetTitle = ""
                                snippetError = ""
                                snippetsMenu = false
                                snippetSaveDialog = true
                            }
                        })
                    if (snippetError.isNotBlank() && !snippetSaveDialog)
                        Text(snippetError, Modifier.padding(horizontal = 12.dp),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall)
                    savedSnippets.filter { snippetQuery.isBlank() ||
                        it.title.contains(snippetQuery, ignoreCase = true) }.forEach { entry ->
                        DropdownMenuItem(
                            text = { Text("Saved · " + entry.title) },
                            enabled = !readOnly,
                            onClick = { view?.insertAtSelection(entry.body); snippetsMenu = false },
                            trailingIcon = {
                                IconButton(onClick = {
                                    persistSnippets(SavedSnippetCodec.remove(savedSnippets, entry.title))
                                }) { Icon(Icons.Default.DeleteOutline, "Delete snippet ${entry.title}") }
                            }
                        )
                    }
                    if (snippetQuery.isBlank() && savedSnippets.isNotEmpty()) HorizontalDivider()
                    listOf(
                        "Kotlin · function" to "fun example() {\n    \n}",
                        "Kotlin · data class" to "data class Item(val id: Int, val name: String)",
                        "Kotlin · when" to "when (value) {\n    else -> Unit\n}",
                        "Kotlin · try/catch" to "try {\n    \n} catch (e: Exception) {\n    e.printStackTrace()\n}",
                        "JavaScript · function" to "function example() {\n  \n}",
                        "JavaScript · async" to "async function loadData() {\n  try {\n    \n  } catch (error) {\n    console.error(error);\n  }\n}",
                        "JavaScript · event" to "document.addEventListener('DOMContentLoaded', () => {\n  \n});",
                        "TypeScript · interface" to "interface Item {\n  id: number;\n  name: string;\n}",
                        "Python · function" to "def example():\n    pass",
                        "Python · main" to "def main():\n    pass\n\nif __name__ == '__main__':\n    main()",
                        "Python · class" to "class Example:\n    def __init__(self):\n        pass",
                        "HTML · document" to "<!doctype html>\n<html lang=\"en\">\n<head>\n  <meta charset=\"utf-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n  <title>Document</title>\n</head>\n<body>\n  \n</body>\n</html>",
                        "HTML · section" to "<section aria-label=\"Content\">\n  <h2>Heading</h2>\n  <p>Text</p>\n</section>",
                        "CSS · reset" to "*, *::before, *::after { box-sizing: border-box; }\nbody { margin: 0; }",
                        "CSS · responsive" to "@media (max-width: 600px) {\n  .container { padding: 1rem; }\n}",
                        "SQL · create table" to "CREATE TABLE IF NOT EXISTS items (\n    id INTEGER PRIMARY KEY,\n    name TEXT NOT NULL\n);",
                        "SQL · select" to "SELECT id, name\nFROM items\nWHERE id = 1\nORDER BY name ASC;",
                        "Lua · function" to "local function example()\n    \nend",
                        "Java · class" to "public class Example {\n    public static void main(String[] args) {\n        \n    }\n}",
                        "Shell · script" to "#!/usr/bin/env sh\nset -eu\n\n",
                        "Kotlin · coroutine" to "suspend fun fetchItems(): List<String> {\n    return emptyList()\n}",
                        "Kotlin · sealed class" to "sealed class Result {\n    data class Success(val value: String) : Result()\n    data class Error(val message: String) : Result()\n}",
                        "Kotlin · extension" to "fun String.normalized(): String = trim().lowercase()",
                        "JavaScript · fetch" to "fetch('/api/items')\n  .then(response => response.json())\n  .then(data => console.log(data))\n  .catch(error => console.error(error));",
                        "JavaScript · array map" to "const results = items.map(item => item);",
                        "JavaScript · debounce" to "function debounce(fn, delay) {\n  let timer;\n  return (...args) => {\n    clearTimeout(timer);\n    timer = setTimeout(() => fn(...args), delay);\n  };\n}",
                        "TypeScript · type alias" to "type Status = 'idle' | 'loading' | 'success' | 'error';",
                        "TypeScript · generic" to "function identity<T>(value: T): T {\n  return value;\n}",
                        "Python · list comprehension" to "results = [item for item in items if item]",
                        "Python · try/except" to "try:\n    pass\nexcept Exception as error:\n    print(error)",
                        "Python · dataclass" to "from dataclasses import dataclass\n\n@dataclass\nclass Item:\n    id: int\n    name: str",
                        "HTML · form" to "<form>\n  <label for=\"name\">Name</label>\n  <input id=\"name\" name=\"name\" required>\n  <button type=\"submit\">Submit</button>\n</form>",
                        "HTML · image" to "<img src=\"image.png\" alt=\"Description\" loading=\"lazy\">",
                        "CSS · grid" to ".grid {\n  display: grid;\n  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n  gap: 1rem;\n}",
                        "CSS · flex" to ".row {\n  display: flex;\n  align-items: center;\n  justify-content: space-between;\n  gap: 1rem;\n}",
                        "SQL · insert" to "INSERT INTO items (name) VALUES ('Example');",
                        "SQL · update" to "UPDATE items SET name = 'Updated' WHERE id = 1;",
                        "SQL · join" to "SELECT a.id, b.name\nFROM table_a AS a\nINNER JOIN table_b AS b ON b.id = a.id;",
                        "Lua · table" to "local config = {\n    enabled = true,\n    name = 'Example'\n}",
                        "Shell · loop" to "for file in *.txt; do\n  echo \"processing file\"\ndone"
                    ).filter { (label, _) ->
                        snippetQuery.isBlank() || label.contains(snippetQuery, ignoreCase = true)
                    }.forEach { (label, snippet) ->
                        DropdownMenuItem(text = { Text(label) }, enabled = !readOnly,
                            onClick = {
                                view?.insertAtSelection(snippet)
                                snippetsMenu = false
                            })
                    }
                }
            }
            EditorIcon(Icons.Default.ContentCopy, "Duplicate current line") {
                if (!readOnly) view?.duplicateCurrentLine()
            }
            EditorIcon(Icons.Default.DeleteSweep, "Delete current line") {
                if (!readOnly) view?.deleteCurrentLine()
            }
            EditorIcon(Icons.Default.SelectAll, "Select all code") {
                view?.let { editor -> editor.requestFocus(); editor.selectAll() }
            }
            EditorIcon(Icons.Default.Home, "Go to beginning of file") { view?.scrollToStart() }
            EditorIcon(Icons.Default.VerticalAlignBottom, "Go to end of file") {
                view?.let { editor -> editor.requestFocus(); editor.setSelection(editor.length()) }
            }
            }
        }
        if (find) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(search, { search = it; result = "" }, Modifier.weight(1f),
                    singleLine = true, label = { Text("Find in file") })
                EditorAction("Prev") { previousMatch() }
                EditorAction("Next") { selectMatch() }
                EditorAction(if (replaceMode) "Hide" else "Edit") { replaceMode = !replaceMode }
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                FilterChip(selected = matchCase, onClick = {
                    matchCase = !matchCase
                    preferences.edit().putBoolean("search-match-case", matchCase).apply()
                    searchError = ""
                },
                    label = { Text("Aa") })
                Spacer(Modifier.width(6.dp))
                FilterChip(selected = regexMode, onClick = {
                    regexMode = !regexMode
                    preferences.edit().putBoolean("search-regex", regexMode).apply()
                    searchError = ""
                },
                    label = { Text(".*") })
                Spacer(Modifier.width(8.dp))
                Text(if (matchCase) "Case-sensitive" else "Ignore case",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (replaceMode) Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(replacement, { replacement = it }, Modifier.weight(1f), singleLine = true, label = { Text("Replace with") })
                EditorAction("All") {
                    if (!readOnly) {
                    val editor = view
                    if (editor != null && search.isNotEmpty()) {
                        val original = editor.text.toString()
                        val pattern = searchRegex()
                        if (pattern != null) {
                            val count = pattern.findAll(original).count()
                            if (count > 0) {
                                editor.replaceFromUser(pattern.replace(original) { replacement })
                                result = "$count replacements"
                            } else result = "No matches"
                        }
                    }
                    }
                }
                EditorAction("Replace") {
                    val editor = view
                    if (!readOnly && editor != null && search.isNotEmpty() &&
                        searchRegex()?.matches(editor.selectedSource()) == true) {
                        editor.insertAtSelection(replacement)
                    }
                    selectMatch()
                }
            }
            if (search.isNotEmpty() && searchError.isEmpty()) {
                Text("${matchCount()} matches", Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (searchError.isNotEmpty()) Text(searchError, Modifier.padding(horizontal = 12.dp),
                color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            if (result.isNotEmpty()) Text(result, Modifier.padding(horizontal = 12.dp), style = MaterialTheme.typography.labelSmall)
        }
        Row(Modifier.fillMaxWidth().weight(1f)) {
        AndroidView(
            factory = {
                SourceView(it, filename).apply {
                    setText(text)
                    restorePosition(fileKey)
                    changed = { source -> changes(source) }
                    selectionChanged = { line, col ->
                        cursorStatus.label = "Ln " + line + " · Col " + col
                        // Use the live Editable, not the captured Composable text snapshot.
                        // Android can emit selection span changes while replacing characters.
                        val live = this.text
                        val start = minOf(selectionStart, selectionEnd).coerceIn(0, live.length)
                        val end = maxOf(selectionStart, selectionEnd).coerceIn(0, live.length)
                        if (start == end) onSelectionChange("", line)
                        else {
                            val firstLine = live.subSequence(0, start).count { it == '\n' } + 1
                            onSelectionChange(live.subSequence(start, end).toString().take(24000),
                                firstLine)
                        }
                    }
                    view = this
                }
            },
            modifier = Modifier.weight(1f).fillMaxHeight(),
            update = { editor ->
                editor.appearance(fontSize, foreground, background, secondary, accent, dark)
                editor.setCodeWrap(wordWrap)
                editor.setGutterOptions(showLineNumbers, showActiveLine)
                editor.setIndentPolicy(indentWidth, useTabs)
                editor.setSyntaxPalette(syntaxPalette)
                editor.setStudioEditorStyle(studioStyle)
                editor.setStructuralHints(diagnosticMarkers)
                editor.findShortcut = { find = true }
                editor.lineShortcut = { lineInput = ""; lineDialog = true }
                editor.bookmarkShortcut = { editor.toggleBookmark() }
                editor.saveShortcut = onSave
                editor.runShortcut = onRun
                editor.commandShortcut = onOpenCommands
                editor.zoomShortcut = { delta ->
                    fontSize = (fontSize + delta).coerceIn(10, 28)
                    preferences.edit().putInt("font-size", fontSize).apply()
                }
                editor.isEnabled = !readOnly
                if (editor.sourceSnapshot != text) editor.replaceSource(text)
            }
        )
        if (minimap) EditorMinimap(text = text, onNavigate = { line ->
            view?.goToLogicalLine(line)
        })
        }
        if (!compactEditor && symbols && !(hardwareKeyboard && hideToolbarOnHardware)) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically) {
                if (shortcutLayout != "Minimal" && shortcutLayout != "Custom") EditorAction("Tab") { if (!readOnly) view?.indentSelection() }
                if (shortcutLayout != "Minimal" && shortcutLayout != "Custom") EditorAction("Outdent") { if (!readOnly) view?.outdentSelection() }
                if (shortcutLayout != "Brackets" && shortcutLayout != "Custom") EditorAction("←") { view?.let { editor ->
                    editor.setSelection((editor.selectionStart - 1).coerceAtLeast(0))
                } }
                if (shortcutLayout != "Brackets" && shortcutLayout != "Custom") EditorAction("→") { view?.let { editor ->
                    editor.setSelection((editor.selectionEnd + 1).coerceAtMost(editor.length()))
                } }
                if (shortcutLayout == "Selection") {
                    EditorAction("Line") { view?.selectCurrentLine() }
                    EditorAction("All") { view?.selectAll() }
                    EditorAction("Copy") { view?.onTextContextMenuItem(android.R.id.copy) }
                    EditorAction("Cut") { if (!readOnly) view?.onTextContextMenuItem(android.R.id.cut) }
                    EditorAction("Paste") { if (!readOnly) view?.onTextContextMenuItem(android.R.id.paste) }
                    EditorAction("Undo") { if (!readOnly) view?.onTextContextMenuItem(android.R.id.undo) }
                    EditorAction("Redo") { if (!readOnly) view?.onTextContextMenuItem(android.R.id.redo) }
                }
                if (shortcutLayout == "Navigation") {
                    EditorAction("Start") { view?.scrollToStart() }
                    EditorAction("End") { view?.setSelection(view?.length() ?: 0) }
                    EditorAction("↑") { view?.moveCursorLine(-1) }
                    EditorAction("↓") { view?.moveCursorLine(1) }
                    EditorAction("Mark") { view?.toggleBookmark() }
                }
                val shortcutSymbols = when (shortcutLayout) {
                    "Brackets" -> listOf("{", "}", "(", ")", "[", "]", "<", ">", "'", "\"")
                    "Navigation" -> listOf("{", "}", "(", ")", ";")
                    "Selection" -> emptyList()
                    "Custom" -> emptyList()
                    "Minimal" -> listOf("{", "}", "(", ")", ";")
                    else -> listOf("{", "}", "(", ")", "[", "]", "=", ";", ":", "'", "\"", "/", "<", ">")
                }
                shortcutSymbols.forEach { symbol ->
                    EditorAction(symbol) { if (!readOnly) view?.insertAtSelection(symbol) }
                }
                if (shortcutLayout == "Custom") {
                    customShortcuts.forEach { symbol ->
                        EditorAction(symbol) {
                            val editor = view
                            if (editor != null) when (symbol) {
                                "Tab" -> if (!readOnly) editor.indentSelection()
                                "Esc" -> editor.hideKeyboard()
                                "←" -> editor.setSelection((editor.selectionStart - 1).coerceAtLeast(0))
                                "→" -> editor.setSelection((editor.selectionEnd + 1).coerceAtMost(editor.length()))
                                "↑" -> editor.moveCursorLine(-1)
                                "↓" -> editor.moveCursorLine(1)
                                else -> if (!readOnly) editor.insertAtSelection(symbol)
                            }
                        }
                    }
                }
            }
        }
        if (!compactEditor) EditorStatusBar(
            fileKey = fileKey, filename = filename, text = text, status = cursorStatus,
            readOnly = readOnly, wordWrap = wordWrap,
            onPreviousTab = onPreviousTab, onNextTab = onNextTab
        )
    }
    }
}


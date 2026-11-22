package site.lxwis.codelab

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Editable
import android.text.InputType
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.content.res.Configuration
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.Future

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
    var symbols by remember { mutableStateOf(preferences.getBoolean("symbol-toolbar", false)) }
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
    var cursor by remember { mutableStateOf("Ln 1 · Col 1") }
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
    var outlineMenu by remember { mutableStateOf(false) }
    var referenceMenu by remember { mutableStateOf(false) }
    var renameDialog by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf("") }
    var renameError by remember { mutableStateOf("") }
    var bookmarksMenu by remember { mutableStateOf(false) }
    var showLineNumbers by remember { mutableStateOf(preferences.getBoolean("line-numbers", true)) }
    var showActiveLine by remember { mutableStateOf(preferences.getBoolean("active-line", true)) }
    var result by remember { mutableStateOf("") }
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
                if (renameError.isNotBlank())
                    Text(renameError, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = {
            val editor = view
            if (editor != null) {
                val oldName = CodeAnalysis.identifierAt(
                    editor.text.toString(), editor.selectionStart)
                if (oldName == null) renameError = "No identifier at cursor."
                else try {
                    editor.replaceSource(CodeAnalysis.lexicalRename(
                        editor.text.toString(), oldName, renameValue))
                    renameDialog = false
                } catch (ex: IllegalArgumentException) {
                    renameError = ex.message.orEmpty()
                }
            }
        }) { Text("Rename") } },
        dismissButton = { TextButton(onClick = { renameDialog = false }) { Text("Cancel") } })

    LaunchedEffect(jumpLine, view) {
        if (jumpLine != null && view != null) {
            view?.goToLogicalLine(jumpLine)
            onJumpHandled()
        }
    }
    val palette = MaterialTheme.colorScheme
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
    Column(modifier) {
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
                    val refs = name?.let { CodeAnalysis.references(source, it) }.orEmpty()
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
                renameValue = editor?.let { CodeAnalysis.identifierAt(source, it.selectionStart) }.orEmpty()
                renameError = ""
                renameDialog = renameValue.isNotBlank() && !readOnly
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
                        CodeAnalysis.wordCompletions(it.text.toString(), it.selectionStart)
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
                    val issues = CodeAnalysis.bracketIssues(view?.text?.toString() ?: text)
                    if (issues.isEmpty()) DropdownMenuItem(text = { Text("No bracket problems detected") },
                        enabled = false, onClick = {})
                    issues.forEach { issue ->
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
                    ).forEach { (label, snippet) ->
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
        if (symbols && !(hardwareKeyboard && hideToolbarOnHardware)) {
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
                                editor.replaceSource(pattern.replace(original) { replacement })
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
                        cursor = "Ln " + line + " · Col " + col
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
                editor.setStructuralHints(diagnosticMarkers)
                editor.findShortcut = { find = true }
                editor.lineShortcut = { lineInput = ""; lineDialog = true }
                editor.bookmarkShortcut = { editor.toggleBookmark() }
                editor.saveShortcut = onSave
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
        var tabSwipe by remember { mutableFloatStateOf(0f) }
        Row(Modifier.fillMaxWidth().height(38.dp)
            .pointerInput(fileKey) {
                detectHorizontalDragGestures(onDragStart = { tabSwipe = 0f },
                    onDragEnd = {
                        if (tabSwipe > 70f) onPreviousTab()
                        else if (tabSwipe < -70f) onNextTab()
                        tabSwipe = 0f
                    }, onHorizontalDrag = { change, amount ->
                        change.consume()
                        tabSwipe += amount
                    })
            }
            .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(cursor + " · " + (view?.text?.count { it == '\n' }?.plus(1) ?: text.count { it == '\n' } + 1) + " lines",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text((if (readOnly) "READ ONLY · " else "") + (if (wordWrap) "Wrap · " else "") + "UTF-8 · " + filename.substringAfterLast('.', "text").uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EditorIcon(icon: ImageVector, description: String, selected: Boolean = false,
                       onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp)
        .background(if (selected) colors.primaryContainer else Color.Transparent, RoundedCornerShape(10.dp))) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(20.dp),
            tint = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant)
    }
}

@Composable
private fun EditorAction(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 9.dp),
        modifier = Modifier.defaultMinSize(minWidth = 44.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun EditorMinimap(text: String, onNavigate: (Int) -> Unit) {
    val ink = MaterialTheme.colorScheme.onSurfaceVariant
    val surface = MaterialTheme.colorScheme.surfaceContainer
    val model by produceState(initialValue = Pair(emptyList<Int>(), 1), text) {
        delay(180)
        value = withContext(Dispatchers.Default) {
            Pair(text.lineSequence().map { it.trimEnd().length }.take(240).toList(),
                text.count { it == '\n' } + 1)
        }
    }
    val sample = model.first
    val allLines = model.second
    ComposeCanvas(Modifier.width(34.dp).fillMaxHeight()
        .background(surface)
        .pointerInput(allLines) {
            detectTapGestures { offset ->
                val ratio = (offset.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f)
                onNavigate((ratio * allLines).toInt().coerceIn(1, allLines))
            }
        }) {
        val total = sample.size.coerceAtLeast(1)
        val step = size.height / total
        val sampleColor = ink.copy(alpha = .38f)
        sample.forEachIndexed { index, length ->
            val y = index * step + 2f
            val width = (length.coerceAtMost(48) / 48f) * (size.width - 7f)
            if (width > 0f) drawRect(
                sampleColor, topLeft = Offset(3f, y),
                size = Size(width, step.coerceAtLeast(.8f).coerceAtMost(2.3f))
            )
        }
    }
}

private class SyntaxInk(color: Int) : ForegroundColorSpan(color)

internal class SourceView(context: Context, filename: String) : EditText(context) {
    var changed: ((String) -> Unit)? = null
    var selectionChanged: ((Int, Int) -> Unit)? = null
    private var codeWrapped: Boolean? = null
    private val bookmarks = sortedSetOf<Int>()
    private var diagnostics = emptySet<Int>()
    private var hintsEnabled = true
    private var hintGeneration = 0
    private var hintTask: Future<*>? = null
    private val hintJob = Runnable {
        val snapshot = sourceSnapshot
        val generation = ++hintGeneration
        hintTask?.cancel(true)
        val shouldCheck = hintsEnabled && snapshot.length < 32000 &&
            language in setOf("js", "mjs", "ts", "kt", "kts", "java", "c", "cpp", "cs", "css", "go", "rs")
        if (!shouldCheck) {
            diagnostics = emptySet()
            invalidate()
        } else {
            hintTask = highlightWorker.submit(Runnable {
                val issues = CodeAnalysis.bracketIssues(snapshot).map { it.line }.toSet()
                post(Runnable applyHints@ {
                    if (generation != hintGeneration || snapshot != sourceSnapshot) return@applyHints
                    diagnostics = issues
                    invalidate()
                })
            })
        }
    }
    fun setStructuralHints(enabled: Boolean) {
        if (hintsEnabled == enabled) return
        hintsEnabled = enabled
        removeCallbacks(hintJob)
        postDelayed(hintJob, 500)
    }
    fun bookmarkedLines(): List<Int> = bookmarks.toList()
    fun toggleBookmark() {
        val line = text.substring(0, selectionStart.coerceIn(0, length())).count { it == '\n' } + 1
        if (!bookmarks.add(line)) bookmarks.remove(line)
        persistBookmarks()
        invalidate()
    }
    private fun persistBookmarks() {
        positionKey?.let { key ->
            context.getSharedPreferences("editor-positions", Context.MODE_PRIVATE)
                .edit().putString("$key|bookmarks", bookmarks.joinToString(",")).apply()
        }
    }
    fun goToLogicalLine(number: Int) {
        if (number < 1) return
        val content = text.toString()
        var position = 0
        repeat(number - 1) {
            val next = content.indexOf('\n', position)
            if (next < 0) return
            position = next + 1
        }
        requestFocus()
        setSelection(position)
    }
    var findShortcut: (() -> Unit)? = null
    var lineShortcut: (() -> Unit)? = null
    var bookmarkShortcut: (() -> Unit)? = null
    var saveShortcut: (() -> Unit)? = null
    var commandShortcut: (() -> Unit)? = null
    var zoomShortcut: ((Int) -> Unit)? = null
    private val zoomDetector = ScaleGestureDetector(context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            private var accumulator = 1f
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                accumulator = 1f
                return true
            }
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                accumulator *= detector.scaleFactor
                if (accumulator > 1.17f) {
                    zoomShortcut?.invoke(1)
                    accumulator = 1f
                } else if (accumulator < .85f) {
                    zoomShortcut?.invoke(-1)
                    accumulator = 1f
                }
                return true
            }
        })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        zoomDetector.onTouchEvent(event)
        if (zoomDetector.isInProgress || event.pointerCount > 1) return true
        return super.onTouchEvent(event)
    }

    fun hideKeyboard() {
        val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE)
            as android.view.inputmethod.InputMethodManager
        manager.hideSoftInputFromWindow(windowToken, 0)
        clearFocus()
    }
    fun selectCurrentLine() {
        val value = text.toString()
        val caret = selectionStart.coerceIn(0, value.length)
        val first = value.lastIndexOf('\n', (caret - 1).coerceAtLeast(0)) + 1
        val last = value.indexOf('\n', caret).let { if (it < 0) value.length else it }
        requestFocus()
        setSelection(first, last)
    }

    fun moveCursorLine(offset: Int) {
        val layout = layout ?: return
        val caret = selectionStart.coerceIn(0, length())
        val line = layout.getLineForOffset(caret)
        val target = (line + offset).coerceIn(0, layout.lineCount - 1)
        val column = caret - layout.getLineStart(line)
        setSelection((layout.getLineStart(target) + column)
            .coerceAtMost(layout.getLineEnd(target)).coerceIn(0, length()))
    }
    private var indentUnit = "    "
    private var indentSpaces = 4
    fun setIndentPolicy(width: Int, tabs: Boolean) {
        indentSpaces = width.coerceIn(2, 8)
        indentUnit = if (tabs) "\t" else " ".repeat(indentSpaces)
    }
    fun completeCurrentWord(suggestion: String) {
        val end = selectionStart.coerceIn(0, length())
        var start = end
        while (start > 0 && (text[start - 1].isLetterOrDigit() || text[start - 1] == '_')) start--
        text.replace(start, end, suggestion)
        setSelection(start + suggestion.length)
    }
    fun jumpToMatchingBracket() {
        val pair = CodeAnalysis.matchBracket(text.toString(), selectionStart) ?: return
        val at = if (selectionStart <= pair.first + 1) pair.second else pair.first
        requestFocus()
        setSelection(at)
    }
    private var showNumbers = true
    private var showCurrentLine = true
    fun setGutterOptions(numbers: Boolean, currentLine: Boolean) {
        if (showNumbers != numbers || showCurrentLine != currentLine) {
            showNumbers = numbers
            showCurrentLine = currentLine
            invalidate()
        }
    }
    var sourceSnapshot = ""
        private set
    private var syncing = false
    private var positionKey: String? = null
    private var pendingScroll: Pair<Int,Int>? = null
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.RIGHT
    }
    private val fill = Paint()
    private var gutterWidth = 0
    private var muted = 0
    private var paper = 0
    private var active = 0
    private var darkMode = true
    private var syntaxPalette = "Classic"
    fun setSyntaxPalette(value: String) {
        if (syntaxPalette != value) {
            syntaxPalette = value
            scheduleHighlight(true)
        }
    }
    private var lastAppearance: List<Any> = emptyList()
    private var ready = false
    private val language = filename.substringAfterLast('.', "").lowercase()
    private val tokens = Regex(when (language) {
        in hashCommentLanguages -> "#[^\n]*|" + tokenPattern
        "lua", "sql" -> "--[^\n]*|" + tokenPattern
        else -> tokenPattern
    })
    private val highlightWorker = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "CodeLab-editor-analysis").apply { isDaemon = true }
    }
    private var highlightTask: Future<*>? = null
    private var highlightGeneration = 0
    private val highlightJob = Runnable { highlightVisible() }
    private var highlightedStart = -1
    private var highlightedEnd = -1
    private var highlightedSnapshot = ""
    private data class HighlightMark(val start: Int, val end: Int, val ink: Int)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).roundToInt()
    private fun dp(value: Float) = (value * resources.displayMetrics.density)

    init {
        background = null
        gravity = Gravity.TOP or Gravity.START
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE
        imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
        typeface = Typeface.MONOSPACE
        setHorizontallyScrolling(true)
        isHorizontalScrollBarEnabled = true
        isVerticalScrollBarEnabled = true
        contentDescription = "Source code, $filename"
        setLineSpacing(dp(3).toFloat(), 1f)
        includeFontPadding = false
        setPadding(dp(52), dp(8), dp(16), dp(36))
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sourceSnapshot = s?.toString().orEmpty()
                if (!syncing) changed?.invoke(sourceSnapshot)
                scheduleHighlight(true)
                removeCallbacks(hintJob)
                postDelayed(hintJob, 650)
            }
        })
        ready = true
    }

    fun appearance(size: Int, foreground: Int, backgroundColor: Int, secondary: Int, accent: Int, dark: Boolean) {
        val values = listOf(size, foreground, backgroundColor, secondary, accent, dark)
        if (values == lastAppearance) return
        lastAppearance = values
        setTextSize(size.toFloat())
        setTextColor(foreground)
        highlightColor = (accent and 0x00ffffff) or 0x55000000
        darkMode = dark
        paper = backgroundColor
        active = accent
        muted = secondary
        linePaint.textSize = textSize * .77f
        gutterWidth = dp(18) + linePaint.measureText("0000").roundToInt()
        setPadding(gutterWidth + dp(10), dp(8), dp(16), dp(36))
        scheduleHighlight(true)
        invalidate()
    }

    fun setCodeWrap(enabled: Boolean) {
        if (codeWrapped != enabled) {
            codeWrapped = enabled
            setHorizontallyScrolling(!enabled)
            isHorizontalScrollBarEnabled = !enabled
            if (enabled) scrollTo(0, scrollY)
        }
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        val current = layout ?: return
        val line = current.getLineForOffset(selEnd.coerceIn(0, length()))
        val column = selEnd - current.getLineStart(line) + 1
        selectionChanged?.invoke(line + 1, column)
    }

    fun scrollToStart() {
        setSelection(0)
        scrollTo(0, 0)
        requestFocus()
    }

    fun restorePosition(key: String) {
        positionKey = key
        val prefs = context.getSharedPreferences("editor-positions", Context.MODE_PRIVATE)
        bookmarks.clear()
        prefs.getString("$key|bookmarks", "")?.split(",")?.forEach {
            it.toIntOrNull()?.takeIf { line -> line > 0 }?.let(bookmarks::add)
        }
        setSelection(prefs.getInt("$key|start", 0).coerceIn(0, length()),
            prefs.getInt("$key|end", 0).coerceIn(0, length()))
        pendingScroll = 0 to prefs.getInt("$key|y", 0)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        pendingScroll?.let { (x,y) ->
            pendingScroll = null
            val maxY = ((layout?.height ?: 0) + totalPaddingTop + totalPaddingBottom - height).coerceAtLeast(0)
            val maxX = (computeHorizontalScrollRange() - width + totalPaddingLeft + totalPaddingRight).coerceAtLeast(0)
            scrollTo(x.coerceIn(0,maxX), y.coerceIn(0,maxY))
        }
    }

    fun replaceSource(source: String) {
        val cursor = selectionStart.coerceIn(0, source.length)
        syncing = true
        try {
            setText(source)
            setSelection(cursor)
        } finally {
            syncing = false
        }
    }

    fun duplicateCurrentLine() {
        val source = text ?: return
        val position = selectionStart.coerceIn(0, source.length)
        val start = source.lastIndexOf('\n', (position - 1).coerceAtLeast(0)).let {
            if (it < 0) 0 else it + 1
        }
        val lineEnd = source.indexOf('\n', position).let { if (it < 0) source.length else it }
        val line = source.subSequence(start, lineEnd).toString()
        source.insert(lineEnd, "\n" + line)
        setSelection((position + line.length + 1).coerceAtMost(source.length))
    }

    fun deleteCurrentLine() {
        val source = text ?: return
        if (source.isEmpty()) return
        val position = selectionStart.coerceIn(0, source.length)
        val start = source.lastIndexOf('\n', (position - 1).coerceAtLeast(0)).let {
            if (it < 0) 0 else it + 1
        }
        val end = source.indexOf('\n', position).let {
            if (it < 0) source.length else it + 1
        }
        if (start == 0 && end == source.length) source.clear()
        else if (end == source.length && start > 0) source.delete(start - 1, end)
        else source.delete(start, end)
        setSelection(start.coerceAtMost(source.length))
    }

    fun indentSelection() {
        val source = text ?: return
        val from = minOf(selectionStart, selectionEnd).coerceAtLeast(0)
        val to = maxOf(selectionStart, selectionEnd).coerceAtLeast(0)
        if (from == to) {
            insertAtSelection(indentUnit)
            return
        }
        val start = source.lastIndexOf('\n', (from - 1).coerceAtLeast(0)).let {
            if (it < 0) 0 else it + 1
        }
        val chunk = source.subSequence(start, to).toString()
        val indented = indentUnit + chunk.replace("\n", "\n" + indentUnit)
        source.replace(start, to, indented)
        setSelection(start, start + indented.length)
    }

    fun outdentSelection() {
        val source = text ?: return
        val from = minOf(selectionStart, selectionEnd).coerceAtLeast(0)
        val to = maxOf(selectionStart, selectionEnd).coerceAtLeast(0)
        val start = source.lastIndexOf('\n', (from - 1).coerceAtLeast(0)).let {
            if (it < 0) 0 else it + 1
        }
        val chunk = source.subSequence(start, to).toString()
        val outdented = chunk.split("\n").joinToString("\n") { line ->
            line.drop(if (line.startsWith("\t")) 1 else line.takeWhile { it == ' ' }.length.coerceAtMost(indentSpaces))
        }
        source.replace(start, to, outdented)
        setSelection(start, start + outdented.length)
    }

    fun applyCodeTool(index: Int) {
        if (!isEnabled || index !in 0..21) return
        if (index == 20 || index == 21) {
            val current = text.toString()
            val lf = current.replace("\r\n", "\n").replace("\r", "\n")
            val changed = if (index == 21) lf.replace("\n", "\r\n") else lf
            if (current != changed) {
                val caret = selectionStart.coerceIn(0, changed.length)
                text.replace(0, length(), changed)
                setSelection(caret)
            }
            return
        }
        val editable = text ?: return
        val source = editable.toString()
        val from = minOf(selectionStart, selectionEnd).coerceIn(0, source.length)
        val to = maxOf(selectionStart, selectionEnd).coerceIn(0, source.length)
        val start = (source.lastIndexOf('\n', (from - 1).coerceAtLeast(0)) + 1).coerceAtMost(from)
        val end = source.indexOf('\n', to).let { if (it < 0) source.length else it }
        val chunk = source.substring(start, end)
        val lines = chunk.split("\n")
        if (index == 18 || index == 19) {
            if (index == 18) {
                if (start == 0) return
                val priorEnd = start - 1
                val priorStart = source.lastIndexOf('\n', (priorEnd - 1).coerceAtLeast(0)) + 1
                val prior = source.substring(priorStart, priorEnd)
                editable.replace(priorStart, end, chunk + "\n" + prior)
                setSelection(priorStart, priorStart + chunk.length)
            } else {
                if (end >= source.length) return
                val nextStart = end + 1
                val nextEnd = source.indexOf('\n', nextStart).let { if (it < 0) source.length else it }
                val next = source.substring(nextStart, nextEnd)
                editable.replace(start, nextEnd, next + "\n" + chunk)
                val movedStart = start + next.length + 1
                setSelection(movedStart, movedStart + chunk.length)
            }
            return
        }
        val transformed = when (index) {
            0 -> chunk.uppercase()
            1 -> chunk.lowercase()
            2 -> chunk.split(Regex("(?<=\\W)|(?=\\W)")).joinToString("") { word ->
                word.lowercase().replaceFirstChar { it.titlecase() }
            }
            3 -> chunk.trim()
            4 -> lines.joinToString("\n") { it.trimEnd() }
            5 -> chunk.replace("\t", "    ")
            6 -> chunk.replace("    ", "\t")
            7 -> lines.sortedWith(String.CASE_INSENSITIVE_ORDER).joinToString("\n")
            8 -> lines.sortedWith(String.CASE_INSENSITIVE_ORDER.reversed()).joinToString("\n")
            9 -> lines.reversed().joinToString("\n")
            10 -> lines.distinct().joinToString("\n")
            11 -> lines.filter { it.isNotBlank() }.joinToString("\n")
            12 -> lines.joinToString(" ") { it.trim() }
            13 -> chunk.replace(Regex(",\\s*"), ",\n")
            14 -> lines.joinToString("\n") { "  " + it }
            15 -> lines.joinToString("\n") { "    " + it }
            16 -> lines.joinToString("\n") { it.drop(it.takeWhile { ch -> ch == ' ' }.length.coerceAtMost(2)) }
            17 -> {
                val prefix = when (language) {
                    "py", "rb", "sh", "bash", "yaml", "yml", "toml" -> "#"
                    "sql", "lua" -> "--"
                    else -> "//"
                }
                val commented = lines.filter { it.isNotBlank() }.all { it.trimStart().startsWith(prefix) }
                lines.joinToString("\n") { line ->
                    if (line.isBlank()) line
                    else if (commented) {
                        val indent = line.length - line.trimStart().length
                        line.substring(0, indent) + line.substring(indent).removePrefix(prefix).removePrefix(" ")
                    } else prefix + " " + line
                }
            }
            else -> return
        }
        if (transformed != chunk) {
            editable.replace(start, end, transformed)
            setSelection(start, start + transformed.length)
        }
    }

    fun selectedSource(): String {
        val live = text
        val a = minOf(selectionStart, selectionEnd).coerceIn(0, live.length)
        val b = maxOf(selectionStart, selectionEnd).coerceIn(0, live.length)
        return live.subSequence(a, b).toString()
    }

    fun insertAtSelection(value: String) {
        requestFocus()
        val a = selectionStart.coerceAtLeast(0)
        val b = selectionEnd.coerceAtLeast(0)
        text.replace(minOf(a, b), maxOf(a, b), value)
        setSelection((minOf(a,b) + value.length).coerceAtMost(length()))
    }

    private fun typeCodeKey(key: Char): Boolean {
        val start = minOf(selectionStart, selectionEnd)
        val end = maxOf(selectionStart, selectionEnd)
        if (start < 0 || BaseInputConnection.getComposingSpanStart(text) >= 0) return false
        if (key == '\n') {
            var first = start
            while (first > 0 && text[first - 1] != '\n') first--
            var spaces = first
            while (spaces < start && (text[spaces] == ' ' || text[spaces] == '\t')) spaces++
            val extra = if (start > 0 && text[start - 1] in "{[(:") indentUnit else ""
            insertAtSelection("\n" + text.subSequence(first, spaces) + extra)
            return true
        }
        if (start == end && end < length() && text[end] == key && key in closers) {
            setSelection(end + 1)
            return true
        }
        val closing = pairs[key] ?: return false
        // A quote inside a name or after an escape is usually a literal apostrophe.
        if (key == '\'' || key == '"') {
            if (start > 0 && (text[start - 1].isLetterOrDigit() || text[start - 1] == '\\')) return false
        }
        if (start == end && end < length() && !text[end].isWhitespace() && text[end] !in closers) return false
        val selected = text.subSequence(start, end).toString()
        text.replace(start, end, key + selected + closing)
        setSelection(start + 1, start + 1 + selected.length)
        return true
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val connection = super.onCreateInputConnection(outAttrs) ?: return null
        return object : InputConnectionWrapper(connection, false) {
            override fun commitText(value: CharSequence?, newCursorPosition: Int): Boolean {
                if (value != null && value.length == 1 && newCursorPosition == 1 && typeCodeKey(value[0])) return true
                return super.commitText(value, newCursorPosition)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.isCtrlPressed && event.isShiftPressed &&
            keyCode == KeyEvent.KEYCODE_P) {
            commandShortcut?.invoke()
            return true
        }
        if (event.isCtrlPressed && !event.isAltPressed) {
            when (keyCode) {
                KeyEvent.KEYCODE_S -> { saveShortcut?.invoke(); return true }
                KeyEvent.KEYCODE_F -> { findShortcut?.invoke(); return true }
                KeyEvent.KEYCODE_G -> { lineShortcut?.invoke(); return true }
                KeyEvent.KEYCODE_B -> { bookmarkShortcut?.invoke(); return true }
                KeyEvent.KEYCODE_D -> { duplicateCurrentLine(); return true }
                KeyEvent.KEYCODE_SLASH -> { applyCodeTool(17); return true }
            }
        }
        if (keyCode == KeyEvent.KEYCODE_TAB && event.hasNoModifiers()) {
            indentSelection()
            return true
        }
        if (event.hasNoModifiers() || event.isShiftPressed && !event.isCtrlPressed && !event.isAltPressed) {
            val typed = if (keyCode == KeyEvent.KEYCODE_ENTER) '\n' else event.unicodeChar.toChar()
            if (typeCodeKey(typed)) return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val lines = layout ?: return
        val first = lines.getLineForVertical((scrollY - totalPaddingTop).coerceAtLeast(0))
        val last = lines.getLineForVertical(scrollY + height)
        val selected = lines.getLineForOffset(selectionStart.coerceIn(0, length()))
        fill.color = paper
        canvas.drawRect(scrollX.toFloat(), scrollY.toFloat(), (scrollX + gutterWidth).toFloat(), (scrollY + height).toFloat(), fill)
        for (line in first..last) {
            if (!showNumbers) break
            linePaint.color = if (showCurrentLine && line == selected) active else muted
            linePaint.alpha = if (line == selected) 255 else 170
            if (diagnostics.contains(line + 1)) {
                fill.color = 0xffdc6a6a.toInt()
                canvas.drawCircle((scrollX + dp(5)).toFloat(),
                    (totalPaddingTop + lines.getLineBaseline(line) - dp(4)).toFloat(),
                    dp(2.5f).toFloat(), fill)
            }
            if (bookmarks.contains(line + 1)) {
                fill.color = active
                canvas.drawCircle((scrollX + dp(7)).toFloat(),
                    (totalPaddingTop + lines.getLineBaseline(line) - dp(4)).toFloat(),
                    dp(3).toFloat(), fill)
            }
            canvas.drawText((line + 1).toString(), (scrollX + gutterWidth - dp(8)).toFloat(),
                (totalPaddingTop + lines.getLineBaseline(line)).toFloat(), linePaint)
        }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (ready && t != oldt) scheduleHighlight(false)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (ready) scheduleHighlight(true)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(highlightJob)
        removeCallbacks(hintJob)
        ++highlightGeneration
        ++hintGeneration
        highlightTask?.cancel(true)
        hintTask?.cancel(true)
        highlightWorker.shutdownNow()
        persistBookmarks()
        positionKey?.let { key ->
            context.getSharedPreferences("editor-positions", Context.MODE_PRIVATE).edit()
                .putInt("$key|start", selectionStart).putInt("$key|end", selectionEnd)
                .putInt("$key|x", scrollX).putInt("$key|y", scrollY).apply()
        }
        super.onDetachedFromWindow()
    }

    private fun scheduleHighlight(force: Boolean) {
        // Ignore individual scroll ticks while the visible viewport remains within the
        // highlighted margin. Recompute only after crossing the highlighted window.
        if (!force && highlightedStart >= 0) {
            val lines = layout
            if (lines != null && lines.lineCount > 0) {
                val first = lines.getLineStart(lines.getLineForVertical(scrollY.coerceAtLeast(0)))
                val last = lines.getLineEnd(lines.getLineForVertical((scrollY + height).coerceAtLeast(0)))
                if (first >= highlightedStart && last <= highlightedEnd) return
            }
        }
        if (force) highlightedStart = -1
        ++highlightGeneration
        highlightTask?.cancel(true)
        removeCallbacks(highlightJob)
        postDelayed(highlightJob, if (force) 110 else 100)
    }

    private fun highlightVisible() {
        val lines = layout ?: return
        if (lines.lineCount == 0) return
        val first = (lines.getLineForVertical(scrollY.coerceAtLeast(0)) - 14).coerceAtLeast(0)
        val last = (lines.getLineForVertical((scrollY + height).coerceAtLeast(0)) + 14)
            .coerceAtMost(lines.lineCount - 1)
        val start = lines.getLineStart(first)
        val end = minOf(lines.getLineEnd(last), start + 24000, length())
        val snapshot = sourceSnapshot
        if (start == highlightedStart && end == highlightedEnd &&
            snapshot == highlightedSnapshot) return
        val ticket = ++highlightGeneration
        highlightTask?.cancel(true)
        if (start >= end || language !in supportedExtensions) {
            text?.getSpans(0, length(), SyntaxInk::class.java)?.forEach(text::removeSpan)
            highlightedStart = start
            highlightedEnd = end
            highlightedSnapshot = snapshot
            return
        }
        // Copy only a viewport-sized fragment on the UI thread. Regex parsing and
        // structural analysis run on the dedicated, cancellable worker.
        val fragment = snapshot.substring(start.coerceAtMost(snapshot.length), end.coerceAtMost(snapshot.length))
        val colors = when (syntaxPalette) {
            "Neon", "Dracula" -> if (darkMode) neonDarkInk else neonLightInk
            "Monochrome" -> if (darkMode) monoDarkInk else monoLightInk
            "One Dark" -> darkInk
            "Monokai" -> neonDarkInk
            "GitHub Light" -> lightInk
            else -> if (darkMode) darkInk else lightInk
        }.copyOf()
        highlightTask = highlightWorker.submit(Runnable {
            val marks = ArrayList<HighlightMark>()
            fun add(from: Int, to: Int, kind: Int) {
                if (to > from && marks.size < 3200) marks.add(HighlightMark(start + from, start + to, colors[kind]))
            }
            if (language == "html" || language == "htm") {
                for (tag in htmlTags.findAll(fragment)) {
                    if (Thread.currentThread().isInterrupted) return@Runnable
                    val value = tag.value
                    if (value.startsWith("<!--")) {
                        add(tag.range.first, tag.range.last + 1, 0)
                    } else {
                        htmlTagName.find(value)?.let { name ->
                            add(tag.range.first + name.range.first, tag.range.first + name.range.last + 1, 3)
                        }
                        for (attr in htmlAttributes.findAll(value))
                            add(tag.range.first + attr.range.first, tag.range.first + attr.range.last + 1, 2)
                        for (quoted in htmlStrings.findAll(value))
                            add(tag.range.first + quoted.range.first, tag.range.first + quoted.range.last + 1, 1)
                    }
                }
            } else {
                if (language == "css") {
                    for (property in cssNames.findAll(fragment))
                        add(property.range.first, property.range.last + 1, 3)
                }
                for (token in tokens.findAll(fragment)) {
                    if (Thread.currentThread().isInterrupted) return@Runnable
                    val value = token.value
                    val kind = when {
                        value.startsWith("//") || value.startsWith("/*") -> 0
                        value.startsWith("#") -> if (language in hashCommentLanguages) 0 else continue
                        value.startsWith("--") -> if (language in setOf("sql", "lua")) 0 else continue
                        value[0] == '"' || value[0] == '\'' || value[0] == '`' -> 1
                        value[0].isDigit() -> 2
                        value.lowercase() in keywords -> 3
                        else -> continue
                    }
                    add(token.range.first, token.range.last + 1, kind)
                }
            }
            post(Runnable applySpans@ {
                if (ticket != highlightGeneration || !isAttachedToWindow || sourceSnapshot != snapshot)
                    return@applySpans
                val editable = text ?: return@applySpans
                editable.getSpans(0, editable.length, SyntaxInk::class.java).forEach(editable::removeSpan)
                for (mark in marks) if (mark.end <= editable.length) {
                    editable.setSpan(SyntaxInk(mark.ink), mark.start, mark.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                highlightedStart = start
                highlightedEnd = end
                highlightedSnapshot = snapshot
            })
        })
    }

    companion object {
        private val pairs = mapOf('(' to ')', '[' to ']', '{' to '}', '"' to '"', '\'' to '\'')
        private val closers = pairs.values.toSet()
        private val supportedExtensions = setOf("js","mjs","ts","jsx","tsx","json","py","lua","sql","kt","kts","java","c","h","cpp","css","html","htm","rs","sh","cs","go","rb","php","swift","dart","scala","pl","hs","r")
        private val hashCommentLanguages = setOf("py","sh","rb","pl","r")
        private val htmlTags = Regex("""<!--[\s\S]*?-->|</?[a-zA-Z][^>]*?>""")
        private val htmlTagName = Regex("""</?[a-zA-Z][\w:-]*""")
        private val htmlAttributes = Regex("""[a-zA-Z_:][\w:.-]*(?=\s*=)""")
        private val htmlStrings = Regex(""""[^"]*"|'[^']*'""")
        private val cssNames = Regex("""[a-zA-Z_-][\w-]*(?=\s*:)""")
        private val tokenPattern  = """//[^\n]*|/\*[\s\S]*?(?:\*/|$)|"(?:\\.|[^"\\\n])*+"?|'(?:\\.|[^'\\\n])*+'?|`(?:\\.|[^`\\])*+`?|\b(?:0[xX][0-9a-fA-F]+|\d+(?:\.\d+)?)\b|[a-zA-Z_][a-zA-Z_0-9]*"""
        private val keywords = "async await break case catch class const continue debugger default delete do else export extends false finally for from function if import in instanceof interface let new null of return static super switch this throw true try typeof undefined var void while with yield def elif except lambda pass none print console log select where insert create table into values order by join update set delete and or end local then repeat until fun val object when package public private int char float double struct bool return func println puts echo using namespace string cout std printf include int main fn mut impl enum trait pub use mod async await go defer chan type map nil var let class struct self this try catch else if do end".split(' ').toSet()
        private val darkInk = intArrayOf(0xff91a09d.toInt(),0xffa9dda4.toInt(),0xffe7b381.toInt(),0xffb8acff.toInt())
        private val lightInk = intArrayOf(0xff58675e.toInt(),0xff246632.toInt(),0xff8b501b.toInt(),0xff64449b.toInt())
        private val neonDarkInk = intArrayOf(0xff768597.toInt(),0xff6ee7b7.toInt(),0xfff9a8d4.toInt(),0xff93c5fd.toInt())
        private val neonLightInk = intArrayOf(0xff637084.toInt(),0xff107353.toInt(),0xffa72f71.toInt(),0xff295ca6.toInt())
        private val monoDarkInk = intArrayOf(0xff808b96.toInt(),0xffbfcbd5.toInt(),0xffdce3e9.toInt(),0xff9aaabb.toInt())
        private val monoLightInk = intArrayOf(0xff687580.toInt(),0xff485663.toInt(),0xff303b45.toInt(),0xff566575.toInt())
    }
}

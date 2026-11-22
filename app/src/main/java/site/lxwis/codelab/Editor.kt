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
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.roundToInt

@Composable
fun CodeEditor(
    fileKey: String,
    filename: String,
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) = key(fileKey) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("editor", Context.MODE_PRIVATE) }
    var fontSize by remember { mutableStateOf(preferences.getInt("font-size", 15)) }
    var wordWrap by remember { mutableStateOf(preferences.getBoolean("word-wrap", false)) }
    var symbols by remember { mutableStateOf(preferences.getBoolean("symbol-toolbar", false)) }
    var readOnly by remember { mutableStateOf(false) }
    var view by remember { mutableStateOf<SourceView?>(null) }
    var cursor by remember { mutableStateOf("Ln 1 · Col 1") }
    var find by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var replacement by remember { mutableStateOf("") }
    var replaceMode by remember { mutableStateOf(false) }
    var matchCase by remember { mutableStateOf(false) }
    var regexMode by remember { mutableStateOf(false) }
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
    var result by remember { mutableStateOf("") }
    val changes by rememberUpdatedState(onTextChange)
    val palette = MaterialTheme.colorScheme
    val foreground = palette.onSurface.toArgb()
    val background = palette.surface.toArgb()
    val secondary = palette.onSurfaceVariant.toArgb()
    val accent = palette.primary.toArgb()
    val dark = android.graphics.Color.luminance(background) < .35f
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
            EditorIcon(Icons.Default.FormatListNumbered, "Go to line") {
                lineInput = ""; lineError = false; lineDialog = true
            }
            EditorIcon(Icons.Default.WrapText, "Toggle word wrap", selected = wordWrap) {
                wordWrap = !wordWrap
                preferences.edit().putBoolean("word-wrap", wordWrap).apply()
            }
            EditorIcon(Icons.Default.Code, "Show code symbols", selected = symbols) {
                symbols = !symbols
                preferences.edit().putBoolean("symbol-toolbar", symbols).apply()
            }
            Box {
                EditorIcon(Icons.Default.TextFields, "Font size: ${fontSize}sp") { settings = true }
                DropdownMenu(expanded = settings, onDismissRequest = { settings = false }) {
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
            EditorIcon(Icons.Default.SelectAll, "Select all code") {
                view?.let { editor -> editor.requestFocus(); editor.selectAll() }
            }
            EditorIcon(Icons.Default.Home, "Go to beginning of file") { view?.scrollToStart() }
            EditorIcon(Icons.Default.VerticalAlignBottom, "Go to end of file") {
                view?.let { editor -> editor.requestFocus(); editor.setSelection(editor.length()) }
            }
        }
        if (symbols) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically) {
                EditorAction("Tab") { view?.insertAtSelection("    ") }
                listOf("{", "}", "(", ")", "[", "]", "=", ";", ":", "'", "\"", "/", "<", ">").forEach { symbol ->
                    EditorAction(symbol) { view?.insertAtSelection(symbol) }
                }
            }
        }
        if (find) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(search, { search = it; result = "" }, Modifier.weight(1f),
                    singleLine = true, label = { Text("Find in file") })
                EditorAction("Next") { selectMatch() }
                EditorAction(if (replaceMode) "Hide" else "Edit") { replaceMode = !replaceMode }
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                FilterChip(selected = matchCase, onClick = { matchCase = !matchCase },
                    label = { Text("Aa") })
                Spacer(Modifier.width(6.dp))
                FilterChip(selected = regexMode, onClick = { regexMode = !regexMode },
                    label = { Text(".*") })
                Spacer(Modifier.width(8.dp))
                Text(if (matchCase) "Case-sensitive" else "Ignore case",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (replaceMode) Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(replacement, { replacement = it }, Modifier.weight(1f), singleLine = true, label = { Text("Replace with") })
                EditorAction("All") {
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
                EditorAction("Replace") {
                    val editor = view
                    if (editor != null && search.isNotEmpty() &&
                        searchRegex()?.matches(editor.selectedSource()) == true) {
                        editor.insertAtSelection(replacement)
                    }
                    selectMatch()
                }
            }
            if (searchError.isNotEmpty()) Text(searchError, Modifier.padding(horizontal = 12.dp),
                color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            if (result.isNotEmpty()) Text(result, Modifier.padding(horizontal = 12.dp), style = MaterialTheme.typography.labelSmall)
        }
        AndroidView(
            factory = {
                SourceView(it, filename).apply {
                    setText(text)
                    restorePosition(fileKey)
                    changed = { source -> changes(source) }
                    selectionChanged = { line, col -> cursor = "Ln " + line + " · Col " + col }
                    view = this
                }
            },
            modifier = Modifier.fillMaxWidth().weight(1f),
            update = { editor ->
                editor.appearance(fontSize, foreground, background, secondary, accent, dark)
                editor.setCodeWrap(wordWrap)
                editor.isEnabled = !readOnly
                if (editor.sourceSnapshot != text) editor.replaceSource(text)
            }
        )
        Row(Modifier.fillMaxWidth().height(28.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(cursor, style = MaterialTheme.typography.labelSmall,
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
    IconButton(onClick = onClick, modifier = Modifier.size(41.dp)
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

private class SyntaxInk(color: Int) : ForegroundColorSpan(color)

internal class SourceView(context: Context, filename: String) : EditText(context) {
    var changed: ((String) -> Unit)? = null
    var selectionChanged: ((Int, Int) -> Unit)? = null
    private var codeWrapped: Boolean? = null
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
    private var lastAppearance: List<Any> = emptyList()
    private var ready = false
    private val language = filename.substringAfterLast('.', "").lowercase()
    private val tokens = Regex(when (language) {
        in hashCommentLanguages -> "#[^\n]*|" + tokenPattern
        "lua", "sql" -> "--[^\n]*|" + tokenPattern
        else -> tokenPattern
    })
    private val highlightJob = Runnable { highlightVisible() }
    private var highlightedStart = -1
    private var highlightedEnd = -1
    private fun dp(value: Int) = (value * resources.displayMetrics.density).roundToInt()

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

    fun selectedSource(): String {
        val a = selectionStart.coerceAtLeast(0)
        val b = selectionEnd.coerceAtLeast(0)
        return text.subSequence(minOf(a,b), maxOf(a,b)).toString()
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
            val extra = if (start > 0 && text[start - 1] in "{[(:") "    " else ""
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
        if (keyCode == KeyEvent.KEYCODE_TAB && event.hasNoModifiers()) {
            insertAtSelection("    ")
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
            linePaint.color = if (line == selected) active else muted
            linePaint.alpha = if (line == selected) 255 else 170
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
        positionKey?.let { key ->
            context.getSharedPreferences("editor-positions", Context.MODE_PRIVATE).edit()
                .putInt("$key|start", selectionStart).putInt("$key|end", selectionEnd)
                .putInt("$key|x", scrollX).putInt("$key|y", scrollY).apply()
        }
        super.onDetachedFromWindow()
    }

    private fun scheduleHighlight(force: Boolean) {
        if (force) highlightedStart = -1
        removeCallbacks(highlightJob)
        postDelayed(highlightJob, 45)
    }

    private fun highlightVisible() {
        val lines = layout ?: return
        val source = text
        val first = (lines.getLineForVertical(scrollY) - 14).coerceAtLeast(0)
        val last = (lines.getLineForVertical(scrollY + height) + 14).coerceAtMost(lines.lineCount - 1)
        val start = lines.getLineStart(first)
        val end = minOf(lines.getLineEnd(last), start + 24000, source.length)
        if (start == highlightedStart && end == highlightedEnd) return
        highlightedStart = start
        highlightedEnd = end
        source.getSpans(0, source.length, SyntaxInk::class.java).forEach(source::removeSpan)
        if (language !in supportedExtensions || start >= end) return
        val snippet = source.subSequence(start, end).toString()
        val colors = if (darkMode) darkInk else lightInk
        if (language == "html" || language == "htm") {
            htmlTags.findAll(snippet).forEach { tag ->
                val value = tag.value
                if (value.startsWith("<!--")) {
                    source.setSpan(SyntaxInk(colors[0]), start + tag.range.first,
                        start + tag.range.last + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else {
                    val name = htmlTagName.find(value)
                    if (name != null) source.setSpan(SyntaxInk(colors[3]),
                        start + tag.range.first + name.range.first,
                        start + tag.range.first + name.range.last + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    htmlAttributes.findAll(value).forEach { attr ->
                        source.setSpan(SyntaxInk(colors[2]),
                            start + tag.range.first + attr.range.first,
                            start + tag.range.first + attr.range.last + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    htmlStrings.findAll(value).forEach { quoted ->
                        source.setSpan(SyntaxInk(colors[1]),
                            start + tag.range.first + quoted.range.first,
                            start + tag.range.first + quoted.range.last + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
            return
        }
        if (language == "css") {
            cssNames.findAll(snippet).forEach { property ->
                source.setSpan(SyntaxInk(colors[3]), start + property.range.first,
                    start + property.range.last + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        tokens.findAll(snippet).forEach { token ->
            val value = token.value
            val type = when {
                value.startsWith("//") || value.startsWith("/*") -> 0
                value.startsWith("#") -> if (language in hashCommentLanguages) 0 else return@forEach
                value.startsWith("--") -> if (language == "sql" || language == "lua") 0 else return@forEach
                value[0] == '"' || value[0] == '\'' || value[0] == '`' -> 1
                value[0].isDigit() -> 2
                value.lowercase() in keywords -> 3
                else -> return@forEach
            }
            source.setSpan(SyntaxInk(colors[type]), start + token.range.first, start + token.range.last + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
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
    }
}

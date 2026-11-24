package site.lxwis.codelab

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

@Composable
internal fun StudioAppearanceControls(m: WorkspaceModel) {
    var open by remember { mutableStateOf(false) }
    var section by remember { mutableStateOf("Colors") }
    val s = m.studioAppearance
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var fontTarget by remember { mutableStateOf(false) }
    var fontNotice by remember { mutableStateOf("") }
    val fontPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val editor = fontTarget
            scope.launch {
                val revision = withContext(Dispatchers.IO) { ImportedFonts.install(context, uri, editor) }
                if (revision != null) m.updateStudioAppearance(if (editor) m.studioAppearance.copy(editorFont = "Custom", editorFontRevision = revision)
                    else m.studioAppearance.copy(uiFont = "Custom", uiFontRevision = revision))
                fontNotice = if (revision != null) "Custom font installed" else "Could not load this font (maximum 5 MB)"
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Theme Studio", style = MaterialTheme.typography.titleLarge)
        StudioLivePreview()
        ThemeColor("Accent", MaterialTheme.colorScheme.primary) { m.chooseAccent(it) }
        TextButton(onClick = { open = !open }) { Text(if (open) "Hide customization" else "Customize theme") }
        if (open) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf("Colors", "Materials", "Fonts", "Syntax").forEach { choice ->
                    FilterChip(selected = choice == section, onClick = { section = choice }, label = { Text(choice) })
                }
            }
            if (section == "Colors") {
            ThemeColor("Background", MaterialTheme.colorScheme.background) {
                m.updateStudioAppearance(m.studioAppearance.copy(background = it))
            }
            ThemeColor("Panel background", MaterialTheme.colorScheme.surface) {
                m.updateStudioAppearance(m.studioAppearance.copy(surface = it))
            }
            ThemeColor("Editor background", studioColor(s.editorBackground, MaterialTheme.colorScheme.surface)) {
                m.updateStudioAppearance(m.studioAppearance.copy(editorBackground = it))
            }
            ThemeColor("Console background", studioColor(s.consoleBackground, MaterialTheme.colorScheme.surface)) {
                m.updateStudioAppearance(m.studioAppearance.copy(consoleBackground = it))
            }
            ThemeColor("Navigation and run toolbar", studioColor(s.navigationBackground, MaterialTheme.colorScheme.surfaceContainerHigh)) {
                m.updateStudioAppearance(m.studioAppearance.copy(navigationBackground = it))
            }
            ThemeColor("Borders", studioColor(s.borderColor, MaterialTheme.colorScheme.outlineVariant)) {
                m.updateStudioAppearance(m.studioAppearance.copy(borderColor = it))
            }
            ThemeColor("Editor selection", studioColor(s.selectionColor, MaterialTheme.colorScheme.primary)) {
                m.updateStudioAppearance(m.studioAppearance.copy(selectionColor = it))
            }
            ThemeColor("Text", MaterialTheme.colorScheme.onSurface) {
                m.updateStudioAppearance(m.studioAppearance.copy(foreground = it))
            }
            }
            if (section == "Materials") {
            StudioSlider("Corner radius", s.corners, 4f..32f) { m.updateStudioAppearance(m.studioAppearance.copy(corners = it)) }
            Text("Glass quality")
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Auto", "Lightweight", "High").forEach { quality ->
                    FilterChip(s.glassQuality == quality, { m.updateStudioAppearance(m.studioAppearance.copy(glassQuality = quality)) },
                        label = { Text(quality) })
                }
            }
            StudioSlider("Backdrop blur", s.blur, 0f..24f) { m.updateStudioAppearance(m.studioAppearance.copy(blur = it)) }
            StudioSlider("Glass opacity", s.opacity, 35f..100f) { m.updateStudioAppearance(m.studioAppearance.copy(opacity = it)) }
            StudioSlider("Glass border", s.border, 0f..3f) { m.updateStudioAppearance(m.studioAppearance.copy(border = it)) }
            StudioSlider("Panel shadow", s.shadow, 0f..12f) { m.updateStudioAppearance(m.studioAppearance.copy(shadow = it)) }
            }
            if (section == "Fonts") {
            Text("Typography", style = MaterialTheme.typography.titleMedium)
            Text("Interface font")
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Inter", "System", "Serif").forEach { font ->
                    FilterChip(s.uiFont == font, { m.updateStudioAppearance(m.studioAppearance.copy(uiFont = font)) }, label = { Text(font) })
                }
            }
            TextButton(onClick = { fontTarget = false; fontPicker.launch(arrayOf("*/*")) }) {
                Text(if (s.uiFont == "Custom") "Replace custom interface font" else "Import interface font")
            }
            Text("Editor font")
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("JetBrains Mono", "System Mono").forEach { font ->
                    FilterChip(s.editorFont == font, { m.updateStudioAppearance(m.studioAppearance.copy(editorFont = font)) }, label = { Text(font) })
                }
            }
            TextButton(onClick = { fontTarget = true; fontPicker.launch(arrayOf("*/*")) }) {
                Text(if (s.editorFont == "Custom") "Replace custom editor font" else "Import editor font")
            }
            if (fontNotice.isNotBlank()) Text(fontNotice, style = MaterialTheme.typography.bodySmall)
            }
            if (section == "Syntax") {
            Text("Syntax colors", style = MaterialTheme.typography.titleMedium)
            fun ink(value: String, fallback: Color) = if (value.isEmpty()) fallback else Color(android.graphics.Color.parseColor(value))
            ThemeColor("Comments", ink(s.comments, Color(0xff91a09d))) {
                m.updateStudioAppearance(m.studioAppearance.copy(comments = it))
            }
            ThemeColor("Strings", ink(s.strings, Color(0xffa9dda4))) {
                m.updateStudioAppearance(m.studioAppearance.copy(strings = it))
            }
            ThemeColor("Numbers", ink(s.numbers, Color(0xffe7b381))) {
                m.updateStudioAppearance(m.studioAppearance.copy(numbers = it))
            }
            ThemeColor("Keywords", ink(s.keywords, Color(0xffb8acff))) {
                m.updateStudioAppearance(m.studioAppearance.copy(keywords = it))
            }
            }
            TextButton(onClick = { m.updateStudioAppearance(StudioAppearance()) }) { Text("Reset theme overrides") }
        }
    }
}

@Composable
private fun StudioSlider(label: String, value: Int, range: ClosedFloatingPointRange<Float>, onChange: (Int) -> Unit) {
    Text("$label · $value", style = MaterialTheme.typography.labelMedium)
    Slider(value.toFloat(), { onChange(it.toInt()) }, valueRange = range)
}

@Composable
private fun ThemeColor(label: String, color: Color, onChange: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Surface(onClick = { open = true }, shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, Modifier.weight(1f))
            Box(Modifier.size(30.dp).background(color, RoundedCornerShape(8.dp)))
        }
    }
    if (open) {
        val argb = color.toArgb()
        var red by remember { mutableIntStateOf(android.graphics.Color.red(argb)) }
        var green by remember { mutableIntStateOf(android.graphics.Color.green(argb)) }
        var blue by remember { mutableIntStateOf(android.graphics.Color.blue(argb)) }
        val hex = String.format(java.util.Locale.ROOT, "#%02X%02X%02X", red, green, blue)
        var mode by remember { mutableStateOf("RGB") }
        val rgb = floatArrayOf(red / 255f, green / 255f, blue / 255f)
        val high = rgb.maxOrNull() ?: 0f
        val low = rgb.minOrNull() ?: 0f
        val delta = high - low
        val light = (high + low) / 2f
        val sat = if (delta == 0f) 0f else delta / (1f - abs(2f * light - 1f))
        val hue = if (delta == 0f) 0f else when (high) {
            rgb[0] -> ((rgb[1] - rgb[2]) / delta + 6f) % 6f * 60f
            rgb[1] -> ((rgb[2] - rgb[0]) / delta + 2f) * 60f
            else -> ((rgb[0] - rgb[1]) / delta + 4f) * 60f
        }
        var h by remember { mutableFloatStateOf(hue) }
        var s by remember { mutableFloatStateOf(sat) }
        var l by remember { mutableFloatStateOf(light) }
        var draft by remember { mutableStateOf(hex) }
        fun applyHsl() {
            val chroma = (1f - abs(2f * l - 1f)) * s
            val intermediate = chroma * (1f - abs((h / 60f) % 2f - 1f))
            val components = when (h.toInt() / 60) {
                0, 6 -> floatArrayOf(chroma, intermediate, 0f)
                1 -> floatArrayOf(intermediate, chroma, 0f)
                2 -> floatArrayOf(0f, chroma, intermediate)
                3 -> floatArrayOf(0f, intermediate, chroma)
                4 -> floatArrayOf(intermediate, 0f, chroma)
                else -> floatArrayOf(chroma, 0f, intermediate)
            }
            val shift = l - chroma / 2f
            red = ((components[0] + shift) * 255).roundToInt().coerceIn(0, 255)
            green = ((components[1] + shift) * 255).roundToInt().coerceIn(0, 255)
            blue = ((components[2] + shift) * 255).roundToInt().coerceIn(0, 255)
        }
        AlertDialog(onDismissRequest = { open = false }, title = { Text(label) }, text = {
            Column(Modifier.heightIn(max = 390.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val sample = Color(red, green, blue)
                Surface(color = sample, shape = MaterialTheme.shapes.large,
                    contentColor = studioReadable(MaterialTheme.colorScheme.onSurface, sample)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Aa", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.width(12.dp))
                        Text("Preview", Modifier.weight(1f))
                        Text(hex, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Text(hex, style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("RGB", "HSL", "HEX").forEach { option ->
                        FilterChip(mode == option, {
                            if (option == "HSL") { h = hue; s = sat; l = light }
                            if (option == "HEX") draft = hex
                            mode = option
                        }, label = { Text(option) })
                    }
                }
                when (mode) {
                    "HSL" -> {
                        Text("Hue · ${h.toInt()}°")
                        Slider(h, { h = it; applyHsl() }, valueRange = 0f..360f)
                        Text("Saturation · ${(s * 100).toInt()}%")
                        Slider(s, { s = it; applyHsl() })
                        Text("Lightness · ${(l * 100).toInt()}%")
                        Slider(l, { l = it; applyHsl() })
                    }
                    "HEX" -> OutlinedTextField(draft, { value ->
                        draft = value.take(7)
                        if (draft.matches(Regex("#[0-9a-fA-F]{6}"))) {
                            val parsed = android.graphics.Color.parseColor(draft)
                            red = android.graphics.Color.red(parsed)
                            green = android.graphics.Color.green(parsed)
                            blue = android.graphics.Color.blue(parsed)
                        }
                    }, singleLine = true, label = { Text("#RRGGBB") }, isError = !draft.matches(Regex("#[0-9a-fA-F]{6}")))
                    else -> {
                        StudioSlider("Red", red, 0f..255f) { red = it }
                        StudioSlider("Green", green, 0f..255f) { green = it }
                        StudioSlider("Blue", blue, 0f..255f) { blue = it }
                    }
                }
            }
        }, confirmButton = { TextButton(enabled = mode != "HEX" || draft.matches(Regex("#[0-9a-fA-F]{6}")), onClick = { onChange(hex); open = false }) { Text("Apply") } },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancel") } })
    }
}

@Composable
private fun StudioLivePreview() {
    val s = LocalStudioAppearance.current
    val c = MaterialTheme.colorScheme
    val editor = studioColor(s.editorBackground, c.surface)
    val console = studioColor(s.consoleBackground, c.surface)
    val nav = studioColor(s.navigationBackground, c.surfaceContainerHigh)
    val ink = studioReadable(c.onSurface, editor)
    ClayPanel {
        Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Live workspace preview", style = MaterialTheme.typography.labelMedium)
            Surface(color = nav, contentColor = studioReadable(c.onSurface, nav), shape = MaterialTheme.shapes.medium) {
                Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    FileTypeIcon("main.js", Modifier.size(18.dp))
                    Text("main.js", Modifier.weight(1f).padding(start = 6.dp))
                    Surface(color = c.primaryContainer, shape = MaterialTheme.shapes.small) {
                        Text("Run", Modifier.padding(horizontal = 12.dp, vertical = 5.dp), color = c.onPrimaryContainer)
                    }
                }
            }
            Surface(color = editor, contentColor = ink, shape = MaterialTheme.shapes.medium) {
                Column(Modifier.fillMaxWidth().padding(10.dp)) {
                    val font = androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(R.font.jetbrains_mono))
                    Text("1  // Main program", fontFamily = font, color = studioColor(s.comments, c.onSurfaceVariant))
                    Row {
                        Text("2  const ", fontFamily = font, color = studioColor(s.keywords, c.primary))
                        Text("answer = ", fontFamily = font)
                        Text("42", fontFamily = font, color = studioColor(s.numbers, c.primary))
                        Text(";", fontFamily = font)
                    }
                    Text("3  print(\"Hello\");", fontFamily = font, color = studioColor(s.strings, ink))
                }
            }
            Surface(color = console, contentColor = studioReadable(c.onSurface, console), shape = MaterialTheme.shapes.medium) {
                Column(Modifier.fillMaxWidth().padding(10.dp)) {
                    Text("Console", style = MaterialTheme.typography.labelMedium)
                    Text("Hello", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    Text("Exit 0", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

package site.lxwis.codelab

import android.content.Context
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

@Composable
internal fun AppearanceSettings(m: WorkspaceModel) {
    SettingChoices("Color mode", appAppearanceModes, m.appearance, m::setMode)
    FontChoices(m, editor = false)
    SettingToggle("Liquid Glass", m.glassEnabled, m::chooseGlass)
    SettingToggle("Progressive blur", m.progressiveBlur, m::chooseProgressiveBlur)
    SettingToggle("Reduce dynamic blur", m.reduceDynamicBlur, m::chooseReduceDynamicBlur)
}

@Composable
internal fun EditorAppearanceControls(m: WorkspaceModel) {
    val context = LocalContext.current
    val settings = m.editorAppearance
    val preferences = remember(context) { context.getSharedPreferences("editor", Context.MODE_PRIVATE) }
    val size = settings.editorTextSize.takeIf { it in 10..28 } ?: preferences.getInt("font-size", 15)
    FontChoices(m, editor = true)
    SettingChoices("Code surface", appAppearanceModes, settings.editorMode) {
        m.updateEditorAppearance(m.editorAppearance.copy(editorMode = it))
    }
    Text("Text size · ${size} sp", style = MaterialTheme.typography.titleSmall)
    Slider(size.toFloat(), { m.updateEditorAppearance(m.editorAppearance.copy(editorTextSize = it.toInt())) },
        valueRange = 10f..28f, steps = 17,
        modifier = Modifier.semantics { contentDescription = "Editor text size" })
    var syntaxPalette by remember { mutableStateOf(preferences.getString("syntax-palette", "Classic") ?: "Classic") }
    SettingChoices("Syntax theme", editorSyntaxThemes, syntaxPalette) {
        syntaxPalette = it
        preferences.edit().putString("syntax-palette", it).apply()
    }
    var showColors by rememberSaveable { mutableStateOf(false) }
    TextButton(onClick = { showColors = !showColors }) {
        Text(if (showColors) "Hide syntax colors" else "Customize syntax colors")
    }
    if (showColors) {
        val fallback = MaterialTheme.colorScheme.onSurface
        ThemeColor("Comments", studioColor(settings.comments, fallback)) {
            m.updateEditorAppearance(m.editorAppearance.copy(comments = it))
        }
        ThemeColor("Strings", studioColor(settings.strings, fallback)) {
            m.updateEditorAppearance(m.editorAppearance.copy(strings = it))
        }
        ThemeColor("Numbers", studioColor(settings.numbers, fallback)) {
            m.updateEditorAppearance(m.editorAppearance.copy(numbers = it))
        }
        ThemeColor("Keywords", studioColor(settings.keywords, fallback)) {
            m.updateEditorAppearance(m.editorAppearance.copy(keywords = it))
        }
        ThemeColor("Functions", studioColor(settings.functions, fallback)) {
            m.updateEditorAppearance(m.editorAppearance.copy(functions = it))
        }
        ThemeColor("Types", studioColor(settings.types, fallback)) {
            m.updateEditorAppearance(m.editorAppearance.copy(types = it))
        }
        ThemeColor("Properties", studioColor(settings.properties, fallback)) {
            m.updateEditorAppearance(m.editorAppearance.copy(properties = it))
        }
        TextButton(onClick = {
            m.updateEditorAppearance(m.editorAppearance.copy(comments = "", strings = "", numbers = "",
                keywords = "", functions = "", types = "", properties = ""))
        }) { Text("Reset syntax colors") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingChoices(title: String, choices: List<String>, selected: String, select: (String) -> Unit) {
    Text(title, style = MaterialTheme.typography.titleSmall)
    if (choices.size <= 3) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            choices.forEachIndexed { index, choice ->
                SegmentedButton(choice == selected, { select(choice) },
                    SegmentedButtonDefaults.itemShape(index, choices.size), icon = {}) { Text(choice) }
            }
        }
    } else {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            choices.forEach { choice ->
                FilterChip(choice == selected, { select(choice) }, label = { Text(choice) },
                    modifier = Modifier.heightIn(min = 48.dp))
            }
        }
    }
}

@Composable
private fun FontChoices(m: WorkspaceModel, editor: Boolean) {
    val setting = m.editorAppearance
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var notice by rememberSaveable { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val revision = withContext(Dispatchers.IO) { ImportedFonts.install(context, uri, editor) }
            if (revision != null) {
                val current = m.editorAppearance
                m.updateEditorAppearance(if (editor) current.copy(editorFont = "Custom", editorFontRevision = revision)
                    else current.copy(uiFont = "Custom", uiFontRevision = revision))
                notice = ""
            } else notice = "Could not load this font. Choose a valid TTF or OTF file."
        }
    }
    val choices = if (editor) listOf("JetBrains Mono", "System Mono") else listOf("Inter", "System", "Serif")
    val current = if (editor) setting.editorFont else setting.uiFont
    val customAvailable = if (editor) setting.editorFontRevision != 0L else setting.uiFontRevision != 0L
    SettingChoices(if (editor) "Editor font" else "Interface font",
        if (customAvailable) choices + "Custom" else choices, current) { font ->
        val latest = m.editorAppearance
        m.updateEditorAppearance(if (editor) latest.copy(editorFont = font) else latest.copy(uiFont = font))
    }
    if (!editor) {
        Text("Interface text size · ${setting.uiScale}%", style = MaterialTheme.typography.titleSmall)
        Slider(setting.uiScale.toFloat(), {
            m.updateEditorAppearance(m.editorAppearance.copy(uiScale = it.toInt()))
        }, valueRange = 80f..140f, modifier = Modifier.semantics { contentDescription = "Interface text size" })
    }
    TextButton(onClick = { picker.launch(arrayOf("*/*")) }) { Text("Import font") }
    if (notice.isNotEmpty()) Text(notice, color = MaterialTheme.colorScheme.error)
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
        CodeLabAlertDialog(onDismissRequest = { open = false }, title = { Text(label) }, text = {
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
                        ColorSlider("Red", red, 0f..255f) { red = it }
                        ColorSlider("Green", green, 0f..255f) { green = it }
                        ColorSlider("Blue", blue, 0f..255f) { blue = it }
                    }
                }
            }
        }, confirmButton = { TextButton(enabled = mode != "HEX" || draft.matches(Regex("#[0-9a-fA-F]{6}")), onClick = { onChange(hex); open = false }) { Text("Apply") } },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancel") } })
    }
}


@Composable
private fun ColorSlider(label: String, value: Int, range: ClosedFloatingPointRange<Float>, onChange: (Int) -> Unit) {
    Text("$label · $value", style = MaterialTheme.typography.labelMedium)
    Slider(value.toFloat(), { onChange(it.toInt()) }, valueRange = range,
        modifier = Modifier.semantics { contentDescription = label })
}

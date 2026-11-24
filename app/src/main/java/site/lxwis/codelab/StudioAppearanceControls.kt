package site.lxwis.codelab

import androidx.compose.foundation.background
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
    val s = m.studioAppearance
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Theme Studio", style = MaterialTheme.typography.titleLarge)
        ThemeColor("Accent", MaterialTheme.colorScheme.primary) { m.chooseAccent(it) }
        TextButton(onClick = { open = !open }) { Text(if (open) "Hide material controls" else "Customize materials") }
        if (open) {
            ThemeColor("Background", MaterialTheme.colorScheme.background) {
                m.updateStudioAppearance(m.studioAppearance.copy(background = it))
            }
            ThemeColor("Panel and editor background", MaterialTheme.colorScheme.surface) {
                m.updateStudioAppearance(m.studioAppearance.copy(surface = it))
            }
            ThemeColor("Text", MaterialTheme.colorScheme.onSurface) {
                m.updateStudioAppearance(m.studioAppearance.copy(foreground = it))
            }
            StudioSlider("Corner radius", s.corners, 4f..32f) { m.updateStudioAppearance(m.studioAppearance.copy(corners = it)) }
            StudioSlider("Glass opacity", s.opacity, 35f..100f) { m.updateStudioAppearance(m.studioAppearance.copy(opacity = it)) }
            StudioSlider("Glass border", s.border, 0f..3f) { m.updateStudioAppearance(m.studioAppearance.copy(border = it)) }
            StudioSlider("Panel shadow", s.shadow, 0f..12f) { m.updateStudioAppearance(m.studioAppearance.copy(shadow = it)) }
            TextButton(onClick = { m.updateStudioAppearance(StudioAppearance()) }) { Text("Reset material overrides") }
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
        AlertDialog(onDismissRequest = { open = false }, title = { Text(label) }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.fillMaxWidth().height(44.dp).background(Color(red, green, blue), RoundedCornerShape(12.dp)))
                Text(hex, style = MaterialTheme.typography.titleMedium)
                StudioSlider("Red", red, 0f..255f) { red = it }
                StudioSlider("Green", green, 0f..255f) { green = it }
                StudioSlider("Blue", blue, 0f..255f) { blue = it }
            }
        }, confirmButton = { TextButton(onClick = { onChange(hex); open = false }) { Text("Apply") } },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancel") } })
    }
}

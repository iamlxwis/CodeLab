package site.lxwis.codelab

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeScreenshotStudio(filename: String, source: String, close: () -> Unit) {
    val context = LocalContext.current
    var preset by rememberSaveable { mutableStateOf("Obsidian") }
    var aspect by rememberSaveable { mutableStateOf(ScreenshotAspect.SQUARE) }
    var background by rememberSaveable { mutableStateOf("Gradient") }
    var lineNumbers by rememberSaveable { mutableStateOf(true) }
    var start by rememberSaveable { mutableStateOf("1") }
    var count by rememberSaveable { mutableFloatStateOf(18f) }
    var title by rememberSaveable { mutableStateOf("") }
    var footer by rememberSaveable { mutableStateOf("") }
    var highlight by rememberSaveable { mutableStateOf("") }
    val firstLine = (start.toIntOrNull() ?: 1).coerceIn(1, 100000)
    val options = ScreenshotOptions(
        palette = preset, aspect = aspect, background = background,
        lineNumbers = lineNumbers, firstLine = firstLine,
        selectedLines = count.toInt(), title = title, footer = footer,
        highlightLine = highlight.toIntOrNull()?.coerceAtLeast(0) ?: 0)
    val preview = remember(filename, source, options) {
        ScreenshotRenderer.render(filename, source, options)
    }
    val export = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        if (uri != null) {
            val saved = runCatching {
                checkNotNull(context.contentResolver.openOutputStream(uri, "w")).use { stream ->
                    check(preview.compress(Bitmap.CompressFormat.PNG, 100, stream))
                }
            }.isSuccess
            Toast.makeText(context, if (saved) "PNG saved" else "Unable to save PNG",
                Toast.LENGTH_SHORT).show()
        }
    }
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = close, sheetState = sheet,
        containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.94f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Code Screenshot Studio", style = MaterialTheme.typography.headlineSmall)
            Text("Syntax-colored code cards, rendered locally without internet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Theme", style = MaterialTheme.typography.labelMedium)
            Row(Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("Obsidian", "Nocturne", "Paper").forEach { value ->
                    FilterChip(selected = preset == value, onClick = { preset = value },
                        label = { Text(value) })
                }
            }
            Text("Aspect ratio", style = MaterialTheme.typography.labelMedium)
            Row(Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                ScreenshotAspect.values().forEach { value ->
                    FilterChip(selected = aspect == value, onClick = { aspect = value },
                        label = { Text(value.label) })
                }
            }
            Text("Background", style = MaterialTheme.typography.labelMedium)
            Row(Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("Gradient", "Solid", "Transparent").forEach { value ->
                    FilterChip(selected = background == value,
                        onClick = { background = value }, label = { Text(value) })
                }
            }
            OutlinedTextField(title, { title = it.take(55) }, Modifier.fillMaxWidth(),
                singleLine = true, label = { Text("Card title (optional)") })
            OutlinedTextField(footer, { footer = it.take(62) }, Modifier.fillMaxWidth(),
                singleLine = true, label = { Text("Footer (optional)") })
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Line numbers", Modifier.weight(1f))
                Switch(checked = lineNumbers, onCheckedChange = { lineNumbers = it })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(start, { start = it.filter(Char::isDigit).take(6) },
                    modifier = Modifier.weight(1f), singleLine = true,
                    label = { Text("Start line") })
                OutlinedTextField(highlight, { highlight = it.filter(Char::isDigit).take(6) },
                    modifier = Modifier.weight(1f), singleLine = true,
                    label = { Text("Highlight line") })
            }
            Text("Requested lines · " + count.toInt() +
                " · " + ScreenshotRenderer.lineCapacity(aspect) + " fit in this layout",
                style = MaterialTheme.typography.labelSmall)
            Slider(value = count, onValueChange = { count = it }, valueRange = 1f..40f)
            Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 1.dp) {
                Image(preview.asImageBitmap(), contentDescription = "Code screenshot preview",
                    modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth)
            }
            Button(onClick = {
                export.launch(filename.substringAfterLast('/') + "-" +
                    aspect.name.lowercase() + ".png")
            }, modifier = Modifier.fillMaxWidth()) { Text("Export PNG") }
            Text("1080 px wide · PNG alpha supported · all rendering stays on-device",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
        }
    }
}

package site.lxwis.codelab

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Color as AColor
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
import kotlin.math.min

/**
 * Deterministic raster export: no external service, permission, or network call.
 * Limits are deliberate to cap bitmap memory on inexpensive Android devices.
 */
internal object ScreenshotRenderer {
    fun render(filename: String, source: String, style: String, lineNumbers: Boolean,
               firstLine: Int, visibleLines: Int): Bitmap {
        val palette = when (style) {
            "Paper" -> intArrayOf(0xfff6f7f4.toInt(), 0xff1c2826.toInt(),
                0xff6d7b78.toInt(), 0xffdbe2dc.toInt(), 0xff276d57.toInt())
            "Nocturne" -> intArrayOf(0xff111626.toInt(), 0xffe6e9f6.toInt(),
                0xff8792ad.toInt(), 0xff202942.toInt(), 0xff9ab5fa.toInt())
            else -> intArrayOf(0xff141a1d.toInt(), 0xffe2ede7.toInt(),
                0xff82998d.toInt(), 0xff28352f.toInt(), 0xff8fd5b2.toInt())
        }
        val data = source.lineSequence().drop((firstLine - 1).coerceAtLeast(0))
            .take(visibleLines.coerceIn(1, 55))
            .map { it.replace('\t', ' ').take(100) }.toList()
        val entries = if (data.isEmpty()) listOf("") else data
        val width = 1080
        val padding = 54f
        val lineHeight = 42f
        val contentTop = 160f
        val height = (contentTop + entries.size * lineHeight + 52).toInt().coerceAtMost(2600)
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        canvas.drawColor(palette[0])
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        paint.color = palette[3]
        canvas.drawRoundRect(32f, 26f, width - 32f, 119f, 18f, 18f, paint)
        paint.color = palette[4]
        paint.textSize = 23f
        canvas.drawCircle(66f, 71f, 8f, paint)
        canvas.drawCircle(94f, 71f, 8f, paint)
        canvas.drawCircle(122f, 71f, 8f, paint)
        paint.color = palette[1]
        paint.textSize = 27f
        val title = filename.take(48)
        canvas.drawText(title, 162f, 81f, paint)
        paint.textSize = 28f
        for ((position, raw) in entries.withIndex()) {
            val baseline = contentTop + position * lineHeight
            val number = firstLine + position
            if (lineNumbers) {
                paint.color = palette[2]
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(number.toString(), 116f, baseline, paint)
            }
            paint.textAlign = Paint.Align.LEFT
            paint.color = palette[1]
            val column = if (lineNumbers) 145f else padding
            val fit = paint.breakText(raw, true, width - column - padding, null)
            canvas.drawText(raw.substring(0, min(raw.length, fit)), column, baseline, paint)
        }
        return image
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeScreenshotStudio(filename: String, source: String, close: () -> Unit) {
    val context = LocalContext.current
    var preset by rememberSaveable { mutableStateOf("Obsidian") }
    var lineNumbers by rememberSaveable { mutableStateOf(true) }
    var start by rememberSaveable { mutableStateOf("1") }
    var count by rememberSaveable { mutableFloatStateOf(22f) }
    val firstLine = (start.toIntOrNull() ?: 1).coerceIn(1, 100000)
    val output = remember(filename, source, preset, lineNumbers, firstLine, count) {
        ScreenshotRenderer.render(filename, source, preset, lineNumbers, firstLine, count.toInt())
    }
    val export = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        if (uri != null) {
            val successful = runCatching {
                checkNotNull(context.contentResolver.openOutputStream(uri, "w")).use { stream ->
                    check(output.compress(Bitmap.CompressFormat.PNG, 100, stream))
                }
            }.isSuccess
            Toast.makeText(context, if (successful) "PNG saved" else "Unable to save PNG",
                Toast.LENGTH_SHORT).show()
        }
    }
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = close, sheetState = state,
        containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.94f).verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Code Screenshot Studio", style = MaterialTheme.typography.headlineSmall)
            Text("Create a PNG locally from the selected source file.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("Obsidian", "Nocturne", "Paper").forEach { option ->
                    FilterChip(selected = preset == option, onClick = { preset = option },
                        label = { Text(option) })
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Line numbers", Modifier.weight(1f))
                Switch(checked = lineNumbers, onCheckedChange = { lineNumbers = it })
            }
            OutlinedTextField(start, { start = it.filter(Char::isDigit).take(6) },
                modifier = Modifier.fillMaxWidth(), label = { Text("Start at line") },
                singleLine = true)
            Text("Lines in image · " + count.toInt(), style = MaterialTheme.typography.labelMedium)
            Slider(value = count, onValueChange = { count = it }, valueRange = 5f..55f)
            Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 1.dp) {
                Image(output.asImageBitmap(), contentDescription = "Code screenshot preview",
                    modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth)
            }
            Button(onClick = { export.launch(filename.substringAfterLast('/') + ".png") },
                modifier = Modifier.fillMaxWidth()) { Text("Save PNG") }
            Text("1080 px wide · no internet required",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
        }
    }
}

package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
internal fun PerformancePanel(m: WorkspaceModel, close: () -> Unit) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(PerformanceRecorder.enabled(context)) }
    var sample by remember { mutableStateOf(PerformanceRecorder.snapshot()) }
    fun ms(value: Double?) = value?.let { String.format(Locale.getDefault(), "%.1f ms", it) } ?: "Not sampled"
    CodeLabAlertDialog(onDismissRequest = close, title = { Text("Performance") }, text = {
        Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Record local performance", Modifier.weight(1f))
                Switch(enabled, { enabled = it; PerformanceRecorder.setEnabled(context, it); sample = PerformanceRecorder.snapshot() },
                    modifier = Modifier.semantics { contentDescription = "Record local performance" })
            }
            Text("Records only while the app is visible. Measurements stay in memory on this device; code and file contents are never recorded.", style = MaterialTheme.typography.bodySmall)
            Text("Activity to first draw · ${ms(sample.firstDrawMs)}")
            Text("Edit callback to next frame · ${ms(sample.editFrameMs)}")
            Text("Action to next frame · ${ms(sample.actionFrameMs)}")
            Text("Main-window frames · ${sample.frames}")
            Text("Frames over display refresh budget · ${sample.overBudget}")
            Text("Frame duration median · ${ms(sample.medianMs)}")
            Text("Frame duration 95th percentile · ${ms(sample.p95Ms)}")
            Text("Retained frame sample · ${sample.sampleCount} / 512")
            Text(String.format(Locale.getDefault(), "Editor process memory · %.1f MB", sample.editorMemoryMb))
            Text("Last workspace I/O · ${m.lastOperationName} · ${m.lastOperationMillis} ms")
            Text("Last save · ${m.lastSaveMillis} ms")
            Text("Enable recording before reopening the app to capture its first draw. Samples describe this editor process and main window; they do not certify device performance.", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { sample = PerformanceRecorder.snapshot() }) { Text("Refresh") }
                TextButton(onClick = { PerformanceRecorder.clear(); sample = PerformanceRecorder.snapshot() }) { Text("Clear samples") }
            }
        }
    }, confirmButton = { TextButton(onClick = close) { Text("Done") } })
}

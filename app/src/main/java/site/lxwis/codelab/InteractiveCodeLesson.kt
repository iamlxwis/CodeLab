package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** Real editor and existing isolated runtime; no project creation, network or synthetic output. */
@Composable
internal fun InteractiveCodeLesson(close: () -> Unit) {
    val context = LocalContext.current
    val runner = remember(context) { RunSession(context) }
    var code by rememberSaveable { mutableStateOf("const name = \"Android\";\nprint(\"Hello, \" + name + \"!\");") }
    var executedSource by rememberSaveable { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf("") }
    val complete = executedSource == code && runner.status == "Exit 0" &&
        runner.transcript.lineSequence().any { it.trim() == "Hello, CodeLab!" }
    DisposableEffect(runner) { onDispose { runner.close() } }
    fun run() {
        if (!runner.running) {
            executedSource = code
            notice = ""
            runner.run("practice.js", code)
        }
    }
    Dialog(onDismissRequest = close, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = close) { Icon(Icons.Default.ArrowBack, "Return to guide") }
                    Text("Practice editing", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { if (runner.running) runner.stop() else run() }) {
                        Icon(if (runner.running) Icons.Default.Stop else Icons.Default.PlayArrow,
                            if (runner.running) "Stop lesson" else "Run lesson offline")
                    }
                }
                Text("Change Android to CodeLab in the first line, then run the code offline.",
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.bodyMedium)
                CodeEditor(fileKey = "tutorial/practice.js", filename = "practice.js", text = code,
                    onTextChange = { code = it; notice = "" },
                    onRun = ::run, onSave = { notice = "Practice draft retained in this lesson" },
                    workspaceToolbar = listOf("undo", "redo", "wrap"),
                    modifier = Modifier.weight(1f).fillMaxWidth())
                HorizontalDivider()
                Column(Modifier.fillMaxWidth().heightIn(max = 170.dp)
                    .verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (complete) "Lesson complete" else runner.status,
                        Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        style = MaterialTheme.typography.titleSmall)
                    if (runner.transcript.isNotBlank()) Text(runner.transcript.takeLast(4000), fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall)
                    if (runner.waiting) Text("This lesson uses print output. Stop the run to return to editing.", style = MaterialTheme.typography.bodySmall)
                    if (notice.isNotBlank()) Text(notice, style = MaterialTheme.typography.bodySmall)
                    Text("Practice is separate from your projects. No source is uploaded.",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = close) { Text(if (complete) "Back to guide" else "Skip practice") }
                }
            }
        }
    }
}

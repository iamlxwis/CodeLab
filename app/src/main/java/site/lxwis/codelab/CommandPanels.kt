package site.lxwis.codelab

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.spring
import android.provider.Settings
import android.view.View
import android.view.WindowInsets as PlatformWindowInsets
import android.os.Build
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.ViewModelProvider

@Composable
internal fun CommandPaletteDialog(m: WorkspaceModel, dismiss: () -> Unit,
    execute: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val actions = listOf(
        "newproject" to "Create project",
        "newfile" to "Create file",
        "newfolder" to "Create folder",
        "files" to "Browse files",
        "projectsearch" to "Search project",
        "projectreplace" to "Replace across project",
        "screenshot" to "Code Screenshot Studio",
        "history" to "File history",
        "git" to "Git version control",
        "devtools" to "Developer tools",
        "languages" to "Language manager",
        "appearance" to "Appearance",
        "accessibility" to "Accessibility",
        "performance" to "Performance diagnostics",
        "panels" to "Panel layout"
    )
    AlertDialog(onDismissRequest = dismiss,
        title = { Text("Command palette") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(query, { query = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    placeholder = { Text("Search actions") })
                LazyColumn(Modifier.heightIn(max = 340.dp)) {
                    items(actions.filter { it.second.contains(query, ignoreCase = true) }, key = { it.first }) { item ->
                        val requiresFile = item.first in setOf("screenshot", "history")
                        val requiresProject = item.first in setOf("newfile", "newfolder", "files",
                            "projectsearch", "projectreplace", "screenshot", "history", "git", "panels")
                        TextButton(enabled = (!requiresProject || m.project != null) &&
                            (!requiresFile || m.active != null),
                            modifier = Modifier.fillMaxWidth(), onClick = { execute(item.first) }) {
                            Text(item.second, modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start)
                        }
                    }
                }
                Text("Tip: Ctrl+Shift+P with a hardware keyboard",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { TextButton(onClick = dismiss) { Text("Close") } })
}


@Composable
internal fun JavaScriptDebugDialog(m: WorkspaceModel, dismiss: () -> Unit) {
    var breakpointText by rememberSaveable { mutableStateOf("") }
    val parts = breakpointText.split(',', ' ', ';', '\n')
        .filter { it.isNotBlank() }
    val lines = parts.mapNotNull(String::toIntOrNull)
    val valid = parts.size == lines.size && lines.all { it in 1..100000 } && lines.size <= 80
    AlertDialog(onDismissRequest = dismiss, title = { Text("JavaScript debugger") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Execution pauses at the first statement. Use Step or Continue in the console.",
                    style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(breakpointText, { breakpointText = it.take(600) },
                    label = { Text("Optional breakpoint line numbers") },
                    placeholder = { Text("4, 8, 15") },
                    modifier = Modifier.fillMaxWidth(), maxLines = 3)
                Text("Debugger: Mozilla Rhino, offline. Applies to the active .js file. " +
                    "JavaScript may continue until the timeout limit while paused.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!valid) Text("Enter up to 80 positive line numbers.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error)
            }
        }, confirmButton = {
            TextButton(enabled = valid && !m.runner.running,
                onClick = {
                    m.debugCurrent(lines)
                    dismiss()
                }) { Text("Start debugging") }
        }, dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } })
}

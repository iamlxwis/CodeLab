package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Single editor implementation shared by wide side-by-side and phone stacked layouts.
 * Avoid mounting two native editor views when switching layouts.
 */
@Composable
internal fun WorkspaceEditorPane(
    m: WorkspaceModel,
    project: String,
    modifier: Modifier = Modifier,
    onFiles: () -> Unit,
    onScreenshot: () -> Unit,
    onCommands: () -> Unit,
    onSelection: (String, Int) -> Unit
) {
    Box(modifier) {
        val file = m.active
        if (file != null) key(project, file) {
            CodeEditor(
                fileKey = "$project/$file",
                filename = file,
                text = m.source,
                onTextChange = m::edit,
                jumpLine = m.pendingLine,
                onJumpHandled = m::consumeJump,
                onSave = m::flush,
                onRun = m::run,
                onShare = onScreenshot,
                onPreviousTab = { m.moveActiveTab(-1) },
                onNextTab = { m.moveActiveTab(1) },
                onOpenCommands = onCommands,
                onSelectionChange = onSelection,
                modifier = Modifier.fillMaxSize()
            )
        } else Column(Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally) {
            TextButton(onClick = onFiles) { Text("Open a file") }
            Text("Select a file from Explorer or create one.",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
    }
}

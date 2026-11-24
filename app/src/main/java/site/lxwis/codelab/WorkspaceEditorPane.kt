package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    val motion = rememberCodeLabMotion(m.motionPreference, m.motionIntensity, m.motionSpeed, m.springIntensity)
    val file = m.active
    val alpha = remember { Animatable(1f) }
    LaunchedEffect(file, project) {
        if (motion.millis(160) > 0) { alpha.snapTo(.75f); alpha.animateTo(1f, tween(motion.millis(160))) }
        else alpha.snapTo(1f)
    }
    Box(modifier.graphicsLayer { this.alpha = alpha.value }) {
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
                showStatusBar = m.showEditorStatus,
                motionMillis = motion.millis(170),
                workspaceToolbar = m.editorToolbarIds,
                onWorkspaceToolbarChange = m::chooseEditorToolbar,
                modifier = Modifier.fillMaxSize()
            )
        } else if (m.busy) CircularProgressIndicator(Modifier.align(Alignment.Center).size(24.dp), strokeWidth = 2.dp)
        else Column(Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally) {
            TextButton(onClick = onFiles) { Text("Open a file") }
            Text("Select a file from Explorer or create one.",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
    }
}

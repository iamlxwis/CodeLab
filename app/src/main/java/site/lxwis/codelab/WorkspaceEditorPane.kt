package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
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
    onCaptureReader: ((() -> EditorCapture?)?) -> Unit
) {
    val motion = rememberCodeLabMotion(m.motionPreference, m.motionIntensity, m.motionSpeed, m.springIntensity, m.transitionEffect)
    val file = m.active
    val alpha = remember { Animatable(1f) }
    val transitionMillis = if (m.transitionEffect == "None") 0 else motion.millis(160)
    val slideDistance = with(LocalDensity.current) { 16.dp.toPx() }
    LaunchedEffect(file, project, transitionMillis, m.transitionEffect) {
        if (transitionMillis > 0) { alpha.snapTo(0f); alpha.animateTo(1f, tween(transitionMillis)) }
        else alpha.snapTo(1f)
    }
    Box(modifier.graphicsLayer {
        this.alpha = .75f + .25f * alpha.value
        translationX = if (m.transitionEffect == "Slide") (1f - alpha.value) * slideDistance else 0f
    }) {
        if (file != null) key(project, file) {
            CodeEditor(
                fileKey = "$project/$file",
                filename = file,
                text = m.source,
                readCurrentText = { m.source },
                onTextChange = m::edit,
                jumpLine = m.pendingLine,
                onJumpHandled = m::consumeJump,
                onSave = m::flush,
                onRun = m::run,
                onShare = onScreenshot,
                onPreviousTab = { m.moveActiveTab(-1) },
                onNextTab = { m.moveActiveTab(1) },
                onOpenCommands = onCommands,
                onCaptureReader = onCaptureReader,
                showStatusBar = m.showEditorStatus,
                motionMillis = motion.millis(170),
                touchFeedback = m.touchFeedback,
                workspaceToolbar = m.editorToolbarIds,
                onWorkspaceToolbarChange = m::chooseEditorToolbar,
                modifier = Modifier.fillMaxSize()
            )
        } else if (m.busy) CircularProgressIndicator(Modifier.align(Alignment.Center).size(24.dp), strokeWidth = 2.dp)
        else ReimaginedEmptyState(Icons.Default.Code, "No file open",
            "Select a file from Explorer or create one.", Modifier.align(Alignment.Center),
            action = "Open a file", onAction = onFiles)
    }
}

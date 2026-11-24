package site.lxwis.codelab

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp

/** Mounted only during real loading; reduced motion uses a stationary placeholder. */
@Composable
internal fun WorkspaceSkeleton(rows: Int = 4) {
    val colors = MaterialTheme.colorScheme
    val motion = workspaceMotion()
    val alpha = if (motion.millis(700) > 0) {
        val transition = rememberInfiniteTransition(label = "loading-placeholder")
        val value by transition.animateFloat(.45f, .85f,
            infiniteRepeatable(tween(motion.millis(700)), RepeatMode.Reverse), label = "loading-pulse")
        value
    } else 1f
    Column(Modifier.fillMaxWidth().padding(12.dp).clearAndSetSemantics {
        contentDescription = "Loading content"
        progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
    }.graphicsLayer { this.alpha = alpha }, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(rows) { i ->
            Row(Modifier.fillMaxWidth().background(colors.surfaceContainer, MaterialTheme.shapes.medium)
                .padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(30.dp).background(colors.surfaceContainerHighest, MaterialTheme.shapes.small))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.fillMaxWidth(if (i % 2 == 0) .65f else .45f).height(12.dp)
                        .background(colors.surfaceContainerHighest, MaterialTheme.shapes.small))
                    Box(Modifier.fillMaxWidth(.32f).height(8.dp)
                        .background(colors.surfaceContainerHighest, MaterialTheme.shapes.small))
                }
            }
        }
    }
}

package site.lxwis.codelab

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

internal fun clampPhoneSplit(percent: Int): Int = percent.coerceIn(35, 80)

/** Predictable snap positions help avoid cramped phone editor/console regions. */
internal fun snapPhoneSplit(percent: Int): Int =
    listOf(40, 60, 75).minBy { abs(it - clampPhoneSplit(percent)) }

@Composable
internal fun PhoneSplitToggle(enabled: Boolean, onToggle: () -> Unit) {
    FilterChip(
        selected = enabled,
        onClick = onToggle,
        label = { Text("Split") },
        leadingIcon = { Icon(Icons.Default.VerticalSplit, contentDescription = null) }
    )
}

/**
 * 48dp drag target with a visible handle. No animations: reduced-motion mode is
 * always respected. A settings slider is provided for precision/accessibility.
 */
@Composable
internal fun PhonePanelDivider(
    percent: Int,
    availableHeightPx: Float,
    onPercentChange: (Int) -> Unit
) {
    var dragPosition by remember { mutableFloatStateOf(percent.toFloat()) }
    val currentPercent by rememberUpdatedState(percent)
    val currentCallback by rememberUpdatedState(onPercentChange)
    val safeHeight = availableHeightPx.coerceAtLeast(1f)
    Box(
        Modifier.fillMaxWidth().height(48.dp)
            .pointerInput(safeHeight) {
                detectVerticalDragGestures(
                    onDragStart = { dragPosition = currentPercent.toFloat() },
                    onDragEnd = { currentCallback(snapPhoneSplit(dragPosition.roundToInt())) },
                    onVerticalDrag = { change, dy ->
                        change.consume()
                        dragPosition = (dragPosition + dy / safeHeight * 100f).coerceIn(35f, 80f)
                        currentCallback(dragPosition.roundToInt())
                    }
                )
            }
            .semantics {
                contentDescription = "Resize split panels"
                stateDescription = "Editor $percent percent"
            },
        contentAlignment = Alignment.Center
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Icon(Icons.Default.DragHandle, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

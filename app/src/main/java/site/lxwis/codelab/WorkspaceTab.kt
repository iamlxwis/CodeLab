package site.lxwis.codelab

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/** Long-press actions and a drag handle supplement keyboard-accessible move actions. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun WorkspaceTab(
    path: String,
    active: Boolean,
    pinned: Boolean,
    dirty: Boolean,
    motionMillis: Int,
    onSelect: () -> Unit,
    onClose: () -> Unit,
    onTogglePin: () -> Unit,
    onCloseOthers: () -> Unit,
    onCloseAll: () -> Unit,
    onMove: (Int) -> Unit,
    onReveal: () -> Unit,
    maxWidth: Dp = 240.dp
) {
    var menu by remember { mutableStateOf(false) }
    val background by animateColorAsState(
        if (active) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
        animationSpec = tween(motionMillis),
        label = "tab-selection"
    )
    val selectedInk by animateColorAsState(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(motionMillis), label = "tab-ink")
    val moveThreshold = with(LocalDensity.current) { 32.dp.toPx() }
    var dragX by remember(path) { mutableFloatStateOf(0f) }
    val move by rememberUpdatedState(onMove)
    Row(
        Modifier.widthIn(max = maxWidth).padding(end = 2.dp)
            .background(background, RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileTypeIcon(path, Modifier.padding(start = 8.dp).size(16.dp), selected = active)
        Text(
            buildString {
                append(path.substringAfterLast('/'))
                if (dirty) append(" •")
            },
            Modifier.weight(1f, fill = false).heightIn(min = 48.dp)
                .combinedClickable(onClick = onSelect, onLongClick = { menu = true })
                .semantics {
                    selected = active
                    stateDescription = if (dirty) "Unsaved changes" else "Saved"
                }
                .padding(horizontal = 8.dp, vertical = 14.dp),
            color = selectedInk,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box {
            // Combined action/grip avoids pushing the active close button off phone screens.
            IconButton(onClick = { menu = true },
                modifier = Modifier.size(48.dp).pointerInput(path, moveThreshold) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { dragX = 0f },
                        onDragEnd = {
                            if (abs(dragX) > moveThreshold) move(if (dragX > 0f) 1 else -1)
                            dragX = 0f
                        },
                        onDragCancel = { dragX = 0f },
                        onDrag = { change, delta -> change.consume(); dragX += delta.x }
                    )
                }) {
                Icon(if (pinned) Icons.Default.PushPin else Icons.Default.MoreVert,
                    "Tab actions for $path",
                    Modifier.size(18.dp))
            }
            CodeLabDropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text(if (pinned) "Unpin tab" else "Pin tab") },
                    onClick = { menu = false; onTogglePin() })
                DropdownMenuItem(text = { Text("Move left") },
                    onClick = { menu = false; onMove(-1) })
                DropdownMenuItem(text = { Text("Move right") },
                    onClick = { menu = false; onMove(1) })
                DropdownMenuItem(text = { Text("Close others") },
                    onClick = { menu = false; onCloseOthers() })
                DropdownMenuItem(text = { Text("Close all unpinned") },
                    onClick = { menu = false; onCloseAll() })
                DropdownMenuItem(text = { Text("Reveal in Explorer") },
                    onClick = { menu = false; onReveal() })
            }
        }
        if (!pinned) IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.Close, "Close $path", Modifier.size(16.dp))
        }
    }
}

/** A dropdown provides deterministic access to tabs beyond the visible row. */
@Composable
internal fun OpenTabsMenu(tabs: List<String>, active: String?, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val recent = remember { mutableStateListOf<String>() }
    LaunchedEffect(active, tabs) {
        recent.removeAll { it !in tabs }
        if (active != null && active in tabs) {
            recent.remove(active)
            recent.add(0, active)
        }
    }
    val ordered = (recent + tabs).distinct().filter { it in tabs }
    Box {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.List, "Quick switch open files")
        }
        CodeLabDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ordered.forEach { path ->
                DropdownMenuItem(
                    text = { Text(if (path == active) "✓ $path" else path,
                        maxLines = 2, overflow = TextOverflow.Ellipsis) },
                    onClick = {
                        expanded = false
                        recent.remove(path)
                        recent.add(0, path)
                        onSelect(path)
                    }
                )
            }
        }
    }
}

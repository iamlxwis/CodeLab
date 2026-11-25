package site.lxwis.codelab

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Editable
import android.text.InputType
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.content.res.Configuration
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.Future

@Stable
internal class EditorCursorStatus {
    var label by mutableStateOf("Ln 1 · Col 1")
    var lineCount by mutableIntStateOf(1)
}

@Composable
internal fun EditorStatusBar(
    fileKey: String, filename: String, status: EditorCursorStatus,
    readOnly: Boolean, wordWrap: Boolean,
    onPreviousTab: () -> Unit, onNextTab: () -> Unit
) {
    var tabSwipe by remember { mutableFloatStateOf(0f) }
    val previous by rememberUpdatedState(onPreviousTab)
    val next by rememberUpdatedState(onNextTab)
    val threshold = with(androidx.compose.ui.platform.LocalDensity.current) { 56.dp.toPx() }
    Row(Modifier.fillMaxWidth().heightIn(min = 38.dp)
        .pointerInput(fileKey, threshold) {
            detectHorizontalDragGestures(onDragStart = { tabSwipe = 0f },
                onDragEnd = {
                    if (tabSwipe > threshold) previous()
                    else if (tabSwipe < -threshold) next()
                    tabSwipe = 0f
                }, onDragCancel = { tabSwipe = 0f }, onHorizontalDrag = { change, amount ->
                    change.consume()
                    tabSwipe += amount
                })
        }
        .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(status.label + " · " + status.lineCount + " lines", Modifier.weight(1f), maxLines = 2,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text((if (readOnly) "READ ONLY · " else "") +
            (if (wordWrap) "Wrap · " else "") + "UTF-8 · " +
            when (filename.substringAfterLast('.', "text").lowercase()) {
                "js", "mjs" -> "JavaScript"
                "ts" -> "TypeScript"
                "py" -> "Python"
                "lua" -> "Lua"
                "sql" -> "SQLite"
                "java" -> "Java"
                "html", "htm" -> "HTML"
                else -> filename.substringAfterLast('.', "text").uppercase()
            }, Modifier.weight(1f), maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun EditorIcon(icon: ImageVector, description: String, selected: Boolean = false,
                       onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp)
        .background(if (selected) colors.primaryContainer else Color.Transparent, CodeLabCorners.iconTile)) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(20.dp),
            tint = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant)
    }
}

@Composable
internal fun EditorAction(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 9.dp),
        modifier = Modifier.defaultMinSize(minWidth = 44.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
    }
}

@Composable
internal fun EditorKey(label: String, motionMillis: Int, touchFeedback: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    val background by animateColorAsState(
        if (pressed) colors.primaryContainer else colors.surfaceContainerHigh,
        tween(motionMillis), label = "key-surface")
    val arrow = when (label) {
        "←" -> Icons.Default.ArrowBack
        "→" -> Icons.Default.ArrowForward
        "↑" -> Icons.Default.ArrowUpward
        "↓" -> Icons.Default.ArrowDownward
        else -> null
    }
    val description = when (label) {
        "←" -> stringResource(R.string.editor_cursor_left)
        "→" -> stringResource(R.string.editor_cursor_right)
        "↑" -> stringResource(R.string.editor_cursor_up)
        "↓" -> stringResource(R.string.editor_cursor_down)
        else -> label
    }
    Surface(onClick = {
        if (touchFeedback) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onClick()
    }, modifier = Modifier.widthIn(min = if (label.length >= 4) 62.dp else 48.dp).height(48.dp),
        shape = CodeLabCorners.iconTile, color = background, interactionSource = interaction) {
        Box(contentAlignment = Alignment.Center) {
            if (arrow != null) Icon(arrow, description, Modifier.size(20.dp), tint = colors.onSurface)
            else Text(label, style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = colors.onSurface, maxLines = 1)
        }
    }
}

@Composable
internal fun EditorMinimap(text: String, onNavigate: (Int) -> Unit) {
    val ink = MaterialTheme.colorScheme.onSurfaceVariant
    val surface = MaterialTheme.colorScheme.surfaceContainer
    var model by remember { mutableStateOf(Pair(emptyList<Int>(), 1)) }
    LaunchedEffect(text) {
        delay(180)
        model = withContext(Dispatchers.Default) {
            Pair(text.lineSequence().map { it.trimEnd().length }.take(240).toList(),
                text.count { it == '\n' } + 1)
        }
    }
    val sample = model.first
    val allLines = model.second
    ComposeCanvas(Modifier.width(34.dp).fillMaxHeight()
        .background(surface)
        .pointerInput(allLines) {
            detectTapGestures { offset ->
                val ratio = (offset.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f)
                onNavigate((ratio * allLines).toInt().coerceIn(1, allLines))
            }
        }) {
        val total = sample.size.coerceAtLeast(1)
        val step = size.height / total
        val sampleColor = ink.copy(alpha = .38f)
        sample.forEachIndexed { index, length ->
            val y = index * step + 2f
            val width = (length.coerceAtMost(48) / 48f) * (size.width - 7f)
            if (width > 0f) drawRect(
                sampleColor, topLeft = Offset(3f, y),
                size = Size(width, step.coerceAtLeast(.8f).coerceAtMost(2.3f))
            )
        }
    }
}


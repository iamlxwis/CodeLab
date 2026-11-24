package site.lxwis.codelab

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*

internal val LocalWorkspaceMotion = staticCompositionLocalOf<CodeLabMotion?> { null }

@Composable
internal fun workspaceMotion(): CodeLabMotion = LocalWorkspaceMotion.current
    ?: rememberCodeLabMotion("System", 100)

@Composable
private fun modalEntrance(): Modifier {
    val motion = workspaceMotion()
    val progress = remember { Animatable(if (motion.millis(180) == 0) 1f else 0f) }
    LaunchedEffect(motion) {
        if (motion.millis(180) == 0) progress.snapTo(1f)
        else progress.animateTo(1f, motion.pressSpec())
    }
    return Modifier.graphicsLayer {
        alpha = progress.value.coerceIn(0f, 1f)
        scaleX = .975f + .025f * progress.value
        scaleY = scaleX
    }
}

@Composable
internal fun CodeLabDialog(onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(), content: @Composable () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest, properties = properties) {
        ConfigureModalWindow()
        Box(modalEntrance(), propagateMinConstraints = true) { content() }
    }
}

@Composable
internal fun CodeLabAlertDialog(
    onDismissRequest: () -> Unit, confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier, dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null, title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null, shape: Shape = MaterialTheme.shapes.extraLarge,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    iconContentColor: Color = MaterialTheme.colorScheme.primary,
    titleContentColor: Color = MaterialTheme.colorScheme.onSurface,
    textContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    tonalElevation: Dp = 0.dp, properties: DialogProperties = DialogProperties()
) {
    val decoratedTitle: (@Composable () -> Unit)? = if (title == null) null else ({ ConfigureModalWindow(); title() })
    val decoratedText: (@Composable () -> Unit)? = if (text == null) null else ({ ConfigureModalWindow(); text() })
    androidx.compose.material3.AlertDialog(onDismissRequest, confirmButton,
        modifier = modifier.then(modalEntrance()), dismissButton = dismissButton,
        icon = icon, title = decoratedTitle, text = decoratedText, shape = shape, containerColor = containerColor,
        iconContentColor = iconContentColor, titleContentColor = titleContentColor,
        textContentColor = textContentColor, tonalElevation = tonalElevation, properties = properties)
}

/** A real anchored popup with app-controlled motion and hardware-key navigation. */
@Composable
internal fun CodeLabDropdownMenu(
    expanded: Boolean, onDismissRequest: () -> Unit, modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset.Zero, scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit
) {
    val motion = workspaceMotion()
    val visible = remember { MutableTransitionState(false) }
    visible.targetState = expanded
    val density = LocalDensity.current
    val focus = LocalFocusManager.current
    val configuration = LocalConfiguration.current
    val margin = with(density) { 12.dp.roundToPx() }
    val offsetPx = with(density) { IntOffset(offset.x.roundToPx(), offset.y.roundToPx()) }
    val position = remember(margin, offsetPx) {
        object : PopupPositionProvider {
            override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize,
                layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
                val preferredX = if (layoutDirection == LayoutDirection.Ltr)
                    anchorBounds.right - popupContentSize.width + offsetPx.x
                    else anchorBounds.left - offsetPx.x
                val below = anchorBounds.bottom + offsetPx.y
                val preferredY = if (below + popupContentSize.height <= windowSize.height - margin) below
                    else anchorBounds.top - popupContentSize.height - offsetPx.y
                return IntOffset(preferredX.coerceIn(margin, (windowSize.width - popupContentSize.width - margin).coerceAtLeast(margin)),
                    preferredY.coerceIn(margin, (windowSize.height - popupContentSize.height - margin).coerceAtLeast(margin)))
            }
        }
    }
    if (visible.currentState || visible.targetState) Popup(position, onDismissRequest, properties = properties) {
        AnimatedVisibility(visible, enter = fadeIn(tween(motion.millis(120))) +
            scaleIn(tween(motion.millis(150)), initialScale = .96f, transformOrigin = TransformOrigin(1f, 0f)),
            exit = fadeOut(tween(motion.millis(90)))) {
            Surface(modifier.widthIn(min = 112.dp, max = (configuration.screenWidthDp.dp - 24.dp).coerceIn(112.dp, 320.dp))
                .semantics { paneTitle = "Context actions" }
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) false else when (event.key) {
                        Key.DirectionDown -> focus.moveFocus(FocusDirection.Next)
                        Key.DirectionUp -> focus.moveFocus(FocusDirection.Previous)
                        Key.Escape -> { onDismissRequest(); true }
                        else -> false
                    }
                }, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 2.dp, shadowElevation = 8.dp) {
                Column(Modifier.heightIn(max = (configuration.screenHeightDp.dp * .65f).coerceAtMost(480.dp))
                    .width(IntrinsicSize.Max).verticalScroll(scrollState).focusGroup().padding(vertical = 6.dp),
                    content = content)
            }
        }
    }
}

/** Full pages for substantial tools; confirmations continue to use centered alerts. */
@Composable
internal fun CodeLabToolDialog(onDismissRequest: () -> Unit, confirmButton: @Composable () -> Unit,
    title: (@Composable () -> Unit)? = null, text: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null, modifier: Modifier = Modifier) {
    CodeLabDialog(onDismissRequest, DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().padding(horizontal = 16.dp)) {
                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    IconButton(onClick = onDismissRequest) {
                        Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, "Close tool")
                    }
                    Box(Modifier.weight(1f)) { ProvideTextStyle(MaterialTheme.typography.headlineSmall) { title?.invoke() } }
                }
                HorizontalDivider()
                Box(Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp)) { text?.invoke() }
                HorizontalDivider()
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}

internal val LocalModalStatusVisible = staticCompositionLocalOf { true }

@Composable
private fun ConfigureModalWindow() {
    val view = androidx.compose.ui.platform.LocalView.current
    val window = (view.parent as? DialogWindowProvider)?.window
    val light = MaterialTheme.colorScheme.background.luminance() > .179f
    val motion = workspaceMotion()
    val statusVisible = LocalModalStatusVisible.current
    SideEffect {
        if (window != null) {
            androidx.core.view.WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = light
                isAppearanceLightNavigationBars = light
                if (statusVisible) show(androidx.core.view.WindowInsetsCompat.Type.statusBars())
                else hide(androidx.core.view.WindowInsetsCompat.Type.statusBars())
            }
            if (motion.millis(180) == 0) window.setWindowAnimations(0)
        }
    }
}

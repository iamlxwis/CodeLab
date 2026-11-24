package site.lxwis.codelab

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Consistent, restrained clay depth for navigation and project surfaces.
 * Editing, console, code diagnostics and selectable text remain flat.
 */
internal object CodeLabSpacing {
    val xSmall = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 18.dp
    val xLarge = 24.dp
    val screen = 22.dp

    fun forDensity(density: String) = when (density) {
        "Compact" -> small
        "Spacious" -> large
        else -> medium
    }
}

internal object CodeLabCorners {
    val iconTile = RoundedCornerShape(14.dp)
    val card = RoundedCornerShape(22.dp)
    val panel = RoundedCornerShape(24.dp)
}

internal object CodeLabElevation {
    val icon = 1.dp
    val tonal = 2.dp
    val card = 5.dp
    val panel = 3.dp
}

/** Decorative border is not used as the sole indication of interaction. */
@Composable
internal fun ClayPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val style = LocalCodeLabVisualStyle.current
    val shape = if (style == "Clay") CodeLabCorners.panel else MaterialTheme.shapes.large
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = if (style == "Clay") modifier.shadow(12.dp, shape, clip = false) else modifier,
        shape = shape,
        color = if (style == "Crystal") colors.surfaceContainer.copy(alpha = .72f)
            else colors.surfaceContainer,
        tonalElevation = if (style == "Clay") 2.dp else 0.dp,
        shadowElevation = if (style == "Clay") 3.dp else 0.dp,
        border = when (style) {
            "Crystal" -> BorderStroke(1.dp, Brush.linearGradient(listOf(
                Color.White.copy(alpha = .58f),
                colors.outlineVariant.copy(alpha = .28f))))
            "Clay" -> BorderStroke(1.dp, Brush.linearGradient(listOf(
                Color.White.copy(alpha = .30f),
                colors.outlineVariant.copy(alpha = .5f))))
            "Obsidian" -> BorderStroke(1.dp, colors.outlineVariant.copy(alpha = .72f))
            else -> null
        }
    ) { content() }
}

@Composable
internal fun ClayActionCard(
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val style = LocalCodeLabVisualStyle.current
    val shape = if (style == "Clay") CodeLabCorners.card else MaterialTheme.shapes.large
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = if (style == "Clay") modifier.shadow(12.dp, shape, clip = false) else modifier,
        enabled = true,
        interactionSource = interactionSource,
        shape = shape,
        color = if (style == "Crystal")
            colors.surfaceContainerHigh.copy(alpha = .72f)
            else colors.surfaceContainerHigh,
        tonalElevation = if (style == "Clay") CodeLabElevation.tonal else 0.dp,
        shadowElevation = if (style == "Clay") 9.dp else 0.dp,
        border = when (style) {
            "Crystal" -> BorderStroke(1.dp, Brush.linearGradient(listOf(
                Color.White.copy(alpha = .55f), colors.outlineVariant.copy(alpha = .20f))))
            "Clay" -> BorderStroke(1.dp, Brush.linearGradient(listOf(
                Color.White.copy(alpha = .35f), colors.outlineVariant.copy(alpha = .55f))))
            "Obsidian" -> BorderStroke(1.dp, colors.outlineVariant.copy(alpha = .75f))
            else -> null
        }
    ) { content() }
}

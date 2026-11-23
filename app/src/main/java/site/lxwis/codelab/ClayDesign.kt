package site.lxwis.codelab

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
    Surface(
        modifier = modifier,
        shape = if (style == "Clay") CodeLabCorners.panel else MaterialTheme.shapes.large,
        color = if (style == "Glass") MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .72f)
            else MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = if (style == "Clay") 3.dp else 0.dp,
        shadowElevation = if (style == "Clay") 8.dp else 0.dp,
        border = if (style == "Glass" || style == "Clay")
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .65f))
            else null
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
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = true,
        interactionSource = interactionSource,
        shape = if (style == "Clay") CodeLabCorners.card else MaterialTheme.shapes.large,
        color = if (style == "Glass")
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = .72f)
            else MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = if (style == "Clay") CodeLabElevation.tonal else 0.dp,
        shadowElevation = if (style == "Clay") 9.dp else 0.dp,
        border = if (style == "Glass" || style == "Clay")
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .65f))
            else null
    ) { content() }
}

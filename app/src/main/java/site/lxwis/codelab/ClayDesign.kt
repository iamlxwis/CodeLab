package site.lxwis.codelab

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shared spacing and surface geometry.
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

@Composable
internal fun ClayPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(modifier, shape = CodeLabCorners.panel, color = MaterialTheme.colorScheme.surfaceContainer) {
        content()
    }
}

@Composable
internal fun ClayActionCard(
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        interactionSource = interactionSource,
        shape = CodeLabCorners.card,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) { content() }
}

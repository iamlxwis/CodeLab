package site.lxwis.codelab

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Every tab can be closed, including background tabs. */
@Composable
internal fun WorkspaceTab(
    path: String,
    active: Boolean,
    motionMillis: Int,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    val background by animateColorAsState(
        if (active) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
        animationSpec = tween(motionMillis),
        label = "tab-selection"
    )
    Row(
        Modifier.padding(end = 2.dp)
            .background(background, RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            path.substringAfterLast('/'),
            Modifier.clickable(onClick = onSelect)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            color = if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.Close, "Close $path", Modifier.size(16.dp))
        }
    }
}

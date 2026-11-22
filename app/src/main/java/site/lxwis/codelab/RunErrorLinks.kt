package site.lxwis.codelab

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** File/line labels are part of the action, not conveyed only by color. */
@Composable
internal fun RunErrorLinks(
    references: List<SourceLineReference>,
    onJump: (String, Int) -> Unit
) {
    if (references.isEmpty()) return
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        references.take(6).forEach { reference ->
            AssistChip(
                onClick = { onJump(reference.path, reference.line) },
                modifier = Modifier.heightIn(min = 48.dp),
                leadingIcon = { Icon(Icons.Default.ErrorOutline, contentDescription = null) },
                label = { Text("Open " + reference.path.substringAfterLast('/') +
                    " · line " + reference.line) }
            )
        }
    }
}

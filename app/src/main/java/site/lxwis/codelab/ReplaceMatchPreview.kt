package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * Each row shows the exact matched span and the literal replacement.
 * The preview caps at 100 hits; apply still performs a fresh bounded preflight.
 */
@Composable
internal fun ReplaceMatchPreview(hits: List<ProjectStore.ReplaceHit>) {
    hits.groupBy { it.path }.forEach { (path, matches) ->
        Text("$path · ${matches.size} shown",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Monospace)
        matches.forEach { hit ->
            Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("Line ${hit.line}", style = MaterialTheme.typography.labelSmall)
                Text("Before: ${hit.before}", fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall)
                Text("After: ${hit.after}", fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall)
            }
            HorizontalDivider()
        }
    }
}

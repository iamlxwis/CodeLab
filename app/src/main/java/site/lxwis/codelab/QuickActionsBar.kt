package site.lxwis.codelab

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Configurable quick actions in the existing scrollable bottom bar. */
@Composable
internal fun QuickActionsBar(ids: List<String>, onAction: (String) -> Unit) {
    Row {
        ids.take(6).forEach { id ->
            val entry = when (id) {
                "newfile" -> Icons.Default.NoteAdd to "New file"
                "files" -> Icons.Default.FolderOpen to "Explorer"
                "projectsearch" -> Icons.Default.Search to "Search project"
                "projectreplace" -> Icons.Default.FindReplace to "Replace across project"
                "commands" -> Icons.Default.Terminal to "Command palette"
                "history" -> Icons.Default.History to "File history"
                "projectsettings" -> Icons.Default.Settings to "Project settings"
                "git" -> Icons.Default.AccountTree to "Git history"
                "panels" -> Icons.Default.VerticalSplit to "Panel layout"
                "languages" -> Icons.Default.Code to "Languages"
                "appearance" -> Icons.Default.Palette to "Appearance"
                "accessibility" -> Icons.Default.AccessibilityNew to "Accessibility"
                else -> null
            } ?: return@forEach
            IconButton(onClick = { onAction(id) }, modifier = Modifier.size(48.dp)) {
                Icon(entry.first, entry.second, modifier = Modifier.size(20.dp))
            }
        }
    }
}

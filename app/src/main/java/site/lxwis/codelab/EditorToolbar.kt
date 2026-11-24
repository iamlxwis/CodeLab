package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

internal data class EditorToolbarAction(val id: String, val title: String,
    val icon: ImageVector, val selected: Boolean = false, val invoke: () -> Unit)

internal object EditorToolbarOrder {
    val default = listOf("undo", "redo", "format", "find", "wrap")
    val known = default + listOf("share", "save", "run", "commands", "settings", "symbols")
    fun decode(raw: String?): List<String> = raw?.split(',')?.filter { it in known }?.distinct() ?: default
    fun move(ids: List<String>, id: String, offset: Int): List<String> {
        val index = ids.indexOf(id)
        if (index < 0) return ids
        val target = (index + offset).coerceIn(0, ids.lastIndex)
        return ids.toMutableList().apply { add(target, removeAt(index)) }
    }
}

@Composable
internal fun EditorToolbarSettings(actions: List<EditorToolbarAction>, ids: List<String>,
    onChange: (List<String>) -> Unit, close: () -> Unit) {
    CodeLabToolDialog(onDismissRequest = close, title = { Text("Customize editor toolbar") },
        text = {
            Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Pin the actions you use. Unpinned actions remain in the editor menu.",
                    style = MaterialTheme.typography.bodySmall)
                val ordered = ids.mapNotNull { id -> actions.firstOrNull { it.id == id } } +
                    actions.filterNot { it.id in ids }
                ordered.forEach { action ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = action.id in ids, onCheckedChange = { enabled ->
                            onChange(if (enabled) ids + action.id else ids - action.id)
                        })
                        Icon(action.icon, null, Modifier.size(19.dp))
                        Text(action.title, Modifier.weight(1f).padding(start = 8.dp),
                            style = MaterialTheme.typography.bodySmall)
                        if (action.id in ids) {
                            IconButton(onClick = { onChange(EditorToolbarOrder.move(ids, action.id, -1)) },
                                enabled = ids.firstOrNull() != action.id) {
                                Icon(Icons.Default.ArrowUpward, "Move ${action.title} earlier")
                            }
                            IconButton(onClick = { onChange(EditorToolbarOrder.move(ids, action.id, 1)) },
                                enabled = ids.lastOrNull() != action.id) {
                                Icon(Icons.Default.ArrowDownward, "Move ${action.title} later")
                            }
                        }
                    }
                }
                TextButton(onClick = { onChange(EditorToolbarOrder.default) }) { Text("Restore defaults") }
            }
        }, confirmButton = { TextButton(onClick = close) { Text("Done") } })
}

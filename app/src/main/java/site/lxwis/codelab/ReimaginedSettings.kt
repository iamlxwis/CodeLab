package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private data class SettingDestination(val name: String, val detail: String, val id: String, val icon: ImageVector)

@Composable
internal fun ReimaginedSettings(m: WorkspaceModel, onAction: (String) -> Unit, close: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val groups = listOf(
        listOf(SettingDestination("Appearance & Theme Studio","Presets, materials, typography and syntax","appearance",Icons.Default.Palette),
            SettingDestination("Accessibility","Contrast, text size and haptics","accessibility",Icons.Default.AccessibilityNew)),
        listOf(SettingDestination("Workspace layout","Density, panel size and status bar","panels",Icons.Default.ViewSidebar),
            SettingDestination("Saved layouts","Save or restore your workspace","layoutpresets",Icons.Default.Dashboard),
            SettingDestination("Languages","Offline packs and online availability","languages",Icons.Default.Code)),
        listOf(SettingDestination("Recently deleted","Recover local projects and files","trash",Icons.Default.RestoreFromTrash),
            SettingDestination("Quick start guide","Interactive help and tutorials","tutorial",Icons.Default.School),
            SettingDestination("Tools","All available project and editor actions","actions",Icons.Default.Build)))
    Dialog(onDismissRequest = close, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = close) { Icon(Icons.Default.ArrowBack, "Close settings") }
                    Text("Settings", Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium)
                }
                CodeLabSearch(query, { query = it }, "Search settings")
                Spacer(Modifier.height(12.dp))
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    groups.forEach { group ->
                        val rows = group.filter { it.name.contains(query, true) || it.detail.contains(query, true) }
                        if (rows.isNotEmpty()) item {
                            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
                                Column {
                                    rows.forEachIndexed { index, row ->
                                        Surface(onClick = { onAction(row.id) }, color = MaterialTheme.colorScheme.surfaceContainer) {
                                            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(row.icon, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                                                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                                    Text(row.name, style = MaterialTheme.typography.titleSmall)
                                                    Text(row.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Icon(Icons.Default.ChevronRight, null)
                                            }
                                        }
                                        if (index != rows.lastIndex) HorizontalDivider(Modifier.padding(start = 48.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun WorkspaceLayoutPresets(m: WorkspaceModel, close: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = close, title = { Text("Workspace layouts") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name, { name = it.take(64) }, label = { Text("Layout name") }, singleLine = true)
            Button(enabled = name.isNotBlank(), onClick = {
                notice = if (m.saveNamedLayout(name)) "Layout saved" else "Could not save layout"
                if (notice == "Layout saved") name = ""
            }) { Text("Save current layout") }
            if (notice.isNotEmpty()) Text(notice)
            LazyColumn(Modifier.heightIn(max = 240.dp)) {
                items(m.savedWorkspaceLayouts, key = { it.name }) { layout ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(layout.name, style = MaterialTheme.typography.titleSmall)
                            Text("${layout.density} · ${layout.phonePercent}% editor · ${layout.toolbar?.size ?: 0} pinned tools",
                                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { m.restoreNamedLayout(layout.name); notice = "Layout restored" }) { Text("Restore") }
                        IconButton(onClick = { m.deleteNamedLayout(layout.name) }) { Icon(Icons.Default.DeleteOutline, "Delete layout ${layout.name}") }
                    }
                }
            }
        }
    }, confirmButton = { TextButton(onClick = close) { Text("Done") } })
}

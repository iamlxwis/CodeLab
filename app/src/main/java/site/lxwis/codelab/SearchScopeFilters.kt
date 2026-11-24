package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

internal fun scopedProjectFiles(
    files: List<String>,
    folder: String,
    kind: ProjectFileKind?
): List<String> = files.filter {
    (folder.isEmpty() || it.startsWith(folder)) &&
        (kind == null || projectFileKind(it) == kind)
}

/** Scoped filters are reusable by Find and Replace, preventing accidental broad edits. */
@Composable
internal fun SearchScopeFilters(
    folders: List<String>,
    folder: String,
    kind: ProjectFileKind?,
    onFolder: (String) -> Unit,
    onKind: (ProjectFileKind?) -> Unit
) {
    var foldersExpanded by remember { mutableStateOf(false) }
    var kindsExpanded by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f)) {
            OutlinedButton(onClick = { foldersExpanded = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(folder.ifEmpty { "All folders" }, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
            }
            CodeLabDropdownMenu(expanded = foldersExpanded,
                onDismissRequest = { foldersExpanded = false }) {
                DropdownMenuItem(text = { Text("All folders") },
                    onClick = { onFolder(""); foldersExpanded = false })
                folders.forEach { path ->
                    DropdownMenuItem(text = { Text(path) },
                        onClick = { onFolder(path); foldersExpanded = false })
                }
            }
        }
        Box(Modifier.weight(1f)) {
            OutlinedButton(onClick = { kindsExpanded = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(kind?.name ?: "All file types", maxLines = 1)
            }
            CodeLabDropdownMenu(expanded = kindsExpanded,
                onDismissRequest = { kindsExpanded = false }) {
                DropdownMenuItem(text = { Text("All file types") },
                    onClick = { onKind(null); kindsExpanded = false })
                ProjectFileKind.entries.forEach { option ->
                    DropdownMenuItem(text = { Text(option.name) },
                        onClick = { onKind(option); kindsExpanded = false })
                }
            }
        }
    }
}

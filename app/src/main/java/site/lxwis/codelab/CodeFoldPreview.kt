package site.lxwis.codelab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/** Folded read-only view. The backing editable document is never transformed. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CodeFoldPreview(source: String, onJump: (Int) -> Unit, onClose: () -> Unit) {
    val regions = remember(source) { CodeFolds.regions(source) }
    var collapsed by remember(source) { mutableStateOf(regions.map { it.startLine }.toSet()) }
    val rows = remember(source, regions, collapsed) {
        CodeFolds.preview(source, regions, collapsed)
    }
    ModalBottomSheet(onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.92f).padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Fold preview", style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    collapsed = if (collapsed.isEmpty()) regions.map { it.startLine }.toSet()
                        else emptySet()
                }) { Text(if (collapsed.isEmpty()) "Collapse all" else "Expand all") }
            }
            Text("Read-only outline. Expand sections or tap a line to return to editing.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (regions.isEmpty()) {
                Text("No multiline brace blocks found in this file.",
                    style = MaterialTheme.typography.bodyMedium)
            }
            LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                items(rows, key = { it.line }) { row ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 44.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(row.line.toString(), modifier = Modifier.width(44.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(Modifier.weight(1f).clickable { onJump(row.line) }
                            .padding(vertical = 6.dp)) {
                            Text(row.code.ifEmpty { " " }, fontFamily = FontFamily.Monospace,
                                maxLines = 2, style = MaterialTheme.typography.bodySmall)
                            if (row.foldedLines > 0) {
                                Text(row.foldedLines.toString() + " lines folded",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (row.region != null) {
                            val folded = row.line in collapsed
                            TextButton(onClick = {
                                collapsed = if (folded) collapsed - row.line else collapsed + row.line
                            }) { Text(if (folded) "Expand" else "Fold") }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

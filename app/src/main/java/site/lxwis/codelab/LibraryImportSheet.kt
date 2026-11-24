package site.lxwis.codelab

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Import actions remain bound to their existing secure picker / GitHub validation flows. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryImportSheet(
    onDismiss: () -> Unit,
    onZip: () -> Unit,
    onGitHub: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
    ) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding()
            .padding(start = 22.dp, end = 22.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Import project", style = MaterialTheme.typography.headlineSmall)
            Text("Choose a file or repository. Your original files stay unchanged.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium)
            ImportOption(title = "ZIP archive", detail = "Open a backup or exported project from your device.",
                icon = { Icon(Icons.Default.Archive, contentDescription = null) }, click = onZip)
            ImportOption(title = "GitHub repository",
                detail = "Download a snapshot of a public repository; no account required.",
                icon = { Icon(Icons.Default.CloudDownload, contentDescription = null) }, click = onGitHub)
             TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Cancel") }
        }
    }
}

@Composable
private fun ImportOption(title: String, detail: String,
    icon: @Composable () -> Unit, click: () -> Unit) {
    Surface(onClick = click, modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .48f))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(14.dp)) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { icon() }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(detail, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

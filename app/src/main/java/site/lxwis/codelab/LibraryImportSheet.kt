package site.lxwis.codelab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** One deliberate import action on the dashboard, with two explicit sources. */
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
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = CodeLabCorners.panel
    ) {
        Column(Modifier.fillMaxWidth().padding(
            start = CodeLabSpacing.medium,
            end = CodeLabSpacing.medium,
            bottom = CodeLabSpacing.large
        )) {
            Text("Import project", style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = CodeLabSpacing.small,
                    vertical = CodeLabSpacing.medium))
            ListItem(
                headlineContent = { Text("ZIP archive") },
                supportingContent = { Text("Import files from a project backup") },
                leadingContent = { Icon(Icons.Default.Archive, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().clickable(onClick = onZip)
            )
            Spacer(Modifier.height(CodeLabSpacing.small))
            ListItem(
                headlineContent = { Text("GitHub repository") },
                supportingContent = { Text("Import a public repository snapshot") },
                leadingContent = { Icon(Icons.Default.Public, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().clickable(onClick = onGitHub)
            )
        }
    }
}

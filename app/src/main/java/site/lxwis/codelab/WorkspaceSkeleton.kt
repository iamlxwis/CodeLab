package site.lxwis.codelab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Actual loading placeholder; never shown after data is available. */
@Composable
internal fun WorkspaceSkeleton(rows: Int = 4) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(rows) { i ->
            Row(Modifier.fillMaxWidth().background(colors.surfaceContainer, MaterialTheme.shapes.medium)
                .padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(30.dp).background(colors.surfaceContainerHighest, MaterialTheme.shapes.small))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.fillMaxWidth(if (i % 2 == 0) .65f else .45f).height(12.dp)
                        .background(colors.surfaceContainerHighest, MaterialTheme.shapes.small))
                    Box(Modifier.fillMaxWidth(.32f).height(8.dp)
                        .background(colors.surfaceContainerHighest, MaterialTheme.shapes.small))
                }
            }
        }
    }
}

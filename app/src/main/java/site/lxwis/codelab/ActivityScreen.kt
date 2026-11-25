package site.lxwis.codelab

import android.content.Intent
import android.text.format.DateUtils
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
internal fun ActivityScreen(activity: DeveloperActivity, close: () -> Unit) {
    val context = LocalContext.current
    val report = activity.report
    var reset by remember { mutableStateOf(false) }
    DestinationPage("Developer activity", close, trailing = {
        IconButton(onClick = {
            val recap = "CodeLab · Last 30 days\n${formatCodingTime(report.codingMillis)} coding · " +
                "${report.projects} projects · ${report.files} files\n" +
                "${report.runs} executions · ${report.passed} passed · ${report.failed} failed · ${report.stopped} stopped"
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, recap), "Share activity recap"))
        }) { Icon(Icons.Default.Share, "Share activity recap") }
    }) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text("Last 30 days", style = MaterialTheme.typography.titleLarge) }
            item { ActivitySummary(report) }
            if (report.languages.isNotEmpty()) {
                item { Text("Coding by language", style = MaterialTheme.typography.titleMedium) }
                items(report.languages, key = { it.first }) { (extension, duration) ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                        FileTypeIcon("file.$extension", Modifier.size(24.dp))
                        Text(LanguageCatalog.entries.firstOrNull { it.extension == extension }?.name ?: "Other",
                            Modifier.weight(1f).padding(horizontal = 10.dp))
                        Text(formatCodingTime(duration), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            if (report.executions.isNotEmpty()) {
                item { Text("Recent executions", style = MaterialTheme.typography.titleMedium) }
                items(report.executions) { execution -> ActivityRunRow(execution) }
            }
            item {
                SettingToggle("Record activity on this device", activity.enabled, activity::chooseEnabled)
                if (report.files + report.projects + report.runs > 0) {
                    TextButton(onClick = { reset = true }) { Text("Clear activity history") }
                }
            }
        }
    }
    if (reset) CodeLabAlertDialog(onDismissRequest = { reset = false },
        title = { Text("Clear developer activity?") }, text = { Text("This removes your local activity totals and run recap.") },
        confirmButton = { TextButton(onClick = { activity.reset(); reset = false }) { Text("Clear history") } },
        dismissButton = { TextButton(onClick = { reset = false }) { Text("Cancel") } })
}

@Composable
internal fun ActivitySummary(report: DeveloperActivity.Report) {
    Surface(shape = CodeLabCorners.card, color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(formatCodingTime(report.codingMillis) + " coding", style = MaterialTheme.typography.titleLarge)
            Text("${report.projects} projects created · ${report.files} files edited",
                style = MaterialTheme.typography.bodyMedium)
            Text("${report.runs} runs · ${report.passed} passed · ${report.failed} failed · ${report.stopped} stopped",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun ActivityRunRow(execution: DeveloperActivity.Execution) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        FileTypeIcon(execution.file, Modifier.size(22.dp))
        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text(execution.file.substringAfterLast('/'), style = MaterialTheme.typography.titleSmall)
            Text(DateUtils.getRelativeTimeSpanString(execution.time).toString(), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(execution.outcome.label, style = MaterialTheme.typography.labelMedium,
            color = when (execution.outcome) {
                DeveloperActivity.Outcome.Succeeded -> MaterialTheme.colorScheme.tertiary
                DeveloperActivity.Outcome.Failed -> MaterialTheme.colorScheme.error
                DeveloperActivity.Outcome.Stopped -> MaterialTheme.colorScheme.onSurfaceVariant
            })
    }
}

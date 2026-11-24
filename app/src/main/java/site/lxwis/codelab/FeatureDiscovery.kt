package site.lxwis.codelab

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun ContextualTip(id: String, title: String, detail: String, onGuide: (() -> Unit)? = null) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("feature-discovery", Context.MODE_PRIVATE) }
    var visible by remember(id) { mutableStateOf(prefs.getBoolean("enabled", true) && !prefs.getBoolean(id, false)) }
    DisposableEffect(prefs, id) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            visible = prefs.getBoolean("enabled", true) && !prefs.getBoolean(id, false)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    var expanded by rememberSaveable(id) { mutableStateOf(false) }
    val motion = workspaceMotion()
    AnimatedVisibility(visible, enter = fadeIn(tween(motion.millis(140))),
        exit = fadeOut(tween(motion.millis(100))) + shrinkVertically(tween(motion.millis(140)))) {
        Surface(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column {
                Row(Modifier.fillMaxWidth().padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(title, Modifier.weight(1f).padding(horizontal = 10.dp),
                        style = MaterialTheme.typography.labelLarge, maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            stringResource(if (expanded) R.string.tip_collapse else R.string.tip_expand, title))
                    }
                    IconButton(onClick = { prefs.edit().putBoolean(id, true).apply() }) {
                        Icon(Icons.Default.Close, stringResource(R.string.tip_dismiss, title))
                    }
                }
                AnimatedVisibility(expanded, enter = fadeIn(tween(motion.millis(140))),
                    exit = fadeOut(tween(motion.millis(100))) + shrinkVertically(tween(motion.millis(140)))) {
                    Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp)) {
                        Text(detail, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (onGuide != null) TextButton(onClick = onGuide) { Text(stringResource(R.string.tip_open_guide)) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun FeatureDiscoverySettings() {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("feature-discovery", Context.MODE_PRIVATE) }
    var enabled by remember { mutableStateOf(prefs.getBoolean("enabled", true)) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Contextual tips", Modifier.weight(1f))
        Switch(enabled, { enabled = it; prefs.edit().putBoolean("enabled", it).apply() },
            modifier = Modifier.semantics { contentDescription = "Contextual tips" })
    }
    TextButton(onClick = { prefs.edit().clear().putBoolean("enabled", true).apply(); enabled = true }) {
        Text("Replay feature tips")
    }
}

package site.lxwis.codelab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

/** A full-window destination keeps the native editor view and its selection alive underneath. */
@Composable
internal fun DestinationPage(
    title: String,
    close: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    CodeLabDialog(close, DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()
                .padding(horizontal = 20.dp)) {
                Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(close, Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                    Text(title, Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    trailing?.invoke()
                }
                content()
            }
        }
    }
}

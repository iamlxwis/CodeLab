package site.lxwis.codelab

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Every accent has a spoken name and at least a 48dp touch target. */
@Composable
internal fun AppearanceAccentSwatches(selectedAccent: String, choose: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            "Mint" to Color(0xff83e5ca),
            "Blue" to Color(0xff8ab7fa),
            "Violet" to Color(0xffc4b0ff),
            "Amber" to Color(0xffffc978),
            "Rose" to Color(0xffff92b7),
            "Mono" to Color(0xffaeb8bb)
        ).forEach { (name, color) ->
            val active = selectedAccent == name
            Surface(
                onClick = { choose(name) },
                modifier = Modifier.size(48.dp).semantics {
                    contentDescription = "$name accent"
                    selected = active
                },
                shape = CircleShape,
                color = color,
                border = if (active)
                    BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (active) {
                        Icon(Icons.Default.Check, contentDescription = null,
                            tint = Color(0xff162426), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

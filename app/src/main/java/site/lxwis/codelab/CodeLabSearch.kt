package site.lxwis.codelab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/** Shared compact, filled search field with explicit clear affordance. */
@Composable
internal fun CodeLabSearch(value: String, change: (String) -> Unit, hint: String,
    modifier: Modifier = Modifier.fillMaxWidth()) {
    val colors = MaterialTheme.colorScheme
    TextField(value = value, onValueChange = change, modifier = modifier.semantics { contentDescription = hint },
        textStyle = MaterialTheme.typography.bodyMedium,
        singleLine = true, shape = MaterialTheme.shapes.large,
        placeholder = { Text(hint, style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(20.dp)) },
        trailingIcon = if (value.isNotEmpty()) {
            { IconButton(onClick = { change("") }) { Icon(Icons.Default.Close, "Clear " + hint.lowercase()) } }
        } else null,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceContainerHigh,
            unfocusedContainerColor = colors.surfaceContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent))
}

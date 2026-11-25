package site.lxwis.codelab

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import org.json.JSONObject
import org.json.JSONArray
import org.json.JSONTokener

internal object DeveloperTransforms {
    val actions = listOf("JSON format", "JSON minify", "Base64 encode",
        "Base64 decode", "URL encode", "URL decode", "SHA-256 hash")
    fun apply(action: String, input: String): String {
        require(input.length <= 12000) { "Input is limited to 12,000 characters." }
        return when (action) {
            "JSON format" -> {
                when (val value = JSONTokener(input).nextValue()) {
                    is JSONObject -> value.toString(2)
                    is JSONArray -> value.toString(2)
                    else -> throw IllegalArgumentException("Expected a JSON object or array.")
                }
            }
            "JSON minify" -> {
                when (val value = JSONTokener(input).nextValue()) {
                    is JSONObject -> value.toString()
                    is JSONArray -> value.toString()
                    else -> throw IllegalArgumentException("Expected a JSON object or array.")
                }
            }
            "Base64 encode" -> Base64.getEncoder()
                .encodeToString(input.toByteArray(StandardCharsets.UTF_8))
            "Base64 decode" -> {
                String(Base64.getDecoder().decode(input.trim()), StandardCharsets.UTF_8)
            }
            "URL encode" -> URLEncoder.encode(input, "UTF-8")
            "URL decode" -> URLDecoder.decode(input, "UTF-8")
            "SHA-256 hash" -> MessageDigest.getInstance("SHA-256")
                .digest(input.toByteArray(StandardCharsets.UTF_8))
                .joinToString("") { "%02x".format(it.toInt() and 255) }
            else -> throw IllegalArgumentException("Unknown developer tool.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperToolbox(dismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var action by rememberSaveable { mutableStateOf(DeveloperTransforms.actions.first()) }
    var input by rememberSaveable { mutableStateOf("") }
    var output by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    DestinationPage("Developer tools", dismiss) {
        Column(Modifier.fillMaxWidth().weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Local transformations. Nothing is uploaded.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                DeveloperTransforms.actions.forEach { choice ->
                    FilterChip(selected = choice == action,
                        onClick = { action = choice; output = ""; error = "" },
                        label = { Text(choice) })
                }
            }
            OutlinedTextField(input, {
                input = it.take(12000)
                error = ""
            }, modifier = Modifier.fillMaxWidth(),
                label = { Text("Input") }, minLines = 3, maxLines = 9)
            Button(enabled = input.isNotEmpty(), onClick = {
                try {
                    output = DeveloperTransforms.apply(action, input)
                    error = ""
                } catch (ex: Exception) {
                    output = ""
                    error = ex.localizedMessage ?: "Conversion failed"
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Transform") }
            if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
            if (output.isNotBlank()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Result", style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = {
                        clipboard.setText(AnnotatedString(output))
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    }) { Text("Copy result") }
                }
                SelectionContainer {
                    Text(output.take(30000), fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

package site.lxwis.codelab

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

private data class SupportedLanguage(val name: String, val extension: String, val local: String? = null)

private val catalog = listOf(
    SupportedLanguage("JavaScript", "js", "Offline · Rhino"),
    SupportedLanguage("Lua", "lua", "Offline · LuaJ"),
    SupportedLanguage("Java", "java", "Offline beta · BeanShell"),
    SupportedLanguage("SQLite", "sql", "Offline · in-memory"),
    SupportedLanguage("HTML", "html", "Offline preview"),
    SupportedLanguage("CSS", "css", "Offline preview"),
    SupportedLanguage("Python", "py"),
    SupportedLanguage("C", "c"),
    SupportedLanguage("C++", "cpp"),
    SupportedLanguage("C#", "cs"),
    SupportedLanguage("TypeScript", "ts"),
    SupportedLanguage("Kotlin", "kt"),
    SupportedLanguage("Rust", "rs"),
    SupportedLanguage("Go", "go"),
    SupportedLanguage("PHP", "php"),
    SupportedLanguage("Ruby", "rb"),
    SupportedLanguage("Swift", "swift"),
    SupportedLanguage("Bash", "sh"),
    SupportedLanguage("Dart", "dart"),
    SupportedLanguage("Scala", "scala"),
    SupportedLanguage("Perl", "pl"),
    SupportedLanguage("Haskell", "hs"),
    SupportedLanguage("R", "r"),
    SupportedLanguage("Elixir", "ex"),
    SupportedLanguage("Erlang", "erl"),
    SupportedLanguage("OCaml", "ml"),
    SupportedLanguage("Fortran", "f90"),
    SupportedLanguage("COBOL", "cob"),
    SupportedLanguage("Pascal", "pas"),
    SupportedLanguage("Assembly", "asm"),
    SupportedLanguage("Objective-C", "m"),
    SupportedLanguage("Groovy", "groovy"),
    SupportedLanguage("F#", "fs"),
    SupportedLanguage("Visual Basic .NET", "vb"),
    SupportedLanguage("Prolog", "plg")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageManagerSheet(model: WorkspaceModel, close: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var endpoint by rememberSaveable { mutableStateOf(model.cloudEndpoint) }
    var header by rememberSaveable { mutableStateOf(model.cloudHeader) }
    var credential by rememberSaveable { mutableStateOf(model.cloudCredential) }

    ModalBottomSheet(onDismissRequest = close, sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.92f).padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Languages & Cloud", style = MaterialTheme.typography.headlineSmall)
                    Text("Local execution first · online when available",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                }
                Icon(if (model.isOnline) Icons.Default.Cloud else Icons.Default.WifiOff,
                    contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            HorizontalDivider()
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp),
                contentPadding = PaddingValues(bottom = 28.dp)) {
                item {
                    Text("COMPILER CONNECTION", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(if (!model.isOnline) "Device offline — local runtimes remain available"
                        else if (model.cloudChecking) "Checking compiler language availability…"
                        else if (model.cloudError != null) "Compiler not ready: " + model.cloudError
                        else "Online compiler verified · " + model.cloudLanguages.size + " language families",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (model.cloudError != null && model.isOnline)
                            MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    if (model.cloudChecking) {
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(endpoint, { endpoint = it.take(256) }, Modifier.fillMaxWidth(),
                        label = { Text("Judge0 HTTPS endpoint") }, singleLine = true,
                        shape = RoundedCornerShape(12.dp))
                    Spacer(Modifier.height(6.dp))
                    Text("Optional authentication header",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("X-Auth-Token", "Authorization", "X-RapidAPI-Key").forEach { name ->
                            FilterChip(selected = header == name, onClick = { header = name },
                                label = { Text(name) })
                        }
                    }
                    OutlinedTextField(credential, { credential = it.take(512) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        label = { Text("API token (optional)") },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp))
                    Spacer(Modifier.height(6.dp))
                    Button(onClick = { model.saveCloudConfig(endpoint, header, credential) },
                        modifier = Modifier.fillMaxWidth(), enabled = !model.cloudChecking,
                        shape = RoundedCornerShape(12.dp)) {
                        Text("Save and check languages")
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Online runs send source code and the input you provide to this server. " +
                        "CodeLab always asks before uploading. Public endpoints may have quotas or require a key. " +
                        "An internet connection does not guarantee compiler availability.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text("LANGUAGE AVAILABILITY", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                }
                items(catalog, key = { it.extension }) { language ->
                    val cloud = model.cloudLanguages[language.extension]
                    Row(Modifier.fillMaxWidth().heightIn(min = 50.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(19.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(language.name, style = MaterialTheme.typography.bodyMedium)
                            Text(when {
                                language.local != null && cloud != null -> language.local + " · Online ready"
                                language.local != null -> language.local
                                cloud != null && model.isOnline -> "Online ready · " + cloud.name
                                !model.isOnline -> "Online only · no connection"
                                else -> "Online execution not verified"
                            }, style = MaterialTheme.typography.labelSmall,
                                maxLines = 2,
                                color = if (cloud != null || language.local != null)
                                    MaterialTheme.colorScheme.primary else
                                    MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("." + language.extension, fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f))
                }
                item {
                    Text("Other file types can still be edited. Runtime availability is verified " +
                        "against the configured compiler's /languages endpoint; editor support " +
                        "does not mean code can be executed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun OnlineRunConfirmation(
    filename: String, endpoint: String, language: String,
    cancel: () -> Unit, start: (String) -> Unit
) {
    var stdin by rememberSaveable(filename) { mutableStateOf("") }
    AlertDialog(onDismissRequest = cancel,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        icon = { Icon(Icons.Default.Cloud, contentDescription = null) },
        title = { Text("Run online?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(filename + " · " + language, style = MaterialTheme.typography.bodyMedium)
                Text("Your current source code and the input below will be sent to:",
                    style = MaterialTheme.typography.bodySmall)
                Text(endpoint, color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium)
                Text("Do not upload passwords, private keys, or other secrets.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(stdin, { stdin = it.take(4096) },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Standard input (optional)") },
                    placeholder = { Text("Text supplied before execution") },
                    minLines = 2, maxLines = 4)
                Text("Online input is provided before the run, not interactively.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = { start(stdin) }) { Text("Send and run") }
        },
        dismissButton = { TextButton(onClick = cancel) { Text("Cancel") } }
    )
}

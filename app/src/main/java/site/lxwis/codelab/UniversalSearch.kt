package site.lxwis.codelab

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import org.json.JSONArray

internal enum class SearchCategory(val label: String) {
    All("All"), Projects("Projects"), Files("Files"), Code("Code"), Snippets("Snippets"),
    Templates("Templates"), Settings("Settings"), Tools("Tools"), Commands("Commands")
}

internal data class SearchResult(
    val category: SearchCategory,
    val title: String,
    val subtitle: String = category.label,
    val location: WorkspaceLocation? = null,
    val line: Int? = null,
    val action: String? = null,
    val body: String? = null
)

internal val projectTemplates = listOf("Web") + LanguageCatalog.entries.map { it.name }

private val discoverableTools = listOf(
    "languages" to "Offline runtimes", "catalog" to "Language catalog", "downloads" to "Downloads",
    "updates" to "Software update", "activity" to "Developer activity", "utilities" to "Utilities",
    "devtools" to "Text and data tools", "httpsinspector" to "HTTP inspector",
    "trash" to "Recently deleted", "tutorial" to "Quick start guide", "licenses" to "Licenses"
)

@Composable
internal fun UniversalSearch(
    model: WorkspaceModel,
    initialCategory: SearchCategory = SearchCategory.All,
    onAction: (String) -> Unit,
    onTemplate: (String) -> Unit,
    close: () -> Unit
) {
    val context = LocalContext.current
    val searchPreferences = remember(context) { context.getSharedPreferences("universal-search", Context.MODE_PRIVATE) }
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(initialCategory) }
    var selectedSnippet by remember { mutableStateOf<SearchResult?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    val isLibrary = initialCategory == SearchCategory.Snippets || initialCategory == SearchCategory.Templates
    var historyEnabled by remember { mutableStateOf(searchPreferences.getBoolean("remember-history", true)) }
    var history by remember {
        mutableStateOf(runCatching {
            val list = JSONArray(searchPreferences.getString("queries", "[]"))
            (0 until minOf(12, list.length())).mapNotNull { list.optString(it).takeIf(String::isNotBlank) }
        }.getOrDefault(emptyList()))
    }
    fun saveHistory(updated: List<String>) {
        history = updated
        searchPreferences.edit().putString("queries", JSONArray(updated).toString()).apply()
    }
    fun rememberQuery() {
        val value = query.trim()
        if (!isLibrary && historyEnabled && value.isNotEmpty()) {
            saveHistory((listOf(value) + history.filterNot { it.equals(value, true) }).take(12))
        }
    }

    val projects = model.projects
    val commands = remember(model.project, model.active) {
        workspaceCommandCatalog.filter {
            (!it.requiresProject || model.project != null) && (!it.requiresFile || model.active != null)
        }
    }
    val snippets = remember(context) {
        val preferences = context.getSharedPreferences("editor", Context.MODE_PRIVATE)
        SavedSnippetCodec.decode(preferences.getString("saved-snippets-v1", ""))
    }
    val dirtyLocation = if (model.dirty) model.project?.let { name -> model.active?.let { WorkspaceLocation(name, it) } }
        else null
    val unsavedSource = if (dirtyLocation != null) model.source else null
    var fileResults by remember { mutableStateOf(emptyList<SearchResult>()) }
    var searching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    val needle = query.trim()
    val searchFiles = category == SearchCategory.All || category == SearchCategory.Files
    val searchCode = category == SearchCategory.All || category == SearchCategory.Code
    LaunchedEffect(needle, category, projects, dirtyLocation, unsavedSource) {
        fileResults = emptyList()
        searchError = null
        searching = needle.isNotEmpty() && (searchFiles || searchCode)
        if (!searching) return@LaunchedEffect
        try {
            delay(220)
            fileResults = model.findWorkspaceMatches(needle, projects, searchFiles, searchCode, dirtyLocation, unsavedSource)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            searchError = "Could not search files. Try again."
        } finally {
            searching = false
        }
    }
    val results = remember(needle, category, projects, fileResults, snippets, commands, model.recentCommandIds) {
        buildList {
            fun includes(type: SearchCategory) = category == type || category == SearchCategory.All
            fun matches(value: String) = value.contains(needle, ignoreCase = true)
            if (needle.isNotEmpty()) {
                if (includes(SearchCategory.Projects)) addAll(projects.filter(::matches).take(24).map {
                    SearchResult(SearchCategory.Projects, it, action = "project", body = it)
                })
                if (includes(SearchCategory.Settings)) addAll(settingsDestinations.filter { matches(it.name) || matches(it.detail) }.map {
                    SearchResult(SearchCategory.Settings, it.name, it.detail,
                        action = if (it.id == "languages") "languages" else "settings/" + it.id)
                })
                if (includes(SearchCategory.Tools)) addAll(discoverableTools.filter { matches(it.second) }.map { (action, title) ->
                    SearchResult(SearchCategory.Tools, title, action = action)
                })
                addAll(fileResults)
            }
            if (includes(SearchCategory.Snippets) && (needle.isNotEmpty() || isLibrary)) {
                addAll(snippets.filter { matches(it.title) || matches(it.body) }.take(30).map {
                    SearchResult(SearchCategory.Snippets, it.title, it.body.lineSequence().firstOrNull().orEmpty(), body = it.body)
                })
            }
            if (includes(SearchCategory.Templates) && (needle.isNotEmpty() || isLibrary)) {
                addAll(projectTemplates.filter(::matches).map {
                    SearchResult(SearchCategory.Templates, it, "Project template", action = "template")
                })
            }
            if (!isLibrary && includes(SearchCategory.Commands) && (needle.isNotEmpty() || category == SearchCategory.Commands)) {
                addAll(rankWorkspaceCommands(needle, commands, model.recentCommandIds).take(16).map {
                    SearchResult(SearchCategory.Commands, it.label, it.shortcut ?: "Command", action = it.id)
                })
            }
        }
    }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    fun open(result: SearchResult) {
        rememberQuery()
        keyboard?.hide()
        when {
            result.location != null -> {
                model.openWorkspaceLocation(result.location, result.line)
                close()
            }
            result.action == "project" -> {
                model.openProject(result.title)
                close()
            }
            result.action == "template" -> onTemplate(result.title)
            result.category == SearchCategory.Snippets -> selectedSnippet = result
            result.action != null -> {
                if (result.category == SearchCategory.Commands) model.rememberCommand(result.action)
                onAction(result.action)
            }
        }
    }
    val snippet = selectedSnippet
    if (snippet != null) {
        DestinationPage(snippet.title, close = { selectedSnippet = null }) {
            SelectionSnippet(snippet.body.orEmpty())
            Button(onClick = {
                val clipboard = context.getSystemService(ClipboardManager::class.java)
                clipboard?.setPrimaryClip(ClipData.newPlainText(snippet.title, snippet.body))
                notice = "Snippet copied"
            }, modifier = Modifier.padding(vertical = 12.dp)) { Text("Copy code") }
            notice?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
        return
    }
    DestinationPage(if (isLibrary) initialCategory.label else "Search", close) {
        CodeLabSearch(query, { query = it.take(160) }, if (isLibrary) "Find " + initialCategory.label.lowercase() else "Search CodeLab",
            modifier = Modifier.fillMaxWidth().focusRequester(focus),
            submit = { results.firstOrNull()?.let(::open) ?: rememberQuery() })
        if (!isLibrary) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SearchCategory.entries.forEach { choice ->
                    FilterChip(category == choice, { category = choice }, label = { Text(choice.label) },
                        modifier = Modifier.heightIn(min = 48.dp))
                }
            }
            LaunchedEffect(Unit) { focus.requestFocus() }
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (!isLibrary && needle.isEmpty() && category == SearchCategory.All) {
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Recent searches", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        if (history.isNotEmpty()) TextButton(onClick = { saveHistory(emptyList()) }) { Text("Clear history") }
                    }
                }
                items(history, key = { it }) { value ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton({ query = value }, Modifier.weight(1f)) { Text(value) }
                        IconButton(onClick = { saveHistory(history.filterNot { it == value }) }) {
                            Icon(Icons.Default.Close, "Remove search for $value")
                        }
                    }
                }
                item {
                    SettingToggle("Remember searches on this device", historyEnabled) {
                        historyEnabled = it
                        searchPreferences.edit().putBoolean("remember-history", it).apply()
                        if (!it) saveHistory(emptyList())
                    }
                }
            }
            if (searching) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            searchError?.let { problem -> item { Text(problem, color = MaterialTheme.colorScheme.error) } }
            if (!searching && results.isEmpty() && (needle.isNotEmpty() || isLibrary)) item {
                Text(if (needle.isEmpty() && initialCategory == SearchCategory.Snippets)
                    "Save a snippet from selected code in the editor to find it here." else "No matches",
                    Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(results) { result ->
                DestinationRow(result.title, result.subtitle, leading = {
                    when {
                        result.location != null -> FileTypeIcon(result.location.file, Modifier.size(24.dp))
                        result.category == SearchCategory.Templates -> ProjectLanguageIcon(result.title, Modifier.size(24.dp))
                        result.category == SearchCategory.Projects -> ProjectLanguageIcon(model.projectCardDetails[result.title]?.template)
                        result.category == SearchCategory.Snippets -> Icon(Icons.Default.ContentCut, null)
                        result.category == SearchCategory.Settings -> Icon(Icons.Default.Settings, null)
                        else -> Icon(Icons.Default.Terminal, null)
                    }
                }) { open(result) }
            }
        }
    }
}

@Composable
private fun ColumnScope.SelectionSnippet(body: String) {
    LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(vertical = 12.dp)) {
        item {
            androidx.compose.foundation.text.selection.SelectionContainer {
                Text(body, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

package site.lxwis.codelab

import androidx.compose.foundation.BorderStroke
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private data class StyleSample(val name: String, val summary: String)
private val styles = listOf(
    StyleSample("Obsidian", "Pure black"),
    StyleSample("Titanium", "Neutral metallic"),
    StyleSample("Crystal", "Liquid glass"),
    StyleSample("Midnight", "Deep navy"),
    StyleSample("Porcelain", "Refined light"),
    StyleSample("Sandstone", "Warm neutral")
)

/**
 * Render miniature previews with the SAME theme, backdrop and panel pipeline
 * used by the real app. Previously hand-picked sample palettes could lie
 * about the appearance of the selected theme.
 */
@Composable
private fun StyleCard(choice: StyleSample, isSelected: Boolean, onClick: () -> Unit,
    modifier: Modifier, model: WorkspaceModel
) {
    Surface(
        onClick = onClick, modifier = modifier.semantics {
            selected = isSelected
            // Provide a stable, contextual spoken label independent of decorative icons.
            contentDescription = if (isSelected) "${choice.name} style, selected"
                else "${choice.name} style"
        },
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLow,
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth().height(96.dp).clip(RoundedCornerShape(12.dp))) {
                CodeLabTheme(
                    themePreset = model.themePreset,
                    visualStyle = choice.name,
                    appearance = if (isSelected) model.appearance else CodeLabVisualStyles.defaultMode(choice.name),
                    accentPreset = if (isSelected) model.accent else AppearanceRecipes.defaultAccent(choice.name),
                    highContrast = model.highContrast,
                    largeInterfaceText = false,
                    studio = if (isSelected) model.studioAppearance else StudioAppearance()
                ) { dark ->
                    CodeLabBackdrop(choice.name, dark) {
                        Column(Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Code, null, Modifier.size(13.dp),
                                    tint = MaterialTheme.colorScheme.primary)
                                Text("main.js", Modifier.weight(1f).padding(start = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface)
                                Icon(Icons.Default.PlayArrow, null, Modifier.size(13.dp),
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                            ClayPanel {
                                Column(Modifier.fillMaxWidth().padding(6.dp)) {
                                    Text("1  const x = 42;", fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                                    Text("2  print(x);", fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(choice.name, style = MaterialTheme.typography.titleSmall)
                    Text(choice.summary, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isSelected) Icon(Icons.Default.CheckCircle, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(19.dp))
            }
        }
    }
}

@Composable
private fun SettingChoices(title: String, choices: List<String>, currentChoice: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            choices.forEach { choice ->
                val active = currentChoice == choice
                Surface(onClick = { onSelect(choice) },
                    modifier = Modifier.weight(1f).semantics { selected = active },
                    shape = RoundedCornerShape(12.dp),
                    color = if (active) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerLow) {
                    Text(choice, Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .padding(horizontal = 3.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (active) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

/** Full-width, scrollable visual settings page, replacing the cramped modal. */
@Composable
internal fun AppearanceGallery(m: WorkspaceModel, openStudio: Boolean = false, dismiss: () -> Unit) {
    val galleryState = rememberLazyListState(initialFirstVisibleItemIndex = if (openStudio) 6 else 0)
    var customHex by remember(m.visualStyle) {
        mutableStateOf(if (m.accent.startsWith("#")) m.accent else "#")
    }
    var recipeName by rememberSaveable { mutableStateOf("") }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf("") }
    var advanced by rememberSaveable { mutableStateOf(false) }
    var menuId by remember { mutableStateOf<String?>(null) }
    var renameId by rememberSaveable { mutableStateOf<String?>(null) }
    var renameName by rememberSaveable { mutableStateOf("") }
    var renameError by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var exportId by rememberSaveable { mutableStateOf<String?>(null) }
    val exportTheme = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val recipe = m.savedAppearanceRecipes.firstOrNull { it.id == exportId }
        if (uri != null && recipe != null) {
            val json = AppearanceRecipes.encode(listOf(recipe))
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    runCatching { checkNotNull(context.contentResolver.openOutputStream(uri, "wt")).bufferedWriter().use { it.write(json) } }.isSuccess
                }
                notice = if (ok) "Theme exported. Imported font files remain local." else "Could not export theme"
            }
        }
        exportId = null
    }
    val importTheme = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val json = withContext(Dispatchers.IO) {
                runCatching {
                    checkNotNull(context.contentResolver.openInputStream(uri)).bufferedReader().use { reader ->
                        val buffer = CharArray(4096)
                        val result = StringBuilder()
                        while (true) {
                            val n = reader.read(buffer)
                            if (n < 0) break
                            require(result.length + n <= 65_536)
                            result.append(buffer, 0, n)
                        }
                        result.toString()
                    }
                }.getOrNull()
            }
            notice = if (json != null && m.importAppearanceRecipe(json)) "Appearance imported" else "Invalid or oversized theme file"
        }
    }
    var importOpen by remember { mutableStateOf(false) }
    var importText by rememberSaveable { mutableStateOf("") }
    if (importOpen) AlertDialog(onDismissRequest = { importOpen = false },
        title = { Text("Import appearance") },
        text = { OutlinedTextField(importText, { importText = it.take(65536) },
            label = { Text("Paste exported theme JSON") }, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { TextButton(onClick = {
            notice = if (m.importAppearanceRecipe(importText)) "Appearance imported" else "Invalid theme JSON"
            importOpen = false
        }) { Text("Import") } },
        dismissButton = { TextButton(onClick = { importOpen = false }) { Text("Cancel") } })
    if (renameId != null) AlertDialog(onDismissRequest = { renameId = null },
        title = { Text("Rename theme") }, text = {
            OutlinedTextField(renameName, { renameName = it.take(32); renameError = false },
                label = { Text("Theme name") }, singleLine = true, isError = renameError,
                supportingText = { if (renameError) Text("Enter a name using 1–32 characters") })
        }, confirmButton = { TextButton(onClick = {
            val id = renameId ?: return@TextButton
            if (m.renameAppearanceRecipe(id, renameName)) {
                if (editingId == id) recipeName = renameName.trim()
                renameId = null
                notice = "Theme renamed"
            } else renameError = true
        }) { Text("Rename") } }, dismissButton = { TextButton(onClick = { renameId = null }) { Text("Cancel") } })
    Dialog(onDismissRequest = dismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = dismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Close appearance")
                    }
                    Text(if (openStudio) "Theme Studio" else "Appearance", Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineSmall)
                    TextButton(onClick = dismiss) { Text("Done") }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .4f))
                LazyColumn(Modifier.weight(1f), state = galleryState,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Themes", style = MaterialTheme.typography.titleLarge)
                            Text("Preview a visual treatment. Your editor stays readable and your files stay unchanged.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    items(styles.chunked(2)) { row ->
                        BoxWithConstraints(Modifier.fillMaxWidth()) {
                            if (maxWidth < 340.dp) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    row.forEach { style ->
                                        StyleCard(style, isSelected = CodeLabVisualStyles.canonical(m.visualStyle) == style.name,
                                            onClick = { m.chooseVisualStyle(style.name) },
                                            modifier = Modifier.fillMaxWidth(), model = m)
                                    }
                                }
                            } else {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    row.forEach { style ->
                                        StyleCard(style, isSelected = CodeLabVisualStyles.canonical(m.visualStyle) == style.name,
                                            onClick = { m.chooseVisualStyle(style.name) },
                                            modifier = Modifier.weight(1f), model = m)
                                    }
                                    if (row.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SettingChoices("COLOR MODE", listOf("Dark", "Light", "System"),
                                m.appearance, m::setMode)
                            Text("Obsidian is pure black. Porcelain and Sandstone use light palettes.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("ACCENT", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            AppearanceAccentSwatches(m.accent, m::chooseAccent)
                            Text("Selected: " + m.accent,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedTextField(
                                value = customHex,
                                onValueChange = { next ->
                                    if (next.length <= 7) {
                                        customHex = next
                                        if (AppearanceRecipes.validAccent(next)) m.chooseAccent(next)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Custom color (hex)") },
                                placeholder = { Text("#AABBCC") },
                                singleLine = true,
                                isError = customHex.length > 1 &&
                                    !AppearanceRecipes.validAccent(customHex),
                                supportingText = {
                                    Text("Enter #RRGGBB to preview. Text contrast adapts automatically.")
                                })
                        }
                    }
                    item { StudioAppearanceControls(m, initiallyOpen = openStudio) }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("Saved appearances", Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleLarge)
                                Text(m.savedAppearanceRecipes.size.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                TextButton(onClick = { importTheme.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }) { Text("Import file") }
                                TextButton(onClick = { importText = ""; importOpen = true }) { Text("Paste JSON") }
                            }
                            Text("Save or transfer a look. Custom font assets stay on this device.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall)
                            m.savedAppearanceRecipes.forEach { recipe ->
                                Surface(onClick = {
                                    m.useAppearanceRecipe(recipe.id)
                                    editingId = recipe.id
                                    recipeName = recipe.name
                                    notice = "Loaded " + recipe.name
                                }, color = MaterialTheme.colorScheme.surfaceContainer,
                                    shape = RoundedCornerShape(16.dp)) {
                                    Row(Modifier.fillMaxWidth().padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Palette, contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary)
                                        Column(Modifier.weight(1f)) {
                                            Text(recipe.name, style = MaterialTheme.typography.titleSmall)
                                            Text(recipe.style + " · " + recipe.accent + (recipe.motion?.let { " · " + it.preference } ?: ""),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Box {
                                            IconButton(onClick = { menuId = recipe.id }) {
                                                Icon(Icons.Default.MoreVert, "Actions for theme " + recipe.name)
                                            }
                                            DropdownMenu(menuId == recipe.id, { menuId = null }) {
                                                DropdownMenuItem(text = { Text("Edit") }, onClick = {
                                                    menuId = null
                                                    m.useAppearanceRecipe(recipe.id)
                                                    editingId = recipe.id
                                                    recipeName = recipe.name
                                                    notice = "Adjust the look, then select Update"
                                                })
                                                DropdownMenuItem(text = { Text("Rename") }, onClick = {
                                                    menuId = null; renameId = recipe.id; renameName = recipe.name; renameError = false
                                                })
                                                DropdownMenuItem(text = { Text("Duplicate") }, onClick = {
                                                    menuId = null
                                                    notice = if (m.duplicateAppearanceRecipe(recipe.id)) "Theme duplicated" else "Could not duplicate theme"
                                                })
                                                DropdownMenuItem(text = { Text("Export") }, onClick = {
                                                    menuId = null; exportId = recipe.id
                                                    exportTheme.launch(recipe.name.replace(Regex("[^a-zA-Z0-9_-]"), "_") + ".json")
                                                })
                                                HorizontalDivider()
                                                DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                                    onClick = { menuId = null; pendingDelete = recipe.id })
                                            }
                                        }
                                    }
                                }
                            }
                            OutlinedTextField(value = recipeName, onValueChange = {
                                if (it.length <= 32) recipeName = it
                            }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                                label = { Text("Preset name") },
                                placeholder = { Text("My workspace") })
                            Row(Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Button(onClick = {
                                    val saved = m.saveAppearanceRecipe(recipeName, editingId)
                                    notice = if (saved) "Appearance saved"
                                        else "Name must be 1–32 characters"
                                    if (saved) { editingId = null; recipeName = "" }
                                }, enabled = recipeName.isNotBlank()) {
                                    Icon(Icons.Default.Save, contentDescription = null,
                                        modifier = Modifier.size(17.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (editingId == null) "Save new" else "Update")
                                }
                                if (editingId != null) TextButton(onClick = {
                                    editingId = null
                                    recipeName = ""
                                    notice = ""
                                }) { Text("Cancel edit") }
                            }
                            if (notice.isNotBlank()) Text(notice,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary)
                            TextButton(onClick = {
                                m.restoreAppearanceDefaults()
                                editingId = null
                                recipeName = ""
                                customHex = "#"
                                notice = "Style defaults restored; saved looks unchanged"
                            }) {
                                Icon(Icons.Default.Restore, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Restore " + m.visualStyle + " defaults")
                            }
                        }
                    }
                    item {
                        TextButton(onClick = { advanced = !advanced }) {
                            Text(if (advanced) "Hide advanced options" else "Advanced options")
                            Icon(if (advanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null)
                        }
                    }
                    if (advanced) item {
                        SettingChoices("INTERFACE DENSITY",
                            listOf("Compact", "Comfortable", "Spacious"),
                            m.interfaceDensity, m::chooseDensity)
                    }
                    if (advanced) item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SettingChoices("ANIMATIONS", listOf("Reduced", "Standard", "Fluid"),
                                when (m.motionPreference) { "System" -> "Standard"; "Full" -> "Fluid"; else -> m.motionPreference }, m::chooseMotionPreset)
                            Text("Motion intensity · " + m.motionIntensity + "%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Slider(value = m.motionIntensity.toFloat(),
                                onValueChange = { m.chooseMotionIntensity(it.toInt()) },
                                valueRange = 0f..100f)
                            Text("Animation speed · " + m.motionSpeed + "%", style = MaterialTheme.typography.bodySmall)
                            Slider(m.motionSpeed.toFloat(), { m.chooseMotionSpeed(it.toInt()) }, valueRange = 50f..200f)
                            Text("Spring response · " + m.springIntensity + "%", style = MaterialTheme.typography.bodySmall)
                            Slider(m.springIntensity.toFloat(), { m.chooseSpringIntensity(it.toInt()) }, valueRange = 0f..100f)
                            Text("Reduced motion and Android's Remove animations setting are respected.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    item {
                        Text("Editor font size, keyboard controls and highlighting remain in the editor settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
    pendingDelete?.let { id ->
        val recipe = m.savedAppearanceRecipes.firstOrNull { it.id == id }
        AlertDialog(onDismissRequest = { pendingDelete = null },
            title = { Text("Delete saved appearance?") },
            text = { Text("Delete " + (recipe?.name ?: "this preset") +
                "? Your current theme will not change.") },
            confirmButton = {
                TextButton(onClick = {
                    m.deleteAppearanceRecipe(id)
                    if (editingId == id) { editingId = null; recipeName = "" }
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            })
    }
}

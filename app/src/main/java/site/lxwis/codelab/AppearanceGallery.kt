package site.lxwis.codelab

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    StyleSample("Minimal", "Clean & focused"),
    StyleSample("Glass", "Frosted & luminous"),
    StyleSample("Clay", "Raised & tactile"),
    StyleSample("AMOLED", "Pure black"),
    StyleSample("Paper", "Warm & bright")
)

/**
 * Render miniature previews with the SAME theme, backdrop and panel pipeline
 * used by the real app. Previously hand-picked sample palettes could lie
 * about the appearance of the selected theme.
 */
@Composable
private fun StyleCard(choice: StyleSample, selected: Boolean, onClick: () -> Unit,
    modifier: Modifier, model: WorkspaceModel
) {
    Surface(
        onClick = onClick, modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLow,
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth().height(119.dp).clip(RoundedCornerShape(12.dp))) {
                CodeLabTheme(
                    themePreset = model.themePreset,
                    visualStyle = choice.name,
                    appearance = model.appearance,
                    accentPreset = model.accent,
                    highContrast = model.highContrast,
                    largeInterfaceText = false
                ) { dark ->
                    CodeLabBackdrop(choice.name, dark) {
                        Column(Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                repeat(3) { index ->
                                    Box(Modifier.size(6.dp).background(
                                        MaterialTheme.colorScheme.primary.copy(
                                            alpha = if (index == 0) 1f else .55f), CircleShape))
                                }
                            }
                            ClayPanel {
                                Column(Modifier.fillMaxWidth().padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Box(Modifier.fillMaxWidth(.75f).height(5.dp).background(
                                        MaterialTheme.colorScheme.primary, CircleShape))
                                    Box(Modifier.fillMaxWidth(.92f).height(4.dp).background(
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .45f),
                                        CircleShape))
                                    Box(Modifier.fillMaxWidth(.55f).height(4.dp).background(
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .45f),
                                        CircleShape))
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
                if (selected) Icon(Icons.Default.CheckCircle, contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(19.dp))
            }
        }
    }
}

@Composable
private fun SettingChoices(title: String, choices: List<String>, selected: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            choices.forEach { choice ->
                val active = selected == choice
                Surface(onClick = { onSelect(choice) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = if (active) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerLow) {
                    Text(choice, Modifier.padding(vertical = 12.dp, horizontal = 3.dp),
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
internal fun AppearanceGallery(m: WorkspaceModel, dismiss: () -> Unit) {
    var customHex by remember(m.visualStyle) {
        mutableStateOf(if (m.accent.startsWith("#")) m.accent else "#")
    }
    var recipeName by rememberSaveable { mutableStateOf("") }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf("") }
    var advanced by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }
    Dialog(onDismissRequest = dismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = dismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Close appearance")
                    }
                    Text("Appearance", Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineSmall)
                    TextButton(onClick = dismiss) { Text("Done") }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .4f))
                LazyColumn(Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Choose your style", style = MaterialTheme.typography.titleLarge)
                            Text("Preview a visual treatment. Your editor stays readable and your files stay unchanged.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    items(styles.chunked(2)) { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { style ->
                                StyleCard(style, selected = m.visualStyle == style.name,
                                    onClick = { m.chooseVisualStyle(style.name) },
                                    modifier = Modifier.weight(1f), model = m)
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SettingChoices("COLOR MODE", listOf("Dark", "Light", "System"),
                                m.appearance, m::setMode)
                            Text("AMOLED uses true black; Paper uses its dedicated light palette.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("ACCENT", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                listOf(
                                    "Mint" to Color(0xff83e5ca),
                                    "Blue" to Color(0xff8ab7fa),
                                    "Violet" to Color(0xffc4b0ff),
                                    "Amber" to Color(0xffffc978),
                                    "Rose" to Color(0xffff92b7),
                                    "Mono" to Color(0xffaeb8bb)
                                ).forEach { (name, color) ->
                                    Surface(onClick = { m.chooseAccent(name) },
                                        shape = CircleShape,
                                        color = color,
                                        border = if (m.accent == name)
                                            BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null) {
                                        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                            if (m.accent == name)
                                                Icon(Icons.Default.Check, contentDescription = name + " selected",
                                                    tint = Color(0xff162426), modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
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
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("Saved appearances", Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleLarge)
                                Text(m.savedAppearanceRecipes.size.toString() + "/" +
                                    AppearanceRecipes.LIMIT,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("Load looks from any style, or save the current one.",
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
                                            Text(recipe.style + " · " + recipe.accent,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(onClick = {
                                            m.useAppearanceRecipe(recipe.id)
                                            editingId = recipe.id
                                            recipeName = recipe.name
                                            notice = "Adjust the look, then select Update"
                                        }) {
                                            Icon(Icons.Default.Edit,
                                                contentDescription = "Edit " + recipe.name)
                                        }
                                        IconButton(onClick = { pendingDelete = recipe.id }) {
                                            Icon(Icons.Default.DeleteOutline,
                                                contentDescription = "Delete " + recipe.name)
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
                                        else "Name must be 1–32 characters; maximum 20 presets"
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
                            SettingChoices("ANIMATIONS", listOf("System", "Full", "Reduced"),
                                m.motionPreference, m::chooseMotion)
                            Text("Motion intensity · " + m.motionIntensity + "%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Slider(value = m.motionIntensity.toFloat(),
                                onValueChange = { m.chooseMotionIntensity(it.toInt()) },
                                valueRange = 0f..100f)
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

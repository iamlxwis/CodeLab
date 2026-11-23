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
import androidx.compose.runtime.Composable
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
                        }
                    }
                    item {
                        SettingChoices("INTERFACE DENSITY",
                            listOf("Compact", "Comfortable", "Spacious"),
                            m.interfaceDensity, m::chooseDensity)
                    }
                    item {
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
}

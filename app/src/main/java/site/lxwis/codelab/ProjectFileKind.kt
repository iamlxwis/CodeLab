package site.lxwis.codelab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier

internal enum class ProjectFileKind {
    Code, Markup, Stylesheet, Data, Document, Image, Other
}

/** Deliberately independent of a runtime's ability to execute a language. */
internal fun projectFileKind(path: String): ProjectFileKind {
    val extension = languageExtension(path)
    return when (extension) {
        "js", "ts", "jsx", "tsx", "kt", "kts", "java", "lua", "py", "rb", "go",
        "rs", "c", "h", "cpp", "hpp", "cs", "php", "swift", "sh", "bash",
        "dart", "scala", "r", "sql" -> ProjectFileKind.Code
        "html", "htm", "xml", "svg" -> ProjectFileKind.Markup
        "css", "scss", "sass", "less" -> ProjectFileKind.Stylesheet
        "json", "jsonl", "yaml", "yml", "toml", "ini", "csv", "db",
        "sqlite" -> ProjectFileKind.Data
        "md", "txt", "rst", "log", "pdf" -> ProjectFileKind.Document
        "png", "jpg", "jpeg", "gif", "webp", "bmp", "avif" -> ProjectFileKind.Image
        else -> ProjectFileKind.Other
    }
}

@Composable
internal fun FileTypeIcon(path: String, modifier: Modifier = Modifier, selected: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    val iconTint by animateColorAsState(if (selected) colors.primary else colors.onSurfaceVariant,
        tween(workspaceMotion().millis(140)), label = "file-icon-state")
    val extension = languageExtension(path)
    val visual = languageVisual(extension)
    if (visual != null) {
        val tint = contrastAccent(visual.color, colors.surfaceContainer)
        val logo = visual.logo
        if (logo != null) {
            val opticalInset = if (extension in setOf("js", "mjs", "cjs", "ts", "toml")) 1.dp else 0.dp
            Icon(androidx.compose.ui.res.painterResource(logo), null, modifier.padding(opticalInset), tint = tint)
        } else {
            val tile = visual.color.copy(alpha = .15f).compositeOver(colors.surfaceContainer)
            val density = LocalDensity.current
            Box(modifier.background(tile, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                CompositionLocalProvider(LocalDensity provides Density(density.density, 1f)) {
                    Text(visual.monogram.orEmpty(), color = contrastAccent(tint, tile), fontSize = 7.sp,
                        lineHeight = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }
            }
        }
        return
    }
    val icon = when (projectFileKind(path)) {
        ProjectFileKind.Code -> Icons.Default.Code
        ProjectFileKind.Markup -> Icons.Default.Language
        ProjectFileKind.Stylesheet -> Icons.Default.Palette
        ProjectFileKind.Data -> Icons.Default.DataObject
        ProjectFileKind.Document -> Icons.Default.Description
        ProjectFileKind.Image -> Icons.Default.Image
        ProjectFileKind.Other -> Icons.Default.InsertDriveFile
    }
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = modifier,
        tint = iconTint
    )
}

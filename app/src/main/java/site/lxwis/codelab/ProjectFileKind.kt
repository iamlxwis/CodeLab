package site.lxwis.codelab

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.util.Locale

internal enum class ProjectFileKind {
    Code, Markup, Stylesheet, Data, Document, Image, Other
}

/** Deliberately independent of a runtime's ability to execute a language. */
internal fun projectFileKind(path: String): ProjectFileKind {
    val extension = path.substringAfterLast('/').substringAfterLast('.', "")
        .lowercase(Locale.ROOT)
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
        tint = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

package site.lxwis.codelab

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val extension = path.substringAfterLast('/').substringAfterLast('.', "").lowercase(Locale.ROOT)
    val badge = when (extension) {
        "js", "jsx" -> "JS" to 0xffc5a629
        "ts", "tsx" -> "TS" to 0xff3178c6
        "py" -> "Py" to 0xff3476a8
        "java" -> "J" to 0xffb65b35
        "lua" -> "L" to 0xff555ca7
        "kt", "kts" -> "K" to 0xff9661c7
        "html", "htm" -> "H" to 0xffd75c39
        "css" -> "C" to 0xff3478bd
        "sql", "sqlite" -> "DB" to 0xff4089a2
        "cpp", "hpp" -> "C+" to 0xff536cbb
        "c" -> "C" to 0xff526a9b
        "rs" -> "R" to 0xffa66740
        "go" -> "Go" to 0xff387d91
        "json" -> "{}" to 0xff7c7f87
        else -> null
    }
    if (badge != null) {
        Box(modifier.background(Color(badge.second).copy(alpha = .22f), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center) {
            Text(badge.first, fontSize = 8.sp, fontWeight = FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1)
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
        tint = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

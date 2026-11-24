package site.lxwis.codelab

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
    val iconTint by animateColorAsState(if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant, tween(workspaceMotion().millis(140)), label = "file-icon-state")
    val logo = when (extension) {
        "js", "jsx", "mjs", "cjs" -> R.drawable.ic_lang_javascript
        "ts", "tsx" -> R.drawable.ic_lang_typescript
        "py", "pyw" -> R.drawable.ic_lang_python
        "java" -> R.drawable.ic_lang_java
        "lua" -> R.drawable.ic_lang_lua
        "kt", "kts" -> R.drawable.ic_lang_kotlin
        "html", "htm" -> R.drawable.ic_lang_html5
        "css", "scss", "sass", "less" -> R.drawable.ic_lang_css
        "sql", "sqlite", "db" -> R.drawable.ic_lang_sqlite
        "cpp", "hpp", "cc", "cxx", "hxx" -> R.drawable.ic_lang_cplusplus
        "c", "h" -> R.drawable.ic_lang_c
        "rs" -> R.drawable.ic_lang_rust
        "go" -> R.drawable.ic_lang_go
        "json", "jsonl" -> R.drawable.ic_lang_json
        "php" -> R.drawable.ic_lang_php
        "rb" -> R.drawable.ic_lang_ruby
        "swift" -> R.drawable.ic_lang_swift
        "dart" -> R.drawable.ic_lang_dart
        "sh", "bash" -> R.drawable.ic_lang_gnubash
        else -> null
    }
    if (logo != null) {
        // Monochrome glyphs inherit the readable theme foreground; active tabs use accent.
        Icon(androidx.compose.ui.res.painterResource(logo), contentDescription = null,
            modifier = modifier, tint = iconTint)
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

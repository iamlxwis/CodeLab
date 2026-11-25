package site.lxwis.codelab

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

internal val appAppearanceModes = listOf("System", "Light", "Dark")

internal fun migrateAppearanceMode(saved: String?): String = saved
    ?.takeIf { it in appAppearanceModes } ?: "System"

internal fun appearanceIsDark(appearance: String, systemDark: Boolean): Boolean = when (appearance) {
    "Light" -> false
    "Dark" -> true
    else -> systemDark
}

private val night = darkColorScheme(
    primary = Color(0xffeeeeef), onPrimary = Color(0xff131314),
    primaryContainer = Color(0xff373739), onPrimaryContainer = Color(0xfffbfbfc),
    secondary = Color(0xffb9b9bd), onSecondary = Color.Black,
    secondaryContainer = Color(0xff29292a), onSecondaryContainer = Color(0xffeeeeef),
    tertiary = Color(0xff79d4a1), onTertiary = Color(0xff042413),
    tertiaryContainer = Color(0xff17452d), onTertiaryContainer = Color(0xffd3f4e0),
    inverseSurface = Color(0xffeeeeef), inverseOnSurface = Color(0xff242426), inversePrimary = Color(0xff262628),
    background = Color.Black, onBackground = Color(0xfff5f5f6),
    surface = Color.Black, onSurface = Color(0xfff5f5f6),
    surfaceVariant = Color(0xff222223), onSurfaceVariant = Color(0xffb0b0b5),
    outline = Color(0xff858589), outlineVariant = Color(0xff38383a),
    surfaceContainerLowest = Color(0xff080808), surfaceContainerLow = Color(0xff101011),
    surfaceContainer = Color(0xff171718), surfaceContainerHigh = Color(0xff232324),
    surfaceContainerHighest = Color(0xff303031), surfaceBright = Color(0xff333335),
    surfaceDim = Color.Black, surfaceTint = Color(0xffeeeeef),
    error = Color(0xffffa9a5), onError = Color(0xff4f0704),
    errorContainer = Color(0xff662622), onErrorContainer = Color(0xffffdedb)
)
private val day = lightColorScheme(
    primary = Color(0xff262628), onPrimary = Color.White,
    primaryContainer = Color(0xffe7e7ea), onPrimaryContainer = Color(0xff141416),
    secondary = Color(0xff505054), onSecondary = Color.White,
    secondaryContainer = Color(0xffeeeef0), onSecondaryContainer = Color(0xff252527),
    tertiary = Color(0xff17623d), onTertiary = Color.White,
    tertiaryContainer = Color(0xffd3f4e0), onTertiaryContainer = Color(0xff0b3520),
    inverseSurface = Color(0xff242426), inverseOnSurface = Color(0xffeeeeef), inversePrimary = Color(0xffeeeeef),
    background = Color.White, onBackground = Color(0xff171719),
    surface = Color.White, onSurface = Color(0xff171719),
    surfaceVariant = Color(0xffebebee), onSurfaceVariant = Color(0xff59595f),
    outline = Color(0xff77777d), outlineVariant = Color(0xffd6d6da),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xfff7f7f8),
    surfaceContainer = Color(0xfff3f3f5), surfaceContainerHigh = Color(0xffededef),
    surfaceContainerHighest = Color(0xffe4e4e7), surfaceBright = Color.White,
    surfaceDim = Color(0xffdedee1), surfaceTint = Color(0xff262628),
    error = Color(0xffa82126), onError = Color.White,
    errorContainer = Color(0xffffdada), onErrorContainer = Color(0xff42090d)
)

internal fun codeLabColorScheme(dark: Boolean, highContrast: Boolean = false): ColorScheme {
    val base = if (dark) night else day
    if (!highContrast) return base
    val ink = if (dark) Color.White else Color.Black
    return base.copy(onSurface = ink, onBackground = ink, onSurfaceVariant = ink, outline = ink)
}

@Composable
internal fun CodeLabTheme(
    appearance: String,
    highContrast: Boolean,
    largeInterfaceText: Boolean,
    editorAppearance: EditorAppearance = EditorAppearance(),
    content: @Composable (Boolean) -> Unit
) {
    val dark = appearanceIsDark(appearance, isSystemInDarkTheme())
    val scheme = remember(dark, highContrast) { codeLabColorScheme(dark, highContrast) }
    val fontContext = LocalContext.current
    val typography = remember(largeInterfaceText, editorAppearance.uiFont, editorAppearance.uiFontRevision, editorAppearance.uiScale) {
        val family = when (editorAppearance.uiFont) {
            "System" -> FontFamily.Default
            "Serif" -> FontFamily.Serif
            "Custom" -> ImportedFonts.load(fontContext, false, editorAppearance.uiFontRevision)?.let { FontFamily(it) }
                ?: FontFamily(Font(R.font.inter))
            else -> FontFamily(Font(R.font.inter))
        }
        val baseTypography = Typography()
            val textScale = (if (largeInterfaceText) 1.16f else 1f) * editorAppearance.uiScale.coerceIn(80, 140) / 100f
        fun styled(style: androidx.compose.ui.text.TextStyle, weight: FontWeight? = null) =
            style.copy(fontFamily = family, fontSize = style.fontSize * textScale, lineHeight = style.lineHeight * textScale,
                fontWeight = weight ?: style.fontWeight)
        Typography(
            displayLarge = styled(baseTypography.displayLarge),
            displayMedium = styled(baseTypography.displayMedium),
            displaySmall = styled(baseTypography.displaySmall),
            headlineLarge = styled(baseTypography.headlineLarge, FontWeight.Bold),
            headlineMedium = styled(baseTypography.headlineMedium, FontWeight.SemiBold),
            headlineSmall = styled(baseTypography.headlineSmall, FontWeight.SemiBold),
            titleLarge = styled(baseTypography.titleLarge, FontWeight.SemiBold),
            titleMedium = styled(baseTypography.titleMedium, FontWeight.SemiBold),
            titleSmall = styled(baseTypography.titleSmall, FontWeight.Medium),
            bodyLarge = styled(baseTypography.bodyLarge),
            bodyMedium = styled(baseTypography.bodyMedium),
            bodySmall = styled(baseTypography.bodySmall),
            labelLarge = styled(baseTypography.labelLarge),
            labelMedium = styled(baseTypography.labelMedium),
            labelSmall = styled(baseTypography.labelSmall)
        )
    }
    CompositionLocalProvider(LocalEditorAppearance provides editorAppearance) {
        MaterialTheme(colorScheme = scheme, typography = typography, shapes = codeLabShapes) {
            content(dark)
        }
    }
}

private val codeLabShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
internal fun CodeLabForegroundSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier, color = MaterialTheme.colorScheme.background,
        contentColor = studioReadable(MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.background), content = content
    )
}


/** Resolve a saved editor color against the active code palette. */
internal fun studioColor(value: String, fallback: Color): Color =
    if (value.matches(Regex("#[0-9a-fA-F]{6}"))) Color(0xff000000 or value.drop(1).toLong(16)) else fallback

internal fun studioReadable(foreground: Color, background: Color): Color {
    val a = foreground.luminance()
    val b = background.luminance()
    return if ((maxOf(a, b) + .05f) / (minOf(a, b) + .05f) >= 4.5f) foreground
        else if (b > .179f) Color.Black else Color.White
}

/** Smallest practical tint toward black/white that keeps text accents legible. */
internal fun contrastAccent(accent: Color, background: Color): Color {
    fun ratio(color: Color): Float {
        val a = color.luminance(); val b = background.luminance()
        return (maxOf(a, b) + .05f) / (minOf(a, b) + .05f)
    }
    if (ratio(accent) >= 4.5f) return accent
    val target = studioReadable(accent, background)
    for (step in 1..20) {
        val mixed = androidx.compose.ui.graphics.lerp(accent, target, step / 20f)
        if (ratio(mixed) >= 4.5f) return mixed
    }
    return target
}

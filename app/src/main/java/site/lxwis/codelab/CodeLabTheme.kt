package site.lxwis.codelab

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val night = darkColorScheme(
    primary = Color(0xff83e5ca), onPrimary = Color(0xff083329),
    primaryContainer = Color(0xff1d3c35), onPrimaryContainer = Color(0xffadf4df),
    surface = Color(0xff101517), background = Color(0xff101517),
    surfaceVariant = Color(0xff20292d), onSurfaceVariant = Color(0xffacbbbd),
    onSurface = Color(0xffe6eeed), outlineVariant = Color(0xff364145),
    surfaceContainer = Color(0xff191f22), surfaceContainerHigh = Color(0xff222a2d),
    surfaceContainerHighest = Color(0xff2b3436), surfaceBright = Color(0xff263033)
)
private val day = lightColorScheme(
    primary = Color(0xff006b57), onPrimary = Color.White,
    primaryContainer = Color(0xffc2efdf), onPrimaryContainer = Color(0xff103c32),
    surface = Color(0xfff5f8f7), background = Color(0xfff5f8f7),
    surfaceVariant = Color(0xffe2eae7), onSurfaceVariant = Color(0xff495852),
    onSurface = Color(0xff152322), outlineVariant = Color(0xffbdcbc5),
    surfaceContainer = Color(0xffebf1ef), surfaceContainerHigh = Color(0xffe3ece9),
    surfaceContainerHighest = Color(0xffdce6e3)
)

@Composable
internal fun CodeLabTheme(
    themePreset: String,
    visualStyle: String,
    appearance: String,
    accentPreset: String,
    highContrast: Boolean,
    largeInterfaceText: Boolean,
    content: @Composable (Boolean) -> Unit
) {
    val dark = when (CodeLabVisualStyles.canonical(visualStyle)) {
        "Obsidian" -> true
        "Porcelain", "Sandstone" -> false
        else -> appearance == "Dark" || (appearance == "System" && isSystemInDarkTheme())
    }
    // Do not allocate a new ColorScheme on unrelated editor recompositions.
    val scheme = remember(themePreset, visualStyle, accentPreset, highContrast, dark) {
            val palette = if (dark) night else day
            val base = when (CodeLabVisualStyles.canonical(visualStyle)) {
                "Obsidian" -> night.copy(
                    background = Color.Black, surface = Color.Black,
                    surfaceContainer = Color(0xff090909),
                    surfaceContainerHigh = Color(0xff121212),
                    surfaceVariant = Color(0xff0a0a0a),
                    outlineVariant = Color(0xff303030))
                "Sandstone" -> day.copy(
                    background = Color(0xfffaf8f1), surface = Color(0xfffaf8f1),
                    surfaceContainer = Color(0xfff3efe5),
                    surfaceContainerHigh = Color(0xffeae5d8),
                    surfaceVariant = Color(0xffe9e4d8),
                    onSurface = Color(0xff272922),
                    onSurfaceVariant = Color(0xff555950))
                "Crystal" -> palette.copy(
                    background = if (dark) Color(0xff111416) else Color(0xffeef0f1),
                    surface = if (dark) Color(0xff24282b) else Color(0xfff7f8f9),
                    surfaceContainer = if (dark) Color(0xff393d41).copy(alpha = .77f)
                        else Color.White.copy(alpha = .73f),
                    surfaceContainerHigh = if (dark) Color(0xff494d51).copy(alpha = .84f)
                        else Color.White.copy(alpha = .86f),
                    surfaceVariant = if (dark) Color(0xff34383c) else Color(0xffe6e9ec),
                    onSurfaceVariant = if (dark) Color(0xffd1d6d9) else Color(0xff454a50),
                    outlineVariant = if (dark) Color(0xff7c8388) else Color(0xffb8bec2))
                "Titanium" -> if (dark) palette.copy(
                    background = Color(0xff17191c), surface = Color(0xff17191c),
                    surfaceContainer = Color(0xff22252a), surfaceContainerHigh = Color(0xff30343a),
                    surfaceVariant = Color(0xff30343a), onSurface = Color(0xffe5e5e7),
                    onSurfaceVariant = Color(0xffb0b3ba), outlineVariant = Color(0xff41454c)
                ) else palette.copy(background = Color(0xfff2f2f7), surface = Color.White,
                    surfaceContainer = Color(0xfff2f2f7), surfaceContainerHigh = Color(0xffe5e5ea))
                "Midnight" -> if (dark) palette.copy(
                    background = Color(0xff090f1d), surface = Color(0xff090f1d),
                    surfaceContainer = Color(0xff141f31), surfaceContainerHigh = Color(0xff182438),
                    surfaceVariant = Color(0xff24334c), onSurface = Color(0xffe6edf9),
                    onSurfaceVariant = Color(0xffb7c8e2), outlineVariant = Color(0xff354965)
                ) else palette
                "Porcelain" -> day.copy(
                    background = Color(0xfff5f5f7), surface = Color.White,
                    surfaceContainer = Color(0xffededf0), surfaceContainerHigh = Color(0xffe5e5ea),
                    surfaceVariant = Color(0xffe5e5ea), onSurface = Color(0xff1c1c1e),
                    onSurfaceVariant = Color(0xff515158), outlineVariant = Color(0xffc7c7cc))
                else -> if (!dark) palette else when (themePreset) {
                    "Nocturne" -> palette.copy(
                        background = Color(0xff0c1020), surface = Color(0xff0c1020),
                        surfaceContainer = Color(0xff151a2d),
                        surfaceContainerHigh = Color(0xff20263b))
                    "Dracula" -> palette.copy(
                        background = Color(0xff282a36), surface = Color(0xff282a36))
                    "One Dark" -> palette.copy(
                        background = Color(0xff282c34), surface = Color(0xff282c34))
                    "Monokai" -> palette.copy(
                        background = Color(0xff272822), surface = Color(0xff272822))
                    else -> palette
                }
            }
            val accent = when (accentPreset) {
                "Blue" -> if (dark) Color(0xff8ab7fa) else Color(0xff245eaf)
                "Violet" -> if (dark) Color(0xffc4b0ff) else Color(0xff6747b7)
                "Amber" -> if (dark) Color(0xffffc978) else Color(0xff936000)
                "Rose" -> if (dark) Color(0xffff92b7) else Color(0xffa52655)
                "Mono" -> if (dark) Color(0xffced4d5) else Color(0xff465152)
                else -> if (AppearanceRecipes.validAccent(accentPreset) &&
                    accentPreset.startsWith("#")) Color(0xff000000 or accentPreset.drop(1).toLong(16))
                    else if (CodeLabVisualStyles.canonical(visualStyle) == "Crystal") {
                        if (dark) Color(0xffd9dee1) else Color(0xff38444b)
                    } else base.primary
            }
            // Choose text contrast from actual color luminance, not theme mode alone.
            val onAccent = if (accent.luminance() > .179f) Color.Black else Color.White
            val accentContainer = accent.copy(alpha = .16f).compositeOver(base.surface)
            val schemeNormal = base.copy(primary = accent,
                onPrimary = onAccent,
                primaryContainer = accentContainer, onPrimaryContainer = base.onSurface,
                secondary = accent, onSecondary = onAccent,
                secondaryContainer = accentContainer, onSecondaryContainer = base.onSurface)
            if (highContrast) schemeNormal.copy(
                onSurface = if (dark) Color.White else Color.Black,
                onSurfaceVariant = if (dark) Color(0xffe3eceb) else Color(0xff243331),
                outline = if (dark) Color.White else Color.Black,
                outlineVariant = if (dark) Color(0xffaabbb5) else Color(0xff536960),
                primary = if (dark) Color(0xffbaffd7) else Color(0xff005b46),
                onPrimaryContainer = if (dark) Color.White else Color.Black
            ) else schemeNormal
    }
    val typography = remember(largeInterfaceText) {
        val baseTypography = Typography()
            val textScale = if (largeInterfaceText) 1.16f else 1f
            Typography(
                headlineLarge = baseTypography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                headlineSmall = baseTypography.headlineSmall.copy(fontWeight = FontWeight.SemiBold,
                    fontSize = baseTypography.headlineSmall.fontSize * textScale),
                titleLarge = baseTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold,
                    fontSize = baseTypography.titleLarge.fontSize * textScale),
                titleMedium = baseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold,
                    fontSize = baseTypography.titleMedium.fontSize * textScale),
                bodyMedium = baseTypography.bodyMedium.copy(
                    fontSize = baseTypography.bodyMedium.fontSize * textScale),
                labelSmall = baseTypography.labelSmall.copy(letterSpacing = .3.sp,
                    fontSize = baseTypography.labelSmall.fontSize * textScale)
            )
    }
    val shapes = remember(visualStyle) {
        when (visualStyle) {
            "Clay" -> Shapes(
                extraSmall = RoundedCornerShape(12.dp), small = RoundedCornerShape(17.dp),
                medium = RoundedCornerShape(22.dp), large = RoundedCornerShape(28.dp),
                extraLarge = RoundedCornerShape(34.dp))
            "AMOLED" -> Shapes(
                extraSmall = RoundedCornerShape(5.dp), small = RoundedCornerShape(8.dp),
                medium = RoundedCornerShape(12.dp), large = RoundedCornerShape(15.dp),
                extraLarge = RoundedCornerShape(20.dp))
            else -> Shapes(
                extraSmall = RoundedCornerShape(7.dp), small = RoundedCornerShape(11.dp),
                medium = RoundedCornerShape(15.dp), large = RoundedCornerShape(20.dp),
                extraLarge = RoundedCornerShape(28.dp))
        }
    }
    CompositionLocalProvider(LocalCodeLabVisualStyle provides CodeLabVisualStyles.canonical(visualStyle)) {
        MaterialTheme(colorScheme = scheme, typography = typography, shapes = shapes) {
            content(dark)
        }
    }
}

/** The transparent root still needs a theme-aware text and icon foreground.
 * Otherwise inherited content color can be black on every dark visual style.
 */
@Composable
internal fun CodeLabForegroundSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier, color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface, content = content
    )
}

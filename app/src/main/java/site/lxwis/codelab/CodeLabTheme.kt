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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal const val MATERIAL_YOU_MODE = "MaterialYou"

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
    studio: StudioAppearance = StudioAppearance(),
    content: @Composable (Boolean) -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (appearance) {
        "System", MATERIAL_YOU_MODE -> systemDark
        "Light" -> false
        else -> true
    }
    val effectiveStyle = CodeLabVisualStyles.effective(visualStyle, dark)
    // Do not allocate a new ColorScheme on unrelated editor recompositions.
    val colorContext = LocalContext.current
    val dynamic = if (appearance == MATERIAL_YOU_MODE && android.os.Build.VERSION.SDK_INT >= 31) {
        if (dark) dynamicDarkColorScheme(colorContext) else dynamicLightColorScheme(colorContext)
    } else null
    val scheme = remember(themePreset, visualStyle, accentPreset, highContrast, dark, studio, dynamic) {
        codeLabColorScheme(themePreset, visualStyle, dark, accentPreset, highContrast, studio, dynamic)
    }
    val fontContext = LocalContext.current
    val typography = remember(largeInterfaceText, studio.uiFont, studio.uiFontRevision, studio.uiScale) {
        val family = when (studio.uiFont) {
            "System" -> FontFamily.Default
            "Serif" -> FontFamily.Serif
            "Custom" -> ImportedFonts.load(fontContext, false, studio.uiFontRevision)?.let { FontFamily(it) }
                ?: FontFamily(Font(R.font.inter))
            else -> FontFamily(Font(R.font.inter))
        }
        val baseTypography = Typography()
            val textScale = (if (largeInterfaceText) 1.16f else 1f) * studio.uiScale.coerceIn(80, 140) / 100f
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
    val shapes = remember(studio.corners) {
        Shapes(extraSmall = RoundedCornerShape((studio.corners / 3).dp),
            small = RoundedCornerShape((studio.corners / 2).dp),
            medium = RoundedCornerShape((studio.corners * 3 / 4).dp),
            large = RoundedCornerShape(studio.corners.dp),
            extraLarge = RoundedCornerShape((studio.corners + 8).dp))
    }
    CompositionLocalProvider(LocalCodeLabVisualStyle provides effectiveStyle,
        LocalStudioAppearance provides studio) {
        MaterialTheme(colorScheme = scheme, typography = typography, shapes = shapes) {
            content(dark)
        }
    }
}

/** Pure palette resolution shared by the Compose theme, startup window and JVM tests. */
internal fun codeLabColorScheme(themePreset: String, visualStyle: String, dark: Boolean,
    accentPreset: String, highContrast: Boolean, studio: StudioAppearance = StudioAppearance(),
    dynamic: ColorScheme? = null): ColorScheme {
    val effectiveStyle = CodeLabVisualStyles.effective(visualStyle, dark)
    return run {
            val palette = if (dark) night else day
            val presetBase = dynamic ?: when (effectiveStyle) {
                "Obsidian" -> night.copy(
                    background = Color.Black, surface = Color.Black,
                    surfaceContainer = Color(0xff090909),
                    surfaceContainerHigh = Color(0xff121212),
                    surfaceVariant = Color(0xff0a0a0a),
                    outlineVariant = Color(0xff303030))
                "Sandstone" -> day.copy(
                    background = Color(0xffeae3d7), surface = Color(0xffeae3d7),
                    surfaceContainer = Color(0xfff3efe5),
                    surfaceContainerHigh = Color(0xffeae5d8),
                    surfaceVariant = Color(0xffe9e4d8),
                    onSurface = Color(0xff272922),
                    onSurfaceVariant = Color(0xff555950))
                "Crystal" -> palette.copy(
                    background = if (dark) Color(0xff252932) else Color(0xffeef0f1),
                    surface = if (dark) Color(0xff252932) else Color(0xfff7f8f9),
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
            fun color(value: String, fallback: Color) =
                if (value.matches(Regex("#[0-9a-fA-F]{6}")))
                    Color(0xff000000 or value.drop(1).toLong(16)) else fallback
            val surface = color(studio.surface, presetBase.surface)
            val background = color(studio.background, presetBase.background)
            val ink = color(studio.foreground, presetBase.onSurface)
            // Avoid illegible text on arbitrary custom colors, including imported themes.
            fun readable(fg: Color, bg: Color): Color {
                val a = fg.luminance(); val b = bg.luminance()
                return if ((maxOf(a,b)+.05f)/(minOf(a,b)+.05f) >= 4.5f) fg
                    else if (b > .179f) Color.Black else Color.White
            }
            val base = presetBase.copy(
                background = background, surface = surface, onBackground = readable(ink, background),
                outlineVariant = studioColor(studio.borderColor, presetBase.outlineVariant),
                onSurface = readable(ink, surface),
                surfaceContainer = if (studio.surface.isNotEmpty()) surface else presetBase.surfaceContainer,
                surfaceContainerHigh = if (studio.surface.isNotEmpty()) surface else presetBase.surfaceContainerHigh,
                surfaceContainerHighest = if (studio.surface.isNotEmpty()) surface else presetBase.surfaceContainerHighest,
                surfaceContainerLow = if (studio.surface.isNotEmpty()) surface else presetBase.surfaceContainerLow,
                surfaceContainerLowest = if (studio.surface.isNotEmpty()) surface else presetBase.surfaceContainerLowest,
                surfaceVariant = if (studio.surface.isNotEmpty()) surface else presetBase.surfaceVariant,
                onSurfaceVariant = if (studio.surface.isNotEmpty() || studio.foreground.isNotEmpty())
                    readable(ink, surface) else readable(presetBase.onSurfaceVariant, surface))
            val accent = if (dynamic != null) dynamic.primary else when (accentPreset) {
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
            val accentContainer = accent.copy(alpha = .16f).compositeOver(base.surface)
            // Accent is used for text/icons as well as fills; retain its hue where possible.
            val visibleAccent = contrastAccent(accent, base.surface)
            val onAccent = studioReadable(base.onSurface, visibleAccent)
            val schemeNormal = base.copy(primary = visibleAccent,
                onPrimary = onAccent,
                primaryContainer = accentContainer, onPrimaryContainer = studioReadable(base.onSurface, accentContainer),
                secondary = visibleAccent, onSecondary = onAccent,
                secondaryContainer = accentContainer, onSecondaryContainer = studioReadable(base.onSurface, accentContainer))
            if (highContrast) {
                val primary = contrastAccent(if (dark) Color(0xffbaffd7) else Color(0xff005b46), base.surface)
                val strong = studioReadable(if (dark) Color.White else Color.Black, base.surface)
                schemeNormal.copy(
                    onSurface = strong,
                    onSurfaceVariant = strong,
                    onBackground = studioReadable(strong, base.background),
                    outline = strong,
                    outlineVariant = strong,
                    primary = primary, onPrimary = studioReadable(strong, primary),
                    onPrimaryContainer = studioReadable(strong, accentContainer)
                )
            } else schemeNormal
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
        contentColor = studioReadable(MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.background), content = content
    )
}

internal val LocalStudioAppearance = androidx.compose.runtime.staticCompositionLocalOf { StudioAppearance() }

/** Shared color resolution used by the real components and Theme Studio preview. */
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

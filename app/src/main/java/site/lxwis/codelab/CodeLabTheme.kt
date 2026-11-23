package site.lxwis.codelab

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
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
    val dark = when (visualStyle) {
        "AMOLED" -> true
        "Paper" -> false
        else -> appearance == "Dark" || (appearance == "System" && isSystemInDarkTheme())
    }
    // Do not allocate a new ColorScheme on unrelated editor recompositions.
    val scheme = remember(themePreset, visualStyle, accentPreset, highContrast, dark) {
            val palette = if (dark) night else day
            val base = when (visualStyle) {
                "AMOLED" -> night.copy(
                    background = Color.Black, surface = Color.Black,
                    surfaceContainer = Color(0xff090909),
                    surfaceContainerHigh = Color(0xff121212),
                    surfaceVariant = Color(0xff0a0a0a),
                    outlineVariant = Color(0xff303030))
                "Paper" -> day.copy(
                    background = Color(0xfffaf8f1), surface = Color(0xfffaf8f1),
                    surfaceContainer = Color(0xfff3efe5),
                    surfaceContainerHigh = Color(0xffeae5d8),
                    surfaceVariant = Color(0xffe9e4d8),
                    onSurface = Color(0xff272922),
                    onSurfaceVariant = Color(0xff555950))
                "Glass" -> palette.copy(
                    background = if (dark) Color(0xff091321) else Color(0xffe8f4fb),
                    surface = if (dark) Color(0xff15293e).copy(alpha = .86f)
                        else Color(0xffecf8fd).copy(alpha = .87f),
                    surfaceContainer = if (dark) Color(0xff1b344b).copy(alpha = .70f)
                        else Color(0xffe2f0f7).copy(alpha = .72f),
                    surfaceContainerHigh = if (dark) Color(0xff29435c).copy(alpha = .74f)
                        else Color(0xffd7eaf2).copy(alpha = .80f),
                    surfaceVariant = if (dark) Color(0xff30485c) else Color(0xffdae8ef),
                    outlineVariant = if (dark) Color(0xff607f98) else Color(0xffb0d0dd))
                "Clay" -> palette.copy(
                    background = if (dark) Color(0xff111b23) else Color(0xffe7eeeb),
                    surface = if (dark) Color(0xff17252e) else Color(0xffeaf0ed),
                    surfaceContainer = if (dark) Color(0xff233742) else Color(0xffedf3ee),
                    surfaceContainerHigh = if (dark) Color(0xff304854) else Color(0xfff6faf6),
                    surfaceVariant = if (dark) Color(0xff354b56) else Color(0xffd9e5df),
                    outlineVariant = if (dark) Color(0xff4d6772) else Color(0xffc2d3c9))
                else -> when (themePreset) {
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
                else -> base.primary
            }
            val accentContainer = accent.copy(alpha = .15f).compositeOver(base.surface)
            val schemeNormal = base.copy(primary = accent,
                onPrimary = if (dark) Color(0xff101517) else Color.White,
                primaryContainer = accentContainer, onPrimaryContainer = accent,
                secondary = accent, onSecondary = if (dark) Color(0xff101517) else Color.White,
                secondaryContainer = accentContainer, onSecondaryContainer = accent)
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
    CompositionLocalProvider(LocalCodeLabVisualStyle provides visualStyle) {
        MaterialTheme(colorScheme = scheme, typography = typography, shapes = shapes) {
            content(dark)
        }
    }
}

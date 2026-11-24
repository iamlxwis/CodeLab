package site.lxwis.codelab

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Full-viewport backgrounds, behind readable app content.
 * Blurred child layers were clipped into hard rectangular edges on Glass/Clay.
 * Full-canvas radial gradients fade continuously, without large blur layers.
 */
@Composable
internal fun CodeLabBackdrop(style: String, dark: Boolean, content: @Composable () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val preset = CodeLabVisualStyles.canonical(style)
    val customBackground = LocalStudioAppearance.current.background.isNotEmpty()
    val background = if (customBackground) Brush.verticalGradient(listOf(scheme.background, scheme.background)) else when (preset) {
        "Crystal" -> if (dark)
            Brush.verticalGradient(listOf(Color(0xff101214), Color(0xff252a2e), Color(0xff14171a)))
            else Brush.verticalGradient(listOf(Color(0xfff8f9fa), Color(0xffdfe3e6), Color(0xfff2f4f5)))
        "Clay" -> if (dark)
            Brush.verticalGradient(listOf(Color(0xff18232a), Color(0xff111b23)))
            else Brush.verticalGradient(listOf(Color(0xfff4f5f0), Color(0xffe3ebe8)))
        "Sandstone" -> Brush.verticalGradient(listOf(Color(0xfffdfbf4), Color(0xfff5f0e4)))
        "Obsidian" -> Brush.verticalGradient(listOf(Color.Black, Color.Black))
        else -> Brush.verticalGradient(listOf(scheme.background, scheme.background))
    }
    Box(Modifier.fillMaxSize().background(background)) {
        if (!customBackground && (preset == "Crystal" || preset == "Clay" || preset == "Sandstone")) {
            Canvas(Modifier.fillMaxSize()) {
                val radius = maxOf(size.width, size.height * .58f)
                if (preset == "Crystal") {
                    val firstGlow = if (dark) Color.White.copy(alpha = .12f)
                        else Color.White.copy(alpha = .70f)
                    val secondGlow = if (dark) Color(0xffaeb7c0).copy(alpha = .10f)
                        else Color(0xffa1abb3).copy(alpha = .18f)
                    drawRect(Brush.radialGradient(
                        colors = listOf(firstGlow, firstGlow.copy(alpha = 0f)),
                        center = Offset(size.width * .10f, size.height * .16f),
                        radius = radius * .76f
                    ))
                    drawRect(Brush.radialGradient(
                        colors = listOf(secondGlow, secondGlow.copy(alpha = 0f)),
                        center = Offset(size.width * .90f, size.height * .42f),
                        radius = radius * .73f
                    ))
                }
                if (preset == "Clay") {
                    val glow = if (dark) Color(0xff748ca2).copy(alpha = .13f)
                        else Color.White.copy(alpha = .52f)
                    drawRect(Brush.radialGradient(
                        colors = listOf(glow, glow.copy(alpha = 0f)),
                        center = Offset(size.width * .16f, size.height * .08f),
                        radius = radius * .80f
                    ))
                }
                if (preset == "Sandstone") {
                    val step = 50.dp.toPx()
                    var y = step
                    while (y < size.height) {
                        drawLine(Color(0xffb7a788).copy(alpha = .09f),
                            Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                        y += step
                    }
                }
            }
        }
        content()
    }
}

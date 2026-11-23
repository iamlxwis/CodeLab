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
    val background = when (style) {
        "Glass" -> if (dark)
            Brush.verticalGradient(listOf(Color(0xff091321), Color(0xff182d42), Color(0xff0a1524)))
            else Brush.verticalGradient(listOf(Color(0xffe8f4fb), Color(0xffd3e6ef), Color(0xfff2f6f9)))
        "Clay" -> if (dark)
            Brush.verticalGradient(listOf(Color(0xff18232a), Color(0xff111b23)))
            else Brush.verticalGradient(listOf(Color(0xfff4f5f0), Color(0xffe3ebe8)))
        "Paper" -> Brush.verticalGradient(listOf(Color(0xfffdfbf4), Color(0xfff5f0e4)))
        "AMOLED" -> Brush.verticalGradient(listOf(Color.Black, Color.Black))
        else -> Brush.verticalGradient(listOf(scheme.background, scheme.background))
    }
    Box(Modifier.fillMaxSize().background(background)) {
        if (style == "Glass" || style == "Clay" || style == "Paper") {
            Canvas(Modifier.fillMaxSize()) {
                val radius = maxOf(size.width, size.height * .58f)
                if (style == "Glass") {
                    val firstGlow = if (dark) Color(0xff5e67c9).copy(alpha = .24f)
                        else Color(0xff70bdcf).copy(alpha = .30f)
                    val secondGlow = if (dark) Color(0xff1ed4b0).copy(alpha = .16f)
                        else Color(0xff96a3ff).copy(alpha = .20f)
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
                if (style == "Clay") {
                    val glow = if (dark) Color(0xff748ca2).copy(alpha = .13f)
                        else Color.White.copy(alpha = .52f)
                    drawRect(Brush.radialGradient(
                        colors = listOf(glow, glow.copy(alpha = 0f)),
                        center = Offset(size.width * .16f, size.height * .08f),
                        radius = radius * .80f
                    ))
                }
                if (style == "Paper") {
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

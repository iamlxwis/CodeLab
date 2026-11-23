package site.lxwis.codelab

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp

/**
 * Actual, full-screen backgrounds: a frosted Glass backdrop, soft Clay
 * lighting, genuinely black AMOLED, warm ruled Paper and flat Minimal.
 * Blurred decorations are behind the UI and do not blur editor glyphs.
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
        if (style == "Glass") {
            Box(Modifier.offset(x = (-115).dp, y = 65.dp).size(290.dp)
                .blur(90.dp).background(
                    if (dark) Color(0xff5e67c9).copy(alpha = .29f)
                    else Color(0xff70bdcf).copy(alpha = .35f), CircleShape))
            Box(Modifier.offset(x = 185.dp, y = 310.dp).size(270.dp)
                .blur(90.dp).background(
                    if (dark) Color(0xff1ed4b0).copy(alpha = .23f)
                    else Color(0xff96a3ff).copy(alpha = .26f), CircleShape))
        }
        if (style == "Clay") {
            Box(Modifier.offset(x = (-90).dp, y = (-100).dp).size(315.dp)
                .blur(105.dp).background(
                    if (dark) Color(0xff748ca2).copy(alpha = .17f)
                    else Color.White.copy(alpha = .7f), CircleShape))
        }
        if (style == "Paper") Canvas(Modifier.fillMaxSize()) {
            val step = 50.dp.toPx()
            var y = step
            while (y < size.height) {
                drawLine(Color(0xffb7a788).copy(alpha = .09f),
                    Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += step
            }
        }
        content()
    }
}

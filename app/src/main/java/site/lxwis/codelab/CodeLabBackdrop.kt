package site.lxwis.codelab

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import android.app.ActivityManager
import android.content.Context
import android.os.Build
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
internal fun CodeLabBackdrop(style: String, dark: Boolean, motionMillis: Int = 0, content: @Composable () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val preset = CodeLabVisualStyles.effective(style, dark)
    val studio = LocalStudioAppearance.current
    val context = LocalContext.current
    val lowRam = remember(context) { (context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.isLowRamDevice == true }
    val powerSave = if (preset == "Crystal") rememberPowerSaveMode() else false
    val blurSupported = glassBlurEnabled(Build.VERSION.SDK_INT, studio.glassQuality, lowRam, powerSave)
    val customBackground = studio.background.isNotEmpty()
    val canvasColor by animateColorAsState(scheme.background, tween(motionMillis), label = "theme-background")
    // All six presets use their approved base color. Glass adds only neutral highlights.
    val background = Brush.verticalGradient(listOf(canvasColor, canvasColor))
    Box(Modifier.fillMaxSize().background(background)) {
        if (!customBackground && preset == "Crystal") {
            // Blur only the decorative backdrop; never blur editable text or interactive controls.
            Canvas(Modifier.fillMaxSize().then(if (preset == "Crystal" && blurSupported && studio.blur > 0)
                Modifier.blur(studio.blur.dp) else Modifier)) {
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

            }
        }
        content()
    }
}

package site.lxwis.codelab

import android.app.ActivityManager
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp

@Composable
internal fun rememberGlassRendering(model: WorkspaceModel): GlassRendering {
    if (!model.glassEnabled || model.highContrast) return GlassRendering.Solid
    val context = LocalContext.current
    val hardwareAccelerated = LocalView.current.isHardwareAccelerated
    val lowRam = remember(context) { context.getSystemService(ActivityManager::class.java)?.isLowRamDevice == true }
    val powerSave = rememberPowerSaveMode()
    return glassRendering(Build.VERSION.SDK_INT, model.glassEnabled, model.highContrast,
        hardwareAccelerated, lowRam, powerSave, model.reduceDynamicBlur, model.automaticOptimization)
}

/** Record scrolling content once. Floating controls are siblings and never enter the recording. */
internal fun Modifier.recordGlassBackdrop(layer: GraphicsLayer, background: Color): Modifier = drawWithContent {
    layer.record {
        drawRect(background)
        this@drawWithContent.drawContent()
    }
    drawLayer(layer)
}

@Composable
internal fun LiquidGlassSurface(
    modifier: Modifier,
    shape: Shape,
    backdrop: GraphicsLayer?,
    backdropOrigin: Offset,
    rendering: GlassRendering,
    progressive: Boolean,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val layer = if (backdrop != null) rememberGraphicsLayer() else null
    var position by remember { mutableStateOf<Offset?>(null) }
    val tint = when (rendering) {
        GlassRendering.Solid -> colors.surfaceContainerHigh
        GlassRendering.Frosted -> colors.surfaceContainerHigh.copy(alpha = .94f)
        else -> colors.surfaceContainerHigh.copy(alpha = .78f)
    }
    val sampling = if (layer != null) Modifier.onGloballyPositioned { position = it.positionInRoot() } else Modifier
    val treatment = sampling
        .shadow(if (rendering == GlassRendering.Solid) 2.dp else 7.dp, shape)
        .clip(shape)
        .drawWithCache {
            if (layer != null && Build.VERSION.SDK_INT >= 31) {
                layer.renderEffect = glassEffect(rendering, size, 8.dp.toPx(), progressive)?.asComposeRenderEffect()
            }
            val edge = Brush.verticalGradient(listOf(
                colors.onSurface.copy(alpha = .21f), Color.White.copy(alpha = .07f),
                colors.outlineVariant.copy(alpha = .36f)))
            onDrawWithContent {
                val placed = position
                if (layer != null && backdrop != null && placed != null &&
                    backdrop.size.width > 0 && backdrop.size.height > 0) {
                    val sampleOrigin = placed - backdropOrigin
                    layer.record {
                        drawRect(colors.background)
                        translate(-sampleOrigin.x, -sampleOrigin.y) { drawLayer(backdrop) }
                    }
                    drawLayer(layer)
                }
                drawRect(tint)
                drawContent()
                if (rendering != GlassRendering.Solid) {
                    val stroke = 1.dp.toPx()
                    drawRoundRect(edge, topLeft = Offset(stroke / 2, stroke / 2),
                        size = Size((size.width - stroke).coerceAtLeast(0f), (size.height - stroke).coerceAtLeast(0f)),
                        cornerRadius = CornerRadius(minOf(size.height, size.width) / 2), style = Stroke(stroke))
                }
            }
        }
    Box(modifier.then(treatment), content = content)
}

@RequiresApi(31)
private fun glassEffect(
    rendering: GlassRendering,
    size: Size,
    radius: Float,
    progressive: Boolean
): android.graphics.RenderEffect? {
    if (rendering < GlassRendering.Blur) return null
    val blur = android.graphics.RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
    return if (rendering == GlassRendering.Refraction && Build.VERSION.SDK_INT >= 33) {
        runCatching { refractingEffect(size, blur, progressive) }.getOrDefault(blur)
    } else blur
}

@RequiresApi(33)
private fun refractingEffect(size: Size, blur: android.graphics.RenderEffect, progressive: Boolean): android.graphics.RenderEffect {
    val shader = RuntimeShader(
        """
        uniform shader backdrop;
        uniform float2 bounds;
        uniform float progressive;

        half4 main(float2 p) {
            float radius = min(bounds.x, bounds.y) * 0.5;
            float2 q = p - bounds * 0.5;
            float2 axis = float2(max(0.0, bounds.x * 0.5 - radius), 0.0);
            float2 d = q - clamp(q, -axis, axis);
            float distanceToEdge = radius - length(d);
            float edge = 1.0 - smoothstep(0.0, max(1.0, radius * 0.52), distanceToEdge);
            float2 inward = -d / max(1.0, length(d));
            float2 lower = float2(0.5);
            float2 upper = max(lower, bounds - lower);
            float2 bent = p + inward * radius * 0.31 * edge * edge;
            half4 center = backdrop.eval(clamp(bent, lower, upper));
            if (progressive < 0.5 || edge < 0.02) return center;
            float2 tangent = float2(-inward.y, inward.x);
            float2 spread = tangent * radius * 0.15 * edge;
            half4 sides = (backdrop.eval(clamp(bent + spread, lower, upper)) +
                backdrop.eval(clamp(bent - spread, lower, upper))) * 0.5;
            return mix(center, sides, half(edge * 0.55));
        }
        """.trimIndent()
    )
    shader.setFloatUniform("bounds", size.width, size.height)
    shader.setFloatUniform("progressive", if (progressive) 1f else 0f)
    return android.graphics.RenderEffect.createChainEffect(
        android.graphics.RenderEffect.createRuntimeShaderEffect(shader, "backdrop"), blur
    )
}

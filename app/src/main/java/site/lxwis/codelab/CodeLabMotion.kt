package site.lxwis.codelab

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlin.math.roundToInt

/**
 * Single duration policy for workspace feedback and transitions.
 *
 * Even "Full" never overrides Android's global Remove animations setting.
 * A global scale of 0 (or the app's Reduced / intensity=0 setting) always
 * yields instant state changes, without hidden spring animations.
 */
internal data class CodeLabMotion(val durationScale: Float, val springIntensity: Int = 35) {
    fun pressSpec(): FiniteAnimationSpec<Float> = if (durationScale <= 0f || springIntensity == 0) tween(millis(140))
        else spring(dampingRatio = 1f - springIntensity.coerceIn(0,100) * .003f, stiffness = 750f / durationScale.coerceIn(.4f, 3f))
    fun millis(base: Int): Int =
        (base.coerceAtLeast(0) * durationScale).roundToInt().coerceIn(0, 900)
}

internal fun resolveMotionScale(preference: String, intensity: Int, systemScale: Float): Float {
    if (preference == "Reduced" || intensity <= 0 || !systemScale.isFinite() ||
        systemScale <= 0f) return 0f
    val adjusted = (0.55f + 0.45f * (intensity.coerceIn(0, 100) / 100f))
    val mode = if (preference == "System") 0.9f else 1f
    return (systemScale.coerceIn(0f, 3f) * adjusted * mode).coerceIn(0f, 3f)
}

/** Observes live Android animator-scale changes; no restart needed. */
@Composable
internal fun rememberCodeLabMotion(preference: String, intensity: Int, speed: Int = 100, springIntensity: Int = 35): CodeLabMotion {
    val context = LocalContext.current
    val resolver = context.contentResolver
    fun currentScale(): Float = Settings.Global.getFloat(
        resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    var scale by remember(resolver) { mutableFloatStateOf(currentScale()) }
    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                scale = currentScale()
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer
        )
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return remember(preference, intensity, scale, speed, springIntensity) {
        CodeLabMotion((resolveMotionScale(preference, intensity, scale) * 100f / speed.coerceIn(50, 200)).coerceIn(0f, 3f), springIntensity)
    }
}

package site.lxwis.codelab

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

internal enum class GlassRendering { Solid, Frosted, Blur, Refraction }

internal fun glassRendering(
    api: Int,
    enabled: Boolean,
    highContrast: Boolean,
    accelerated: Boolean,
    lowRam: Boolean,
    powerSave: Boolean,
    reducedBlur: Boolean,
    automaticOptimization: Boolean
): GlassRendering {
    if (!enabled || highContrast) return GlassRendering.Solid
    if (!accelerated || powerSave || reducedBlur || (lowRam && automaticOptimization) || api < 31) {
        return GlassRendering.Frosted
    }
    return if (api >= 33) GlassRendering.Refraction else GlassRendering.Blur
}

/** Reacts to system changes without polling or running a background worker. */
@Composable
internal fun rememberPowerSaveMode(): Boolean {
    val context = LocalContext.current.applicationContext
    val power = remember(context) { context.getSystemService(Context.POWER_SERVICE) as? PowerManager }
    var saving by remember(power) { mutableStateOf(power?.isPowerSaveMode == true) }
    DisposableEffect(context, power) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                saving = power?.isPowerSaveMode == true
            }
        }
        ContextCompat.registerReceiver(context, receiver, IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED)
        saving = power?.isPowerSaveMode == true
        onDispose { context.unregisterReceiver(receiver) }
    }
    return saving
}

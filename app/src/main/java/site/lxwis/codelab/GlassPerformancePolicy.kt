package site.lxwis.codelab

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/** Manual High can override the low-memory recommendation; system battery saving wins. */
internal fun glassBlurEnabled(api: Int, quality: String, lowRam: Boolean, powerSave: Boolean): Boolean =
    api >= 31 && quality != "Lightweight" && !powerSave && (quality == "High" || !lowRam)

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

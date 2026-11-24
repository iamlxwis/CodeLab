package site.lxwis.codelab

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.SystemClock
import android.util.Log
import android.view.Choreographer
import java.util.Locale

/** Debuggable builds only: time from tap handler to the next UI frame callback. */
internal object UiLatencyProbe {
    fun nextFrame(context: Context, action: String) {
        PerformanceRecorder.recordActionFrame()
        if ((context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) == 0) return
        val start = SystemClock.elapsedRealtimeNanos()
        Choreographer.getInstance().postFrameCallback {
            val elapsedMs = (SystemClock.elapsedRealtimeNanos() - start) / 1_000_000.0
            Log.d("CodeLabLatency", String.format(Locale.US, "%s next-frame: %.1f ms", action, elapsedMs))
        }
    }
}

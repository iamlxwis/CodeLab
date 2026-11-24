package site.lxwis.codelab

import android.app.Activity
import android.content.Context
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Choreographer
import android.view.FrameMetrics
import android.view.ViewTreeObserver
import android.view.Window
import java.lang.ref.WeakReference

/** Opt-in, process-local measurements. No timer, background polling, network or source retention. */
internal object PerformanceRecorder {
    data class Snapshot(val firstDrawMs: Double?, val editFrameMs: Double?, val actionFrameMs: Double?,
        val frames: Long, val overBudget: Long, val sampleCount: Int, val medianMs: Double?,
        val p95Ms: Double?, val editorMemoryMb: Double)
    private var enabled = false
    private var foreground = false
    private var host: WeakReference<Window>? = null
    private var attached: Window? = null
    private var listener: Window.OnFrameMetricsAvailableListener? = null
    private var firstDraw: Double? = null
    private var editFrame: Double? = null
    private var actionFrame: Double? = null
    private var pendingEdit = false
    private var pendingAction = false
    private var frameCount = 0L
    private var overBudgetCount = 0L
    private val samples = ArrayDeque<Double>()

    fun enabled(context: Context) = context.getSharedPreferences("performance-recording", 0).getBoolean("enabled", false)
    fun setEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences("performance-recording", 0).edit().putBoolean("enabled", value).apply()
        enabled = value
        detach()
        clear()
        if (value && foreground) host?.get()?.let(::attach)
    }
    fun beginLaunch(activity: Activity, startedNanos: Long) {
        enabled = enabled(activity)
        if (!enabled) return
        val view = activity.window.decorView
        val draw = object : ViewTreeObserver.OnDrawListener {
            var done = false
            override fun onDraw() {
                if (done) return
                done = true
                if (enabled) firstDraw = (SystemClock.elapsedRealtimeNanos() - startedNanos) / 1_000_000.0
                view.post { if (view.viewTreeObserver.isAlive) view.viewTreeObserver.removeOnDrawListener(this) }
            }
        }
        view.viewTreeObserver.addOnDrawListener(draw)
    }
    fun onStart(activity: Activity) {
        foreground = true
        host = WeakReference(activity.window)
        enabled = enabled(activity)
        if (enabled) attach(activity.window)
    }
    fun onStop() { foreground = false; detach(); host = null }
    private fun attach(window: Window) {
        if (attached === window) return
        detach()
        val refresh = window.decorView.display?.refreshRate?.takeIf { it > 0f } ?: 60f
        val budgetMs = 1000.0 / refresh
        val observer = Window.OnFrameMetricsAvailableListener { _, metrics, _ ->
            if (enabled && foreground) {
                val duration = metrics.getMetric(FrameMetrics.TOTAL_DURATION) / 1_000_000.0
                if (duration >= 0.0) {
                    frameCount++
                    if (duration > budgetMs) overBudgetCount++
                    if (samples.size == 512) samples.removeFirst()
                    samples.addLast(duration)
                }
            }
        }
        window.addOnFrameMetricsAvailableListener(observer, Handler(Looper.getMainLooper()))
        listener = observer
        attached = window
    }
    private fun detach() {
        listener?.let { observer -> attached?.removeOnFrameMetricsAvailableListener(observer) }
        listener = null
        attached = null
    }
    fun recordEditFrame() = nextFrame(true)
    fun recordActionFrame() = nextFrame(false)
    private fun nextFrame(edit: Boolean) {
        if (!enabled || !foreground || Looper.myLooper() != Looper.getMainLooper()) return
        if (if (edit) pendingEdit else pendingAction) return
        if (edit) pendingEdit = true else pendingAction = true
        val start = SystemClock.elapsedRealtimeNanos()
        Choreographer.getInstance().postFrameCallback {
            if (enabled && foreground) {
                val elapsed = (SystemClock.elapsedRealtimeNanos() - start) / 1_000_000.0
                if (edit) editFrame = elapsed else actionFrame = elapsed
            }
            if (edit) pendingEdit = false else pendingAction = false
        }
    }
    fun clear() {
        firstDraw = null; editFrame = null; actionFrame = null
        frameCount = 0; overBudgetCount = 0; samples.clear()
    }
    fun snapshot(): Snapshot {
        val sorted = samples.sorted()
        fun percentile(p: Double): Double? = if (sorted.isEmpty()) null else sorted[((sorted.lastIndex * p).toInt()).coerceIn(sorted.indices)]
        return Snapshot(firstDraw, editFrame, actionFrame, frameCount, overBudgetCount, samples.size,
            percentile(.5), percentile(.95), Debug.getPss() / 1024.0)
    }
}

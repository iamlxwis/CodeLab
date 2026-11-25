package site.lxwis.codelab

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

/** Local 30-day activity. No source text, execution output or credentials enter this store. */
internal class DeveloperActivity(context: Context) {
    private val preferences = context.getSharedPreferences("developer-activity", Context.MODE_PRIVATE)
    private val handler = Handler(Looper.getMainLooper())
    private val buckets = readBuckets()
    private var foreground = false
    private var lastEdit = 0L
    private var countedThrough = 0L
    private var language = ""

    enum class Outcome(val label: String) { Succeeded("Passed"), Failed("Failed"), Stopped("Stopped") }

    data class Execution(val file: String, val time: Long, val outcome: Outcome)
    data class Report(
        val codingMillis: Long,
        val projects: Int,
        val files: Int,
        val passed: Int,
        val failed: Int,
        val stopped: Int,
        val languages: List<Pair<String, Long>>,
        val executions: List<Execution>
    ) {
        val runs: Int get() = passed + failed + stopped
    }

    private class Day {
        val times = mutableMapOf<String, Long>()
        val files = mutableSetOf<String>()
        var projects = 0
        var passed = 0
        var failed = 0
        var stopped = 0
        val executions = mutableListOf<Execution>()
    }

    var enabled by mutableStateOf(preferences.getBoolean("enabled", true))
        private set
    var report by mutableStateOf(report())
        private set

    private val heartbeat = object : Runnable {
        override fun run() {
            val now = SystemClock.elapsedRealtime()
            countUntil(now)
            publish()
            if (foreground && enabled && now < lastEdit + idleLimit) handler.postDelayed(this, 15_000)
        }
    }

    fun chooseEnabled(value: Boolean) {
        if (!value) finishEditing()
        enabled = value
        preferences.edit().putBoolean("enabled", value).apply()
    }

    fun setForeground(value: Boolean) {
        if (!value) finishEditing()
        foreground = value
    }

    fun edit(project: String, file: String) {
        if (!foreground || !enabled) return
        val now = SystemClock.elapsedRealtime()
        countUntil(now)
        val wasIdle = lastEdit == 0L || now >= lastEdit + idleLimit
        lastEdit = now
        countedThrough = now
        language = languageExtension(file)
        if (day().files.add(project + "\u0000" + file)) publish()
        if (wasIdle) {
            handler.removeCallbacks(heartbeat)
            handler.postDelayed(heartbeat, 15_000)
        }
    }

    fun finishEditing() {
        if (lastEdit == 0L) return
        countUntil(SystemClock.elapsedRealtime())
        lastEdit = 0L
        handler.removeCallbacks(heartbeat)
        publish()
    }

    fun recordProject() {
        if (!enabled) return
        day().projects++
        publish()
    }

    fun recordRun(run: RunSession.RunRecord) {
        if (!enabled) return
        val outcome = when {
            run.result.startsWith("Stopped") -> Outcome.Stopped
            run.result.endsWith("Exit 0") -> Outcome.Succeeded
            else -> Outcome.Failed
        }
        val current = day()
        when (outcome) {
            Outcome.Succeeded -> current.passed++
            Outcome.Failed -> current.failed++
            Outcome.Stopped -> current.stopped++
        }
        current.executions.add(0, Execution(run.file, run.time, outcome))
        if (current.executions.size > 24) current.executions.removeAt(current.executions.lastIndex)
        publish()
    }

    fun reset() {
        handler.removeCallbacks(heartbeat)
        lastEdit = 0L
        buckets.clear()
        preferences.edit().remove("days").apply()
        report = report()
    }

    private fun countUntil(now: Long) {
        if (lastEdit == 0L) return
        val end = minOf(now, lastEdit + idleLimit)
        val elapsed = (end - countedThrough).coerceAtLeast(0)
        if (elapsed > 0) day().times.merge(language, elapsed, Long::plus)
        countedThrough = end
    }

    private fun day(): Day = buckets.getOrPut(LocalDate.now().toEpochDay()) { Day() }

    private fun report(): Report {
        val cutoff = LocalDate.now().toEpochDay() - 29
        val recent = buckets.filterKeys { it >= cutoff }.values
        val times = recent.flatMap { it.times.entries }.groupingBy { it.key }.fold(0L) { total, entry -> total + entry.value }
        return Report(times.values.sum(), recent.sumOf { it.projects },
            recent.flatMap { it.files }.toSet().size, recent.sumOf { it.passed }, recent.sumOf { it.failed },
            recent.sumOf { it.stopped }, times.toList().sortedByDescending { it.second },
            recent.flatMap { it.executions }.sortedByDescending { it.time }.take(24))
    }

    private fun publish() {
        val cutoff = LocalDate.now().toEpochDay() - 29
        buckets.keys.removeAll { it < cutoff }
        val json = JSONObject()
        for ((date, day) in buckets) {
            val executions = JSONArray()
            day.executions.forEach { entry ->
                executions.put(JSONObject().put("file", entry.file).put("at", entry.time).put("outcome", entry.outcome.name))
            }
            json.put(date.toString(), JSONObject().put("times", JSONObject(day.times)).put("files", JSONArray(day.files))
                .put("projects", day.projects).put("passed", day.passed).put("failed", day.failed)
                .put("stopped", day.stopped).put("executions", executions))
        }
        preferences.edit().putString("days", json.toString()).apply()
        report = report()
    }

    private fun readBuckets(): MutableMap<Long, Day> = runCatching {
        val json = JSONObject(preferences.getString("days", "{}") ?: "{}")
        val cutoff = LocalDate.now().toEpochDay() - 29
        json.keys().asSequence().mapNotNull { key ->
            val date = key.toLongOrNull()?.takeIf { it >= cutoff } ?: return@mapNotNull null
            val saved = json.optJSONObject(key) ?: return@mapNotNull null
            val day = Day()
            saved.optJSONObject("times")?.let { times ->
                times.keys().forEach { day.times[it] = times.optLong(it).coerceAtLeast(0) }
            }
            saved.optJSONArray("files")?.let { files ->
                for (i in 0 until files.length()) files.optString(i).takeIf(String::isNotBlank)?.let(day.files::add)
            }
            day.projects = saved.optInt("projects").coerceAtLeast(0)
            day.passed = saved.optInt("passed").coerceAtLeast(0)
            day.failed = saved.optInt("failed").coerceAtLeast(0)
            day.stopped = saved.optInt("stopped").coerceAtLeast(0)
            saved.optJSONArray("executions")?.let { executions ->
                for (i in 0 until minOf(24, executions.length())) {
                    val entry = executions.optJSONObject(i) ?: continue
                    val outcome = Outcome.entries.find { it.name == entry.optString("outcome") } ?: continue
                    day.executions.add(Execution(entry.optString("file"), entry.optLong("at"), outcome))
                }
            }
            date to day
        }.toMap().toMutableMap()
    }.getOrDefault(mutableMapOf())

    private companion object {
        const val idleLimit = 30_000L
    }
}

internal fun languageExtension(path: String): String {
    val filename = path.substringAfterLast('/').lowercase(Locale.ROOT)
    return when (filename) {
        "dockerfile", "containerfile" -> "dockerfile"
        "makefile", "gnumakefile" -> "mk"
        else -> filename.substringAfterLast('.', "")
    }
}

internal fun formatCodingTime(millis: Long): String {
    val minutes = millis / 60_000
    return when {
        millis == 0L -> "0 min"
        minutes == 0L -> "<1 min"
        minutes < 60 -> "$minutes min"
        else -> "${minutes / 60} h ${minutes % 60} min"
    }
}

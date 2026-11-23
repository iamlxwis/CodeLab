package site.lxwis.codelab

import android.content.*
import android.os.*
import androidx.compose.runtime.*
import org.json.JSONArray
import org.json.JSONObject

class RunSession(context: Context) {
    private val app = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private val settings = app.getSharedPreferences("run-settings", Context.MODE_PRIVATE)
    data class RunRecord(val file: String, val time: Long, val result: String,
                         val excerpt: String)
    var timeLimitSeconds by mutableIntStateOf(settings.getInt("timeout", 60).coerceIn(10, 60))
        private set
    var outputLimitCharacters by mutableIntStateOf(settings.getInt("output", 64000).coerceIn(8000, 64000))
        private set
    fun chooseExecutionLimits(seconds: Int, outputCharacters: Int) {
        if (running) return
        timeLimitSeconds = seconds.coerceIn(10, 60)
        outputLimitCharacters = outputCharacters.coerceIn(8000, 64000)
        settings.edit().putInt("timeout", timeLimitSeconds)
            .putInt("output", outputLimitCharacters).apply()
    }
    private fun loadHistory(): List<RunRecord> = runCatching {
        val encoded = JSONArray(settings.getString("history", "[]") ?: "[]")
        (0 until encoded.length()).mapNotNull { i ->
            encoded.optJSONObject(i)?.let { obj ->
                RunRecord(obj.optString("file"), obj.optLong("time"),
                    obj.optString("result"), obj.optString("excerpt"))
            }
        }.take(12)
    }.getOrDefault(emptyList())
    var history by mutableStateOf(loadHistory())
        private set
    private var activeName: String? = null
    private var lastLocalName: String? = null
    private var lastLocalSource: String? = null
    private var lastDebug = false
    private var lastBreakpoints: List<Int> = emptyList()
    val canRerun: Boolean get() = !running && lastLocalName != null
    fun rerun() {
        val name = lastLocalName ?: return
        val source = lastLocalSource ?: return
        run(name, source, lastDebug, lastBreakpoints)
    }
    private var connection: ServiceConnection? = null
    private var worker: Messenger? = null
    private var onlineThread: Thread? = null
    private var generation = 0
    private val watchdog = Runnable { end("Runtime unavailable") }

    var running by mutableStateOf(false)
        private set
    var waiting by mutableStateOf(false)
        private set
    var pausedLine by mutableIntStateOf(0)
        private set
    val paused: Boolean get() = pausedLine > 0
    fun debugAction(step: Boolean) {
        if (!paused || !running) return
        try {
            worker?.send(packet(if (step) DEBUG_STEP else DEBUG_RESUME))
            pausedLine = 0
            status = if (step) "Stepping" else "Running"
        } catch (_: RemoteException) { end("Debugger disconnected") }
    }
    var transcript by mutableStateOf("")
        private set
    var status by mutableStateOf("Ready")
        private set

    fun run(name: String, source: String, debug: Boolean = false,
        breakpoints: List<Int> = emptyList()) {
        if (running) return
        transcript = ""
        if (!name.endsWith(".js", true) && !name.endsWith(".sql", true) && !name.endsWith(".lua", true) && !name.endsWith(".java", true) && !name.endsWith(".py", true)) { status = "Run a .js, .py, .sql, .lua or .java file"; return }
        if (source.length > 128_000) { status = "Script exceeds 128,000 characters"; return }
        val pythonPack = if (name.endsWith(".py", true)) PythonModuleLocator.component(app) else null
        if (name.endsWith(".py", true) && pythonPack == null) {
            status = PythonModuleLocator.check(app).description
            return
        }
        if (debug && !name.endsWith(".js", true)) {
            status = "Debugger currently supports JavaScript only"
            return
        }
        activeName = name
        lastLocalName = name
        lastLocalSource = source
        lastDebug = debug
        lastBreakpoints = breakpoints
        pausedLine = 0
        running = true
        status = "Starting"
        val token = ++generation
        val replies = Messenger(Handler(Looper.getMainLooper()) { msg ->
            if (generation == token && running) {
                when (msg.what) {
                    OUTPUT -> transcript = (transcript + msg.body()).takeLast(70_000)
                    WAITING -> { waiting = true; status = "Waiting for input" }
                    DEBUG_PAUSED -> {
                        pausedLine = msg.body().toIntOrNull()?.coerceAtLeast(0) ?: 0
                        status = "Paused at line " + pausedLine
                    }
                    FINISH -> end(listOf(msg.body(), "Exit ${msg.arg1}").filter { it.isNotEmpty() }.joinToString(" · "))
                }
            }
            true
        })
        val listener = object : ServiceConnection {
            override fun onServiceConnected(component: ComponentName, binder: IBinder) {
                if (generation != token) return
                main.removeCallbacks(watchdog)
                worker = Messenger(binder)
                val request = packet(START, source).apply {
                    data.putString("name", name)
                    data.putInt("protocol", PythonModuleLocator.PROTOCOL_VERSION)
                    data.putInt("limit-seconds", timeLimitSeconds)
                    data.putInt("output-chars", outputLimitCharacters)
                    data.putBoolean("debug", debug)
                    data.putIntegerArrayList("breakpoints",
                        ArrayList(breakpoints.filter { it in 1..100000 }.distinct().take(80)))
                    replyTo = replies
                }
                status = "Running"
                try {
                    worker?.send(request)
                    main.postDelayed(watchdog, timeLimitSeconds * 1000L + 5_000L)
                } catch (_: RemoteException) { end("Runtime disconnected") }
            }
            override fun onServiceDisconnected(component: ComponentName) {
                if (generation == token) end("Runtime disconnected")
            }
            override fun onBindingDied(component: ComponentName) {
                if (generation == token) end("Runtime disconnected")
            }
            override fun onNullBinding(component: ComponentName) {
                if (generation == token) end("Runtime unavailable")
            }
        }
        connection = listener
        try {
            val sql = name.endsWith(".sql", true)
            val lua = name.endsWith(".lua", true)
            val javaMode = name.endsWith(".java", true)
            val python = name.endsWith(".py", true)
            val intent = if (python) Intent().setComponent(requireNotNull(pythonPack))
                else Intent(app, when {
                    sql -> SqlRuntimeService::class.java
                    lua -> LuaRuntimeService::class.java
                    javaMode -> JavaRuntimeService::class.java
                    else -> RuntimeService::class.java
                })
            val bound = if (python || sql || Build.VERSION.SDK_INT < 29)
                app.bindService(intent, listener, Context.BIND_AUTO_CREATE)
            else app.bindIsolatedService(intent, Context.BIND_AUTO_CREATE,
                "run${java.util.UUID.randomUUID().toString().replace("-", "")}",
                app.mainExecutor, listener)
            if (bound) main.postDelayed(watchdog, 8000) else end("Unable to start runtime")
        } catch (e: RuntimeException) { end("Unable to start runtime · ${e.javaClass.simpleName}") }
    }

    /** Cloud runs require an explicit confirmation from the UI before this is called. */
    fun runOnline(name: String, source: String, endpoint: String, header: String,
                  credential: String, language: CloudJudge0.Language, stdin: String) {
        if (running) return
        transcript = ""
        if (source.length > 128_000) { status = "Source exceeds 128,000 characters"; return }
        activeName = name
        lastLocalName = null
        lastLocalSource = null
        lastDebug = false
        lastBreakpoints = emptyList()
        pausedLine = 0
        running = true
        waiting = false
        status = "Submitting to online compiler…"
        val token = ++generation
        val task = Thread({
            try {
                val result = CloudJudge0.execute(endpoint, header, credential, language, source, stdin)
                main.post {
                    if (token == generation && running) {
                        transcript = result.output.takeLast(70_000)
                        end("Online · " + result.status + " · Exit " + result.exitCode)
                    }
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                main.post {
                    if (token == generation && running) {
                        transcript = "! " + (e.message ?: "Online compiler unavailable") + "\n"
                        end("Online execution failed")
                    }
                }
            }
        }, "CodeLab-CloudCompiler")
        onlineThread = task
        task.start()
    }

    fun submit(text: String) {
        if (!waiting) return
        try {
            worker?.send(packet(INPUT, text.take(4096)))
            waiting = false
            status = "Running"
        } catch (_: RemoteException) { end("Runtime disconnected") }
    }

    fun clear() { transcript = "" }

    fun stop() {
        if (!running) return
        try { worker?.send(packet(STOP)) } catch (_: RemoteException) {}
        val online = onlineThread != null
        onlineThread?.interrupt()
        end(if (online) "Stopped monitoring online run" else "Stopped · Exit 130")
    }

    fun close() { stop() }

    private fun end(result: String) {
        activeName?.let { filename ->
            val record = RunRecord(filename, System.currentTimeMillis(), result,
                transcript.takeLast(3000))
            history = (listOf(record) + history).take(12)
            val encoded = JSONArray()
            history.forEach { item ->
                encoded.put(JSONObject().put("file", item.file).put("time", item.time)
                    .put("result", item.result).put("excerpt", item.excerpt))
            }
            settings.edit().putString("history", encoded.toString()).apply()
        }
        activeName = null
        generation++
        main.removeCallbacks(watchdog)
        onlineThread = null
        status = result
        running = false
        waiting = false
        pausedLine = 0
        connection?.let { try { app.unbindService(it) } catch (_: IllegalArgumentException) {} }
        connection = null
        worker = null
    }
}

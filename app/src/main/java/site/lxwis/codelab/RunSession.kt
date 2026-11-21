package site.lxwis.codelab

import android.content.*
import android.os.*
import androidx.compose.runtime.*

class RunSession(context: Context) {
    private val app = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private var connection: ServiceConnection? = null
    private var worker: Messenger? = null
    private var generation = 0
    private val watchdog = Runnable { end("Runtime unavailable") }

    var running by mutableStateOf(false)
        private set
    var waiting by mutableStateOf(false)
        private set
    var transcript by mutableStateOf("")
        private set
    var status by mutableStateOf("Ready")
        private set

    fun run(name: String, source: String) {
        if (running) return
        transcript = ""
        if (!name.endsWith(".js", true) && !name.endsWith(".sql", true)) { status = "Run a .js or .sql file"; return }
        if (source.length > 128_000) { status = "Script exceeds 128,000 characters"; return }
        running = true
        status = "Starting"
        val token = ++generation
        val replies = Messenger(Handler(Looper.getMainLooper()) { msg ->
            if (generation == token && running) {
                when (msg.what) {
                    OUTPUT -> transcript = (transcript + msg.body()).takeLast(70_000)
                    WAITING -> { waiting = true; status = "Waiting for input" }
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
                    replyTo = replies
                }
                status = "Running"
                try {
                    worker?.send(request)
                    main.postDelayed(watchdog, 65_000)
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
            val intent = Intent(app, RuntimeService::class.java)
            val bound = if (Build.VERSION.SDK_INT >= 29)
                app.bindIsolatedService(intent, Context.BIND_AUTO_CREATE, "run${java.util.UUID.randomUUID().toString().replace("-", "")}", app.mainExecutor, listener)
            else app.bindService(intent, listener, Context.BIND_AUTO_CREATE)
            if (bound) main.postDelayed(watchdog, 8000) else end("Unable to start runtime")
        } catch (e: RuntimeException) { end("Unable to start runtime · ${e.javaClass.simpleName}") }
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
        end("Stopped · Exit 130")
    }

    fun close() { stop() }

    private fun end(result: String) {
        generation++
        main.removeCallbacks(watchdog)
        status = result
        running = false
        waiting = false
        connection?.let { try { app.unbindService(it) } catch (_: IllegalArgumentException) {} }
        connection = null
        worker = null
    }
}

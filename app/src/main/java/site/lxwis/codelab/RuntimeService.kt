package site.lxwis.codelab

import android.app.Service
import android.content.Intent
import android.os.*
import java.util.concurrent.ArrayBlockingQueue

internal const val START = 1
internal const val INPUT = 2
internal const val STOP = 3
internal const val OUTPUT = 4
internal const val WAITING = 5
internal const val FINISH = 6
internal fun packet(kind: Int, body: String = "", code: Int = 0) =
    Message.obtain(null, kind, code, 0).apply { data = Bundle().apply { putString("body", body) } }
internal fun Message.body() = data.getString("body").orEmpty()

class RuntimeService : Service() {
    private val main = Handler(Looper.getMainLooper())
    private var client: Messenger? = null
    private val input = ArrayBlockingQueue<String>(1)
    private val timeout = Runnable { terminate(124, "Execution timed out") }
    @Volatile private var finished = false
    private val messenger = Messenger(Handler(Looper.getMainLooper()) { message ->
        when (message.what) {
            START -> if (client == null) start(message)
            INPUT -> input.offer(message.body().take(4096))
            STOP -> terminate(130, "Stopped")
        }
        true
    })

    override fun onBind(intent: Intent) = messenger.binder

    private fun emit(kind: Int, text: String = "", code: Int = 0) {
        try { client?.send(packet(kind, text, code)) }
        catch (_: RemoteException) { Process.killProcess(Process.myPid()) }
    }

    private fun terminate(code: Int, reason: String) {
        if (!finished) {
            finished = true
            emit(FINISH, reason, code)
        }
        Process.killProcess(Process.myPid())
    }

    private fun start(message: Message) {
        client = message.replyTo
        finished = false
        input.clear()
        val name = message.data.getString("name") ?: "main.js"
        val source = message.body()
        main.postDelayed(timeout, 60_000L)
        Thread({
            var size = 0
            var lines = 0
            val batch = StringBuilder()
            var lastFlush = SystemClock.elapsedRealtime()
            fun flush() {
                if (batch.isNotEmpty()) {
                    emit(OUTPUT, batch.toString())
                    batch.setLength(0)
                    lastFlush = SystemClock.elapsedRealtime()
                }
            }
            val io = object : JsEngine.Terminal {
                override fun checkpoint() {
                    if (SystemClock.elapsedRealtime() - lastFlush >= 40L) flush()
                }
                override fun write(text: String, error: Boolean) {
                    val line = (if (error) "! " else "") + text.take(8192) + "\n"
                    size += line.length
                    if (++lines > 1500 || size > 64_000 || text.length > 8192) {
                        throw OutputLimit()
                    }
                    batch.append(line)
                    if (lines == 1 || error || batch.length >= 2048) flush() else checkpoint()
                }
                override fun read(prompt: String): String {
                    if (prompt.isNotEmpty()) write(prompt, false)
                    flush()
                    emit(WAITING)
                    val value = input.take()
                    write("> " + value, false)
                    return value
                }
            }
            val code = try { JsEngine.execute(name, source, io) }
                catch (_: OutputLimit) { flush(); emit(OUTPUT, "! Console output limit reached\n"); 1 }
                catch (e: Exception) { flush(); emit(OUTPUT, "! ${e.javaClass.simpleName}\n"); 1 }
            flush()
            main.post {
                main.removeCallbacks(timeout)
                if (!finished) { finished = true; emit(FINISH, "", code) }
                client = null
            }
        }, "CodeLab-JavaScript").start()
    }

    override fun onDestroy() {
        main.removeCallbacks(timeout)
        super.onDestroy()
        Process.killProcess(Process.myPid())
    }

    private class OutputLimit : Error()
}

package site.lxwis.codelab.engine.python;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * CodeLab Python interpreter in a distinct Android application UID.
 * This service is exported only to callers holding the signature permission.
 * It receives source, never a file path to the editor's app-private workspace.
 */
public final class PythonRuntimeService extends Service {
    private static final int START = 1, INPUT = 2, STOP = 3;
    private static final int OUTPUT = 4, WAITING = 5, FINISH = 6;
    private static final int PROTOCOL = 1;

    private final Handler main = new Handler(Looper.getMainLooper());
    private final ArrayBlockingQueue<String> inputs = new ArrayBlockingQueue<>(1);
    private final Messenger inbound = new Messenger(new Handler(Looper.getMainLooper(), msg -> {
        switch (msg.what) {
            case START: if (!started) begin(msg); break;
            case INPUT: inputs.offer(body(msg).substring(0, Math.min(4096, body(msg).length()))); break;
            case STOP: finish(130, "Stopped"); break;
        }
        return true;
    }));
    private Messenger client;
    private boolean started;
    private volatile boolean finished;
    private int outputLimit;
    private int outputChars;
    private int outputLines;

    @Override public IBinder onBind(Intent intent) { return inbound.getBinder(); }
    @Override public void onDestroy() {
        Process.killProcess(Process.myPid());
        super.onDestroy();
    }

    private static String body(Message msg) { return msg.getData().getString("body", ""); }
    private static Message packet(int kind, int code, String text) {
        Message result = Message.obtain(null, kind, code, 0);
        Bundle payload = new Bundle();
        payload.putString("body", text);
        result.setData(payload);
        return result;
    }
    private synchronized void send(int kind, int code, String text) {
        if (client == null || finished) return;
        try { client.send(packet(kind, code, text)); }
        catch (RemoteException e) { main.post(() -> Process.killProcess(Process.myPid())); }
    }

    private void begin(Message request) {
        client = request.replyTo;
        if (client == null) return;
        if (request.getData().getInt("protocol", 0) != PROTOCOL) {
            finish(1, "Incompatible runtime protocol");
            return;
        }
        started = true;
        int seconds = Math.max(10, Math.min(60, request.getData().getInt("limit-seconds", 60)));
        outputLimit = Math.max(8000, Math.min(64000, request.getData().getInt("output-chars", 64000)));
        String filename = request.getData().getString("name", "main.py");
        String source = body(request);
        if (!filename.endsWith(".py") || source.length() > 128000) {
            finish(1, "Invalid Python request");
            return;
        }
        main.postDelayed(() -> finish(124, "Python timed out"), seconds * 1000L);
        new Thread(() -> {
            int code = execute(filename, source);
            main.post(() -> finish(code, ""));
        }, "CodeLab-Python").start();
    }

    private int execute(String filename, String source) {
        try {
            if (!Python.isStarted()) Python.start(new AndroidPlatform(this));
            PyObject result = Python.getInstance().getModule("codelab_runner")
                .callAttr("run", filename, source, new ConsoleBridge());
            return result.toInt();
        } catch (Throwable failure) {
            String message = failure.getClass().getSimpleName();
            if (failure.getMessage() != null) message += ": " + failure.getMessage();
            try { new ConsoleBridge().write("Python module error: " + message, true); }
            catch (RuntimeException ignored) { }
            return 1;
        }
    }

    private synchronized void finish(int code, String note) {
        if (finished) return;
        send(FINISH, code, note);
        finished = true;
        if (code == 124 || code == 130)
            main.postDelayed(() -> Process.killProcess(Process.myPid()), 150);
    }

    public final class ConsoleBridge {
        public synchronized void write(String text, boolean stderr) {
            if (finished) return;
            String line = (stderr ? "! " : "") + text.substring(0, Math.min(8192, text.length())) + "\n";
            outputChars += line.length();
            outputLines++;
            if (outputChars > outputLimit || outputLines > 1500 || text.length() > 8192) {
                send(OUTPUT, 0, "! Console output limit reached\n");
                main.post(() -> finish(1, "Output limit"));
                throw new IllegalStateException("Console output limit reached");
            }
            send(OUTPUT, 0, line);
        }
        public String read(String prompt) throws InterruptedException {
            if (prompt != null && !prompt.isEmpty()) write(prompt, false);
            send(WAITING, 0, "");
            String value = inputs.take();
            write("> " + value, false);
            return value;
        }
    }
}

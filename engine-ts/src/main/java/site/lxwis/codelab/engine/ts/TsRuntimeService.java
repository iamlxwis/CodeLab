package site.lxwis.codelab.engine.ts;

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
import android.os.SystemClock;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Supplier;

/** Signature-only IPC in its own Android UID. The editor never shares file paths. */
public final class TsRuntimeService extends Service {
    private static final int START=1, INPUT=2, STOP=3, OUTPUT=4, WAITING=5, FINISH=6,
        DEBUG_PAUSED=7, DEBUG_RESUME=8, DEBUG_STEP=9, DEBUG_VARIABLES=10;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ArrayBlockingQueue<String> inputs = new ArrayBlockingQueue<>(1);
    private final ArrayBlockingQueue<Boolean> debugging = new ArrayBlockingQueue<>(1);
    private Messenger client;
    private boolean started;
    private volatile boolean finished;
    private final Messenger receiver = new Messenger(new Handler(Looper.getMainLooper(), message -> {
        switch (message.what) {
            case START: if (!started) start(message); break;
            case INPUT: inputs.offer(body(message).substring(0, Math.min(body(message).length(), 4096))); break;
            case STOP: finish(130, "Stopped"); break;
            case DEBUG_RESUME: debugging.offer(false); break;
            case DEBUG_STEP: debugging.offer(true); break;
        }
        return true;
    }));
    @Override public IBinder onBind(Intent intent) { return receiver.getBinder(); }
    @Override public void onDestroy() { super.onDestroy(); Process.killProcess(Process.myPid()); }
    private static String body(Message m) { return m.getData().getString("body", ""); }
    private static Message packet(int what, int code, String body) {
        Message m = Message.obtain(null, what, code, 0);
        Bundle data = new Bundle(); data.putString("body", body); m.setData(data); return m;
    }
    private synchronized void send(int what, int code, String text) {
        if (client == null || finished) return;
        try { client.send(packet(what, code, text)); }
        catch (RemoteException ignored) { main.post(() -> Process.killProcess(Process.myPid())); }
    }
    private synchronized void finish(int code, String text) {
        if (finished) return;
        send(FINISH, code, text);
        finished = true;
        // A separate Android process ensures force-stop cannot affect project data.
        main.postDelayed(() -> Process.killProcess(Process.myPid()), 150);
    }
    private void start(Message message) {
        client = message.replyTo;
        if (client == null) return;
        started = true;
        if (message.getData().getInt("protocol", 0) != 1) {
            finish(1, "Incompatible module protocol");
            return;
        }
        final String filename = message.getData().getString("name", "main.ts");
        final String source = body(message);
        if (!filename.toLowerCase(java.util.Locale.ROOT).endsWith(".ts") ||
            source.length() > 128000) {
            finish(1, "Invalid TypeScript module request");
            return;
        }
        final int seconds = Math.max(10, Math.min(60, message.getData().getInt("limit-seconds", 60)));
        final int outputLimit = Math.max(8000, Math.min(64000, message.getData().getInt("output-chars", 64000)));
        final boolean debug = message.getData().getBoolean("debug", false);
        final java.util.ArrayList<Integer> points = message.getData().getIntegerArrayList("breakpoints");
        final java.util.Set<Integer> breakpoints = new java.util.HashSet<>();
        if (points != null) for (int p : points) if (p >= 1 && p <= 100000) breakpoints.add(p);
        inputs.clear(); debugging.clear();
        main.postDelayed(() -> finish(124, "Execution timed out"), seconds*1000L);
        new Thread(() -> {
            final int[] chars = {0}, lines = {0};
            final boolean[] atStart = {debug}, stepping = {false};
            final JsEngine.Terminal terminal = new JsEngine.Terminal() {
                @Override public void write(String line, boolean error) {
                    if (finished) throw new IllegalStateException("Module stopped");
                    final String text = (error ? "! " : "") + line.substring(0, Math.min(line.length(), 8192)) + "\n";
                    chars[0] += text.length(); lines[0]++;
                    if (chars[0] > outputLimit || lines[0] > 1500 || line.length() > 8192)
                        throw new IllegalStateException("Module output limit");
                    send(OUTPUT, 0, text);
                }
                @Override public String read(String prompt) {
                    try {
                        if (!prompt.isEmpty()) write(prompt, false);
                        send(WAITING, 0, "");
                        String data = inputs.take();
                        write("> " + data, false);
                        return data;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return "";
                    }
                }
                @Override public void checkpoint() { if (finished) throw new IllegalStateException("Module stopped"); }
                @Override public void onLine(int line, Supplier<String> variables) {
                    if (!debug || !(atStart[0] || stepping[0] || breakpoints.contains(line))) return;
                    atStart[0] = false;
                    send(DEBUG_VARIABLES, 0, variables.get().substring(0, Math.min(4096, variables.get().length())));
                    send(DEBUG_PAUSED, 0, Integer.toString(line));
                    try { stepping[0] = debugging.take(); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); throw new IllegalStateException("Debugger interrupted");
                    }
                }
            };
            int code;
            try { code = JsEngine.execute(filename + ".js", new TypeScriptCompiler(TsRuntimeService.this).transpile(source), terminal, false); }
            catch (Throwable failure) {
                try { terminal.write("TypeScript execution error: " + (failure.getMessage() == null ? failure.getClass().getSimpleName() : failure.getMessage()), true); }
                catch (Exception ignored) {}
                code = 1;
            }
            final int result = code;
            main.post(() -> finish(result, ""));
        }, "CodeLab-TsModule").start();
    }
}

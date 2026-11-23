package site.lxwis.codelab;

import android.content.Context;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

/**
 * CPython is started only inside the isolated PythonRuntimeService process.
 * Keep Python itself out of the UI process, with no access to app credentials.
 */
public final class PythonEngine {
    public static int execute(Context service, String filename, String source, JsEngine.Terminal io) {
        try {
            io.checkpoint();
            if (!Python.isStarted()) Python.start(new AndroidPlatform(service));
            PyObject result = Python.getInstance().getModule("codelab_runner")
                .callAttr("run", filename, source, io);
            io.checkpoint();
            return result.toInt();
        } catch (Exception e) {
            io.write("Python runtime error: " + e.getClass().getSimpleName() +
                (e.getMessage() == null ? "" : ": " + e.getMessage()), true);
            return 1;
        } catch (UnsatisfiedLinkError e) {
            io.write("Python native runtime unavailable for this device architecture", true);
            return 1;
        }
    }
    private PythonEngine() {}
}

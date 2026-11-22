package site.lxwis.codelab;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.debug.Debugger;
import org.mozilla.javascript.debug.DebugFrame;
import org.mozilla.javascript.debug.DebuggableScript;

/** An interpreted ECMAScript runtime with only the host functions declared here. */
public final class JsEngine {
    public interface Terminal {
        void write(String text, boolean error);
        String read(String prompt);
        void checkpoint();
        default void onLine(int lineNumber) {}
    }

    public static int execute(String name, String source, Terminal io) {
        return execute(name, source, io, false);
    }

    public static int execute(String name, String source, Terminal io, boolean debug) {
        ContextFactory factory = new ContextFactory() {
            @Override protected Context makeContext() {
                Context cx = super.makeContext();
                cx.setOptimizationLevel(-1);
                cx.setLanguageVersion(Context.VERSION_ES6);
                cx.setInstructionObserverThreshold(8192);
                cx.setClassShutter(className -> false);
                if (debug) {
                    cx.setGeneratingDebug(true);
                    cx.setDebugger(new Debugger() {
                        @Override public DebugFrame getFrame(Context context, DebuggableScript script) {
                            return new DebugFrame() {
                                @Override public void onEnter(Context c, Scriptable activation,
                                        Scriptable thisObj, Object[] args) {}
                                @Override public void onLineChange(Context c, int line) {
                                    io.onLine(line);
                                }
                                @Override public void onExceptionThrown(Context c, Throwable error) {}
                                @Override public void onExit(Context c, boolean thrown, Object result) {}
                                @Override public void onDebuggerStatement(Context c) {}
                            };
                        }
                        @Override public void handleCompilationDone(Context context,
                                DebuggableScript script, String scriptSource) {}
                    }, null);
                }
                return cx;
            }
            @Override protected void observeInstructionCount(Context cx, int count) {
                io.checkpoint();
            }
        };
        try {
            factory.call(cx -> {
                ScriptableObject scope = cx.initSafeStandardObjects();
                Scriptable console = cx.newObject(scope);
                BaseFunction out = printer(scope, io, false);
                BaseFunction err = printer(scope, io, true);
                ScriptableObject.putProperty(scope, "print", out);
                ScriptableObject.putProperty(console, "log", out);
                ScriptableObject.putProperty(console, "warn", err);
                ScriptableObject.putProperty(console, "error", err);
                ScriptableObject.putProperty(scope, "console", console);
                ScriptableObject.putProperty(scope, "readLine", new BaseFunction(scope, ScriptableObject.getFunctionPrototype(scope)) {
                    @Override public Object call(Context ctx, Scriptable s, Scriptable self, Object[] args) {
                        return io.read(args.length == 0 ? "" : Context.toString(args[0]));
                    }
                });
                cx.evaluateString(scope, source, name, 1, null);
                return null;
            });
            return 0;
        } catch (RhinoException e) {
            io.write(e.getMessage(), true);
            return 1;
        } catch (StackOverflowError e) {
            io.write("Maximum call stack exceeded", true);
            return 1;
        }
    }

    private static BaseFunction printer(Scriptable scope, Terminal io, boolean error) {
        return new BaseFunction(scope, ScriptableObject.getFunctionPrototype(scope)) {
            @Override public Object call(Context cx, Scriptable s, Scriptable self, Object[] args) {
                StringBuilder line = new StringBuilder();
                for (Object arg : args) {
                    if (line.length() > 0) line.append(' ');
                    String value = Context.toString(arg);
                    int count = Math.min(value.length(), 8193 - line.length());
                    line.append(value, 0, count);
                    if (line.length() >= 8192) break;
                }
                io.write(line.toString(), error);
                return Context.getUndefinedValue();
            }
        };
    }

    private JsEngine() {}
}

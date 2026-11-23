package site.lxwis.codelab.engine.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 * Offline Lua 5.2-style interpreter using LuaJ.
 *
 * Deliberately exposes no java reflection, filesystem, network or OS library.
 * The worker is separately terminated by RuntimeService's stop/timeout controls.
 */
public final class LuaEngine {
    public static int execute(String name, String source, ModuleTerminal terminal) {
        try {
            Globals globals = JsePlatform.standardGlobals();
            // JsePlatform exposes host capabilities intended for trusted scripts.
            // Remove them before evaluating user code.
            for (String forbidden : new String[] {
                    "luajava", "io", "os", "package", "require",
                    "loadfile", "dofile", "module", "collectgarbage", "debug"
            }) {
                globals.set(forbidden, LuaValue.NIL);
            }
            globals.set("print", new VarArgFunction() {
                @Override public Varargs invoke(Varargs args) {
                    terminal.checkpoint();
                    StringBuilder buffer = new StringBuilder();
                    for (int i = 1; i <= args.narg(); i++) {
                        if (i > 1) buffer.append('\t');
                        buffer.append(args.arg(i).tojstring());
                        if (buffer.length() > 8192) break;
                    }
                    terminal.write(buffer.toString(), false);
                    return LuaValue.NIL;
                }
            });
            VarArgFunction readLine = new VarArgFunction() {
                @Override public Varargs invoke(Varargs args) {
                    terminal.checkpoint();
                    String prompt = args.narg() > 0 && !args.arg1().isnil()
                            ? args.arg1().tojstring() : "";
                    return LuaValue.valueOf(terminal.read(prompt));
                }
            };
            globals.set("readLine", readLine);
            globals.set("input", readLine);
            terminal.checkpoint();
            globals.load(source, name).call();
            terminal.checkpoint();
            return 0;
        } catch (LuaError error) {
            terminal.write(error.getMessage() == null ? "Lua execution failed" : error.getMessage(), true);
            return 1;
        } catch (StackOverflowError error) {
            terminal.write("Maximum Lua call stack exceeded", true);
            return 1;
        }
    }

    private LuaEngine() {}
}

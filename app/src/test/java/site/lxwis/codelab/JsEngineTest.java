package site.lxwis.codelab;

import org.junit.Test;
import static org.junit.Assert.*;

public class JsEngineTest {
    static class Capture implements JsEngine.Terminal {
        String out = "", err = "", prompt;
        int checkpoints;
        public void write(String value, boolean error) {
            if (error) err += value + "\n";
            else out += value + "\n";
        }
        public String read(String value) { prompt = value; return "Ada"; }
        public void checkpoint() { checkpoints++; }
    }

    @Test public void debuggerReportsExecutedSourceLines() {
        java.util.List<Integer> visited = new java.util.ArrayList<>();
        Capture trace = new Capture() {
            @Override public void onLine(int line) { visited.add(line); }
        };
        assertEquals(0, JsEngine.execute("trace.js", "let x = 1;\nprint(x);", trace, true));
        assertTrue("Second line must be traceable", visited.contains(2));
        assertEquals("1\n", trace.out);
    }

    @Test public void debuggerSuppliesBoundedLocalVariableSnapshots() {
        java.util.List<String> snapshots = new java.util.ArrayList<>();
        Capture trace = new Capture() {
            @Override public void onLine(int line, java.util.function.Supplier<String> variables) {
                snapshots.add(variables.get());
            }
        };
        assertEquals(0, JsEngine.execute("locals.js",
            "function total(n) {\n  var amount = n + 2;\n  print(amount);\n}\ntotal(3);",
            trace, true));
        assertFalse(snapshots.isEmpty());
        assertTrue(snapshots.stream().allMatch(value -> value.length() <= 4096));
        assertEquals("5\n", trace.out);
    }

    @Test public void evaluatesExpressionsAndInput() {
        Capture capture = new Capture();
        assertEquals(0, JsEngine.execute("input.js", "let n = readLine('Name?'); print('hello', n); console.log([1,2,3].reduce((a,b)=>a+b,0)); console.error('warning');", capture));
        assertEquals("Name?", capture.prompt);
        assertEquals("hello Ada\n6\n", capture.out);
        assertEquals("warning\n", capture.err);
    }

    @Test public void reportsSourceLocationForFailure() {
        Capture capture = new Capture();
        assertEquals(1, JsEngine.execute("broken.js", "print(1);\nthrow Error('boom')", capture));
        assertTrue(capture.err, capture.err.contains("broken.js#2"));
        assertTrue(capture.err.contains("boom"));
    }

    @Test public void omitsJavaAndFilesystemGlobals() {
        Capture capture = new Capture();
        assertEquals(0, JsEngine.execute("env.js", "print(typeof Packages, typeof java, typeof importClass, typeof readFile)", capture));
        assertEquals("undefined undefined undefined undefined\n", capture.out);
    }

    @Test public void loopsCanBeInterrupted() {
        class Stop extends Error {}
        Capture capture = new Capture() {
            public void checkpoint() { if (++checkpoints == 3) throw new Stop(); }
        };
        assertThrows(Stop.class, () -> JsEngine.execute("loop.js", "while (true) {}", capture));
        assertTrue(capture.checkpoints >= 3);
    }
}

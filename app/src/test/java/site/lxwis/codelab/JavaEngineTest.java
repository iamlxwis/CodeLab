package site.lxwis.codelab;

import org.junit.Test;
import static org.junit.Assert.*;

public class JavaEngineTest {
    @Test public void extractsMainWithNestedLoopsAndQuotedBraces() {
        String source = "public class Main {\n" +
            "public static void main(String[] args) {\n" +
            "String a = \"{hello}\";\n" +
            "for (int i = 0; i < 3; i++) { System.out.println(i); }\n" +
            "// } comment\n" +
            "/* { nested } */\n" +
            "System.out.println(a);\n" +
            "}\n}\n";
        String parsed = JavaEngine.prepareSource(source);
        assertTrue(parsed.contains("for (int i = 0;"));
        assertTrue(parsed.contains("System.out.println(a)"));
        assertFalse(parsed.contains("public class"));
    }

    @Test public void reportsClassesWithoutSupportedMain() {
        assertThrows(IllegalArgumentException.class,
            () -> JavaEngine.prepareSource("public class Example { void go() {} }"));
    }

    @Test public void interpretsMainWithActualComputedOutput() {
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();
        JsEngine.Terminal terminal = new JsEngine.Terminal() {
            @Override public void checkpoint() { }
            @Override public String read(String prompt) { return "Ada"; }
            @Override public void write(String text, boolean error) {
                (error ? stderr : stdout).append(text).append('\n');
            }
        };
        int code = JavaEngine.execute("Main.java",
            "public class Main { public static void main(String[] args) {" +
            "int sum=0; for(int i=1;i<=4;i++){sum += i;}" +
            "System.out.println(\"sum=\" + sum);" +
            "} }", terminal);
        assertEquals(stderr.toString(), 0, code);
        assertTrue(stdout.toString(), stdout.toString().contains("sum=10"));
    }
}

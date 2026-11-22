package site.lxwis.codelab;

import org.junit.Test;
import static org.junit.Assert.*;

public class LuaEngineTest {
    @Test public void offlineLuaComputesAndReadsInput() {
        StringBuilder log = new StringBuilder();
        JsEngine.Terminal io = new JsEngine.Terminal() {
            @Override public void checkpoint() { }
            @Override public String read(String prompt) { return "Ada"; }
            @Override public void write(String text, boolean error) { log.append(text).append('\n'); }
        };
        int code = LuaEngine.execute("main.lua",
            "local x=0; for i=1,4 do x=x+i end\n" +
            "print(x)\nprint(readLine('name?'))", io);
        assertEquals(log.toString(), 0, code);
        assertTrue(log.toString(), log.toString().contains("10"));
        assertTrue(log.toString(), log.toString().contains("Ada"));
    }
}

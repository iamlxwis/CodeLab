package site.lxwis.codelab.engine.java;
import java.util.function.Supplier;
/** Bounded text-only console bridge for the Java beta interpreter. */
public interface JavaTerminal {
    void write(String text, boolean error);
    String read(String prompt);
    void checkpoint();
    default void onLine(int line, Supplier<String> variables) {}
}

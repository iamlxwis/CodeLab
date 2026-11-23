package site.lxwis.codelab.engine.lua;
public interface ModuleTerminal {
    void write(String text, boolean error);
    String read(String prompt);
    void checkpoint();
}

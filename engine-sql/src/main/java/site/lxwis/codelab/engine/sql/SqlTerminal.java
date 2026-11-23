package site.lxwis.codelab.engine.sql;
/** Trusted-signed console bridge; only bounded text crosses IPC. */
public interface SqlTerminal {
    void write(String text, boolean error);
    String read(String prompt);
    void checkpoint();
}
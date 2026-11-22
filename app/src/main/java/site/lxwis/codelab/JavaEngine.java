package site.lxwis.codelab;

import bsh.EvalError;
import bsh.Interpreter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Offline Java console mode. BeanShell interprets ordinary Java statements and a
 * basic public static void main(String[] args) entry point. This is NOT javac.
 *
 * Always execute in JavaRuntimeService's isolated Android process: the interpreter
 * intentionally has access to standard Java reflection and is not a secure sandbox.
 */
public final class JavaEngine {
    private static final Pattern MAIN = Pattern.compile(
        "\\b(?:public\\s+)?static\\s+void\\s+main\\s*\\(\\s*String\\s*" +
        "(?:\\[\\s*\\]\\s*[\\w$]+|[\\w$]+\\s*\\[\\s*\\])\\s*\\)\\s*\\{");
    private static final Pattern CLASS = Pattern.compile("\\bclass\\s+[A-Za-z_$][\\w$]*\\b");
    private static final Pattern IMPORTS = Pattern.compile(
        "(?m)^\\s*import\\s+(?!static\\s+)[\\w.*]+\\s*;");

    public static int execute(String filename, String source, JsEngine.Terminal io) {
        ConsoleOutput stdout = new ConsoleOutput(io, false);
        ConsoleOutput stderr = new ConsoleOutput(io, true);
        PrintStream out = new PrintStream(stdout, true);
        PrintStream err = new PrintStream(stderr, true);
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        InputStream originalIn = System.in;
        try {
            io.checkpoint();
            String code = prepareSource(source);
            System.setOut(out);
            System.setErr(err);
            System.setIn(new InputStream() {
                private ByteArrayInputStream line = new ByteArrayInputStream(new byte[0]);

                @Override public int read() throws IOException {
                    if (line.available() == 0) {
                        String value = io.read("");
                        if (value == null) return -1;
                        line = new ByteArrayInputStream((value + "\n").getBytes(StandardCharsets.UTF_8));
                    }
                    return line.read();
                }

                @Override public int read(byte[] b, int off, int len) throws IOException {
                    if (len == 0) return 0;
                    int c = read();
                    if (c < 0) return -1;
                    b[off] = (byte) c;
                    int count = 1;
                    while (count < len && line.available() > 0) {
                        b[off + count++] = (byte) line.read();
                    }
                    return count;
                }
            });
            Interpreter interpreter = new Interpreter(null, out, err, false);
            interpreter.eval(code);
            io.checkpoint();
            return 0;
        } catch (IllegalArgumentException e) {
            io.write(e.getMessage(), true);
            return 1;
        } catch (EvalError e) {
            io.write(filename + ": " + e.getMessage(), true);
            return 1;
        } catch (StackOverflowError e) {
            io.write("Maximum Java stack depth exceeded", true);
            return 1;
        } finally {
            out.flush();
            err.flush();
            System.setOut(originalOut);
            System.setErr(originalErr);
            System.setIn(originalIn);
        }
    }

    static String prepareSource(String source) {
        Matcher main = MAIN.matcher(source);
        if (!main.find()) {
            if (CLASS.matcher(source).find()) {
                throw new IllegalArgumentException(
                    "Java beta supports a basic static void main(String[] args) " +
                    "entry point. Full Java compilation and custom classes are not yet available.");
            }
            return source; // Java statements and methods in BeanShell script form.
        }
        int start = main.end();
        int level = 1;
        int cursor = start;
        boolean string = false, singleQuote = false, lineComment = false, blockComment = false, escape = false;
        for (; cursor < source.length(); cursor++) {
            char c = source.charAt(cursor);
            char next = cursor + 1 < source.length() ? source.charAt(cursor + 1) : 0;
            if (lineComment) {
                if (c == '\n') lineComment = false;
                continue;
            }
            if (blockComment) {
                if (c == '*' && next == '/') { blockComment = false; cursor++; }
                continue;
            }
            if (string || singleQuote) {
                if (escape) { escape = false; continue; }
                if (c == '\\') { escape = true; continue; }
                if (string && c == '"' || singleQuote && c == '\'') {
                    string = false; singleQuote = false;
                }
                continue;
            }
            if (c == '/' && next == '/') { lineComment = true; cursor++; continue; }
            if (c == '/' && next == '*') { blockComment = true; cursor++; continue; }
            if (c == '"') { string = true; continue; }
            if (c == '\'') { singleQuote = true; continue; }
            if (c == '{') level++;
            if (c == '}' && --level == 0) break;
        }
        if (level != 0) throw new IllegalArgumentException("Unclosed Java main method");
        StringBuilder imports = new StringBuilder();
        Matcher found = IMPORTS.matcher(source.substring(0, main.start()));
        while (found.find()) imports.append(found.group().trim()).append('\n');
        // No generated .class or DEX files: run only the main method statements.
        return imports + source.substring(start, cursor);
    }

    private static final class ConsoleOutput extends OutputStream {
        private final JsEngine.Terminal io;
        private final boolean error;
        private final ByteArrayOutputStream pending = new ByteArrayOutputStream();
        ConsoleOutput(JsEngine.Terminal io, boolean error) { this.io = io; this.error = error; }

        @Override public synchronized void write(int value) {
            if (value == '\n') {
                flushLine();
            } else if (value != '\r') {
                pending.write(value);
                if (pending.size() >= 4096) flushLine();
            }
        }

        @Override public synchronized void write(byte[] bytes, int offset, int length) {
            for (int i = offset; i < offset + length; i++) write(bytes[i] & 0xff);
        }

        private void flushLine() {
            io.write(new String(pending.toByteArray(), StandardCharsets.UTF_8), error);
            pending.reset();
        }

        @Override public synchronized void flush() {
            if (pending.size() != 0) flushLine();
        }
    }

    private JavaEngine() {}
}

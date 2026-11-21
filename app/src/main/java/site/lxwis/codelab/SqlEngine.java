package site.lxwis.codelab;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Executes SQL against a fresh in-memory SQLite database, completely offline. */
public final class SqlEngine {
    public static int execute(String name, String source, JsEngine.Terminal io) {
        List<String> statements;
        try {
            statements = split(source);
        } catch (IllegalArgumentException e) {
            io.write(e.getMessage(), true);
            return 1;
        }
        if (statements.isEmpty()) {
            io.write("No SQL statements to execute", false);
            return 0;
        }
        try (SQLiteDatabase db = SQLiteDatabase.create(null)) {
            int index = 0;
            for (String sql : statements) {
                io.checkpoint();
                index++;
                String leading = sql.replaceAll("(?m)--[^\\r\\n]*", " ")
                        .replaceAll("(?s)/\\*.*?\\*/", " ").trim();
                if (leading.isEmpty()) continue;
                String verb = leading.split("\\s+", 2)[0].toUpperCase(Locale.ROOT);
                // Never open external databases or access app-private files from user SQL.
                if (verb.equals("ATTACH") || verb.equals("DETACH") || verb.equals("VACUUM")) {
                    io.write("Statement " + index + ": " + verb + " is unavailable in the offline SQL console", true);
                    return 1;
                }
                try {
                    if (verb.equals("SELECT") || verb.equals("WITH") || verb.equals("PRAGMA") ||
                            verb.equals("EXPLAIN") || verb.equals("VALUES")) {
                        try (Cursor cursor = db.rawQuery(sql, null)) {
                            int columns = cursor.getColumnCount();
                            List<String> headers = new ArrayList<>();
                            for (int col = 0; col < columns; col++) headers.add(cursor.getColumnName(col));
                            io.write(String.join("\t", headers), false);
                            int rows = 0;
                            while (cursor.moveToNext()) {
                                io.checkpoint();
                                List<String> values = new ArrayList<>();
                                for (int col = 0; col < columns; col++) {
                                    String cell;
                                    if (cursor.isNull(col)) cell = "NULL";
                                    else if (cursor.getType(col) == Cursor.FIELD_TYPE_BLOB)
                                        cell = "[binary: " + cursor.getBlob(col).length + " bytes]";
                                    else cell = cursor.getString(col).replace("\r", "\\r")
                                            .replace("\n", "\\n").replace("\t", " ");
                                    values.add(cell);
                                }
                                io.write(String.join("\t", values), false);
                                rows++;
                            }
                            io.write(rows + (rows == 1 ? " row" : " rows"), false);
                        }
                    } else {
                        db.execSQL(sql);
                        io.write("OK · " + verb, false);
                    }
                } catch (SQLiteException e) {
                    io.write("Statement " + index + ": " + e.getMessage(), true);
                    return 1;
                }
            }
            return 0;
        } catch (SQLiteException e) {
            io.write("Unable to initialize local SQLite: " + e.getMessage(), true);
            return 1;
        }
    }

    /** Split semicolon-terminated SQL safely without splitting within strings or comments. */
    static List<String> split(String source) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        char quote = 0;
        boolean lineComment = false;
        boolean blockComment = false;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            char next = i + 1 < source.length() ? source.charAt(i + 1) : 0;
            current.append(c);
            if (lineComment) {
                if (c == '\n') lineComment = false;
            } else if (blockComment) {
                if (c == '*' && next == '/') {
                    current.append(next);
                    i++;
                    blockComment = false;
                }
            } else if (quote != 0) {
                if (c == quote) {
                    if (next == quote && quote != ']') { current.append(next); i++; }
                    else quote = 0;
                }
            } else if (c == '-' && next == '-') {
                current.append(next); i++;
                lineComment = true;
            } else if (c == '/' && next == '*') {
                current.append(next); i++;
                blockComment = true;
            } else if (c == '\'' || c == '"' || c == (char) 96) {
                quote = c;
            } else if (c == '[') {
                quote = ']';
            } else if (c == ';') {
                String sql = current.substring(0, current.length() - 1).trim();
                if (!sql.isEmpty()) statements.add(sql);
                current.setLength(0);
            }
        }
        if (blockComment || quote != 0) throw new IllegalArgumentException("Unterminated SQL string or comment");
        String last = current.toString().trim();
        if (!last.isEmpty()) statements.add(last);
        if (statements.size() > 1000) throw new IllegalArgumentException("Too many SQL statements (maximum 1000)");
        return statements;
    }

    private SqlEngine() {}
}

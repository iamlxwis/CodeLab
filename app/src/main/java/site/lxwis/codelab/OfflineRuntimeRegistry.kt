package site.lxwis.codelab

/** Local runtime capabilities, backed by libraries bundled inside the APK. */
internal object OfflineRuntimeRegistry {
    data class Runtime(
        val name: String,
        val extension: String,
        val engine: String,
        val version: String,
        val className: String?,
        val capabilities: String,
        val limitations: String
    )
    private val catalog = listOf(
        Runtime("Python", "py", "CPython via Chaquopy", "3.13",
            "com.chaquo.python.Python",
            "Python 3 syntax, standard library where Android-compatible, console I/O",
            "Isolated worker. No native pip installs at runtime and some OS APIs are unavailable."),
        Runtime("JavaScript", "js", "Mozilla Rhino", "1.7.15.1",
            "org.mozilla.javascript.Context",
            "Statements, functions, interactive readLine and console output",
            "Browser and Node.js APIs are unavailable. Interpreter, not V8."),
        Runtime("Lua", "lua", "LuaJ", "3.0.1",
            "org.luaj.vm2.LuaValue",
            "Lua scripts, tables, standard Lua functions and console input",
            "LuaJ-compatible runtime; native Lua modules and OS filesystem APIs are unavailable."),
        Runtime("Java", "java", "BeanShell", "2.1.1",
            "bsh.Interpreter",
            "Basic Java statements and selected main-method syntax",
            "Interpreter beta; not javac or a full JVM compiler."),
        Runtime("SQLite", "sql", "Android SQLite", "Platform",
            "android.database.sqlite.SQLiteDatabase",
            "Standard SQL statements and in-memory query results",
            "Database is new on each run and is not persisted."),
        Runtime("HTML", "html", "Android WebView", "Platform",
            "android.webkit.WebView",
            "Local HTML, CSS and JavaScript preview",
            "Preview only, not command-line code execution."),
        Runtime("CSS", "css", "Android WebView", "Platform",
            "android.webkit.WebView",
            "Local web preview linked from HTML pages",
            "Stylesheets must be referenced by an HTML document.")
    )
    fun manifest(extension: String): Runtime? =
        catalog.firstOrNull { it.extension == extension.lowercase() }
    fun locallyAvailable(extension: String): Boolean {
        val runtime = manifest(extension) ?: return false
        return try {
            runtime.className == null || Class.forName(runtime.className) != null
        } catch (_: LinkageError) {
            false
        } catch (_: ClassNotFoundException) {
            false
        }
    }
    fun engines(): List<Runtime> = catalog.toList()
}

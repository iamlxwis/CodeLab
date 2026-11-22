package site.lxwis.codelab

import java.io.IOException
import java.util.Locale

/**
 * Safe, local workspace command interpreter. This is NOT a POSIX shell or PTY:
 * no native processes, shell expansion, pipes, redirection or host commands.
 */
internal class WorkspaceShell(private val store: ProjectStore, private val project: String) {
    var directory: String = ""
        private set

    private fun tokenize(command: String): List<String> {
        require(command.length <= 512) { "Command exceeds 512 characters." }
        val values = mutableListOf<String>()
        val token = StringBuilder()
        var quote: Char? = null
        var active = false
        for (char in command) {
            when {
                quote != null && char == quote -> quote = null
                quote == null && (char == '\'' || char == '"') -> {
                    quote = char
                    active = true
                }
                quote == null && char.isWhitespace() -> {
                    if (active) { values.add(token.toString()); token.setLength(0); active = false }
                }
                else -> { token.append(char); active = true }
            }
        }
        require(quote == null) { "Unclosed quote." }
        if (active) values.add(token.toString())
        return values
    }

    private fun path(arg: String): String {
        val parts = if (arg.startsWith('/')) mutableListOf<String>() else
            directory.split('/').filter { it.isNotEmpty() }.toMutableList()
        for (segment in arg.split('/')) {
            when (segment) {
                "", "." -> Unit
                ".." -> {
                    if (parts.isEmpty()) throw IOException("Cannot navigate outside the project.")
                    parts.removeAt(parts.lastIndex)
                }
                else -> {
                    if (segment.length > 100 || segment.any {
                        it == '\\' || it == ':' || it.isISOControl()
                    } || segment in setOf(".git", ".codelab"))
                        throw IOException("Invalid or internal file path.")
                    parts.add(segment)
                }
            }
        }
        require(parts.size <= 16) { "Path is too deeply nested." }
        return parts.joinToString("/")
    }

    fun execute(command: String): String {
        val args = tokenize(command)
        if (args.isEmpty()) return ""
        val action = args.first().lowercase(Locale.ROOT)
        val target = args.getOrNull(1)
        return when (action) {
            "help" -> "Built-ins: help, pwd, ls [folder], tree, cd [folder], cat FILE, head FILE, " +
                "wc FILE, grep TEXT, git status. No OS shell or PTY is available."
            "pwd" -> "/" + directory
            "cd" -> {
                val folder = path(target ?: "/")
                if (folder.isNotEmpty() && folder + "/" !in store.folders(project))
                    throw IOException("Folder does not exist.")
                directory = folder
                "/" + directory
            }
            "ls" -> {
                val folder = path(target ?: ".")
                if (folder.isNotEmpty() && folder + "/" !in store.folders(project))
                    throw IOException("Folder does not exist.")
                val prefix = if (folder.isEmpty()) "" else folder + "/"
                val files = store.files(project).filter { it.startsWith(prefix) }
                    .map { it.removePrefix(prefix) }
                    .filter { it.isNotEmpty() && '/' !in it }
                val folders = store.folders(project).filter { it.startsWith(prefix) }
                    .map { it.removePrefix(prefix).substringBefore('/') }
                    .filter { it.isNotEmpty() }.map { it + "/" }
                (files + folders).distinct().sorted().take(120).joinToString("\n")
                    .ifEmpty { "(empty folder)" }
            }
            "tree" -> {
                val root = path(target ?: ".")
                val prefix = if (root.isEmpty()) "" else root + "/"
                (store.folders(project) + store.files(project))
                    .filter { it.startsWith(prefix) }.sorted().take(150)
                    .joinToString("\n") { it.removePrefix(prefix) }
                    .ifEmpty { "(empty folder)" }
            }
            "cat", "head" -> {
                val filename = target ?: throw IOException("Provide a filename.")
                val text = store.read(project, path(filename))
                if (action == "head") text.lineSequence().take(15).joinToString("\n")
                else text.take(8000) + if (text.length > 8000) "\n[output truncated]" else ""
            }
            "wc" -> {
                val filename = target ?: throw IOException("Provide a filename.")
                val text = store.read(project, path(filename))
                val words = Regex("\\S+").findAll(text).count()
                val lines = if (text.isEmpty()) 0 else text.count { it == '\n' } + 1
                "" + lines + " lines · " + words + " words · " + text.length +
                    " characters · " + filename
            }
            "grep" -> {
                val needle = args.drop(1).joinToString(" ")
                if (needle.isBlank()) throw IOException("Provide text to search for.")
                store.searchText(project, needle.take(200), false)
                    .filter { directory.isEmpty() || it.path.startsWith(directory + "/") }
                    .take(70).joinToString("\n") {
                        it.path + ":" + it.line + " " + it.preview
                    }.ifEmpty { "No matches." }
            }
            "git" -> {
                if (target != "status") throw IOException("Supported: git status")
                val status = GitWorkspace(store.projectDirectory(project)).inspect()
                if (!status.initialized) "Git repository not initialized."
                else "On branch " + status.branch + "\n" +
                    status.changes.take(40).joinToString("\n") {
                        it.kind + " " + it.path
                    }.ifBlank { "Working tree clean." }
            }
            else -> "Unknown command: " + args.first() +
                "\nType help to see supported built-ins. This is not a Linux shell."
        }
    }
}

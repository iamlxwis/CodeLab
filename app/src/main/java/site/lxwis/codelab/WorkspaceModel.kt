package site.lxwis.codelab

import android.app.Application
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import java.io.File
import java.util.concurrent.Executors

class WorkspaceModel(app: Application) : AndroidViewModel(app) {
    private val store = ProjectStore(File(app.filesDir, "workspace"))
    private val preferences = app.getSharedPreferences("appearance", 0)
    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    val runner = RunSession(app)
    var appearance by mutableStateOf(preferences.getString("mode", "Dark") ?: "Dark")
        private set
    var projects by mutableStateOf(emptyList<String>())
        private set
    var project by mutableStateOf<String?>(null)
        private set
    var files by mutableStateOf(emptyList<String>())
        private set
    var tabs by mutableStateOf(emptyList<String>())
        private set
    var active by mutableStateOf<String?>(null)
        private set
    var source by mutableStateOf("")
        private set
    var dirty by mutableStateOf(false)
        private set
    var busy by mutableStateOf(false)
        private set
    var console by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    private var revision = 0L
    private val autoSave = Runnable { flush() }

    init {
        work {
            val names = store.projects()
            val session = store.restore()
            val selected = session.project?.takeIf { it in names }
            val open = selected?.let { load(it, session.tabs, session.active) }
            val commit: () -> Unit = { projects = names; if (open != null) apply(open) }
            commit
        }
    }

    private data class Open(val project: String, val files: List<String>, val tabs: List<String>, val file: String?, val text: String)

    private fun load(name: String, wanted: List<String>, target: String?): Open {
        val all = store.files(name)
        val file = target?.takeIf { it in all } ?: wanted.firstOrNull { it in all } ?: all.firstOrNull()
        val open = (wanted.filter { it in all } + listOfNotNull(file)).distinct()
        return Open(name, all, open, file, file?.let { store.read(name, it) }.orEmpty())
    }

    private fun apply(open: Open) {
        project = open.project
        files = open.files
        tabs = open.tabs
        active = open.file
        source = open.text
        dirty = false
        revision++
    }

    private fun persist(open: Open): Open {
        store.remember(open.project, open.tabs, open.file)
        return open
    }

    // Serialize saves and navigation. A failed write keeps the current buffer available.
    private fun work(task: () -> (() -> Unit)) {
        if (busy) return
        main.removeCallbacks(autoSave)
        val p = project
        val f = active
        val text = source
        val mustSave = dirty
        busy = true
        io.execute {
            try {
                if (mustSave && p != null && f != null) store.save(p, f, text)
                val commit = task()
                main.post { dirty = false; commit(); busy = false }
            } catch (e: Exception) {
                main.post { error = e.localizedMessage ?: "Workspace operation failed"; busy = false }
            }
        }
    }

    fun edit(value: String) {
        if (busy || value == source) return
        source = value
        dirty = true
        revision++
        main.removeCallbacks(autoSave)
        main.postDelayed(autoSave, 350)
    }

    fun flush() {
        if (!dirty || busy) return
        main.removeCallbacks(autoSave)
        val p = project ?: return
        val f = active ?: return
        val version = revision
        val text = source
        io.execute {
            try {
                store.save(p, f, text)
                main.post { if (revision == version) dirty = false }
            } catch (e: Exception) { main.post { if (revision == version) error = "Save failed: ${e.localizedMessage}" } }
        }
    }

    fun openProject(name: String, create: Boolean = false) {
        work {
            if (create) store.createProject(name)
            val opened = persist(load(name, emptyList(), null))
            val names = store.projects()
            val commit: () -> Unit = { runner.stop(); console = false; projects = names; apply(opened) }
            commit
        }
    }

    fun home() {
        work {
            store.remember(null, emptyList(), null)
            val names = store.projects()
            val commit: () -> Unit = {
                runner.stop(); projects = names; project = null; active = null
                files = emptyList(); tabs = emptyList(); source = ""; console = false; revision++
            }
            commit
        }
    }

    fun select(path: String, create: Boolean = false) {
        val p = project ?: return
        val wanted = tabs + path
        work {
            if (create) store.createFile(p, path)
            val opened = persist(load(p, wanted, path))
            val commit: () -> Unit = { apply(opened); console = false }
            commit
        }
    }

    fun changeFile(path: String, replacement: String?) {
        val p = project ?: return
        val wanted = tabs.mapNotNull { if (it == path) replacement else it }
        val target = if (active == path) replacement else active
        work {
            if (replacement == null) store.deleteFile(p, path) else store.renameFile(p, path, replacement)
            val opened = persist(load(p, wanted, target))
            val commit: () -> Unit = { apply(opened) }
            commit
        }
    }

    fun closeTab(path: String) {
        val p = project ?: return
        val wanted = tabs - path
        val target = if (active == path) wanted.lastOrNull() else active
        work {
            val opened = if (wanted.isEmpty()) Open(p, store.files(p), emptyList(), null, "") else load(p, wanted, target)
            persist(opened)
            val commit: () -> Unit = { apply(opened) }
            commit
        }
    }

    fun export(uri: Uri) {
        val p = project ?: return
        work {
            val resolver = getApplication<Application>().contentResolver
            requireNotNull(resolver.openOutputStream(uri, "wt")) { "Cannot open destination" }.use { store.exportZip(p, it) }
            val commit: () -> Unit = {}
            commit
        }
    }

    fun importArchive(name: String, uri: Uri) {
        work {
            val resolver = getApplication<Application>().contentResolver
            val p = requireNotNull(resolver.openInputStream(uri)) { "Cannot open archive" }.use { store.importZip(name, it) }
            val opened = persist(load(p, emptyList(), null))
            val names = store.projects()
            val commit: () -> Unit = { projects = names; apply(opened); console = false }
            commit
        }
    }

    fun setMode(mode: String) {
        appearance = mode
        preferences.edit().putString("mode", mode).apply()
    }

    fun run() {
        val filename = active ?: return
        flush()
        console = true
        runner.run(filename, source)
    }

    override fun onCleared() {
        flush()
        main.removeCallbacks(autoSave)
        io.shutdown()
        runner.close()
        super.onCleared()
    }
}

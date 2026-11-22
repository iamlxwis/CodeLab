package site.lxwis.codelab

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.OpenableColumns
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
    private val cloudIo = Executors.newSingleThreadExecutor()
    private val connectivity = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val cloudPreferences = app.getSharedPreferences("cloud-runtime", Context.MODE_PRIVATE)
    var cloudEndpoint by mutableStateOf(
        cloudPreferences.getString("endpoint", CloudJudge0.DEFAULT_ENDPOINT) ?: CloudJudge0.DEFAULT_ENDPOINT)
        private set
    var cloudHeader by mutableStateOf(cloudPreferences.getString("header", "X-Auth-Token") ?: "X-Auth-Token")
        private set
    var cloudCredential by mutableStateOf(cloudPreferences.getString("credential", "") ?: "")
        private set
    var cloudChecking by mutableStateOf(false)
        private set
    var cloudError by mutableStateOf<String?>(null)
        private set
    var cloudLanguages by mutableStateOf<Map<String, CloudJudge0.Language>>(emptyMap())
        private set
    var cloudPending by mutableStateOf<String?>(null)
        private set
    var isOnline by mutableStateOf(false)
        private set
    private val connectivityCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { main.post { updateNetwork() } }
        override fun onLost(network: Network) { main.post { updateNetwork() } }
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            main.post { updateNetwork() }
        }
    }
    private fun updateNetwork() {
        val network = connectivity.activeNetwork
        val connected = network != null && connectivity.getNetworkCapabilities(network)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        val wasConnected = isOnline
        isOnline = connected
        if (!connected) {
            cloudLanguages = emptyMap()
            cloudError = "No internet connection"
            cloudChecking = false
        } else if (!wasConnected) refreshCloudLanguages()
    }

    fun refreshCloudLanguages() {
        if (!isOnline) {
            cloudLanguages = emptyMap()
            cloudError = "No internet connection"
            return
        }
        val endpoint = cloudEndpoint
        val header = cloudHeader
        val credential = cloudCredential
        cloudChecking = true
        cloudError = null
        cloudIo.execute {
            val result = runCatching { CloudJudge0.fetchLanguages(endpoint, header, credential) }
            main.post {
                if (endpoint != cloudEndpoint || credential != cloudCredential || header != cloudHeader) return@post
                cloudChecking = false
                result.onSuccess { cloudLanguages = it; cloudError = null }
                    .onFailure {
                        cloudLanguages = emptyMap()
                        cloudError = it.message ?: "Online compiler unavailable"
                    }
            }
        }
    }

    fun saveCloudConfig(url: String, header: String, credential: String) {
        try {
            val validated = CloudJudge0.validateUrl(url)
            require(header in listOf("X-Auth-Token", "Authorization", "X-RapidAPI-Key")) {
                "Choose a supported authentication header"
            }
            require(credential.length <= 512 && credential.none { it == '\r' || it == '\n' }) {
                "Invalid API token"
            }
            cloudEndpoint = validated
            cloudHeader = header
            cloudCredential = credential
            cloudPreferences.edit().putString("endpoint", validated).putString("header", header)
                .putString("credential", credential).apply()
            cloudLanguages = emptyMap()
            refreshCloudLanguages()
        } catch (e: IllegalArgumentException) {
            error = e.message ?: "Invalid cloud compiler settings"
        }
    }

    fun isLocalExecutable(path: String): Boolean =
        path.endsWith(".js", true) || path.endsWith(".sql", true) ||
        path.endsWith(".lua", true) || path.endsWith(".java", true)

    fun cloudAvailable(path: String): Boolean =
        isOnline && cloudLanguages.containsKey(CloudJudge0.extension(path))

    fun requestCloudRun() {
        val filename = active ?: return
        if (!cloudAvailable(filename)) {
            error = "Online execution is unavailable for this language. Check Languages & Cloud."
            return
        }
        cloudPending = filename
    }

    fun startCloudRun(stdin: String) {
        val filename = cloudPending ?: return
        cloudPending = null
        if (active != filename) { error = "File changed. Select Run again."; return }
        val language = cloudLanguages[CloudJudge0.extension(filename)]
        if (!isOnline || language == null) { error = "Online compiler is unavailable"; return }
        flush()
        runner.runOnline(filename, source, cloudEndpoint, cloudHeader, cloudCredential, language, stdin.take(4096))
        console = true
        preview = false
    }

    fun cancelCloudRun() { cloudPending = null }
    private val main = Handler(Looper.getMainLooper())
    val runner = RunSession(app)
    var appearance by mutableStateOf(preferences.getString("mode", "Dark") ?: "Dark")
        private set
    var themePreset by mutableStateOf(preferences.getString("preset", "Obsidian") ?: "Obsidian")
        private set
    fun choosePreset(value: String) {
        if (value !in listOf("Obsidian", "Nocturne", "Paper")) return
        themePreset = value
        preferences.edit().putString("preset", value).apply()
    }
    var motionPreference by mutableStateOf(preferences.getString("motion", "System") ?: "System")
        private set
    fun chooseMotion(value: String) {
        if (value !in listOf("System", "Full", "Reduced")) return
        motionPreference = value
        preferences.edit().putString("motion", value).apply()
    }
    var motionIntensity by mutableIntStateOf(preferences.getInt("motion-intensity", 100))
        private set
    fun chooseMotionIntensity(value: Int) {
        motionIntensity = value.coerceIn(0, 100)
        preferences.edit().putInt("motion-intensity", motionIntensity).apply()
    }
    var interfaceDensity by mutableStateOf(preferences.getString("density", "Comfortable") ?: "Comfortable")
        private set
    fun chooseDensity(value: String) {
        if (value !in listOf("Compact", "Comfortable", "Spacious")) return
        interfaceDensity = value
        preferences.edit().putString("density", value).apply()
    }
    var accent by mutableStateOf(preferences.getString("accent", "Mint") ?: "Mint")
        private set
    var fileHistory by mutableStateOf(emptyList<ProjectStore.FileVersion>())
        private set
    var historyPreview by mutableStateOf<String?>(null)
        private set
    var trashItems by mutableStateOf(emptyList<ProjectStore.TrashItem>())
        private set
    var replaceSummary by mutableStateOf<ProjectStore.ReplacementSummary?>(null)
        private set
    fun clearReplaceSummary() { replaceSummary = null }
    var searchHits by mutableStateOf(emptyList<ProjectStore.SearchHit>())
        private set
    var pendingLine by mutableStateOf<Int?>(null)
        private set
    fun consumeJump() { pendingLine = null }
    var folders by mutableStateOf(emptyList<String>())
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
    var preview by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    private var revision = 0L
    private val autoSave = Runnable { flush() }

    init {
        updateNetwork()
        try { connectivity.registerDefaultNetworkCallback(connectivityCallback) }
        catch (_: Exception) { cloudError = "Unable to monitor internet connection" }
        work {
            val names = store.projects()
            val deleted = store.trashItems()
            val session = store.restore()
            val selected = session.project?.takeIf { it in names }
            val open = selected?.let { load(it, session.tabs, session.active, autoOpen = false, allowUnavailable = true) }
            val commit: () -> Unit = {
                projects = names
                trashItems = deleted
                if (open != null) apply(open)
            }
            commit
        }
    }

    private data class Open(val project: String, val files: List<String>,
        val tabs: List<String>, val file: String?, val text: String,
        val folders: List<String> = emptyList())

    private fun load(
        name: String, wanted: List<String>, target: String?,
        autoOpen: Boolean = true, allowUnavailable: Boolean = false
    ): Open {
        val all = store.files(name)
        val available = wanted.filter { it in all }
        val default = if (autoOpen) all.firstOrNull { it == "main.js" } ?: all.firstOrNull() else null
        val file = target?.takeIf { it in all } ?: available.firstOrNull() ?: default
        val text = try {
            file?.let { store.read(name, it) }.orEmpty()
        } catch (e: java.io.IOException) {
            if (!allowUnavailable) throw e
            return Open(name, all, available - listOfNotNull(file).toSet(), null, "", store.folders(name))
        }
        return Open(name, all, (available + listOfNotNull(file)).distinct(), file, text, store.folders(name))
    }

    private fun apply(open: Open) {
        project = open.project
        files = open.files
        folders = open.folders
        tabs = open.tabs
        active = open.file
        source = open.text
        dirty = false
        preview = false
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

    fun openProject(name: String, create: Boolean = false, template: String = "JavaScript") {
        work {
            if (create) store.createProject(name, template)
            val opened = persist(load(name, emptyList(), null, allowUnavailable = true))
            val names = store.projects()
            val commit: () -> Unit = { runner.stop(); console = false; projects = names; apply(opened) }
            commit
        }
    }

    fun duplicateProject(from: String, to: String) {
        work {
            store.duplicateProject(from, to)
            val names = store.projects()
            val commit: () -> Unit = { projects = names }
            commit
        }
    }

    fun manageProject(name: String, replacement: String?) {
        work {
            if (replacement == null) store.deleteProject(name) else store.renameProject(name, replacement)
            val names = store.projects()
            val deleted = store.trashItems()
            val commit: () -> Unit = { projects = names; trashItems = deleted }
            commit
        }
    }

    fun home() {
        work {
            store.remember(null, emptyList(), null)
            val names = store.projects()
            val commit: () -> Unit = {
                runner.stop(); projects = names; project = null; active = null
                files = emptyList(); folders = emptyList(); tabs = emptyList()
                source = ""; console = false; preview = false; revision++
            }
            commit
        }
    }

    fun select(path: String, create: Boolean = false, goToLine: Int? = null) {
        val p = project ?: return
        val wanted = tabs + path
        work {
            if (create) store.createFile(p, path)
            val opened = persist(load(p, wanted, path))
            val commit: () -> Unit = {
                apply(opened)
                pendingLine = goToLine
                console = false
            }
            commit
        }
    }

    fun createFolder(path: String) {
        val p = project ?: return
        work {
            store.createFolder(p, path)
            val opened = persist(load(p, tabs, active, allowUnavailable = true))
            val commit: () -> Unit = { apply(opened) }
            commit
        }
    }

    fun changeFolder(path: String, replacement: String?) {
        val p = project ?: return
        val prefix = path.removeSuffix("/") + "/"
        val wanted = tabs.mapNotNull { name ->
            if (name.startsWith(prefix)) replacement?.removeSuffix("/")?.plus("/")?.plus(name.removePrefix(prefix))
            else name
        }
        val target = active?.let { name ->
            if (name.startsWith(prefix)) replacement?.removeSuffix("/")?.plus("/")?.plus(name.removePrefix(prefix))
            else name
        }
        work {
            if (replacement == null) store.deleteFile(p, path.removeSuffix("/"))
            else store.renameFile(p, path.removeSuffix("/"), replacement.removeSuffix("/"))
            val opened = persist(load(p, wanted, target, allowUnavailable = true))
            val deleted = store.trashItems()
            val commit: () -> Unit = { apply(opened); trashItems = deleted }
            commit
        }
    }

    fun copyFileToProject(path: String, destinationProject: String, destination: String) {
        val p = project ?: return
        work {
            store.copyToProject(p, path, destinationProject, destination)
            val commit: () -> Unit = {}
            commit
        }
    }

    fun duplicateFile(from: String, to: String) {
        val p = project ?: return
        work {
            store.duplicateFile(p, from, to)
            val opened = persist(load(p, tabs + to, to))
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
            val deleted = store.trashItems()
            val opened = persist(load(p, wanted, target))
            val commit: () -> Unit = { apply(opened); trashItems = deleted }
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

    fun exportFile(uri: Uri) {
        val p = project ?: return
        val path = active ?: return
        work {
            val resolver = getApplication<Application>().contentResolver
            requireNotNull(resolver.openOutputStream(uri, "wt")) { "Cannot open destination" }
                .use { store.exportSingleFile(p, path, it) }
            val commit: () -> Unit = {}
            commit
        }
    }

    fun importFile(uri: Uri, name: String) {
        val p = project ?: return
        work {
            val resolver = getApplication<Application>().contentResolver
            requireNotNull(resolver.openInputStream(uri)) { "Cannot open source file" }
                .use { store.importSingleFile(p, name, it) }
            val opened = persist(load(p, tabs + name, name))
            val commit: () -> Unit = { apply(opened); console = false }
            commit
        }
    }

    fun importFiles(uris: List<Uri>) {
        val p = project ?: return
        if (uris.isEmpty()) return
        work {
            val resolver = getApplication<Application>().contentResolver
            val existing = store.files(p).toMutableSet()
            val imported = mutableListOf<String>()
            uris.forEachIndexed { index, uri ->
                val display = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME),
                    null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                } ?: "import-${index + 1}.txt"
                val sanitized = display.substringAfterLast('/').substringAfterLast('\\')
                    .replace(Regex("[^A-Za-z0-9._ -]"), "_")
                    .trim().take(100).ifBlank { "import-${index + 1}.txt" }
                val stem = sanitized.substringBeforeLast('.', sanitized)
                val extension = if (sanitized.contains('.')) "." + sanitized.substringAfterLast('.') else ""
                var name = sanitized
                var counter = 2
                while (name in existing) {
                    name = "${stem}-${counter++}${extension}"
                }
                requireNotNull(resolver.openInputStream(uri)) { "Cannot open source file" }
                    .use { store.importSingleFile(p, name, it) }
                existing.add(name)
                imported.add(name)
            }
            val last = imported.last()
            val opened = persist(load(p, tabs + imported, last))
            val commit: () -> Unit = { apply(opened); console = false }
            commit
        }
    }

    fun refreshHistory() {
        val p = project ?: return
        val f = active ?: return
        work {
            val list = store.versions(p, f)
            val commit: () -> Unit = { fileHistory = list; historyPreview = null }
            commit
        }
    }

    fun previewHistory(id: String) {
        val p = project ?: return
        val f = active ?: return
        work {
            val oldText = store.readVersion(p, f, id)
            val commit: () -> Unit = { historyPreview = oldText.take(5000) }
            commit
        }
    }

    fun restoreHistory(id: String) {
        val p = project ?: return
        val f = active ?: return
        work {
            store.restoreVersion(p, f, id)
            val reopened = persist(load(p, tabs, f))
            val versions = store.versions(p, f)
            val commit: () -> Unit = {
                apply(reopened)
                fileHistory = versions
                historyPreview = null
            }
            commit
        }
    }

    fun previewReplaceAcrossFiles(query: String, replacement: String,
        matchCase: Boolean, regex: Boolean) {
        val p = project ?: return
        work {
            val summary = store.replaceAcrossProject(p, query, replacement, matchCase, regex, false)
            val commit: () -> Unit = { replaceSummary = summary }
            commit
        }
    }

    fun replaceAcrossFiles(query: String, replacement: String,
        matchCase: Boolean, regex: Boolean) {
        val p = project ?: return
        work {
            val summary = store.replaceAcrossProject(p, query, replacement, matchCase, regex, true)
            val opened = persist(load(p, tabs, active, allowUnavailable = true))
            val commit: () -> Unit = {
                apply(opened)
                replaceSummary = summary
            }
            commit
        }
    }

    fun searchProject(query: String, caseSensitive: Boolean) {
        val p = project ?: return
        work {
            val found = store.searchText(p, query, caseSensitive)
            val commit: () -> Unit = { searchHits = found }
            commit
        }
    }

    fun restoreTrash(id: String) {
        val current = project
        work {
            store.restoreTrash(id)
            val names = store.projects()
            val deleted = store.trashItems()
            val opened = current?.takeIf { it in names }?.let {
                persist(load(it, tabs, active, allowUnavailable = true))
            }
            val commit: () -> Unit = {
                projects = names
                trashItems = deleted
                if (opened != null) apply(opened)
            }
            commit
        }
    }

    fun emptyTrash() {
        work {
            store.emptyTrash()
            val commit: () -> Unit = { trashItems = emptyList() }
            commit
        }
    }

    fun importArchive(name: String, uri: Uri) {
        work {
            val resolver = getApplication<Application>().contentResolver
            val p = requireNotNull(resolver.openInputStream(uri)) { "Cannot open archive" }.use { store.importZip(name, it) }
            val opened = persist(load(p, emptyList(), null, allowUnavailable = true))
            val names = store.projects()
            val commit: () -> Unit = { projects = names; apply(opened); console = false }
            commit
        }
    }

    fun setMode(mode: String) {
        appearance = mode
        preferences.edit().putString("mode", mode).apply()
    }

    fun chooseAccent(value: String) {
        if (value !in listOf("Mint", "Blue", "Violet", "Amber")) return
        accent = value
        preferences.edit().putString("accent", value).apply()
    }

    fun run() {
        val filename = active ?: return
        if (filename.endsWith(".html", true) || filename.endsWith(".htm", true)) {
            flush()
            preview = true
            console = false
            return
        }
        if (isLocalExecutable(filename)) {
            flush()
            console = true
            preview = false
            runner.run(filename, source)
        } else if (cloudAvailable(filename)) {
            cloudPending = filename
        } else {
            error = if (!isOnline) "This language needs an online compiler. Connect to the internet, then check Languages & Cloud."
                else if (cloudChecking) "Checking available online languages. Try again shortly."
                else "No verified online compiler for this file type. Open Languages & Cloud to configure a Judge0 endpoint."
        }
    }

    override fun onCleared() {
        flush()
        main.removeCallbacks(autoSave)
        io.shutdown()
        cloudIo.shutdownNow()
        try { connectivity.unregisterNetworkCallback(connectivityCallback) } catch (_: Exception) {}
        runner.close()
        super.onCleared()
    }
}

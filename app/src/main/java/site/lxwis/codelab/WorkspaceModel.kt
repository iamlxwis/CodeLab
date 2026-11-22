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
    var recentCommandIds by mutableStateOf(
        preferences.getString("recent-commands", "").orEmpty()
            .split(',').filter { it.isNotBlank() }.take(8))
        private set
    fun rememberCommand(id: String) {
        recentCommandIds = (listOf(id) + recentCommandIds.filterNot { it == id }).take(8)
        preferences.edit().putString("recent-commands", recentCommandIds.joinToString(",")).apply()
    }
    var quickEditorActions by mutableStateOf(
        preferences.getString("quick-editor", "newfile,files,projectsearch,commands")
            .orEmpty().split(',').filter { it.isNotBlank() })
        private set
    var quickHomeActions by mutableStateOf(
        preferences.getString("quick-home", "newproject,importzip,languages,appearance")
            .orEmpty().split(',').filter { it.isNotBlank() })
        private set
    fun toggleQuickAction(id: String, projectOpen: Boolean) {
        if (projectOpen) {
            quickEditorActions = updatedQuickActionIds(quickEditorActions, id)
            preferences.edit().putString("quick-editor", quickEditorActions.joinToString(",")).apply()
        } else {
            quickHomeActions = updatedQuickActionIds(quickHomeActions, id)
            preferences.edit().putString("quick-home", quickHomeActions.joinToString(",")).apply()
        }
    }
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
        if (value !in listOf("Obsidian", "Nocturne", "Paper",
                "Dracula", "One Dark", "Monokai", "GitHub Light", "OLED")) return
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
    // Phone editor/console split is opt-in; preferences survive process death.
    var phoneSplitEnabled by mutableStateOf(preferences.getBoolean("phone-split", false))
        private set
    fun choosePhoneSplitEnabled(enabled: Boolean) {
        phoneSplitEnabled = enabled
        preferences.edit().putBoolean("phone-split", enabled).apply()
    }
    var phoneSplitPercent by mutableIntStateOf(
        clampPhoneSplit(preferences.getInt("phone-split-percent", 60)))
        private set
    fun choosePhoneSplitPercent(percent: Int) {
        phoneSplitPercent = clampPhoneSplit(percent)
        preferences.edit().putInt("phone-split-percent", phoneSplitPercent).apply()
    }

    var panelSplitPercent by mutableIntStateOf(preferences.getInt("panel-split", 60))
        private set
    fun setPanelSplit(value: Int) {
        panelSplitPercent = value.coerceIn(35, 80)
        preferences.edit().putInt("panel-split", panelSplitPercent).apply()
    }
    var motionIntensity by mutableIntStateOf(preferences.getInt("motion-intensity", 100))
        private set
    fun chooseMotionIntensity(value: Int) {
        motionIntensity = value.coerceIn(0, 100)
        preferences.edit().putInt("motion-intensity", motionIntensity).apply()
    }
    var highContrast by mutableStateOf(preferences.getBoolean("high-contrast", false))
        private set
    fun chooseHighContrast(value: Boolean) {
        highContrast = value
        preferences.edit().putBoolean("high-contrast", value).apply()
    }
    var largeInterfaceText by mutableStateOf(preferences.getBoolean("large-interface-text", false))
        private set
    fun chooseLargeInterfaceText(value: Boolean) {
        largeInterfaceText = value
        preferences.edit().putBoolean("large-interface-text", value).apply()
    }
    var touchFeedback by mutableStateOf(preferences.getBoolean("touch-feedback", true))
        private set
    fun chooseTouchFeedback(value: Boolean) {
        touchFeedback = value
        preferences.edit().putBoolean("touch-feedback", value).apply()
    }
    var lastOperationMillis by mutableLongStateOf(0L)
        private set
    var lastSaveMillis by mutableLongStateOf(0L)
        private set
    var lastOperationName by mutableStateOf("Not measured")
        private set
    var interfaceDensity by mutableStateOf(preferences.getString("density", "Comfortable") ?: "Comfortable")
        private set
    fun chooseDensity(value: String) {
        if (value !in listOf("Compact", "Comfortable", "Spacious")) return
        interfaceDensity = value
        preferences.edit().putString("density", value).apply()
    }
    var accent by mutableStateOf(preferences.getString("accent", "Mint") ?: "Mint")
        private set
    private var shellSession: WorkspaceShell? = null
    private var shellProject: String? = null
    var terminalOutput by mutableStateOf("CodeLab workspace commands\nType help for available commands.")
        private set
    fun clearWorkspaceTerminal() {
        terminalOutput = "CodeLab workspace commands\nType help for available commands."
    }
    fun executeWorkspaceCommand(line: String) {
        val p = project ?: return
        work {
            val terminal = if (shellProject == p) shellSession ?: WorkspaceShell(store, p)
                else WorkspaceShell(store, p)
            val result = runCatching { terminal.execute(line) }
                .getOrElse { "Error: " + (it.localizedMessage ?: "Command failed") }
            val apply: () -> Unit = {
                shellSession = terminal
                shellProject = p
                terminalOutput = (terminalOutput + "\n$ " + line.take(512) +
                    "\n" + result).takeLast(24000)
            }
            apply
        }
    }

    var projectMetadata by mutableStateOf(
        ProjectStore.ProjectMetadata("Imported", "", ""))
        private set
    fun configureProject(description: String, entrypoint: String) {
        val p = project ?: return
        work {
            val updated = store.configureProject(p, description, entrypoint)
            val commit: () -> Unit = { projectMetadata = updated }
            commit
        }
    }

    internal var gitState by mutableStateOf<GitWorkspace.Snapshot?>(null)
        private set
    fun gitRefresh() {
        val p = project ?: return
        work {
            val state = GitWorkspace(store.projectDirectory(p)).inspect()
            val apply: () -> Unit = { gitState = state }
            apply
        }
    }
    fun gitInit() {
        val p = project ?: return
        work {
            val git = GitWorkspace(store.projectDirectory(p))
            git.init()
            val state = git.inspect()
            val apply: () -> Unit = { gitState = state }
            apply
        }
    }
    fun gitCommit(message: String, name: String, email: String) {
        val p = project ?: return
        work {
            val git = GitWorkspace(store.projectDirectory(p))
            git.commit(message, name, email)
            val state = git.inspect()
            val apply: () -> Unit = { gitState = state }
            apply
        }
    }
    fun gitBranch(name: String) {
        val p = project ?: return
        work {
            val git = GitWorkspace(store.projectDirectory(p))
            git.createBranch(name)
            val snapshot = git.inspect()
            val opened = persist(load(p, tabs, active, allowUnavailable = true))
            val apply: () -> Unit = { apply(opened); gitState = snapshot }
            apply
        }
    }
    fun gitCheckout(name: String) {
        val p = project ?: return
        work {
            val git = GitWorkspace(store.projectDirectory(p))
            git.checkout(name)
            val snapshot = git.inspect()
            val opened = persist(load(p, tabs, active, allowUnavailable = true))
            val apply: () -> Unit = { apply(opened); gitState = snapshot }
            apply
        }
    }
    var fileHistory by mutableStateOf(emptyList<ProjectStore.FileVersion>())
        private set
    var historyPreview by mutableStateOf<String?>(null)
        private set
    var trashItems by mutableStateOf(emptyList<ProjectStore.TrashItem>())
        private set
    var undoDeletion by mutableStateOf<UndoDeletion?>(null)
        private set
    fun dismissUndo(id: String) {
        if (undoDeletion?.id == id) undoDeletion = null
    }
    fun undoDelete(id: String) {
        if (undoDeletion?.id != id) return
        undoDeletion = null
        restoreTrash(id)
    }
    var replaceSummary by mutableStateOf<ProjectStore.ReplacementSummary?>(null)
        private set
    var replaceHits by mutableStateOf(emptyList<ProjectStore.ReplaceHit>())
        private set
    fun clearReplaceSummary() { replaceSummary = null; replaceHits = emptyList() }
    var searchHits by mutableStateOf(emptyList<ProjectStore.SearchHit>())
        private set
    fun clearSearchHits() { searchHits = emptyList() }
    var pendingLine by mutableStateOf<Int?>(null)
        private set
    fun consumeJump() { pendingLine = null }
    var folders by mutableStateOf(emptyList<String>())
        private set
    var favorites by mutableStateOf(
        preferences.getStringSet("favorite-projects", emptySet())?.toSet() ?: emptySet())
        private set
    private fun storeFavorites(next: Set<String>) {
        favorites = next
        preferences.edit().putStringSet("favorite-projects", next.toSet()).apply()
    }
    fun toggleFavoriteProject(name: String) {
        if (name !in projects) return
        storeFavorites(if (name in favorites) favorites - name else favorites + name)
    }
    var projects by mutableStateOf(emptyList<String>())
        private set
    var project by mutableStateOf<String?>(null)
        private set
    var files by mutableStateOf(emptyList<String>())
        private set
    var tabs by mutableStateOf(emptyList<String>())
        private set
    var pinnedTabs by mutableStateOf(emptySet<String>())
        private set
    var dirtyTabKeys by mutableStateOf(emptySet<String>())
        private set
    fun isTabDirty(path: String): Boolean =
        project?.let { bufferKey(it, path) in dirtyTabKeys } == true

    fun togglePinnedTab(path: String) {
        val p = project ?: return
        if (path !in tabs) return
        pinnedTabs = if (path in pinnedTabs) pinnedTabs - path else pinnedTabs + path
        preferences.edit().putStringSet("pinned:$p", pinnedTabs).apply()
    }

    fun reorderTab(path: String, offset: Int) {
        val p = project ?: return
        val updated = reorderOpenTab(tabs, path, offset)
        if (updated == tabs) return
        tabs = updated
        val selected = active
        io.execute {
            try { store.remember(p, updated, selected) }
            catch (e: Exception) { main.post { error = e.localizedMessage ?: "Could not remember tab order" } }
        }
    }

    fun closeOtherTabs(path: String?) {
        val p = project ?: return
        val wanted = tabs.filter { it in pinnedTabs || it == path }
        if (wanted.size == tabs.size) return
        val target = active?.takeIf { it in wanted } ?: wanted.lastOrNull()
        work {
            val opened = if (wanted.isEmpty()) Open(p, store.files(p), emptyList(), null, "")
                else load(p, wanted, target, autoOpen = false)
            persist(opened)
            val commit: () -> Unit = { apply(opened) }
            commit
        }
    }

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
    private val autoSave = Runnable { saveAsync(durable = false) }
    private val openBuffers = mutableMapOf<String, String>()
    private val unsavedBuffers = mutableSetOf<String>()
    private var pendingOperations = 0
    private var navigationVersion = 0L
    private fun bufferKey(p: String, f: String) = "$p/$f"
    private fun beginOperation() { pendingOperations++; busy = true }
    private fun endOperation() { pendingOperations = (pendingOperations - 1).coerceAtLeast(0); busy = pendingOperations > 0 }
    private fun markSaved(p: String, f: String, text: String) {
        val key = bufferKey(p, f)
        if (openBuffers[key] == text) {
            unsavedBuffers.remove(key)
            dirtyTabKeys = dirtyTabKeys - key
            if (project == p && active == f) dirty = false
        }
    }

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
        val folders: List<String> = emptyList(),
        val metadata: ProjectStore.ProjectMetadata =
            ProjectStore.ProjectMetadata("Imported", "", ""))

    private fun load(
        name: String, wanted: List<String>, target: String?,
        autoOpen: Boolean = true, allowUnavailable: Boolean = false
    ): Open {
        val all = store.files(name)
        val available = wanted.filter { it in all }
        val default = if (autoOpen) {
            listOf("main.js", "index.html", "main.lua", "main.sql", "Main.java",
                "main.py", "main.kt", "main.ts").firstOrNull { it in all }
                ?: all.firstOrNull { !ProjectStore.isBinaryAsset(it) }
        } else null
        val file = target?.takeIf { it in all } ?: available.firstOrNull() ?: default
        val text = try {
            file?.let { store.read(name, it) }.orEmpty()
        } catch (e: java.io.IOException) {
            if (!allowUnavailable) throw e
            return Open(name, all, available - listOfNotNull(file).toSet(), null,
                "", store.folders(name), store.metadata(name))
        }
        return Open(name, all, (available + listOfNotNull(file)).distinct(), file,
            text, store.folders(name), store.metadata(name))
    }

    private fun apply(open: Open) {
        if (project != open.project) {
            shellSession = null
            shellProject = null
            clearWorkspaceTerminal()
        }
        if (project != open.project)
            pinnedTabs = preferences.getStringSet("pinned:${open.project}", emptySet())
                ?.filter { it in open.files }?.toSet() ?: emptySet()
        else pinnedTabs = pinnedTabs.filter { it in open.files }.toSet()
        project = open.project
        files = open.files
        folders = open.folders
        projectMetadata = open.metadata
        tabs = open.tabs
        active = open.file
        val key = open.file?.let { bufferKey(open.project, it) }
        val keepEdits = key != null && key in unsavedBuffers
        source = if (keepEdits) openBuffers[key] ?: open.text else open.text
        dirty = keepEdits
        if (key != null) openBuffers[key] = source
        preview = false
        revision++
    }

    private fun persist(open: Open): Open {
        store.remember(open.project, open.tabs, open.file)
        return open
    }

    // All I/O is serialized. The main-thread editor never waits for a disk operation.
    // A versioned, per-file buffer prevents slow callbacks from overwriting newer typing.
    private fun work(task: () -> (() -> Unit)) {
        main.removeCallbacks(autoSave)
        val p = project
        val f = active
        val captured = source
        val mustSave = dirty
        val requestVersion = revision
        beginOperation()
        io.execute {
            val started = android.os.SystemClock.elapsedRealtime()
            try {
                if (mustSave && p != null && f != null) store.save(p, f, captured, true)
                val commit = task()
                val elapsed = android.os.SystemClock.elapsedRealtime() - started
                main.post {
                    if (mustSave && p != null && f != null) markSaved(p, f, captured)
                    // A callback can arrive after a newer edit or navigation.
                    if (revision == requestVersion && project == p && active == f && mustSave)
                        dirty = false
                    commit()
                    lastOperationMillis = elapsed
                    lastOperationName = "Workspace I/O"
                    endOperation()
                }
            } catch (e: Exception) {
                val elapsed = android.os.SystemClock.elapsedRealtime() - started
                main.post {
                    lastOperationMillis = elapsed
                    lastOperationName = "Failed workspace I/O"
                    error = e.localizedMessage ?: "Workspace operation failed"
                    endOperation()
                }
            }
        }
    }

    fun edit(value: String) {
        if (value == source) return
        source = value
        project?.let { p -> active?.let { f ->
            val key = bufferKey(p, f)
            openBuffers[key] = value
            unsavedBuffers.add(key)
            dirtyTabKeys = dirtyTabKeys + key
        } }
        dirty = true
        revision++
        main.removeCallbacks(autoSave)
        main.postDelayed(autoSave, 1000)
    }

    private fun saveAsync(durable: Boolean) {
        main.removeCallbacks(autoSave)
        val p = project ?: return
        val f = active ?: return
        if (!dirty && !durable) return
        val version = revision
        val content = source
        beginOperation()
        io.execute {
            val started = android.os.SystemClock.elapsedRealtime()
            try {
                store.save(p, f, content, durable)
                val elapsed = android.os.SystemClock.elapsedRealtime() - started
                main.post {
                    lastSaveMillis = elapsed
                    markSaved(p, f, content)
                    if (revision == version && project == p && active == f) dirty = false
                    endOperation()
                }
            } catch (e: Exception) {
                main.post {
                    if (project == p && active == f && revision == version)
                        error = "Save failed: " + e.localizedMessage
                    endOperation()
                }
            }
        }
    }

    // Explicit Save, lifecycle flush and project switches make the rename durable.
    fun flush() = saveAsync(durable = true)

    fun openProject(name: String, create: Boolean = false, template: String = "JavaScript") {
        flush()
        val nav = ++navigationVersion
        project = name
        files = emptyList()
        tabs = emptyList()
        active = null
        source = ""
        dirty = false
        revision++
        work {
            if (create) store.createProject(name, template)
            val opened = persist(load(name, emptyList(), null, allowUnavailable = true))
            val names = store.projects()
            val commit: () -> Unit = {
                projects = names
                if (nav == navigationVersion && project == name) {
                    runner.stop()
                    console = false
                    apply(opened)
                }
            }
            commit
        }
    }

    fun importExternalFolder(uri: Uri, targetProject: String) {
        val app = getApplication<Application>()
        work {
            ExternalFolderImporter.copyToProject(app, uri, targetProject, store)
            val names = store.projects()
            val opened = persist(load(targetProject, emptyList(), null, allowUnavailable = true))
            val commit: () -> Unit = {
                projects = names
                apply(opened)
                console = false
            }
            commit
        }
    }

    fun importPublicGitHub(slug: String, targetProject: String) {
        work {
            GitHubSnapshotImporter.importPublic(store, slug, targetProject)
            val names = store.projects()
            val opened = persist(load(targetProject, emptyList(), null, allowUnavailable = true))
            val commit: () -> Unit = {
                projects = names
                apply(opened)
                console = false
            }
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
            val previous = if (replacement == null) store.trashItems() else emptyList()
            if (replacement == null) store.deleteProject(name) else store.renameProject(name, replacement)
            val names = store.projects()
            val deleted = store.trashItems()
            val newlyDeleted = if (replacement == null)
                newTrashItem(previous, deleted, name, "") else null
            val newFavorites = if (replacement == null) favorites - name
                else if (name in favorites) (favorites - name) + replacement else favorites
            val commit: () -> Unit = {
                projects = names
                trashItems = deleted
                newlyDeleted?.let { undoDeletion = UndoDeletion(it.id, "Project $name moved to trash") }
                storeFavorites(newFavorites)
            }
            commit
        }
    }

    fun home() {
        flush()
        val nav = ++navigationVersion
        runner.stop()
        project = null
        active = null
        files = emptyList()
        folders = emptyList()
        tabs = emptyList()
        source = ""
        dirty = false
        console = false
        preview = false
        revision++
        work {
            store.remember(null, emptyList(), null)
            val names = store.projects()
            val commit: () -> Unit = { if (nav == navigationVersion) projects = names }
            commit
        }
    }

    fun moveActiveTab(offset: Int) {
        val index = tabs.indexOf(active)
        if (index < 0) return
        val target = (index + offset).coerceIn(0, tabs.lastIndex)
        if (target != index) select(tabs[target])
    }

    fun select(path: String, create: Boolean = false, goToLine: Int? = null,
        runOnOpen: Boolean = false) {
        val p = project ?: return
        if (!create && path !in files) return
        val fromFile = active
        val fromText = source
        val savePrevious = dirty && fromFile != null
        main.removeCallbacks(autoSave)
        val nav = ++navigationVersion
        val wanted = (tabs + path).distinct()
        tabs = wanted
        active = path
        val targetKey = bufferKey(p, path)
        source = openBuffers[targetKey].orEmpty()
        dirty = targetKey in unsavedBuffers
        revision++
        pendingLine = goToLine
        console = false
        preview = false
        beginOperation()
        io.execute {
            val started = android.os.SystemClock.elapsedRealtime()
            try {
                if (savePrevious) store.save(p, requireNotNull(fromFile), fromText, true)
                if (create) store.createFile(p, path)
                val opened = persist(load(p, wanted, path))
                val elapsed = android.os.SystemClock.elapsedRealtime() - started
                main.post {
                    if (savePrevious) markSaved(p, requireNotNull(fromFile), fromText)
                    if (nav == navigationVersion && project == p && active == path) {
                        apply(opened)
                        pendingLine = goToLine
                        if (runOnOpen) run()
                    }
                    lastOperationMillis = elapsed
                    lastOperationName = "Switch file"
                    endOperation()
                }
            } catch (e: Exception) {
                main.post {
                    error = e.localizedMessage ?: "Could not open file"
                    endOperation()
                }
            }
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
            val previous = if (replacement == null) store.trashItems() else emptyList()
            if (replacement == null) store.deleteFile(p, path.removeSuffix("/"))
            else store.renameFile(p, path.removeSuffix("/"), replacement.removeSuffix("/"))
            val opened = persist(load(p, wanted, target, allowUnavailable = true))
            val deleted = store.trashItems()
            val newlyDeleted = if (replacement == null)
                newTrashItem(previous, deleted, p, path.removeSuffix("/")) else null
            val commit: () -> Unit = {
                apply(opened)
                trashItems = deleted
                newlyDeleted?.let { undoDeletion = UndoDeletion(it.id, "Folder $path moved to trash") }
            }
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
            val previous = if (replacement == null) store.trashItems() else emptyList()
            if (replacement == null) store.deleteFile(p, path) else store.renameFile(p, path, replacement)
            val deleted = store.trashItems()
            val newlyDeleted = if (replacement == null)
                newTrashItem(previous, deleted, p, path) else null
            val opened = persist(load(p, wanted, target))
            val commit: () -> Unit = {
                apply(opened)
                trashItems = deleted
                newlyDeleted?.let { undoDeletion = UndoDeletion(it.id, "File $path moved to trash") }
            }
            commit
        }
    }

    fun closeTab(path: String) {
        val p = project ?: return
        if (path in pinnedTabs) return
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

    internal fun previewReplaceAcrossFiles(query: String, replacement: String,
        matchCase: Boolean, regex: Boolean, folder: String = "",
        kind: ProjectFileKind? = null) {
        val p = project ?: return
        val allowed = scopedProjectFiles(files, folder, kind)
        work {
            val summary = store.replaceAcrossProject(p, query, replacement, matchCase,
                regex, false, allowed)
            val hits = store.previewReplaceHits(p, query, replacement, matchCase,
                regex, allowed)
            val commit: () -> Unit = {
                if (project == p) { replaceSummary = summary; replaceHits = hits }
            }
            commit
        }
    }

    internal fun replaceAcrossFiles(query: String, replacement: String,
        matchCase: Boolean, regex: Boolean, folder: String = "",
        kind: ProjectFileKind? = null) {
        val p = project ?: return
        val allowed = scopedProjectFiles(files, folder, kind)
        work {
            val summary = store.replaceAcrossProject(p, query, replacement, matchCase,
                regex, true, allowed)
            val opened = persist(load(p, tabs, active, allowUnavailable = true))
            val commit: () -> Unit = {
                if (project == p) {
                    apply(opened)
                    replaceSummary = summary
                    replaceHits = emptyList()
                }
            }
            commit
        }
    }

    internal fun searchProject(query: String, caseSensitive: Boolean,
        folder: String = "", kind: ProjectFileKind? = null) {
        val p = project ?: return
        val allowed = scopedProjectFiles(files, folder, kind)
        searchHits = emptyList()
        work {
            val found = store.searchText(p, query, caseSensitive, allowed)
            val commit: () -> Unit = { if (project == p) searchHits = found }
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

    fun debugCurrent(lines: List<Int>) {
        val filename = active ?: return
        if (!filename.endsWith(".js", true)) {
            error = "The line debugger supports JavaScript files only."
            return
        }
        flush()
        console = true
        preview = false
        runner.run(filename, source, debug = true, breakpoints = lines)
    }

    fun runPreferred() {
        val target = resolveRunTarget(projectMetadata.entrypoint, active, files)
        if (target != null) runTarget(target)
        else error = "Open a file to run"
    }

    fun runTarget(path: String) {
        if (runner.running) return
        if (path !in files) { error = "File no longer exists: $path"; return }
        if (path == active) run()
        else select(path, runOnOpen = true)
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

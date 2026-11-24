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

internal data class ProjectCardDetail(val template: String, val fileCount: Int, val description: String)

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
            rememberProjectLayout()
        } else {
            quickHomeActions = updatedQuickActionIds(quickHomeActions, id)
            preferences.edit().putString("quick-home", quickHomeActions.joinToString(",")).apply()
        }
    }
    fun moveQuickAction(id: String, delta: Int, projectOpen: Boolean) {
        val ids = (if (projectOpen) quickEditorActions else quickHomeActions).toMutableList()
        val from = ids.indexOf(id)
        if (from < 0) return
        val to = (from + delta).coerceIn(0, ids.lastIndex)
        if (from == to) return
        ids.add(to, ids.removeAt(from))
        if (projectOpen) quickEditorActions = ids else quickHomeActions = ids
        preferences.edit().putString(if (projectOpen) "quick-editor" else "quick-home", ids.joinToString(",")).apply()
        if (projectOpen) rememberProjectLayout()
    }
    private val editorPreferences = app.getSharedPreferences("editor", Context.MODE_PRIVATE)
    internal var editorToolbarIds by mutableStateOf(EditorToolbarOrder.decode(editorPreferences.getString("toolbar-actions", null)))
        private set
    internal fun chooseEditorToolbar(ids: List<String>) {
        editorToolbarIds = ids.filter { it in EditorToolbarOrder.known }.distinct()
        editorPreferences.edit().putString("toolbar-actions", editorToolbarIds.joinToString(",")).apply()
        rememberProjectLayout()
    }
    internal var explorerPinned by mutableStateOf(preferences.getBoolean("explorer-pinned", true))
        private set
    internal fun chooseExplorerPinned(value: Boolean) {
        explorerPinned = value
        preferences.edit().putBoolean("explorer-pinned", value).apply()
        rememberProjectLayout()
    }
    var showEditorStatus by mutableStateOf(preferences.getBoolean("show-editor-status", true))
        private set
    fun chooseEditorStatus(value: Boolean) {
        showEditorStatus = value
        preferences.edit().putBoolean("show-editor-status", value).apply()
        rememberProjectLayout()
    }
    var projectGrid by mutableStateOf(preferences.getBoolean("project-grid", false))
        private set
    fun chooseProjectGrid(value: Boolean) {
        projectGrid = value
        preferences.edit().putBoolean("project-grid", value).apply()
    }
    var focusMode by mutableStateOf(preferences.getBoolean("focus-mode", false))
        private set
    fun chooseFocusMode(value: Boolean) {
        focusMode = value
        preferences.edit().putBoolean("focus-mode", value).apply()
        rememberProjectLayout()
    }
    internal var savedWorkspaceLayouts by mutableStateOf(WorkspaceLayout.decode(preferences.getString("workspace-layouts", null)))
        private set
    private fun currentLayout(name: String) = WorkspaceLayout(name, phoneSplitEnabled, phoneSplitPercent,
        panelSplitPercent, showEditorStatus, interfaceDensity, quickEditorActions, editorToolbarIds, explorerPinned, focusMode)
    private fun rememberProjectLayout() {
        val name = project ?: return
        preferences.edit().putString("project-layout:$name", currentLayout(name).json().toString()).apply()
    }
    private fun applyWorkspaceLayout(value: WorkspaceLayout) {
        phoneSplitEnabled = value.phoneSplit
        phoneSplitPercent = value.phonePercent
        panelSplitPercent = value.tabletPercent
        showEditorStatus = value.status
        interfaceDensity = value.density
        quickEditorActions = value.actions
        explorerPinned = value.explorer
        value.focus?.let {
            focusMode = it
            preferences.edit().putBoolean("focus-mode", it).apply()
        }
        value.toolbar?.let {
            editorToolbarIds = it
            editorPreferences.edit().putString("toolbar-actions", it.joinToString(",")).apply()
        }
        preferences.edit().putBoolean("explorer-pinned", value.explorer).putBoolean("phone-split", value.phoneSplit)
            .putInt("phone-split-percent", value.phonePercent).putInt("panel-split", value.tabletPercent)
            .putBoolean("show-editor-status", value.status).putString("density", value.density)
            .putString("quick-editor", value.actions.joinToString(",")).apply()
    }
    private fun loadProjectLayout(name: String) {
        val raw = preferences.getString("project-layout:$name", null) ?: return
        runCatching { WorkspaceLayout.from(org.json.JSONObject(raw)) }.getOrNull()?.let(::applyWorkspaceLayout)
    }
    internal fun saveNamedLayout(name: String): Boolean {
        val clean = name.trim()
        if (clean.isBlank() || clean.length > 64) return false
        if (savedWorkspaceLayouts.size >= 64 && savedWorkspaceLayouts.none { it.name == clean }) return false
        savedWorkspaceLayouts = savedWorkspaceLayouts.filterNot { it.name == clean } + currentLayout(clean)
        preferences.edit().putString("workspace-layouts", WorkspaceLayout.encode(savedWorkspaceLayouts)).apply()
        return true
    }
    internal fun restoreNamedLayout(name: String) {
        savedWorkspaceLayouts.firstOrNull { it.name == name }?.let { applyWorkspaceLayout(it); rememberProjectLayout() }
    }
    internal fun deleteNamedLayout(name: String) {
        savedWorkspaceLayouts = savedWorkspaceLayouts.filterNot { it.name == name }
        preferences.edit().putString("workspace-layouts", WorkspaceLayout.encode(savedWorkspaceLayouts)).apply()
    }
    private val io = Executors.newSingleThreadExecutor()
    private val cloudIo = Executors.newSingleThreadExecutor()
    private val connectivity = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val cloudPreferences = app.getSharedPreferences("cloud-runtime", Context.MODE_PRIVATE)
    private val cloudVault = CloudCredentialVault(cloudPreferences)
    var cloudEndpoint by mutableStateOf(
        cloudPreferences.getString("endpoint", CloudJudge0.DEFAULT_ENDPOINT) ?: CloudJudge0.DEFAULT_ENDPOINT)
        private set
    var cloudHeader by mutableStateOf(cloudPreferences.getString("header", "X-Auth-Token") ?: "X-Auth-Token")
        private set
    var cloudCredential by mutableStateOf(runCatching { cloudVault.load() }.getOrDefault(""))
        private set
    var cloudChecking by mutableStateOf(false)
        private set
    var cloudError by mutableStateOf<String?>(null)
        private set
    var cloudLanguages by mutableStateOf<Map<String, CloudJudge0.Language>>(emptyMap())
        private set
    var cloudPending by mutableStateOf<String?>(null)
        private set
    private var cloudApproval: OnlineRunApproval? = null
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
            cancelCloudRun()
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
            // Never persist API tokens in plaintext SharedPreferences.
            // Persist first so the UI does not claim success if Keystore fails.
            cloudVault.save(credential)
            cloudEndpoint = validated
            cloudHeader = header
            cloudCredential = credential
            cloudPreferences.edit().putString("endpoint", validated).putString("header", header).apply()
            cloudLanguages = emptyMap()
            refreshCloudLanguages()
        } catch (e: IllegalArgumentException) {
            error = e.message ?: "Invalid cloud compiler settings"
        } catch (_: Exception) {
            error = "Could not save cloud credentials securely. Check device Keystore availability."
        }
    }

    fun isLocalExecutable(path: String): Boolean =
        if (path.endsWith(".py", true)) PythonModuleLocator.installed(getApplication<Application>())
        else if (path.endsWith(".ts", true)) OptionalRuntimeLocator.component(
            getApplication<Application>(), OptionalRuntimeLocator.Pack.TYPESCRIPT) != null
        else RunCapability.forPath(path).mode == RunCapability.Mode.LOCAL

    fun cloudAvailable(path: String): Boolean =
        isOnline && cloudLanguages.containsKey(CloudJudge0.extension(path))

    private fun prepareOnlineApproval(filename: String) {
        val language = cloudLanguages[CloudJudge0.extension(filename)]
        if (language == null) {
            error = "No available online compiler for this file."
            return
        }
        cloudApproval = OnlineRunApproval(filename, revision, cloudEndpoint, language.id)
        cloudPending = filename
    }

    fun requestCloudRun() {
        val filename = active ?: return
        if (runner.running) return
        if (!cloudAvailable(filename)) {
            error = "Online execution is unavailable for this language. Check Languages & Cloud."
            return
        }
        // This command explicitly requests cloud execution, even if Offline was selected.
        chooseExecutionMode("Online")
        prepareOnlineApproval(filename)
    }

    fun startCloudRun(stdin: String) {
        val filename = cloudPending ?: return
        val approval = cloudApproval
        cancelCloudRun() // One-time approval is consumed before network activity.
        val language = cloudLanguages[CloudJudge0.extension(filename)]
        if (approval?.matches(active, revision, executionMode, cloudEndpoint, language?.id) != true) {
            error = "Source, run mode, or online compiler changed. Review the upload again."
            return
        }
        if (!isOnline || language == null) { error = "Online compiler is unavailable"; return }
        flush()
        runner.runOnline(filename, source, cloudEndpoint, cloudHeader, cloudCredential, language, stdin.take(4096))
        console = true
        preview = false
    }

    fun cancelCloudRun() {
        cloudPending = null
        cloudApproval = null
    }
    private val main = Handler(Looper.getMainLooper())
    val runner = RunSession(app)
    var executionMode by mutableStateOf(
        preferences.getString("run-mode", "Offline")?.takeIf { it in listOf("Offline", "Online") }
            ?: "Offline"
    )
        private set
    fun chooseExecutionMode(value: String) {
        if (value !in listOf("Offline", "Online") || runner.running) return
        if (executionMode != value) cancelCloudRun()
        executionMode = value
        preferences.edit().putString("run-mode", value).apply()
    }
    var appearance by mutableStateOf(preferences.getString("mode", "Dark") ?: "Dark")
        private set
    var visualStyle by mutableStateOf(
        preferences.getString("visual-style", null)?.takeIf(CodeLabVisualStyles::accepts)
            ?: when (preferences.getString("preset", "Obsidian")) {
                "OLED" -> "AMOLED"
                "Paper", "GitHub Light" -> "Paper"
                else -> "Obsidian"
            }
    )
        private set
    fun chooseVisualStyle(value: String) {
        if (!CodeLabVisualStyles.accepts(value)) return
        val old = visualStyle
        val canonical = CodeLabVisualStyles.canonical(value)
        val oldCanonical = CodeLabVisualStyles.canonical(old)
        // Migrate legacy style preferences rather than discarding saved customization.
        preferences.edit().putString("accent:$old", accent).putString("accent:$oldCanonical", accent)
            .putString("mode:$old", appearance).putString("mode:$oldCanonical", appearance)
            .putString("studio:$oldCanonical", studioAppearance.encode()).apply()
        if (canonical == oldCanonical) {
            visualStyle = canonical
            preferences.edit().putString("visual-style", canonical).apply()
            return
        }
        fun stored(key: String, fallback: String?): String? {
            val candidates = listOf(canonical, value) + CodeLabVisualStyles.legacyNames(canonical)
            return candidates.asSequence().mapNotNull { preferences.getString("$key:$it", null) }.firstOrNull() ?: fallback
        }
        visualStyle = canonical
        studioAppearance = StudioAppearance.decode(stored("studio", null))
        accent = stored("accent", AppearanceRecipes.defaultAccent(canonical))
            ?.takeIf(AppearanceRecipes::validAccent) ?: AppearanceRecipes.defaultAccent(canonical)
        appearance = stored("mode", CodeLabVisualStyles.defaultMode(canonical))
            ?.takeIf { it in listOf("Dark", "Light", "System") } ?: CodeLabVisualStyles.defaultMode(canonical)
        preferences.edit().putString("visual-style", canonical)
            .putString("accent", accent).putString("mode", appearance).apply()
    }
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
        if (value !in listOf("System", "Full", "Reduced", "Standard", "Fluid")) return
        motionPreference = value
        preferences.edit().putString("motion", value).apply()
    }
    var transitionEffect by mutableStateOf(preferences.getString("transition-effect", "Fade")
        ?.takeIf { it in listOf("None", "Fade", "Slide") } ?: "Fade")
        private set
    fun chooseTransitionEffect(value: String) {
        if (value !in listOf("None", "Fade", "Slide")) return
        transitionEffect = value
        preferences.edit().putString("transition-effect", value).apply()
    }
    fun chooseMotionPreset(value: String) {
        if (value !in listOf("Reduced", "Standard", "Fluid")) return
        chooseMotion(value)
        chooseMotionIntensity(if (value == "Reduced") 0 else 100)
        chooseMotionSpeed(100)
        chooseSpringIntensity(if (value == "Fluid") 60 else 0)
    }
    // Phone editor/console split is opt-in; preferences survive process death.
    var phoneSplitEnabled by mutableStateOf(preferences.getBoolean("phone-split", false))
        private set
    fun choosePhoneSplitEnabled(enabled: Boolean) {
        phoneSplitEnabled = enabled
        preferences.edit().putBoolean("phone-split", enabled).apply()
        rememberProjectLayout()
    }
    var phoneSplitPercent by mutableIntStateOf(
        clampPhoneSplit(preferences.getInt("phone-split-percent", 60)))
        private set
    fun choosePhoneSplitPercent(percent: Int) {
        phoneSplitPercent = clampPhoneSplit(percent)
        preferences.edit().putInt("phone-split-percent", phoneSplitPercent).apply()
        rememberProjectLayout()
    }

    var panelSplitPercent by mutableIntStateOf(preferences.getInt("panel-split", 60))
        private set
    fun setPanelSplit(value: Int) {
        panelSplitPercent = value.coerceIn(35, 80)
        preferences.edit().putInt("panel-split", panelSplitPercent).apply()
        rememberProjectLayout()
    }
    var motionIntensity by mutableIntStateOf(preferences.getInt("motion-intensity", 100))
        private set
    fun chooseMotionIntensity(value: Int) {
        motionIntensity = value.coerceIn(0, 100)
        preferences.edit().putInt("motion-intensity", motionIntensity).apply()
    }
    var motionSpeed by mutableIntStateOf(preferences.getInt("motion-speed", 100).coerceIn(50, 200))
        private set
    fun chooseMotionSpeed(value: Int) {
        motionSpeed = value.coerceIn(50, 200)
        preferences.edit().putInt("motion-speed", motionSpeed).apply()
    }
    var springIntensity by mutableIntStateOf(preferences.getInt("spring-intensity", 35).coerceIn(0, 100))
        private set
    fun chooseSpringIntensity(value: Int) {
        springIntensity = value.coerceIn(0, 100)
        preferences.edit().putInt("spring-intensity", springIntensity).apply()
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
        rememberProjectLayout()
    }
    var accent by mutableStateOf(
        preferences.getString("accent:" + visualStyle,
            preferences.getString("accent", AppearanceRecipes.defaultAccent(visualStyle)))
            ?.takeIf(AppearanceRecipes::validAccent) ?: AppearanceRecipes.defaultAccent(visualStyle)
    )
        private set
    internal var studioAppearance by mutableStateOf(
        StudioAppearance.decode(preferences.getString("studio:$visualStyle", null)))
        private set
    internal fun updateStudioAppearance(value: StudioAppearance) {
        studioAppearance = value
        preferences.edit().putString("studio:$visualStyle", value.encode()).apply()
    }
    internal fun importAppearanceRecipe(raw: String): Boolean {
        if (raw.length > 65_536) return false
        val imported = AppearanceRecipes.decode(raw).firstOrNull() ?: return false
        val copy = imported.copy(id = java.util.UUID.randomUUID().toString())
        savedAppearanceRecipes = savedAppearanceRecipes + copy
        preferences.edit().putString("saved-appearance-recipes",
            AppearanceRecipes.encode(savedAppearanceRecipes)).apply()
        return true
    }
    internal var savedAppearanceRecipes by mutableStateOf(
        AppearanceRecipes.decode(preferences.getString("saved-appearance-recipes", "[]")))
        private set

    fun saveAppearanceRecipe(name: String, editingId: String? = null): Boolean {
        val clean = name.trim()
        if (!AppearanceRecipes.validName(clean)) return false
        val old = savedAppearanceRecipes.firstOrNull { it.id == editingId }
        val recipe = AppearanceRecipe(old?.id ?: java.util.UUID.randomUUID().toString(),
            clean, visualStyle, appearance, accent, studioAppearance,
            ThemeMotionRecipe(motionPreference, motionIntensity, motionSpeed, springIntensity, transitionEffect))
        savedAppearanceRecipes = if (old == null) savedAppearanceRecipes + recipe
            else savedAppearanceRecipes.map { if (it.id == old.id) recipe else it }
        preferences.edit().putString("saved-appearance-recipes",
            AppearanceRecipes.encode(savedAppearanceRecipes)).apply()
        return true
    }

    fun useAppearanceRecipe(id: String) {
        val recipe = savedAppearanceRecipes.firstOrNull { it.id == id } ?: return
        chooseVisualStyle(recipe.style)
        setMode(recipe.mode)
        chooseAccent(recipe.accent)
        updateStudioAppearance(recipe.studio)
        recipe.motion?.let { motion ->
            // A saved theme may reduce motion, but must never enable motion after a user
            // has explicitly selected the app's Reduced accessibility setting.
            if (motionPreference != "Reduced") chooseMotion(motion.preference)
            chooseMotionIntensity(motion.intensity)
            chooseMotionSpeed(motion.speed)
            chooseSpringIntensity(motion.spring)
            chooseTransitionEffect(motion.transition)
        }
    }

    fun deleteAppearanceRecipe(id: String) {
        savedAppearanceRecipes = savedAppearanceRecipes.filterNot { it.id == id }
        preferences.edit().putString("saved-appearance-recipes",
            AppearanceRecipes.encode(savedAppearanceRecipes)).apply()
    }

    fun restoreAppearanceDefaults() {
        setMode(CodeLabVisualStyles.defaultMode(visualStyle))
        chooseAccent(AppearanceRecipes.defaultAccent(visualStyle))
        updateStudioAppearance(StudioAppearance())
    }
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

    private val gitCredentialPrefs = app.getSharedPreferences("git-remote", Context.MODE_PRIVATE)
    private val gitVault = CloudCredentialVault(
        gitCredentialPrefs, alias = "CodeLab.github-credential.v1",
        encryptedKey = "github-token-encrypted-v1",
        legacyKey = "unused-github-legacy-token"
    )
    var gitTokenConfigured by mutableStateOf(
        gitCredentialPrefs.contains("github-token-encrypted-v1"))
        private set
    var gitFeedback by mutableStateOf<String?>(null)
        private set
    var gitPullInProgress by mutableStateOf(false)
        private set
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
    fun gitStage(paths: List<String>) {
        val p = project ?: return
        flush()
        work {
            val git = GitWorkspace(store.projectDirectory(p))
            git.stage(paths)
            val snapshot = git.inspect()
            val apply: () -> Unit = { gitState = snapshot }
            apply
        }
    }
    fun gitUnstage(paths: List<String>) {
        val p = project ?: return
        work {
            val git = GitWorkspace(store.projectDirectory(p))
            git.unstage(paths)
            val snapshot = git.inspect()
            val apply: () -> Unit = { if (project == p) gitState = snapshot }
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
    fun gitSaveToken(token: String) {
        val value = token.trim()
        if (value.length > 512 || value.any { it == '\r' || it == '\n' }) {
            error = "Invalid GitHub credential."
            return
        }
        work {
            gitVault.save(value)
            val apply: () -> Unit = {
                gitTokenConfigured = value.isNotEmpty()
                gitFeedback = if (value.isEmpty()) "GitHub credential removed from this device."
                    else "GitHub credential encrypted on this device."
            }
            apply
        }
    }

    fun gitConfigureRemote(input: String) {
        val p = project ?: return
        work {
            val git = GitWorkspace(store.projectDirectory(p))
            git.configureGitHubRemote(input)
            val snapshot = git.inspect()
            val apply: () -> Unit = {
                if (project == p) {
                    gitState = snapshot
                    gitFeedback = "GitHub remote configured. Nothing was uploaded."
                }
            }
            apply
        }
    }

    fun gitFetch() {
        val p = project ?: return
        work {
            val git = GitWorkspace(store.projectDirectory(p))
            val result = git.fetchGitHub(gitVault.load())
            val snapshot = git.inspect()
            val apply: () -> Unit = {
                if (project == p) { gitState = snapshot; gitFeedback = result }
            }
            apply
        }
    }

    fun gitPullFastForward() {
        val p = project ?: return
        if (gitPullInProgress) return
        // Block source edits and workspace actions while JGit may write files.
        // A modal progress dialog in Studio owns focus until the I/O callback.
        gitPullInProgress = true
        flush()
        val nav = ++navigationVersion
        work(onFailure = { gitPullInProgress = false }) {
            val git = GitWorkspace(store.projectDirectory(p))
            val result = git.pullFastForwardGitHub(gitVault.load())
            val snapshot = git.inspect()
            val opened = persist(load(p, tabs, active, allowUnavailable = true))
            val apply: () -> Unit = {
                gitPullInProgress = false
                if (project == p && nav == navigationVersion) {
                    apply(opened)
                    gitState = snapshot
                    gitFeedback = result
                }
            }
            apply
        }
    }

    fun gitPush() {
        val p = project ?: return
        flush()
        work {
            val git = GitWorkspace(store.projectDirectory(p))
            val result = git.pushGitHub(gitVault.load())
            val snapshot = git.inspect()
            val apply: () -> Unit = {
                if (project == p) { gitState = snapshot; gitFeedback = result }
            }
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

    internal var projectCardDetails by mutableStateOf<Map<String, ProjectCardDetail>>(emptyMap())
        private set

    private var projectCardsGeneration = 0

    /** Collect metadata on I/O; ignore stale results after project navigation. */
    fun refreshProjectCardDetails() {
        val current = projects.toList()
        val generation = ++projectCardsGeneration
        io.execute {
            val cards = current.associateWith { name ->
                val metadata = runCatching { store.metadata(name) }.getOrNull()
                val count = runCatching { store.files(name).size }.getOrDefault(0)
                ProjectCardDetail(metadata?.template?.take(42) ?: "Local project",
                    count, metadata?.description?.take(80).orEmpty())
            }
            main.post {
                if (generation == projectCardsGeneration && projects == current)
                    projectCardDetails = cards
            }
        }
    }
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
            val opened = load(p, wanted, target,
                autoOpen = false, allowUnavailable = true)
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
    internal var transferLabel by mutableStateOf<String?>(null)
        private set
    internal var transferNotice by mutableStateOf<String?>(null)
        private set
    private var transferGeneration = 0
    internal fun clearTransferNotice() { transferNotice = null }

    private fun transfer(label: String, success: String, task: () -> (() -> Unit)) {
        val generation = ++transferGeneration
        transferLabel = label
        transferNotice = null
        work(onFailure = { if (generation == transferGeneration) transferLabel = null }) {
            val result = task()
            val completion: () -> Unit = {
                result()
                if (generation == transferGeneration) {
                    transferLabel = null
                    transferNotice = success
                }
            }
            completion
        }
    }
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
            loadProjectLayout(open.project)
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
    private fun work(onFailure: (() -> Unit)? = null, task: () -> (() -> Unit)) {
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
                    onFailure?.invoke()
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

    fun openProject(name: String, create: Boolean = false, template: String = "JavaScript", starter: String = StarterExamples.BEGINNER) {
        rememberProjectLayout()
        flush()
        val nav = ++navigationVersion
        pinnedTabs = preferences.getStringSet("pinned:$name", emptySet())?.toSet()
            ?: emptySet()
        project = name
        loadProjectLayout(name)
        files = emptyList()
        tabs = emptyList()
        active = null
        source = ""
        dirty = false
        revision++
        work(onFailure = {
            // Never strand the user in a nonexistent project when creation fails.
            if (nav == navigationVersion && project == name) home()
        }) {
            if (create) store.createProject(name, template, starter)
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
        transfer("Importing folder…", "Folder imported") {
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
        transfer("Importing repository…", "Repository imported") {
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
                if (replacement != null) {
                    val existing = preferences.getString("project-layout:$name", null)
                    if (existing != null) preferences.edit().putString("project-layout:$replacement", existing)
                        .remove("project-layout:$name").apply()
                }
            }
            commit
        }
    }

    fun home() {
        rememberProjectLayout()
        flush()
        val nav = ++navigationVersion
        runner.stop()
        project = null
        pinnedTabs = emptySet()
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
        if (!create && path !in files) {
            error = "File no longer exists: $path"
            return
        }
        if (create && path in files) {
            error = "That name is already in use."
            return
        }
        val previousTabs = tabs
        val previousDirty = dirty
        val previousPendingLine = pendingLine
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
        val selectingRevision = revision
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
                    // A failed optimistic switch must not leave an invalid tab or
                    // obscure the previous buffer. Preserve any newer user edits.
                    if (nav == navigationVersion && project == p &&
                        active == path && revision == selectingRevision) {
                        tabs = previousTabs
                        active = fromFile
                        source = fromText
                        dirty = previousDirty
                        pendingLine = previousPendingLine
                        revision++
                    }
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
        val remappedPins = remapPinnedPaths(pinnedTabs, path, replacement, directory = true)
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
                pinnedTabs = remappedPins.intersect(files.toSet())
                preferences.edit().putStringSet("pinned:$p", pinnedTabs).apply()
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
        val remappedPins = remapPinnedPaths(pinnedTabs, path, replacement)
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
                pinnedTabs = remappedPins.intersect(files.toSet())
                preferences.edit().putStringSet("pinned:$p", pinnedTabs).apply()
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
            val opened = load(p, wanted, target,
                autoOpen = false, allowUnavailable = true)
            persist(opened)
            val commit: () -> Unit = { apply(opened) }
            commit
        }
    }

    fun export(uri: Uri) {
        val p = project ?: return
        transfer("Exporting project…", "Project exported") {
            val resolver = getApplication<Application>().contentResolver
            requireNotNull(resolver.openOutputStream(uri, "wt")) { "Cannot open destination" }.use { store.exportZip(p, it) }
            val commit: () -> Unit = {}
            commit
        }
    }

    fun exportFile(uri: Uri) {
        val p = project ?: return
        val path = active ?: return
        transfer("Exporting file…", "File exported") {
            val resolver = getApplication<Application>().contentResolver
            requireNotNull(resolver.openOutputStream(uri, "wt")) { "Cannot open destination" }
                .use { store.exportSingleFile(p, path, it) }
            val commit: () -> Unit = {}
            commit
        }
    }

    fun importFile(uri: Uri, name: String) {
        val p = project ?: return
        transfer("Importing file…", "File imported") {
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
        transfer("Importing files…", "Files imported") {
            require(uris.size <= 100) { "Choose up to 100 files per import." }
            val resolver = getApplication<Application>().contentResolver
            val existing = store.files(p).toMutableSet()
            val names = mutableListOf<String>()
            uris.forEachIndexed { index, uri ->
                val display = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME),
                    null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                } ?: "import-${index + 1}.txt"
                val sanitized = display.substringAfterLast('/').substringAfterLast('\\')
                    .replace(Regex("[^A-Za-z0-9._ -]"), "_")
                    .trim().take(100).ifBlank { "import-${index + 1}.txt" }
                val stem = sanitized.substringBeforeLast('.', sanitized)
                val extension = if (sanitized.contains('.'))
                    "." + sanitized.substringAfterLast('.') else ""
                var name = sanitized
                var counter = 2
                while (name in existing) {
                    name = "${stem}-${counter++}${extension}"
                }
                existing.add(name)
                names.add(name)
            }
            store.importFilesAtomically(p, names) { index ->
                resolver.openInputStream(uris[index])
                    ?: throw java.io.IOException("Cannot open source file.")
            }
            val last = names.last()
            val opened = persist(load(p, tabs + names, last))
            val commit: () -> Unit = { if (project == p) { apply(opened); console = false } }
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
        val expectedFingerprint = replaceSummary?.fingerprint ?: run {
            error = "Preview changes before applying replacements."
            return
        }
        work {
            val summary = store.replaceAcrossProject(p, query, replacement, matchCase,
                regex, true, allowed, expectedFingerprint)
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
        transfer("Importing ZIP archive…", "Project imported") {
            val resolver = getApplication<Application>().contentResolver
            val p = requireNotNull(resolver.openInputStream(uri)) { "Cannot open archive" }.use { store.importZip(name, it) }
            val opened = persist(load(p, emptyList(), null, allowUnavailable = true))
            val names = store.projects()
            val commit: () -> Unit = { projects = names; apply(opened); console = false }
            commit
        }
    }

    fun setMode(mode: String) {
        if (mode !in listOf("Dark", "Light", "System")) return
        appearance = mode
        preferences.edit().putString("mode", mode).putString("mode:$visualStyle", mode).apply()
    }

    fun chooseAccent(value: String) {
        if (!AppearanceRecipes.validAccent(value)) return
        accent = value
        preferences.edit().putString("accent", value)
            .putString("accent:$visualStyle", value).apply()
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
        // Never switch from offline to cloud implicitly; the user chooses.
        if (executionMode == "Online") {
            if (cloudAvailable(filename)) prepareOnlineApproval(filename)
            else error = if (!isOnline) "Connect to the internet to run online."
                else if (cloudChecking) "Checking the online compiler. Retry shortly."
                else "No online compiler available for this file. Configure one in Languages & Cloud."
            return
        }
        if (filename.endsWith(".py", true) &&
            !PythonModuleLocator.installed(getApplication<Application>())) {
            error = PythonModuleLocator.check(getApplication<Application>()).description +
                ". Download Python inside the Languages screen."
            return
        }
        if (filename.endsWith(".ts", true) && !isLocalExecutable(filename)) {
            error = OptionalRuntimeLocator.check(getApplication<Application>(),
                OptionalRuntimeLocator.Pack.TYPESCRIPT).description +
                ". Download TypeScript inside Languages to run offline."
            return
        }
        if (isLocalExecutable(filename)) {
            flush()
            console = true
            preview = false
            runner.run(filename, source)
        } else {
            val capability = RunCapability.forPath(filename)
            error = capability.description + ". Offline execution requires an installed runtime; " +
                "choose Online beside Run if a remote compiler supports this file."
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

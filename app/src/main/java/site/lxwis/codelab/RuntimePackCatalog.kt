package site.lxwis.codelab

/**
 * A source-of-truth for execution labels. This is an implementation roadmap,
 * not a downloader: planned packs are NEVER represented as installed.
 *
 * Future optional engines must be delivered as signed Android packages with
 * dedicated process isolation, authenticated IPC, ABI checks and explicit consent.
 * Downloading an arbitrary executable into app-private writable storage is not a runtime.
 */
internal object RuntimePackCatalog {
    enum class State { BUNDLED, CORE_PLANNED, PACK_PLANNED, EDIT_ONLY }

    data class Capability(val state: State, val label: String, val detail: String)

    private val corePlanned = mapOf(
        "TypeScript" to "TypeScript compiler/transpiler plus a compatible local JavaScript engine."
    )
    private val packsPlanned = mapOf(
        "Python" to "Separately signed optional Python 3.13 APK",
        "C" to "C/C++ native toolchain",
        "C++" to "C/C++ native toolchain",
        "Kotlin" to "Java/Kotlin compiler pack",
        "PHP" to "PHP interpreter pack",
        "Ruby" to "Ruby interpreter pack",
        "Go" to "Go compiler pack",
        "Rust" to "Rust compiler pack",
        "C#" to "Compatible .NET execution pack"
    )

    fun capability(language: String): Capability {
        if (language == "Web") return Capability(
            State.BUNDLED, "Offline preview included", "HTML, CSS and JavaScript preview in Android WebView.")
        val extension = LanguageCatalog.extensionFor(language)
        val engine = extension?.let { OfflineRuntimeRegistry.manifest(it) }
        if (extension != null && engine != null && OfflineRuntimeRegistry.locallyAvailable(extension)) {
            val label = when (language) {
                "Java" -> "Offline beta · full Java compiler not included"
                "HTML", "CSS" -> "Offline preview included"
                else -> "Offline · " + engine.engine
            }
            return Capability(State.BUNDLED, label, engine.capabilities + ". " + engine.limitations)
        }
        corePlanned[language]?.let { return Capability(
            State.CORE_PLANNED, "Offline core upgrade planned · not installed", it) }
        packsPlanned[language]?.let { return Capability(
            State.PACK_PLANNED, "Optional engine pack planned · not installed", it) }
        return Capability(State.EDIT_ONLY, "Edit offline · no bundled runtime",
            "A compatible online execution service may be available with explicit approval.")
    }
}

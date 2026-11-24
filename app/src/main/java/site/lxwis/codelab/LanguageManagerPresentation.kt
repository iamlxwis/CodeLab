package site.lxwis.codelab

/**
 * Short status copy for the language browser. The status is a presentation of
 * verified local capabilities, never a promise about online or APK availability.
 */
internal object LanguageManagerPresentation {
    data class RowStatus(val label: String, val detail: String, val emphasis: Boolean = false)

    private val offlinePackOrder =
        listOf("Python", "JavaScript", "Lua", "Java", "TypeScript", "SQLite")

    fun priority(name: String): Int =
        offlinePackOrder.indexOf(name).let { if (it == -1) Int.MAX_VALUE else it }

    fun describe(
        downloadable: Boolean,
        moduleReady: Boolean,
        bundledReady: Boolean,
        onlineReady: Boolean,
        preview: Boolean,
        activeDownload: Boolean,
        moduleProblem: Boolean
    ): RowStatus = when {
        moduleProblem -> RowStatus("Check", "Module needs attention", true)
        activeDownload -> RowStatus("Downloading", "Open to view progress", true)
        moduleReady -> RowStatus("Installed", "Verified offline module", true)
        downloadable && bundledReady ->
            RowStatus("Download", "Built-in engine works offline; optional pack", true)
        downloadable -> RowStatus("Download", "Optional offline runtime", true)
        preview -> RowStatus("Preview", "Local HTML/CSS preview")
        bundledReady -> RowStatus("Ready", "Built-in offline engine", true)
        onlineReady -> RowStatus("Online", "Online execution with approval")
        else -> RowStatus("Edit only", "No offline execution engine")
    }
}

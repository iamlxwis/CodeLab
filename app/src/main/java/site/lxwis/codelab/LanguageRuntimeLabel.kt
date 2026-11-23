package site.lxwis.codelab

/**
 * Language Manager status must not assume all locally executable languages are
 * bundled in the editor. TypeScript and Python are optional companions.
 *
 * A missing engine is NEVER presented as installed without a verified module.
 */
internal object LanguageRuntimeLabel {
    fun describe(
        localInstalled: Boolean,
        engine: OfflineRuntimeRegistry.Runtime?,
        verifiedModuleLabel: String?,
        cloudReady: Boolean
    ): String? {
        val local = when {
            !verifiedModuleLabel.isNullOrBlank() -> "Offline · verified $verifiedModuleLabel module"
            localInstalled && engine != null -> "Offline · ${engine.engine} ${engine.version}"
            else -> return null
        }
        return local + if (cloudReady) " · Cloud ready" else ""
    }
}

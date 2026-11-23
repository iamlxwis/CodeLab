package site.lxwis.codelab

/** APK update policy. Android must never be asked to silently downgrade a module. */
internal object PythonModuleVersionPolicy {
    enum class Decision { NEW_INSTALL, UPDATE, REINSTALL, DOWNGRADE, INVALID }

    fun decide(downloaded: Long, installed: Long?): Decision {
        if (downloaded <= 0L || (installed != null && installed <= 0L)) return Decision.INVALID
        if (installed == null) return Decision.NEW_INSTALL
        return when {
            downloaded > installed -> Decision.UPDATE
            downloaded == installed -> Decision.REINSTALL
            else -> Decision.DOWNGRADE
        }
    }
}

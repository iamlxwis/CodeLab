package site.lxwis.codelab

/** Download and already-installed engine packages must both match CodeLab's signer. */
internal object PythonModuleSignaturePolicy {
    enum class State { TRUSTED, HOST_UNKNOWN, DOWNLOAD_UNTRUSTED, INSTALLED_UNTRUSTED }

    fun check(host: Set<String>, downloaded: Set<String>, installed: Set<String>?): State {
        if (host.isEmpty()) return State.HOST_UNKNOWN
        if (downloaded.isEmpty() || downloaded != host) return State.DOWNLOAD_UNTRUSTED
        if (installed != null && installed != host) return State.INSTALLED_UNTRUSTED
        return State.TRUSTED
    }
}

package site.lxwis.codelab

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * Only bind the exact Python companion APK signed by the same key as CodeLab.
 * The module owns a separate Android UID and is not allowed to access app files.
 */
internal object PythonModuleLocator {
    const val PACKAGE_NAME = "site.lxwis.codelab.engine.python"
    const val SERVICE_CLASS = "$PACKAGE_NAME.PythonRuntimeService"
    const val BIND_PERMISSION = "site.lxwis.codelab.permission.BIND_ENGINE"
    const val PROTOCOL_KEY = "site.lxwis.codelab.engine.PROTOCOL_VERSION"
    const val PROTOCOL_VERSION = 1
    const val DOWNLOAD_URL = "https://github.com/iamlxwis/CodeLab/releases/latest/download/CodeLab-Python.apk"

    enum class State { NOT_INSTALLED, UNTRUSTED, INCOMPATIBLE, READY }
    data class Status(val state: State, val description: String) {
        val ready: Boolean get() = state == State.READY
    }
    fun installed(context: Context): Boolean = check(context).ready
    fun component(context: Context): ComponentName? =
        if (installed(context)) ComponentName(PACKAGE_NAME, SERVICE_CLASS) else null

    fun check(context: Context): Status {
        val manager = context.packageManager
        try {
            @Suppress("DEPRECATION")
            manager.getPackageInfo(PACKAGE_NAME, 0)
        } catch (_: PackageManager.NameNotFoundException) {
            return Status(State.NOT_INSTALLED, "Python runtime module not installed")
        } catch (_: RuntimeException) {
            return Status(State.INCOMPATIBLE, "Unable to inspect Python module")
        }
        if (manager.checkSignatures(context.packageName, PACKAGE_NAME) != PackageManager.SIGNATURE_MATCH)
            return Status(State.UNTRUSTED, "Python runtime module has an untrusted signature")
        return try {
            val service = manager.getServiceInfo(
                ComponentName(PACKAGE_NAME, SERVICE_CLASS), PackageManager.GET_META_DATA)
            if (!service.enabled || !service.exported ||
                service.permission != BIND_PERMISSION ||
                service.metaData?.getInt(PROTOCOL_KEY, 0) != PROTOCOL_VERSION)
                Status(State.INCOMPATIBLE, "Python runtime module is incompatible")
            else Status(State.READY, "Python 3.13 module installed and verified")
        } catch (_: PackageManager.NameNotFoundException) {
            Status(State.INCOMPATIBLE, "Python runtime module service not found")
        } catch (_: RuntimeException) {
            Status(State.INCOMPATIBLE, "Cannot verify Python runtime module")
        }
    }
}

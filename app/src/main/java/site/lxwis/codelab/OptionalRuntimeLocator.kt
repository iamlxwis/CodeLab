package site.lxwis.codelab

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/** Explicit package allowlist: no arbitrary downloaded executables or service discovery. */
internal object OptionalRuntimeLocator {
    enum class Pack(val label: String, val suffix: String, val extension: String,
                    val serviceName: String) {
        JAVASCRIPT("JavaScript", "JavaScript", "js", "JsRuntimeService"),
        LUA("Lua", "Lua", "lua", "LuaRuntimeService"),
        JAVA("Java", "Java", "java", "JavaRuntimeService"),
        TYPESCRIPT("TypeScript", "TypeScript", "ts", "TsRuntimeService"),
        SQLITE("SQLite", "SQLite", "sql", "SqlRuntimeService");
        val packageName get() = "site.lxwis.codelab.engine." + extension
        val serviceClass get() = packageName + "." + serviceName
        val downloadUrl get() =
            "https://github.com/iamlxwis/CodeLab/releases/latest/download/CodeLab-" + suffix + ".apk"
    }
    fun check(context: Context, pack: Pack): PythonModuleLocator.Status {
        val pm = context.packageManager
        try {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(pack.packageName, 0)
        } catch (_: PackageManager.NameNotFoundException) {
            return PythonModuleLocator.Status(PythonModuleLocator.State.NOT_INSTALLED,
                pack.label + " optional runtime not installed" +
                    if (pack == Pack.TYPESCRIPT) "; install the module to run offline" else "; built-in engine remains available")
        } catch (_: RuntimeException) {
            return PythonModuleLocator.Status(PythonModuleLocator.State.INCOMPATIBLE,
                "Unable to inspect " + pack.label + " runtime")
        }
        if (pm.checkSignatures(context.packageName, pack.packageName) != PackageManager.SIGNATURE_MATCH)
            return PythonModuleLocator.Status(PythonModuleLocator.State.UNTRUSTED,
                "Untrusted " + pack.label + " runtime signature")
        return try {
            val info = pm.getServiceInfo(ComponentName(pack.packageName, pack.serviceClass),
                PackageManager.GET_META_DATA)
            if (!info.enabled || !info.exported ||
                info.permission != PythonModuleLocator.BIND_PERMISSION ||
                info.metaData?.getInt(PythonModuleLocator.PROTOCOL_KEY, 0) !=
                PythonModuleLocator.PROTOCOL_VERSION)
                PythonModuleLocator.Status(PythonModuleLocator.State.INCOMPATIBLE,
                    "Incompatible " + pack.label + " module service")
            else PythonModuleLocator.Status(PythonModuleLocator.State.READY,
                "Verified optional " + pack.label + " module installed")
        } catch (_: Exception) {
            PythonModuleLocator.Status(PythonModuleLocator.State.INCOMPATIBLE,
                "Unable to verify " + pack.label + " module service")
        }
    }
    fun component(context: Context, pack: Pack): ComponentName? =
        if (check(context, pack).ready) ComponentName(pack.packageName, pack.serviceClass)
        else null
}

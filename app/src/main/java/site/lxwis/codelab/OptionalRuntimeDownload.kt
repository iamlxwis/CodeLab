package site.lxwis.codelab

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File

/**
 * User-initiated download and Android-approved installation of the signed
 * Python runtime. The APK is never loaded as code in CodeLab's own process.
 * DownloadManager persists transfer state if the app closes or rotates.
 * This side-loading channel is for the GitHub-distributed APK; a Play build
 * needs store-managed feature delivery and policy review instead.
 */
internal class OptionalRuntimeDownload(private val app: Context, private val pack: OptionalRuntimeLocator.Pack) {
    enum class Stage { IDLE, DOWNLOADING, PAUSED, DOWNLOADED, FAILED }
    data class Transfer(val stage: Stage, val received: Long = 0,
        val total: Long = 0, val message: String = "") {
        val progress: Float get() = if (total > 0L)
            (received.toDouble() / total.toDouble()).toFloat().coerceIn(0f, 1f) else 0f
    }

    private val preferences = app.getSharedPreferences("runtime-downloads", Context.MODE_PRIVATE)
    private val manager = app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val apkName = "CodeLab-" + pack.suffix + ".apk"
    private val maxBytes = 120L * 1024L * 1024L
    private fun file() = File(
        requireNotNull(app.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)),
        apkName
    )

    fun lastJob(): Long = preferences.getLong(pack.extension + "-id", -1L)

    /** System DownloadManager persists and resumes transfers while CodeLab is closed. */
    fun wifiOnly(): Boolean = preferences.getBoolean(pack.extension + "-wifi-only", false)

    fun setWifiOnly(enabled: Boolean) {
        preferences.edit().putBoolean(pack.extension + "-wifi-only", enabled).apply()
    }

    /** Approximate installed APK bytes, NOT Android's full app-data/storage footprint. */
    @Suppress("DEPRECATION")
    fun installedApkBytes(): Long = runCatching {
        val installed = app.packageManager.getApplicationInfo(pack.packageName, 0)
        val base = File(installed.sourceDir).takeIf { it.isFile }?.length() ?: 0L
        val splits = installed.splitSourceDirs?.sumOf { path ->
            File(path).takeIf { it.isFile }?.length() ?: 0L
        } ?: 0L
        base + splits
    }.getOrDefault(0L)

    /** Version codes are used for update decisions; installed packages are checked for trust separately. */
    @Suppress("DEPRECATION")
    fun installedVersionCode(): Long? = runCatching {
        version(app.packageManager.getPackageInfo(pack.packageName, 0))
    }.getOrNull()

    /** Cached installer is NOT the installed runtime; never touch user projects. */
    fun cachedBytes(): Long = runCatching {
        file().takeIf { it.isFile }?.length() ?: 0L
    }.getOrDefault(0L)

    /** Explicit user action; deletes only the installer and cancels its system download. */
    fun clearCachedInstaller(): Boolean {
        val id = lastJob()
        if (id > 0L) manager.remove(id)
        preferences.edit().remove(pack.extension + "-id").apply()
        return runCatching { !file().exists() || file().delete() }.getOrDefault(false)
    }

    @Suppress("DEPRECATION")
    private fun version(info: PackageInfo): Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode
        else info.versionCode.toLong()

    /** State is a display hint; verify() still checks package name and real signer. */
    fun downloadedVersionDecision(): PythonModuleVersionPolicy.Decision? = runCatching {
        val archive = file()
        if (!archive.isFile) return@runCatching null
        val pm = app.packageManager
        val candidate = pm.getPackageArchiveInfo(archive.absolutePath, 0)
            ?: return@runCatching null
        if (candidate.packageName != pack.packageName) return@runCatching null
        val installed = try {
            version(pm.getPackageInfo(pack.packageName, 0))
        } catch (_: PackageManager.NameNotFoundException) { null }
        PythonModuleVersionPolicy.decide(version(candidate), installed)
    }.getOrNull()

    fun begin(): Long {
        val previous = lastJob()
        if (previous > 0L) manager.remove(previous)
        val file = file()
        if (file.exists() && !file.delete()) error("Cannot replace prior download")
        val request = DownloadManager.Request(Uri.parse(pack.downloadUrl))
            .setTitle("CodeLab " + pack.label)
            .setDescription("Optional offline " + pack.label + " runtime module")
            .setMimeType("application/vnd.android.package-archive")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(!wifiOnly())
            .setDestinationInExternalFilesDir(app, Environment.DIRECTORY_DOWNLOADS, apkName)
        val id = manager.enqueue(request)
        preferences.edit().putLong(pack.extension + "-id", id).apply()
        return id
    }

    fun inspect(id: Long = lastJob()): Transfer {
        if (id <= 0L) return Transfer(Stage.IDLE)
        return try {
            manager.query(DownloadManager.Query().setFilterById(id))?.use { cursor ->
                if (!cursor.moveToFirst()) return Transfer(Stage.IDLE, message = "Download no longer exists")
                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val received = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                if (received > maxBytes || total > maxBytes) {
                    manager.remove(id)
                    return Transfer(Stage.FAILED, received, total,
                        "Module exceeds the 120 MB download safety limit")
                }
                if (status == DownloadManager.STATUS_SUCCESSFUL &&
                    (!file().isFile || file().length() <= 0L)) {
                    return Transfer(Stage.FAILED, received, total,
                        "Completed download is missing. Download Python again.")
                }
                val stage = when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> Stage.DOWNLOADED
                    DownloadManager.STATUS_FAILED -> Stage.FAILED
                    DownloadManager.STATUS_PAUSED -> Stage.PAUSED
                    DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PENDING -> Stage.DOWNLOADING
                    else -> Stage.IDLE
                }
                Transfer(stage, received, total,
                    when (stage) {
                        Stage.FAILED -> "Download failed (code $reason). Check network and storage."
                        Stage.PAUSED -> "Waiting for network or system download service"
                        else -> ""
                    })
            } ?: Transfer(Stage.IDLE)
        } catch (e: RuntimeException) {
            Transfer(Stage.FAILED, message = "Cannot inspect download: " +
                e.javaClass.simpleName)
        }
    }

    fun cancel() { clearCachedInstaller() }

    /**
     * Verifies exact package ID and APK signing certificate against the
     * installed editor. This blocks tampered or differently signed binaries.
     */
    @Suppress("DEPRECATION")
    fun verify(): String? {
        val archive = runCatching { file() }.getOrNull()
            ?: return "Download storage unavailable"
        if (!archive.isFile || archive.length() <= 0) return "Downloaded module is missing"
        if (archive.length() > maxBytes) return "Module exceeds safe download size"
        val pm = app.packageManager
        return try {
            val flags = if (Build.VERSION.SDK_INT >= 28)
                PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
            val packageInfo = pm.getPackageArchiveInfo(archive.absolutePath, flags)
                ?: return "Invalid Android module package"
            if (packageInfo.packageName != pack.packageName)
                return "Unexpected package identity; installation blocked"
            val host = pm.getPackageInfo(app.packageName, flags)
            fun certs(info: PackageInfo): Set<String> {
                val signatures = if (Build.VERSION.SDK_INT >= 28)
                    info.signingInfo?.apkContentsSigners else info.signatures
                return signatures?.map { it.toCharsString() }?.toSet().orEmpty()
            }
            val existingPack = try {
                pm.getPackageInfo(pack.packageName, flags)
            } catch (_: PackageManager.NameNotFoundException) { null }
            when (PythonModuleSignaturePolicy.check(
                certs(host), certs(packageInfo), existingPack?.let { certs(it) })) {
                PythonModuleSignaturePolicy.State.HOST_UNKNOWN ->
                    "Cannot verify CodeLab's signing key. Installation blocked."
                PythonModuleSignaturePolicy.State.DOWNLOAD_UNTRUSTED ->
                    "Module signature differs from CodeLab. Install both from the same release."
                PythonModuleSignaturePolicy.State.INSTALLED_UNTRUSTED ->
                    "Installed " + pack.label + " uses a different signing key. Remove the old module before installing the trusted download."
                PythonModuleSignaturePolicy.State.TRUSTED -> {
                    when (PythonModuleVersionPolicy.decide(
                        version(packageInfo), existingPack?.let { version(it) })) {
                        PythonModuleVersionPolicy.Decision.INVALID ->
                            pack.label + " module has an invalid version. Download it again."
                        PythonModuleVersionPolicy.Decision.DOWNGRADE ->
                            "Downloaded " + pack.label + " is older than the installed runtime. Downgrades are blocked."
                        else -> null
                    }
                }
            }
        } catch (e: Exception) {
            "Could not verify module: " + e.javaClass.simpleName
        }
    }

    /**
     * The app handles download/verification. Android owns the final install
     * confirmation. There is no silent sideload or arbitrary code extraction.
     */
    fun install(): String {
        verify()?.let { return it }
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                !app.packageManager.canRequestPackageInstalls()) {
                app.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${app.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                "Allow this installation source in Android settings, then tap Install."
            } else {
                val uri = FileProvider.getUriForFile(app,
                    "${app.packageName}.runtimefiles", file())
                app.startActivity(Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "application/vnd.android.package-archive")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK))
                "Confirm " + pack.label + " installation in the Android system dialog."
            }
        } catch (e: Exception) {
            "Cannot launch Android installer: " + e.javaClass.simpleName
        }
    }

    fun uninstall(): String = try {
        app.startActivity(Intent(Intent.ACTION_DELETE,
            Uri.parse("package:${pack.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        "Confirm removal in the Android system dialog."
    } catch (e: Exception) {
        "Cannot open uninstaller: " + e.javaClass.simpleName
    }
}

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal data class AppUpdate(
    val tag: String,
    val title: String,
    val versionCode: Long,
    val notes: String,
    val apkBytes: Long,
    val sha256: String,
    val signerSha256: String?
) {
    val url: String get() = "$officialReleaseDownload/$tag/CodeLab.apk"
}

private const val officialReleaseDownload = "https://github.com/iamlxwis/CodeLab/releases/download"
private const val maxUpdateBytes = 80L * 1024 * 1024
private val validReleaseTag = Regex("[A-Za-z0-9][A-Za-z0-9._-]{0,79}")
private val sha256Pattern = Regex("[0-9a-fA-F]{64}")

internal object OfficialAppReleases {
    private const val latestRelease = "https://api.github.com/repos/iamlxwis/CodeLab/releases/latest"
    private val assetHosts = setOf("github.com", "release-assets.githubusercontent.com", "objects.githubusercontent.com")

    fun fetch(): AppUpdate = parse(read(latestRelease, 256_000, setOf("api.github.com"))) { url, limit ->
        read(url, limit, assetHosts)
    }

    fun parse(json: String, readAsset: (String, Int) -> String): AppUpdate {
        val metadata = JSONObject(json)
        if (metadata.optBoolean("draft") || metadata.optBoolean("prerelease")) throw IOException("Official release is unavailable")
        val tag = metadata.optString("tag_name")
        if (!validReleaseTag.matches(tag)) throw IOException("Unexpected release tag")
        val assets = metadata.optJSONArray("assets") ?: throw IOException("No release assets")
        fun asset(name: String, maxSize: Long): JSONObject {
            for (i in 0 until assets.length()) {
                val item = assets.optJSONObject(i) ?: continue
                if (item.optString("name") == name && item.optString("state") == "uploaded" &&
                    item.optLong("size") in 1..maxSize &&
                    item.optString("browser_download_url") == "$officialReleaseDownload/$tag/$name") return item
            }
            throw IOException("Missing official release asset $name")
        }
        val apk = asset("CodeLab.apk", maxUpdateBytes)
        val buildInfo = asset("CodeLab-build-info.txt", 8192)
        val info = readAsset(buildInfo.getString("browser_download_url"), 8192)
        val version = info.lineSequence().firstNotNullOfOrNull { line ->
            line.takeIf { it.startsWith("Version code: ") }?.substringAfter(": ")?.trim()?.toLongOrNull()
        }?.takeIf { it > 0 } ?: throw IOException("Release version is missing")
        val reportedDigest = apk.optString("digest")
            .takeIf { it.startsWith("sha256:") }?.substringAfter(':')
            ?.takeIf(sha256Pattern::matches)
        val digest = reportedDigest ?: run {
            val checksum = asset("CodeLab.apk.sha256", 8192)
            val sums = readAsset(checksum.getString("browser_download_url"), 8192)
            sums.lineSequence().map { it.trim().split(Regex("\\s+"), limit = 2) }.firstNotNullOfOrNull { parts ->
                parts.firstOrNull()?.takeIf { parts.getOrNull(1)?.removePrefix("*") == "CodeLab.apk" && sha256Pattern.matches(it) }
            } ?: throw IOException("The official APK checksum is missing")
        }
        val signer = info.lineSequence().firstNotNullOfOrNull { line ->
            line.takeIf { it.startsWith("Signer SHA-256: ") }?.substringAfter(": ")?.trim()?.takeIf(sha256Pattern::matches)
        }
        return AppUpdate(tag, metadata.optString("name").ifBlank { "CodeLab $tag" },
            version, metadata.optString("body").take(24_000), apk.getLong("size"), digest.lowercase(), signer?.lowercase())
    }

    private fun read(start: String, maxBytes: Int, hosts: Set<String>): String {
        var next = URL(start)
        repeat(5) {
            if (next.protocol != "https" || next.host !in hosts || next.userInfo != null)
                throw IOException("Release redirected outside GitHub")
            val connection = next.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty("User-Agent", "CodeLab-Android")
            if (next.host == "api.github.com") connection.setRequestProperty("Accept", "application/vnd.github+json")
            try {
                val status = connection.responseCode
                if (status in setOf(301, 302, 303, 307, 308)) {
                    val location = connection.getHeaderField("Location") ?: throw IOException("Invalid release redirect")
                    next = URL(next, location)
                } else {
                    if (status != 200) throw IOException("GitHub returned HTTP $status")
                    return connection.inputStream.use { stream ->
                        val buffer = ByteArray(4096)
                        val out = java.io.ByteArrayOutputStream()
                        while (true) {
                            val count = stream.read(buffer)
                            if (count == -1) break
                            if (out.size() + count > maxBytes) throw IOException("Release response is too large")
                            out.write(buffer, 0, count)
                        }
                        out.toString("UTF-8")
                    }
                }
            } finally {
                connection.disconnect()
            }
        }
        throw IOException("Too many release redirects")
    }
}

internal class AppUpdater(private val context: Context) {
    enum class Stage { Idle, Queued, Downloading, Paused, Downloaded, Failed }
    data class Transfer(
        val stage: Stage = Stage.Idle,
        val received: Long = 0,
        val total: Long = 0,
        val message: String = ""
    ) {
        val active: Boolean get() = stage in setOf(Stage.Queued, Stage.Downloading, Stage.Paused)
    }

    private val preferences = context.getSharedPreferences("app-updater", Context.MODE_PRIVATE)
    private val manager = context.getSystemService(DownloadManager::class.java)
    private val apkName = "CodeLab-update.apk"

    var latest by mutableStateOf<AppUpdate?>(null)
        private set
    var lastCheck by mutableStateOf(0L)
        private set
    var checking by mutableStateOf(false)
        private set
    var checkError by mutableStateOf<String?>(null)
        private set
    private val installed = installedPackage(0)
    val installedVersion: Long = version(installed)
    val installedVersionName: String = installed.versionName.orEmpty()
    val available: AppUpdate? get() = latest?.takeIf { it.versionCode > installedVersion }
    val compatibilityIssue: String?
        get() = available?.signerSha256?.let { official ->
            if (official in installedSignerDigests) null
            else "The official release uses a different signing certificate from this installation."
        }

    @Suppress("DEPRECATION")
    private val installedSignerDigests by lazy {
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES
            else PackageManager.GET_SIGNATURES
        signatures(installedPackage(flags)).map { signature ->
            MessageDigest.getInstance("SHA-256").digest(signature.toByteArray()).toHex()
        }.toSet()
    }

    suspend fun check(force: Boolean = false) {
        if (checking || (!force && lastCheck != 0L && System.currentTimeMillis() - lastCheck < 3_600_000)) return
        checking = true
        checkError = null
        try {
            latest = withContext(Dispatchers.IO) { OfficialAppReleases.fetch() }
            lastCheck = System.currentTimeMillis()
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (failure: IOException) {
            checkError = failure.localizedMessage ?: "Could not check for updates"
        } catch (_: Exception) {
            checkError = "Could not read the official release"
        } finally {
            checking = false
        }
    }

    fun wifiOnly(): Boolean = preferences.getBoolean("wifi-only", false)
    fun setWifiOnly(value: Boolean) {
        preferences.edit().putBoolean("wifi-only", value).apply()
    }

    private fun job(): Long = preferences.getLong("job", -1L)
    private fun file(): File = File(requireNotNull(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)), apkName)
    fun cacheBytes(): Long = runCatching { file().length() }.getOrDefault(0L)

    @Synchronized fun begin(update: AppUpdate) {
        require(update == available && sha256Pattern.matches(update.sha256) &&
            validReleaseTag.matches(update.tag) && update.apkBytes in 1..maxUpdateBytes)
        if (compatibilityIssue != null) throw IOException(compatibilityIssue)
        if (inspect().active) return
        clear()
        val request = DownloadManager.Request(Uri.parse(update.url))
            .setTitle("CodeLab update")
            .setDescription("Official CodeLab APK")
            .setMimeType("application/vnd.android.package-archive")
            .setAllowedOverMetered(!wifiOnly())
            .apply { if (wifiOnly()) setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI) }
            .setAllowedOverRoaming(!wifiOnly())
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, apkName)
        val id = manager.enqueue(request)
        if (!preferences.edit().putLong("job", id).putLong("bytes", update.apkBytes)
                .putLong("version", update.versionCode).putString("sha256", update.sha256).commit()) {
            manager.remove(id)
            throw IOException("Could not save the download")
        }
    }

    @Synchronized fun clear() {
        val id = job()
        if (id > 0) manager.remove(id)
        val apk = file()
        if (apk.exists() && !apk.delete()) throw IOException("Cannot remove the cached update")
        preferences.edit().remove("job").remove("bytes").remove("version").remove("sha256").commit()
    }

    fun inspect(): Transfer {
        val id = job()
        if (id <= 0) return Transfer()
        return try {
            manager.query(DownloadManager.Query().setFilterById(id))?.use { cursor ->
                if (!cursor.moveToFirst()) return Transfer(Stage.Failed, message = "Download is unavailable. Start it again.")
                fun field(name: String) = cursor.getLong(cursor.getColumnIndexOrThrow(name))
                val received = field(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val total = field(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val expected = preferences.getLong("bytes", 0)
                if (received > expected || total > expected || expected !in 1..maxUpdateBytes) {
                    manager.remove(id)
                    return Transfer(Stage.Failed, received, expected, "APK size did not match the official release.")
                }
                val status = field(DownloadManager.COLUMN_STATUS).toInt()
                val reason = field(DownloadManager.COLUMN_REASON).toInt()
                when (status) {
                    DownloadManager.STATUS_PENDING -> Transfer(Stage.Queued, received, expected, "Queued")
                    DownloadManager.STATUS_RUNNING -> Transfer(Stage.Downloading, received, expected, "Downloading")
                    DownloadManager.STATUS_PAUSED -> Transfer(Stage.Paused, received, expected,
                        when (reason) {
                            DownloadManager.PAUSED_QUEUED_FOR_WIFI -> "Waiting for Wi-Fi"
                            DownloadManager.PAUSED_WAITING_FOR_NETWORK -> "Waiting for a connection"
                            DownloadManager.PAUSED_WAITING_TO_RETRY -> "Waiting to retry"
                            else -> "Paused"
                        })
                    DownloadManager.STATUS_SUCCESSFUL -> if (file().length() == expected)
                        Transfer(Stage.Downloaded, received, expected, "APK downloaded")
                    else Transfer(Stage.Failed, received, expected, "Completed APK is missing or incomplete.")
                    else -> Transfer(Stage.Failed, received, expected,
                        if (reason == DownloadManager.ERROR_INSUFFICIENT_SPACE) "Free some storage and retry."
                        else "Download failed. Check the connection and retry.")
                }
            } ?: Transfer(Stage.Failed, message = "Could not read the download")
        } catch (_: Exception) {
            Transfer(Stage.Failed, message = "Could not read the download")
        }
    }

    @Suppress("DEPRECATION")
    fun verify(): String? = try {
        val apk = file()
        val expected = preferences.getString("sha256", "").orEmpty()
        val wantedVersion = preferences.getLong("version", 0)
        when {
            inspect().stage != Stage.Downloaded -> "The APK download is incomplete."
            !sha256Pattern.matches(expected) || checksum(apk) != expected -> "APK checksum verification failed."
            else -> {
                val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES
                    else PackageManager.GET_SIGNATURES
                val candidate = context.packageManager.getPackageArchiveInfo(apk.absolutePath, flags)
                val installed = installedPackage(flags)
                when {
                    candidate == null || candidate.packageName != context.packageName -> "The APK is not CodeLab."
                    version(candidate) != wantedVersion || version(candidate) <= version(installed) -> "This APK is not a newer release."
                    (candidate.applicationInfo?.minSdkVersion ?: Int.MAX_VALUE) > Build.VERSION.SDK_INT -> "This release requires a newer Android version."
                    signers(candidate).isEmpty() || signers(candidate) != signers(installed) ->
                        "The official APK uses a different signing certificate from this installation."
                    else -> null
                }
            }
        }
    } catch (_: Exception) {
        "Could not verify the downloaded APK."
    }

    fun installerIntent(): Intent = if (!context.packageManager.canRequestPackageInstalls()) {
        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
    } else {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.runtimefiles", file())
        Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    @Suppress("DEPRECATION")
    private fun installedPackage(flags: Int): PackageInfo = context.packageManager.getPackageInfo(context.packageName, flags)

    @Suppress("DEPRECATION")
    private fun version(info: PackageInfo): Long = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode
        else info.versionCode.toLong()

    @Suppress("DEPRECATION")
    private fun signatures(info: PackageInfo): Array<out android.content.pm.Signature> =
        (if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures).orEmpty()

    private fun signers(info: PackageInfo): Set<String> = signatures(info).map { it.toCharsString() }.toSet()

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun checksum(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(32_768)
            while (true) {
                val size = input.read(buffer)
                if (size < 0) break
                digest.update(buffer, 0, size)
            }
        }
        return digest.digest().toHex()
    }
}

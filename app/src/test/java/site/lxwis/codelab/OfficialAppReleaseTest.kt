package site.lxwis.codelab

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class OfficialAppReleaseTest {
    private val digest = "a".repeat(64)
    private val signer = "b".repeat(64)
    private val project = "https://github.com/iamlxwis/CodeLab/releases/download/latest"

    @Test fun readsCanonicalApkVersionAndOfficialFileChecksum() {
        val release = OfficialAppReleases.parse(metadata()) { url, limit ->
            assertEquals(8192, limit)
            when (url) {
                "$project/CodeLab-build-info.txt" -> "Version code: 1337\nSigner SHA-256: $signer"
                "$project/CodeLab.apk.sha256" -> "$digest  CodeLab.apk\n${"c".repeat(64)}  CodeLab-Python.apk"
                else -> error("Unexpected asset URL")
            }
        }
        assertEquals(1337L, release.versionCode)
        assertEquals("$project/CodeLab.apk", release.url)
        assertEquals(digest, release.sha256)
        assertEquals(signer, release.signerSha256)
    }

    @Test fun rejectsAnApkLinkToAnUnofficialPlaceholder() {
        try {
            OfficialAppReleases.parse(metadata("https://example.org/CodeLab.apk")) { _, _ ->
                error("No additional metadata should be read")
            }
            fail("Accepted a non-official asset")
        } catch (_: IOException) { }
    }

    private fun metadata(url: String = "$project/CodeLab.apk") = """
        {"tag_name":"latest","assets":[
          {"name":"CodeLab.apk","size":3593402,"state":"uploaded","browser_download_url":"$url"},
          {"name":"CodeLab-build-info.txt","size":245,"state":"uploaded","browser_download_url":"$project/CodeLab-build-info.txt"},
          {"name":"CodeLab.apk.sha256","size":591,"state":"uploaded","browser_download_url":"$project/CodeLab.apk.sha256"}
        ]}
    """.trimIndent()
}

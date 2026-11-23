package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class RuntimeReleaseAssetsTest {
    @Test fun onlyPublishedTrustedNamesInSizeRangeAreAvailable() {
        val release = """{"assets":[
            {"name":"CodeLab-JavaScript.apk","state":"uploaded","size":4096},
            {"name":"CodeLab-Lua.apk","state":"uploaded","size":8192},
            {"name":"CodeLab-TypeScript.apk","state":"starter","size":500},
            {"name":"CodeLab-Java.apk","state":"uploaded","size":0},
            {"name":"CodeLab-SQLite.apk","state":"uploaded","size":999999999},
            {"name":"CodeLab-Malicious.apk","state":"uploaded","size":15000}
        ]}"""
        assertEquals(setOf("CodeLab-JavaScript.apk", "CodeLab-Lua.apk"),
            RuntimeReleaseAssets.parseAssetNames(release))
    }

    @Test fun missingOrEmptyReleaseAssetsDisableDownloads() {
        assertTrue(RuntimeReleaseAssets.parseAssetNames("{}").isEmpty())
        assertTrue(RuntimeReleaseAssets.parseAssetNames("""{"assets":[]}""").isEmpty())
    }
}

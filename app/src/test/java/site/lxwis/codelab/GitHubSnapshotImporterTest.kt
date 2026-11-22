package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipInputStream
import java.io.ByteArrayInputStream

class GitHubSnapshotImporterTest {
    @Test fun rejectsArbitraryHostsAndPaths() {
        assertEquals("owner/repo", GitHubSnapshotImporter.canonicalSlug(
            "https://github.com/owner/repo.git"))
        for (bad in listOf("https://evil.test/a/b", "../repo", "owner/repo/path",
            "owner/../other", "owner", "a:password@github.com/repo")) {
            try {
                GitHubSnapshotImporter.canonicalSlug(bad)
                fail("Expected rejection: " + bad)
            } catch (_: IllegalArgumentException) {}
        }
    }

    @Test fun removesGitHubPrefixFromZip() {
        val buffer = ByteArrayOutputStream()
        ZipOutputStream(buffer).use { out ->
            out.putNextEntry(ZipEntry("owner-repo-abc/src/hello.js"))
            out.write("print(42)".toByteArray())
            out.closeEntry()
        }
        val rewritten = GitHubSnapshotImporter.normalizeArchive(buffer.toByteArray())
        ZipInputStream(ByteArrayInputStream(rewritten)).use {
            val entry = it.nextEntry!!
            assertEquals("src/hello.js", entry.name)
            assertEquals("print(42)", it.readBytes().toString(Charsets.UTF_8))
        }
    }
}

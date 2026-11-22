package site.lxwis.codelab

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/** Exercises real JGit bytecode and filesystem APIs on an Android emulator. */
@RunWith(AndroidJUnit4::class)
class GitWorkspaceAndroidTest {
    @Test fun canCommitAndReadLocalHistoryOnAndroid() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File(ctx.cacheDir, "git-test-" + UUID.randomUUID().toString())
        assertTrue(folder.mkdirs())
        try {
            val repo = GitWorkspace(folder)
            repo.init()
            File(folder, "main.js").writeText("print(42)\n")
            repo.stage(listOf("main.js"))
            val id = repo.commit("Initial source", "Test User", "test@example.com")
            assertTrue(id.isNotBlank())
            assertTrue(repo.inspect().changes.isEmpty())
            assertEquals("Initial source", repo.inspect().commits.first().message)
        } finally {
            folder.deleteRecursively()
        }
    }
}

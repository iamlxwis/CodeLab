package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GitWorkspaceTest {
    @Test fun remoteAddressRejectsOffHostAndEmbeddedTokens() {
        assertEquals("https://github.com/openai/hello.git",
            GitHubRemoteAddress.canonical("openai/hello"))
        assertEquals("https://github.com/openai/hello.git",
            GitHubRemoteAddress.canonical("https://github.com/openai/hello.git"))
        for (bad in listOf("http://github.com/openai/hello",
            "https://evil.example.com/openai/hello",
            "https://user:token@github.com/openai/hello",
            "https://github.com/openai/hello?token=123",
            "file:///tmp/repo", "../../secrets")) {
            assertThrows(IllegalArgumentException::class.java) {
                GitHubRemoteAddress.canonical(bad)
            }
        }
    }

    @Test fun configuringRemoteNeverContactsNetworkOrStagesFiles() {
        val folder = temporary.newFolder("remote")
        val git = GitWorkspace(folder)
        git.init()
        folder.resolve("draft.js").writeText("uncommitted")
        git.configureGitHubRemote("openai/hello")
        val status = git.inspect()
        assertEquals("https://github.com/openai/hello.git", status.remoteUrl)
        assertTrue(status.changes.any { it.path == "draft.js" })
        assertTrue(status.staged.isEmpty())
        assertThrows(IllegalArgumentException::class.java) {
            git.pushGitHub("")
        }
        val config = folder.resolve(".git/config")
        config.writeText(config.readText().replace("https://github.com/openai/hello.git",
            "https://evil.example.com/not-allowed.git"))
        val now = git.inspect()
        assertTrue(now.unsafeRemote)
        assertNull(now.remoteUrl)
        assertThrows(IllegalArgumentException::class.java) {
            git.fetchGitHub("")
        }
    }

    @Test fun configuredTrackingIsReportedWithoutNetworkAccess() {
        val folder = temporary.newFolder("tracking")
        val git = GitWorkspace(folder)
        git.init()
        git.configureGitHubRemote("openai/example")
        val branch = git.inspect().branch
        org.eclipse.jgit.api.Git.open(folder).use { handle ->
            val config = handle.repository.config
            config.setString("branch", branch, "remote", "origin")
            config.setString("branch", branch, "merge", "refs/heads/$branch")
            config.save()
        }
        assertEquals("origin/$branch", git.inspect().trackingBranch)
    }

    @Test fun onlySelectedPathsAreCommitted() {
        val folder = temporary.newFolder("selective")
        val git = GitWorkspace(folder)
        git.init()
        folder.resolve("a.js").writeText("A")
        folder.resolve("b.js").writeText("B")
        assertThrows(IllegalArgumentException::class.java) { git.stage(listOf("../outside")) }
        git.stage(listOf("a.js"))
        git.commit("Only A", "Test", "test@example.com")
        assertTrue(git.inspect().changes.any { it.path == "b.js" && it.kind == "Untracked" })
        assertTrue(git.inspect().staged.isEmpty())
    }

    @Test fun unstagingOnUnbornBranchPreservesWorkingFilesAndOtherStagedPaths() {
        val folder = temporary.newFolder("unstage-initial")
        val git = GitWorkspace(folder)
        git.init()
        folder.resolve("one.js").writeText("one")
        folder.resolve("two.js").writeText("two")
        git.stage(listOf("one.js", "two.js"))
        git.unstage(listOf("one.js"))
        assertEquals(listOf("two.js"), git.inspect().staged)
        assertTrue(git.inspect().changes.any { it.path == "one.js" && it.kind == "Untracked" })
        assertEquals("one", folder.resolve("one.js").readText())
        assertThrows(IllegalArgumentException::class.java) { git.unstage(listOf("../invalid")) }
    }

    @Test fun unstagingAfterFirstCommitLeavesEditsIntact() {
        val folder = temporary.newFolder("unstage-tracked")
        val git = GitWorkspace(folder)
        git.init()
        folder.resolve("tracked.js").writeText("before")
        git.stage(listOf("tracked.js"))
        git.commit("baseline", "Tester", "tester@example.com")
        folder.resolve("tracked.js").writeText("after")
        git.stage(listOf("tracked.js"))
        git.unstage(listOf("tracked.js"))
        assertTrue(git.inspect().staged.isEmpty())
        assertTrue(git.inspect().changes.any { it.path == "tracked.js" && it.kind == "Modified" })
        assertEquals("after", folder.resolve("tracked.js").readText())
    }

    @get:Rule val temporary = TemporaryFolder()

    @Test fun initializesCommitsAndTracksChanges() {
        val folder = temporary.newFolder("example")
        val workspace = GitWorkspace(folder)
        assertFalse(workspace.inspect().initialized)
        workspace.init()
        assertTrue(workspace.inspect().initialized)
        folder.resolve("hello.js").writeText("print('hello')")
        assertTrue(workspace.inspect().changes.any { it.path == "hello.js" })
        assertThrows(IllegalStateException::class.java) {
            workspace.commit("Create source", "CodeLab User", "user@example.com")
        }
        workspace.stage(listOf("hello.js"))
        assertEquals(listOf("hello.js"), workspace.inspect().staged)
        val commit = workspace.commit("Create source", "CodeLab User", "user@example.com")
        assertTrue(commit.isNotBlank())
        assertTrue(workspace.inspect().changes.isEmpty())
        assertEquals("Create source", workspace.inspect().commits.first().message)
        workspace.createBranch("feature/demo")
        assertEquals("feature/demo", workspace.inspect().branch)
    }
}

package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GitWorkspaceTest {
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

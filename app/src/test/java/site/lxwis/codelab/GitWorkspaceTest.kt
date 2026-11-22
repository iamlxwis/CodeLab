package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GitWorkspaceTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun initializesCommitsAndTracksChanges() {
        val folder = temporary.newFolder("example")
        val workspace = GitWorkspace(folder)
        assertFalse(workspace.inspect().initialized)
        workspace.init()
        assertTrue(workspace.inspect().initialized)
        folder.resolve("hello.js").writeText("print('hello')")
        assertTrue(workspace.inspect().changes.any { it.path == "hello.js" })
        val commit = workspace.commit("Create source", "CodeLab User", "user@example.com")
        assertTrue(commit.isNotBlank())
        assertTrue(workspace.inspect().changes.isEmpty())
        assertEquals("Create source", workspace.inspect().commits.first().message)
        workspace.createBranch("feature/demo")
        assertEquals("feature/demo", workspace.inspect().branch)
    }
}

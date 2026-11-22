package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class WorkspaceShellTest {
    @get:Rule val temp = TemporaryFolder()

    @Test fun readsFilesAndNavigatesNestedDirectoriesSafely() {
        val store = ProjectStore(temp.newFolder("project-storage"))
        store.createProject("Demo", "JavaScript")
        store.createFolder("Demo", "src")
        store.createFile("Demo", "src/notes.txt", "first\nsecond keyword")
        val shell = WorkspaceShell(store, "Demo")
        assertEquals("/", shell.execute("pwd"))
        assertTrue(shell.execute("ls").contains("src/"))
        assertEquals("/src", shell.execute("cd src"))
        assertEquals("/src", shell.execute("pwd"))
        assertTrue(shell.execute("cat notes.txt").contains("second keyword"))
        assertTrue(shell.execute("grep keyword").contains("notes.txt:2"))
        assertTrue(shell.execute("wc notes.txt").contains("2 lines"))
        assertEquals("/", shell.execute("cd .."))
        assertTrue(runCatching { shell.execute("cd ..") }.isFailure)
    }

    @Test fun quotedArgumentsRemainLiteral() {
        val store = ProjectStore(temp.newFolder("quoted-storage"))
        store.createProject("Demo", "JavaScript")
        store.createFile("Demo", "hello world.txt", "alpha beta")
        val shell = WorkspaceShell(store, "Demo")
        assertEquals("alpha beta", shell.execute("cat 'hello world.txt'"))
        assertTrue(shell.execute("help").contains("No OS shell", ignoreCase = true))
    }
}

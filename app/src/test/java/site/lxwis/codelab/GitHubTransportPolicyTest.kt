package site.lxwis.codelab

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.Transport
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GitHubTransportPolicyTest {
    @get:Rule val temporary = TemporaryFolder()
    private val origin = "https://github.com/example/repository.git"

    @Test fun insecureGlobalAndUrlSpecificTlsSettingsAreRejectedBeforeNetwork() {
        Git.init().setDirectory(temporary.newFolder()).call().use { git ->
            val config = git.repository.config
            config.setString("remote", "origin", "url", origin)
            assertEquals(origin, GitHubTransportPolicy.requireSafeConfig(config))
            config.setBoolean("http", null, "sslVerify", false)
            assertThrows(IllegalArgumentException::class.java) { GitHubTransportPolicy.requireSafeConfig(config) }
            config.unset("http", null, "sslVerify")
            config.setBoolean("http", "https://github.com", "sslVerify", false)
            assertThrows(IllegalArgumentException::class.java) { GitHubTransportPolicy.requireSafeConfig(config) }
        }
    }

    @Test fun alternatePushDestinationIsRejectedAndProjectConfigIsPreserved() {
        val folder = temporary.newFolder()
        val workspace = GitWorkspace(folder)
        workspace.init()
        workspace.configureGitHubRemote("example/repository")
        Git.open(folder).use { git ->
            git.repository.config.apply {
                setString("remote", "origin", "pushurl", "https://other.example/repository.git")
                save()
            }
        }
        val before = folder.resolve(".git/config").readText()
        assertThrows(IllegalArgumentException::class.java) { workspace.fetchGitHub("") }
        assertEquals(before, folder.resolve(".git/config").readText())
    }

    @Test fun actualRewrittenTransportIsRejectedBeforeItConnects() {
        Git.init().setDirectory(temporary.newFolder()).call().use { git ->
            val config = git.repository.config
            config.setString("remote", "origin", "url", origin)
            config.setString("url", "https://other.example/", "insteadOf", "https://github.com/")
            config.save()
            val expected = GitHubTransportPolicy.requireSafeConfig(config)
            Transport.open(git.repository, "origin").use { transport ->
                assertTrue(transport.uri.toString().startsWith("https://other.example/"))
                assertThrows(IllegalArgumentException::class.java) {
                    GitHubTransportPolicy.requireSafeTransport(config, expected, transport)
                }
            }
        }
    }
}

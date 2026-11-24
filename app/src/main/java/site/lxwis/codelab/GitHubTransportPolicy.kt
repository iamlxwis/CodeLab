package site.lxwis.codelab

import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.transport.HttpConfig
import org.eclipse.jgit.transport.Transport
import org.eclipse.jgit.transport.TransportHttp
import org.eclipse.jgit.transport.URIish

/** Project Git configuration must not redirect an authenticated CodeLab operation. */
internal object GitHubTransportPolicy {
    fun requireSafeConfig(config: Config): String {
        val expected = GitHubRemoteAddress.requireConfigured(config.getString("remote", "origin", "url"))
        require(config.getStringList("remote", "origin", "pushurl").all { it == expected }) {
            "Origin has a different push destination. Reconfigure it before syncing."
        }
        require(HttpConfig(config, URIish(expected)).isSslVerify) {
            "Git sync requires TLS certificate verification. Remove insecure project HTTP settings."
        }
        return expected
    }

    fun requireSafeTransport(config: Config, expected: String, transport: Transport) {
        // JGit applies insteadOf/pushInsteadOf before constructing this transport.
        // Check the effective URI before it can connect or ask for credentials.
        require(transport is TransportHttp && transport.uri.toString() == expected) {
            "Project configuration rewrites the Git destination. Remove URL rewrites before syncing."
        }
        require(HttpConfig(config, transport.uri).isSslVerify) {
            "Git sync requires TLS certificate verification."
        }
    }
}

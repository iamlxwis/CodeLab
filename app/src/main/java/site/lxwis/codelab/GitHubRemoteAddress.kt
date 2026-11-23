package site.lxwis.codelab

/**
 * Only Github HTTPS git URLs with no port, query, fragment, username or token.
 * A normalized URL is compared again before every remote operation, so a
 * manually edited / imported .git/config cannot redirect a credential.
 */
internal object GitHubRemoteAddress {
    fun canonical(input: String): String {
        val slug = GitHubSnapshotImporter.canonicalSlug(input)
        return "https://github.com/$slug.git"
    }

    fun requireConfigured(url: String?): String {
        require(!url.isNullOrBlank() && canonical(url) == url) {
            "Only a canonical GitHub HTTPS remote is supported. Reconfigure origin first."
        }
        return url
    }
}
